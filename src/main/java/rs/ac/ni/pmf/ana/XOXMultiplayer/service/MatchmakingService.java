package rs.ac.ni.pmf.ana.XOXMultiplayer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import rs.ac.ni.pmf.ana.XOXMultiplayer.exception.InvalidOperationException;
import rs.ac.ni.pmf.ana.XOXMultiplayer.exception.ResourceNotFoundException;
import rs.ac.ni.pmf.ana.XOXMultiplayer.mapper.GameMapper;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.GameSession;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.dto.LobbyWaitingDto;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.entity.UserEntity;
import rs.ac.ni.pmf.ana.XOXMultiplayer.repository.UserRepository;

import java.util.Random;

// Metode su synchronized jer poruke sa WebSocket-a obrađuje više niti: bez toga bi dva igrača
// koja uđu u istom trenutku oba videla prazan red i nijedan ne bi bio uparen.
@Service
@RequiredArgsConstructor
public class MatchmakingService
{
	// Lični kanal igrača: klijent se pretplaćuje na /user/queue/match
	private static final String MATCH_DESTINATION = "/queue/match";

	// Tema za sve prijavljene igrače: ko trenutno čeka protivnika
	private static final String LOBBY_TOPIC = "/topic/lobby";

	private static final Random RANDOM = new Random();

	private final UserRepository _userRepository;
	private final GameService _gameService;
	private final GameMapper _gameMapper;
	private final SimpMessagingTemplate _messagingTemplate;

	// Igrači se uparuju čim stigne drugi, pa u redu nikad ne čeka više od jednog. Zato je red jedno polje.
	private WaitingPlayer _waitingPlayer;

	public synchronized void join(final String username, final String sessionId)
	{
		final UserEntity user = _userRepository.findByUsername(username)
		                                       .orElseThrow(() -> new ResourceNotFoundException("Korisnik nije pronađen"));

		if (_gameService.isPlaying(user.getId()))
		{
			throw new InvalidOperationException("Već ste u partiji");
		}

		if (_waitingPlayer == null)
		{
			_waitingPlayer = new WaitingPlayer(user, sessionId);
			publishWaiting(user.getUsername());
			return;
		}

		// Isti igrač iz druge kartice: već čeka, pa se ne uparuje sam sa sobom
		if (_waitingPlayer.user().getId().equals(user.getId()))
		{
			return;
		}

		final UserEntity opponent = _waitingPlayer.user();
		_waitingPlayer = null;
		publishWaiting(null);

		final boolean userIsX = RANDOM.nextBoolean();
		final GameSession session = userIsX
				? _gameService.createGame(user, opponent)
				: _gameService.createGame(opponent, user);

		// Poruka se šalje jednom i svakom igraču posebno, jer svaki dobija svoj simbol i ime onog drugog
		_messagingTemplate.convertAndSendToUser(
				user.getUsername(), MATCH_DESTINATION, _gameMapper.toMatchFoundDto(session, user.getId()));
		_messagingTemplate.convertAndSendToUser(
				opponent.getUsername(), MATCH_DESTINATION, _gameMapper.toMatchFoundDto(session, opponent.getId()));
	}

	public synchronized void leave(final String username)
	{
		if (_waitingPlayer != null && _waitingPlayer.user().getUsername().equals(username))
		{
			_waitingPlayer = null;
			publishWaiting(null);
		}
	}

	// Poziva se kad se WebSocket sesija zatvori. Gleda se sesija, a ne korisnik, da zatvaranje
	// druge kartice istog igrača ne bi iz reda izbacilo onu koja čeka.
	public synchronized void removeSession(final String sessionId)
	{
		if (_waitingPlayer != null && _waitingPlayer.sessionId().equals(sessionId))
		{
			_waitingPlayer = null;
			publishWaiting(null);
		}
	}

	// Za GET /api/v1/lobby: igrač koji se poveže kasnije nije dobio poruku sa teme
	public synchronized String waitingUsername()
	{
		return _waitingPlayer == null ? null : _waitingPlayer.user().getUsername();
	}

	// Poziva se samo iz synchronized metoda, pa poruke stižu istim redom kojim se red menjao.
	// username je null kad više niko ne čeka.
	private void publishWaiting(final String username)
	{
		_messagingTemplate.convertAndSend(LOBBY_TOPIC, LobbyWaitingDto.builder().waitingUsername(username).build());
	}

	private record WaitingPlayer(UserEntity user, String sessionId)
	{
	}
}
