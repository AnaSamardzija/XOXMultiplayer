package rs.ac.ni.pmf.ana.XOXMultiplayer.security;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

// Pregledač pri otvaranju WebSocket veze ne može da pošalje Authorization zaglavlje, pa token stiže
// kao zaglavlje STOMP CONNECT poruke. Ovde se proverava i korisnik se postavlja na sesiju.
@Component
@RequiredArgsConstructor
@Slf4j
public class StompAuthChannelInterceptor implements ChannelInterceptor
{
	private final JwtTokenUtil _jwtTokenUtil;
	private final UserDetailsService _userDetailsService;

	@Override
	public Message<?> preSend(final Message<?> message, final MessageChannel channel)
	{
		final StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

		if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand()))
		{
			final String authorizationHeader = accessor.getFirstNativeHeader("Authorization");

			if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer "))
			{
				throw new BadCredentialsException("Token nedostaje");
			}

			// Za razliku od JwtAuthenticationFilter-a, ovde poruka ne sme da nastavi kao neprijavljena:
			// izuzetak je jedini način da se konekcija odbije. Spring tada klijentu pošalje STOMP ERROR
			// i zatvori vezu.
			try
			{
				final String jwt = authorizationHeader.substring(7);
				final String username = _jwtTokenUtil.extractUsername(jwt);
				final UserDetails userDetails = _userDetailsService.loadUserByUsername(username);

				if (!_jwtTokenUtil.validateToken(jwt, userDetails))
				{
					throw new BadCredentialsException("Token nije ispravan");
				}

				// Korisnik postavljen na CONNECT ostaje vezan za sesiju, pa ga Spring daje uz svaku
				// narednu poruku te sesije
				accessor.setUser(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
				log.info("WebSocket connected: user {}, session {}", userDetails.getUsername(), accessor.getSessionId());
			}
			catch (final JwtException | IllegalArgumentException ex)
			{
				throw new BadCredentialsException("Token nije ispravan", ex);
			}
		}

		return message;
	}
}
