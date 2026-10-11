package rs.ac.ni.pmf.ana.XOXMultiplayer.model.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.dto.UserDto;

@Value
@Builder
public class AuthResponse
{
	@NotBlank
	String accessToken;

	@NotNull
	UserDto user;
}
