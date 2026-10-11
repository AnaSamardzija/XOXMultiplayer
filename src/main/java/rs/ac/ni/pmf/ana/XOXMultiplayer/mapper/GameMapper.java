package rs.ac.ni.pmf.ana.XOXMultiplayer.mapper;

import org.springframework.stereotype.Component;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.GameSession;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.Symbol;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.dto.MatchFoundDto;

@Component
public class GameMapper
{
	// Ista partija daje dve različite poruke, pa se prosleđuje i igrač kome je poruka namenjena
	public MatchFoundDto toMatchFoundDto(final GameSession session, final Long userId)
	{
		final boolean isPlayerX = session.getPlayerXId().equals(userId);

		return MatchFoundDto.builder()
		                    .gameId(session.getId())
		                    .opponentUsername(isPlayerX ? session.getPlayerOUsername() : session.getPlayerXUsername())
		                    .symbol(isPlayerX ? Symbol.X : Symbol.O)
		                    .build();
	}
}
