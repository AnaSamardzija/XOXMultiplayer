package rs.ac.ni.pmf.ana.XOXMultiplayer.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import rs.ac.ni.pmf.ana.XOXMultiplayer.service.MatchmakingService;
import rs.ac.ni.pmf.ana.XOXMultiplayer.service.PresenceService;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener
{
	private final MatchmakingService _matchmakingService;
	private final PresenceService _presenceService;

	// Spring objavi ovaj događaj kad je CONNECT prihvaćen, dakle tek pošto je
	// StompAuthChannelInterceptor proverio token i postavio korisnika na sesiju.
	@EventListener
	public void onConnected(final SessionConnectedEvent event)
	{
		if (event.getUser() == null)
		{
			return;
		}

		final String sessionId = StompHeaderAccessor.wrap(event.getMessage()).getSessionId();
		_presenceService.connected(event.getUser().getName(), sessionId);
	}

	// Spring objavi ovaj događaj kad se WebSocket sesija zatvori: zatvoren pregledač, osvežena
	// strana ili pukla veza. Igrač koji je sa te sesije čekao u redu se izbacuje.
	// Redosled je bitan: prvo izlazak iz reda, pa prisustvo. Obrnuto bi igrač koji je otišao mogao
	// da bude uparen pošto je prisustvo već provereno, i u toj partiji mu rok za povratak ne bi krenuo.
	@EventListener
	public void onDisconnect(final SessionDisconnectEvent event)
	{
		_matchmakingService.removeSession(event.getSessionId());

		// Sesija kojoj je CONNECT odbijen nema korisnika
		if (event.getUser() != null)
		{
			_presenceService.disconnected(event.getUser().getName(), event.getSessionId());
		}
	}
}
