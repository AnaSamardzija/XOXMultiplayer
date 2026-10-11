package rs.ac.ni.pmf.ana.XOXMultiplayer.mapper;

import org.springframework.stereotype.Component;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.GameSession;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.Symbol;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.UltimateXoxGame;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.dto.GameStateDto;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.dto.MatchFoundDto;

import java.util.Arrays;

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

	// Nizovi se kopiraju, jer se DTO pretvara u JSON tek posle izlaska iz zaključavanja partije,
	// a tada bi sledeći potez već mogao da menja originalne.
	public GameStateDto toGameStateDto(final GameSession session)
	{
		final UltimateXoxGame game = session.getGame();

		return GameStateDto.builder()
		                   .gameId(session.getId())
		                   .playerXUsername(session.getPlayerXUsername())
		                   .playerOUsername(session.getPlayerOUsername())
		                   .cells(Arrays.stream(game.getCells()).map(Symbol[]::clone).toArray(Symbol[][]::new))
		                   .boardStates(game.getBoardStates().clone())
		                   .currentPlayer(game.getCurrentPlayer())
		                   .forcedBoard(game.getForcedBoard())
		                   .moveNumber(game.getMoveCount())
		                   .build();
	}
}
