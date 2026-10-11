package rs.ac.ni.pmf.ana.XOXMultiplayer.model.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserStatsDto
{
	long played;
	long wins;
	long losses;
	long draws;
}
