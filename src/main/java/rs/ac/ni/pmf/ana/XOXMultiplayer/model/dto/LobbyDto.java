package rs.ac.ni.pmf.ana.XOXMultiplayer.model.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LobbyDto
{
	int onlinePlayers;
	int activeGames;
}
