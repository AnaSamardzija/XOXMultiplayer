package rs.ac.ni.pmf.ana.XOXMultiplayer.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import rs.ac.ni.pmf.ana.XOXMultiplayer.service.MatchmakingService;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener
{
	private final MatchmakingService _matchmakingService;

	// Spring objavi ovaj događaj kad se WebSocket sesija zatvori: zatvoren pregledač, osvežena
	// strana ili pukla veza. Igrač koji je sa te sesije čekao u redu se izbacuje.
	@EventListener
	public void onDisconnect(final SessionDisconnectEvent event)
	{
		_matchmakingService.removeSession(event.getSessionId());
	}
}
