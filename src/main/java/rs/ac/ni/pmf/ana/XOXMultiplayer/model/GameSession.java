package rs.ac.ni.pmf.ana.XOXMultiplayer.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Partija u toku, onako kako je server drži u memoriji: ko igra koji simbol i stanje table.
 * Nije entitet ni Spring komponenta. Identifikator je isti kao id reda u tabeli games.
 */
@Getter
@RequiredArgsConstructor
public class GameSession
{
	private final Long id;
	private final Long playerXId;
	private final String playerXUsername;
	private final Long playerOId;
	private final String playerOUsername;
	private final UltimateXoxGame game = new UltimateXoxGame();

	public boolean hasPlayer(final Long userId)
	{
		return playerXId.equals(userId) || playerOId.equals(userId);
	}
}
