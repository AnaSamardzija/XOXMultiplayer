package rs.ac.ni.pmf.ana.XOXMultiplayer.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import rs.ac.ni.pmf.ana.XOXMultiplayer.security.JwtAuthenticationFilter;
import rs.ac.ni.pmf.ana.XOXMultiplayer.security.JwtTokenUtil;
import rs.ac.ni.pmf.ana.XOXMultiplayer.security.RestAuthenticationEntryPoint;
import tools.jackson.databind.json.JsonMapper;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig
{
	private final JwtTokenUtil _jwtTokenUtil;
	private final UserDetailsService _userDetailsService;
	private final JsonMapper _jsonMapper;

	@Bean
	public PasswordEncoder passwordEncoder()
	{
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(final AuthenticationConfiguration configuration)
	{
		return configuration.getAuthenticationManager();
	}

	@Bean
	public AuthenticationEntryPoint authenticationEntryPoint()
	{
		return new RestAuthenticationEntryPoint(_jsonMapper);
	}

	@Bean
	public SecurityFilterChain securityFilterChain(final HttpSecurity http)
	{
		return http
				.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint()))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/api/v1/auth/**").permitAll()
						.requestMatchers("/api/v1/**").authenticated()
						// Slobodni su fajlovi fronta i /ws: pregledač pri otvaranju WebSocket veze ne može
						// da pošalje token, pa se korisnik tamo proverava tek na STOMP CONNECT
						.anyRequest().permitAll()
				)
				.addFilterBefore(new JwtAuthenticationFilter(
										 _jwtTokenUtil,
						                 _userDetailsService),
				                 UsernamePasswordAuthenticationFilter.class)
				.build();
	}
}
