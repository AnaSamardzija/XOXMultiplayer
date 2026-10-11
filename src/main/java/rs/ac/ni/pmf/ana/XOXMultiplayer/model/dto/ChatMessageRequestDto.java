package rs.ac.ni.pmf.ana.XOXMultiplayer.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@Builder
@NoArgsConstructor(force = true, access = lombok.AccessLevel.PRIVATE)
@AllArgsConstructor
public class ChatMessageRequestDto
{
	@NotBlank(message = "Poruka je obavezna")
	@Size(max = 200, message = "Poruka može imati najviše 200 karaktera")
	String text;
}
