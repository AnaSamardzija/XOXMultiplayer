package rs.ac.ni.pmf.ana.XOXMultiplayer.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@NullMarked
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter
{
	private final JwtTokenUtil _jwtTokenUtil;
	private final UserDetailsService _userDetailsService;

	@Override
	protected void doFilterInternal(
			final HttpServletRequest request,
			final HttpServletResponse response,
			final FilterChain filterChain) throws ServletException, IOException
	{
		final String authorizationHeader = request.getHeader("Authorization");

		if (authorizationHeader != null && authorizationHeader.startsWith("Bearer "))
		{
			// Istekao ili pokvaren token (JwtException), prazan token (IllegalArgumentException) i token
			// za korisnika koji više ne postoji (UsernameNotFoundException) ne smeju da obore zahtev
			// sa 500: zahtev samo nastavlja kao neprijavljen, pa ga Spring odbije kroz
			// RestAuthenticationEntryPoint sa 401.
			try
			{
				final String jwt = authorizationHeader.substring(7);
				final String username = _jwtTokenUtil.extractUsername(jwt);

				if (username != null && SecurityContextHolder.getContext().getAuthentication() == null)
				{
					final UserDetails userDetails = _userDetailsService.loadUserByUsername(username);

					if (_jwtTokenUtil.validateToken(jwt, userDetails))
					{
						final UsernamePasswordAuthenticationToken authToken =
								new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
						authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
						SecurityContextHolder.getContext().setAuthentication(authToken);
					}
				}
			}
			catch (final JwtException | AuthenticationException | IllegalArgumentException ex)
			{
				log.debug("Rejected JWT: {}", ex.getMessage());
				SecurityContextHolder.clearContext();
			}
		}

		filterChain.doFilter(request, response);
	}
}
