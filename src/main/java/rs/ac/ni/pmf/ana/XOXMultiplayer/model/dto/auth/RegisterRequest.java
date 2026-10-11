package rs.ac.ni.pmf.ana.XOXMultiplayer.model.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@Builder
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class RegisterRequest
{
	@NotBlank(message = "Korisničko ime je obavezno")
	@Size(max = 100, message = "Korisničko ime može imati najviše 100 znakova")
	String username;

	@NotBlank(message = "Lozinka je obavezna")
	@Size(min = 4, max = 24, message = "Lozinka mora imati između 4 i 24 znaka")
	String password;
}
