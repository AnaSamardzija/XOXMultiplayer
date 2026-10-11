package rs.ac.ni.pmf.ana.XOXMultiplayer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.ni.pmf.ana.XOXMultiplayer.exception.InvalidOperationException;
import rs.ac.ni.pmf.ana.XOXMultiplayer.exception.ResourceNotFoundException;
import rs.ac.ni.pmf.ana.XOXMultiplayer.mapper.GameMapper;
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
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class GameService
{
	// Tema partije: klijenti se pretplaćuju na /topic/games/{id}
	private static final String GAME_TOPIC = "/topic/games/";

	private static final Duration TURN_DURATION = Duration.ofSeconds(20);

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
				             return _gameMapper.toGameStateDto(session);
			             }
		             });
	}

	public void makeMove(final Long gameId, final String username, final int boardIndex, final int cellIndex)
	{
		final GameSession session = _games.get(gameId);

		if (session == null)
		{
			throw new ResourceNotFoundException("Partija nije pronađena");
		}

		final Symbol symbol = session.symbolOf(username);

		if (symbol == null)
		{
			throw new InvalidOperationException("Niste igrač u ovoj partiji");
		}

		// Zaključavanje po partiji: dve poruke za istu partiju mogu da se obrađuju paralelno.
		// Da li je igrač na potezu, da li je mini-tabla dozvoljena i da li je polje prazno proverava
		// applyMove. Stanje se objavljuje unutar zaključavanja, da na temu izlazi redom kojim su potezi primenjeni.
		synchronized (session)
		{
			session.getGame().applyMove(symbol, boardIndex, cellIndex);
			scheduleTurnTimeout(session);
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
			scheduleTurnTimeout(session);
			publishState(session);
		}
	}

	private void publishState(final GameSession session)
	{
		_messagingTemplate.convertAndSend(GAME_TOPIC + session.getId(), _gameMapper.toGameStateDto(session));
	}
}
