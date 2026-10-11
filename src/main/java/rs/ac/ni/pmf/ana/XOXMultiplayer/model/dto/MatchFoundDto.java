package rs.ac.ni.pmf.ana.XOXMultiplayer.model.dto;

import lombok.Builder;
import lombok.Value;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.Symbol;

// Poruka o uparivanju: svaki igrač dobija svoju, sa svojim simbolom i imenom onog drugog
@Value
@Builder
public class MatchFoundDto
{
	Long gameId;
	String opponentUsername;
	Symbol symbol;
}
