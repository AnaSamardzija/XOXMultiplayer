package rs.ac.ni.pmf.ana.XOXMultiplayer.model.dto;

import lombok.Builder;
import lombok.Value;

// Poruka na temi /topic/lobby
@Value
@Builder
public class LobbyWaitingDto
{
	// null kad niko ne čeka
	String waitingUsername;
}
