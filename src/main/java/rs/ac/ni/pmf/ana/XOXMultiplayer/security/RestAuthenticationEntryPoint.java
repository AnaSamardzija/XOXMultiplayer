package rs.ac.ni.pmf.ana.XOXMultiplayer.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.dto.ErrorDto;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.time.OffsetDateTime;

@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint
{
	private final JsonMapper _responseMapper;

	@Override
	@NullMarked
	public void commence(
			final HttpServletRequest request,
			final HttpServletResponse response,
			final AuthenticationException authException) throws IOException
	{
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);

		// Poruka je stalna, jer je Spring-ova (authException.getMessage()) na engleskom
		final ErrorDto error = ErrorDto.builder()
				.message("Prijavite se da biste nastavili")
				.path(request.getRequestURI())
				.timestamp(OffsetDateTime.now())
				.build();

		_responseMapper.writeValue(response.getOutputStream(), error);
	}
}
