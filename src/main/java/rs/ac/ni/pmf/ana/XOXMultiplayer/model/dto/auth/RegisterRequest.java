package rs.ac.ni.pmf.ana.XOXMultiplayer.model.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
	@Size(min = 3, max = 20, message = "Korisničko ime mora imati između 3 i 20 znakova")
	@Pattern(regexp = "[A-Za-z0-9._!*-]*",
			message = "Korisničko ime sme da sadrži samo slova engleske abecede, cifre i znakove . - _ ! *")
	String username;

	@NotBlank(message = "Lozinka je obavezna")
	@Size(min = 4, max = 24, message = "Lozinka mora imati između 4 i 24 znaka")
	String password;
}
