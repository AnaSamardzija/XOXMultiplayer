package rs.ac.ni.pmf.ana.XOXMultiplayer.model.dto;

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
public class ChangePasswordRequestDto
{
	@NotBlank(message = "Trenutna lozinka je obavezna")
	String currentPassword;

	// Isto ograničenje kao u RegisterRequest
	@NotBlank(message = "Nova lozinka je obavezna")
	@Size(min = 4, max = 24, message = "Nova lozinka mora imati između 4 i 24 znaka")
	String newPassword;
}
