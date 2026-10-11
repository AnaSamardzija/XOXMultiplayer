package rs.ac.ni.pmf.ana.XOXMultiplayer.controller;

import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.dto.EchoRequestDto;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.dto.EchoResponseDto;

import java.security.Principal;

// Privremeno: probna poruka iz zadatka 006, briše se kad stigne prva prava poruka
@Controller
public class EchoController
{
	// Klijent šalje na /app/echo, a odgovor stiže samo njemu, na /user/queue/echo.
	// Principal je korisnik koga je StompAuthChannelInterceptor postavio na sesiju pri CONNECT-u.
	@MessageMapping("/echo")
	@SendToUser("/queue/echo")
	public EchoResponseDto echo(
			final EchoRequestDto request,
			final Principal principal,
			@Header("simpSessionId") final String sessionId)
	{
		return EchoResponseDto.builder()
				.username(principal.getName())
				.sessionId(sessionId)
				.text(request.getText())
				.build();
	}
}
