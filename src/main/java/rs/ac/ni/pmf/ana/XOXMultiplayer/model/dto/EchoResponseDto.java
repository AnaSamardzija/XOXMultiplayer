package rs.ac.ni.pmf.ana.XOXMultiplayer.model.dto;

import lombok.Builder;
import lombok.Value;

// Privremeno: probna poruka iz zadatka 006, briše se kad stigne prva prava poruka
@Value
@Builder
public class EchoResponseDto
{
	String username;
	String sessionId;
	String text;
}
