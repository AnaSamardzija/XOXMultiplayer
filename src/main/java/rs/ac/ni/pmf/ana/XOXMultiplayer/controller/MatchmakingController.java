package rs.ac.ni.pmf.ana.XOXMultiplayer.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import rs.ac.ni.pmf.ana.XOXMultiplayer.service.MatchmakingService;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class MatchmakingController
{
	private final MatchmakingService _matchmakingService;

	// Klijent šalje na /app/matchmaking/join, bez tela. Odgovora nema: poruka o uparivanju stiže
	// na /user/queue/match tek kad se nađe protivnik.
	// Principal je korisnik koga je StompAuthChannelInterceptor postavio na sesiju pri CONNECT-u.
	@MessageMapping("/matchmaking/join")
	public void join(final Principal principal, @Header("simpSessionId") final String sessionId)
	{
		_matchmakingService.join(principal.getName(), sessionId);
	}

	@MessageMapping("/matchmaking/leave")
	public void leave(final Principal principal)
	{
		_matchmakingService.leave(principal.getName());
	}
}
