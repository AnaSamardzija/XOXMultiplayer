package rs.ac.ni.pmf.ana.XOXMultiplayer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.ni.pmf.ana.XOXMultiplayer.exception.InvalidOperationException;
import rs.ac.ni.pmf.ana.XOXMultiplayer.exception.ResourceNotFoundException;
import rs.ac.ni.pmf.ana.XOXMultiplayer.mapper.GameMapper;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.ChatMessage;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.GameEndReason;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.GameOutcome;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.GameSession;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.GameStatus;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.Move;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.Symbol;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.UltimateXoxGame;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.dto.GameStateDto;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.entity.GameEntity;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.entity.UserEntity;
import rs.ac.ni.pmf.ana.XOXMultiplayer.repository.GameRepository;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
@RequiredArgsConstructor
public class GameService
{
	// Tema partije: klijenti se pretplaćuju na /topic/games/{id}
	private static final String GAME_TOPIC = "/topic/games/";
	// Tema četa je odvojena od teme partije: /topic/games/{id}/chat
	private static final String CHAT_TOPIC_SUFFIX = "/chat";

	private static final Duration TURN_DURATION = Duration.ofSeconds(20);
	private static final Duration RECONNECT_DURATION = Duration.ofSeconds(40);

	private final GameRepository _gameRepository;
	private final GameMapper _gameMapper;
	private final SimpMessagingTemplate _messagingTemplate;
	private final TaskScheduler _taskScheduler;

	// Partije u toku žive samo u memoriji, po identifikatoru partije. ConcurrentHashMap zato što
	// poruke sa WebSocket-a obrađuje više niti.
	private final Map<Long, GameSession> _games = new ConcurrentHashMap<>();

	@Transactional
	public GameSession createGame(final UserEntity playerX, final UserEntity playerO)
	{
		final GameEntity gameEntity = GameEntity.builder()
		                                        .playerX(playerX)
		                                        .playerO(playerO)
		                                        .status(GameStatus.IN_PROGRESS)
		                                        .build();

		final GameEntity saved = _gameRepository.save(gameEntity);

		final GameSession session = new GameSession(
				saved.getId(),
				playerX.getId(),
				playerX.getUsername(),
				playerO.getId(),
				playerO.getUsername());

		_games.put(session.getId(), session);

		// Prvi istek, za X-ov prvi potez: njemu ne prethodi nijedan potez koji bi ga zakazao
		synchronized (session)
		{
			scheduleTurnTimeout(session);
		}

		return session;
	}

	public boolean isPlaying(final Long userId)
	{
		return _games.values().stream().anyMatch(session -> session.hasPlayer(userId));
	}

	public Optional<GameStateDto> findActiveGame(final Long userId)
	{
		return _games.values().stream()
		             .filter(session -> session.hasPlayer(userId))
		             .findFirst()
		             .map(session ->
		             {
			             // Zaključavanje po partiji: čitanje stanja prolazi kroz isto zaključavanje kao potez,
			             // da se tabla ne pročita usred poteza.
			             synchronized (session)
			             {
				             return _gameMapper.toActiveGameStateDto(session);
			             }
		             });
	}

	public void makeMove(final Long gameId, final String username, final int boardIndex, final int cellIndex)
	{
		final GameSession session = findSession(gameId);
		final Symbol symbol = symbolOf(session, username);

		// Zaključavanje po partiji: dve poruke za istu partiju mogu da se obrađuju paralelno.
		// Da li je igrač na potezu, da li je mini-tabla dozvoljena i da li je polje prazno proverava
		// applyMove. Stanje se objavljuje unutar zaključavanja, da na temu izlazi redom kojim su potezi primenjeni.
		synchronized (session)
		{
			session.getGame().applyMove(symbol, boardIndex, cellIndex);
			afterMove(session);
		}
	}

	// Ishod je pobeda protivnika, a razlog završetka predaja. Prolazi kroz isto zaključavanje kao potez:
	// ako je partija u međuvremenu završena, surrender baca izuzetak i rezultat se ne upisuje dvaput.
	public void surrender(final Long gameId, final String username)
	{
		final GameSession session = findSession(gameId);
		final Symbol symbol = symbolOf(session, username);

		synchronized (session)
		{
			session.getGame().surrender(symbol);
			// Igra je završena, pa ovo samo otkazuje tajmer
			scheduleTurnTimeout(session);
			finishGame(session, GameEndReason.SURRENDER);
		}
	}

	// Poruka se čuva uz partiju i objavljuje na temu četa. Prolazi kroz isto zaključavanje kao potez:
	// redni broj se dodeljuje i poruka objavljuje unutar njega, da na temu izlaze redom kojim su numerisane.
	public void sendChatMessage(final Long gameId, final String username, final String text)
	{
		final GameSession session = findSession(gameId);
		// Šalje samo igrač te partije
		symbolOf(session, username);

		synchronized (session)
		{
			// Partija je mogla da se završi dok se čekalo na zaključavanje
			if (session.getGame().isFinished())
			{
				throw new InvalidOperationException("Partija je završena");
			}

			final ChatMessage chatMessage = new ChatMessage(session.getChatMessages().size() + 1, username, text.strip());

			session.getChatMessages().add(chatMessage);
			_messagingTemplate.convertAndSend(
					GAME_TOPIC + session.getId() + CHAT_TOPIC_SUFFIX, _gameMapper.toChatMessageDto(chatMessage));
		}
	}

	// Poziva PresenceService kad igrač ostane bez ijedne žive veze. Zakazuje mu se rok za povratak,
	// a protivnik preko novog stanja vidi da je odsutan. Tajmer poteza se ne dira: i dalje teče,
	// da odsustvo ne bi bilo način da se dobije vreme.
	public void playerLeft(final String username)
	{
		final GameSession session = findSessionOf(username);

		if (session == null)
		{
			return;
		}

		final Symbol symbol = session.symbolOf(username);

		synchronized (session)
		{
			// Partija je mogla da se završi dok se čekalo na zaključavanje
			if (!_games.containsKey(session.getId()) || session.isAway(symbol))
			{
				return;
			}

			session.getReconnectTimeouts().put(symbol, _taskScheduler.schedule(
					() -> onReconnectTimeout(session, symbol), Instant.now().plus(RECONNECT_DURATION)));
			publishState(session);
		}
	}

	// Poziva PresenceService kad igrač koji nije imao nijednu vezu otvori novu
	public void playerReturned(final String username)
	{
		final GameSession session = findSessionOf(username);

		if (session == null)
		{
			return;
		}

		final Symbol symbol = session.symbolOf(username);

		synchronized (session)
		{
			final ScheduledFuture<?> reconnectTimeout = session.getReconnectTimeouts().remove(symbol);

			if (reconnectTimeout == null)
			{
				return;
			}

			reconnectTimeout.cancel(false);
			publishState(session);
		}
	}

	private GameSession findSessionOf(final String username)
	{
		return _games.values().stream()
		             .filter(session -> session.symbolOf(username) != null)
		             .findFirst()
		             .orElse(null);
	}

	private GameSession findSession(final Long gameId)
	{
		final GameSession session = _games.get(gameId);

		if (session == null)
		{
			throw new ResourceNotFoundException("Partija nije pronađena");
		}

		return session;
	}

	private Symbol symbolOf(final GameSession session, final String username)
	{
		final Symbol symbol = session.symbolOf(username);

		if (symbol == null)
		{
			throw new InvalidOperationException("Niste igrač u ovoj partiji");
		}

		return symbol;
	}

	// Poziva se samo unutar zaključavanja partije, posle svakog primenjenog poteza, ručnog ili
	// onog koji je server odigrao kad je vreme isteklo.
	private void afterMove(final GameSession session)
	{
		final UltimateXoxGame game = session.getGame();

		scheduleTurnTimeout(session);

		if (game.isFinished())
		{
			finishGame(session, game.getOutcome() == GameOutcome.DRAW
					? GameEndReason.ALL_BOARDS_RESOLVED
					: GameEndReason.THREE_IN_ROW);
			return;
		}

		publishState(session);
	}

	// Poziva se samo unutar zaključavanja partije, kad je igra već završena i tajmer otkazan.
	// Nije @Transactional: zove se i iz niti zakazivača, pozivom unutar iste klase, a takav poziv
	// ne prolazi kroz Spring-ov proxy. findById i save imaju svaka svoju transakciju.
	private void finishGame(final GameSession session, final GameEndReason endReason)
	{
		session.setEndReason(endReason);

		// Rokovi za povratak više nemaju šta da završe. Kad su odsutna oba igrača, ovako partiju
		// završava rok koji prvi istekne, a drugi se otkazuje.
		session.getReconnectTimeouts().values().forEach(reconnectTimeout -> reconnectTimeout.cancel(false));
		session.getReconnectTimeouts().clear();

		try
		{
			final GameEntity gameEntity = _gameRepository.findById(session.getId())
			                                             .orElseThrow(() -> new ResourceNotFoundException("Partija nije pronađena"));
			final GameOutcome outcome = session.getGame().getOutcome();

			gameEntity.setStatus(GameStatus.FINISHED);
			gameEntity.setOutcome(outcome);
			gameEntity.setEndReason(endReason);
			gameEntity.setEndedAt(LocalDateTime.now());

			// Igrači su lenje veze, ali ovde služe samo kao strani ključ, pa se ne učitavaju.
			// Kod nerešenog ishoda pobednik ostaje prazan.
			if (outcome == GameOutcome.X_WON)
			{
				gameEntity.setWinner(gameEntity.getPlayerX());
			}
			else if (outcome == GameOutcome.O_WON)
			{
				gameEntity.setWinner(gameEntity.getPlayerO());
			}

			_gameRepository.save(gameEntity);
		}
		finally
		{
			// I kad upis u bazu padne: inače bi igrači ostali zaglavljeni u završenoj partiji
			// i ne bi mogli u novi red čekanja.
			_games.remove(session.getId());
			publishState(session);
		}
	}

	// Poziva se samo unutar zaključavanja partije. Otkazuje prethodni istek i zakazuje novi,
	// za igrača koji je sada na potezu.
	private void scheduleTurnTimeout(final GameSession session)
	{
		if (session.getTurnTimeout() != null)
		{
			session.getTurnTimeout().cancel(false);
		}

		if (session.getGame().isFinished())
		{
			session.setTurnTimeout(null);
			session.setTurnDeadline(null);
			return;
		}

		final int moveNumber = session.getGame().getMoveCount();
		final Instant deadline = Instant.now().plus(TURN_DURATION);

		session.setTurnDeadline(deadline);
		session.setTurnTimeout(_taskScheduler.schedule(() -> onTurnTimeout(session, moveNumber), deadline));
	}

	// Izvršava se u niti zakazivača kad igraču istekne vreme. Otkazivanje nije dovoljno, jer istek i
	// potez mogu da stignu u istom trenutku: zato istek prolazi kroz isto zaključavanje kao potez i
	// proverava redni broj poteza za koji je zakazan. Ako se promenio, potez je stigao prvi.
	private void onTurnTimeout(final GameSession session, final int expectedMoveNumber)
	{
		synchronized (session)
		{
			final UltimateXoxGame game = session.getGame();

			if (game.isFinished() || game.getMoveCount() != expectedMoveNumber)
			{
				return;
			}

			// Red se ne preskače: server odigra nasumičan dozvoljen potez umesto igrača
			final Move move = game.randomMove();
			game.applyMove(game.getCurrentPlayer(), move.boardIndex(), move.cellIndex());
			afterMove(session);
		}
	}

	// Izvršava se u niti zakazivača kad igraču istekne rok za povratak. Kao i istek vremena za potez,
	// prolazi kroz isto zaključavanje kao potez i proverava da partija još postoji i da je igrač
	// još odsutan: povratak ili kraj partije su mogli da stignu u istom trenutku.
	private void onReconnectTimeout(final GameSession session, final Symbol symbol)
	{
		synchronized (session)
		{
			if (!_games.containsKey(session.getId()) || !session.isAway(symbol))
			{
				return;
			}

			// Ishod je isti kao kod predaje, pobeda protivnika, pa se zove ista metoda pravila.
			// Razlikuje se samo razlog završetka.
			session.getGame().surrender(symbol);
			// Igra je završena, pa ovo samo otkazuje tajmer
			scheduleTurnTimeout(session);
			finishGame(session, GameEndReason.DISCONNECT);
		}
	}

	private void publishState(final GameSession session)
	{
		_messagingTemplate.convertAndSend(GAME_TOPIC + session.getId(), _gameMapper.toGameStateDto(session));
	}
}
