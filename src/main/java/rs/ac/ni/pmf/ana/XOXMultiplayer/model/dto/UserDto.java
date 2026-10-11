package rs.ac.ni.pmf.ana.XOXMultiplayer.model.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserDto
{
	Long id;
	String username;
}
