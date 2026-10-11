package rs.ac.ni.pmf.ana.XOXMultiplayer.model.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LeaderboardEntryDto
{
	// Izjednačeni igrači dele mesto, a sledeće se preskače: 1, 2, 2, 4
	int rank;
	String username;
	long wins;
}
