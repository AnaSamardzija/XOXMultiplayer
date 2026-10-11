package rs.ac.ni.pmf.ana.XOXMultiplayer.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;

// Privremeno: probna poruka iz zadatka 006, briše se kad stigne prva prava poruka
@Value
@Builder
@NoArgsConstructor(force = true, access = lombok.AccessLevel.PRIVATE)
@AllArgsConstructor
public class EchoRequestDto
{
	String text;
}
