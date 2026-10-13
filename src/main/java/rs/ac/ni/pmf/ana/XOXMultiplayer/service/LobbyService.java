package rs.ac.ni.pmf.ana.XOXMultiplayer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.dto.LobbyDto;

// Svi podaci se čitaju iz memorije servera, bez upita u bazu. DTO se gradi ovde, bez mapera:
// nema ni entiteta ni partije koja bi se pretvarala, samo dva broja i ime igrača koji čeka.
@Service
@RequiredArgsConstructor
public class LobbyService
{
	private final PresenceService _presenceService;
	private final GameService _gameService;
	private final MatchmakingService _matchmakingService;

	public LobbyDto lobby()
	{
		return LobbyDto.builder()
		               .onlinePlayers(_presenceService.onlineCount())
		               .activeGames(_gameService.activeGameCount())
		               .waitingUsername(_matchmakingService.waitingUsername())
		               .build();
	}
}
