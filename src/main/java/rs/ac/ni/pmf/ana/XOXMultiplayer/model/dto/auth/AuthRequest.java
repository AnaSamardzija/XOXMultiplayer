package rs.ac.ni.pmf.ana.XOXMultiplayer.model.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@Builder
@NoArgsConstructor(force = true, access = lombok.AccessLevel.PRIVATE)
@AllArgsConstructor
public class AuthRequest
{
	@NotBlank(message = "Korisničko ime je obavezno")
	String username;

	@NotNull(message = "Lozinka je obavezna")
	String password;
}
