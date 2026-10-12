package rs.ac.ni.pmf.ana.XOXMultiplayer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// Prisustvo se prati po sesiji, a ne po korisniku: posle osvežavanja strane nova veza često stigne
// pre nego što server obradi prekid stare, a igrač može da ima i igru otvorenu u dve kartice.
// Igrač je odsutan tek kad nema nijednu živu sesiju.
// Metode su synchronized da provera „da li je ovo prva, odnosno poslednja sesija“ i javljanje
// partiji budu jedna nedeljiva radnja, inače bi prekid stare i dolazak nove veze mogli da se izmešaju.
@Service
@RequiredArgsConstructor
public class PresenceService
{
	private final GameService _gameService;

	// Žive WebSocket sesije po korisničkom imenu
	private final Map<String, Set<String>> _sessions = new HashMap<>();

	public synchronized void connected(final String username, final String sessionId)
	{
		final Set<String> sessions = _sessions.computeIfAbsent(username, key -> new HashSet<>());
		final boolean wasAway = sessions.isEmpty();

		sessions.add(sessionId);

		if (wasAway)
		{
			_gameService.playerReturned(username);
		}
	}

	// Spring može da objavi prekid iste sesije više puta, pa se sesija koja je već izbačena preskače
	public synchronized void disconnected(final String username, final String sessionId)
	{
		final Set<String> sessions = _sessions.get(username);

		if (sessions == null || !sessions.remove(sessionId))
		{
			return;
		}

		if (sessions.isEmpty())
		{
			_sessions.remove(username);
			_gameService.playerLeft(username);
		}
	}

	// Broje se igrači, a ne sesije: igrač sa dve otvorene kartice je jedan igrač na mreži
	public synchronized int onlineCount()
	{
		return _sessions.size();
	}
}
