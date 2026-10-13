package rs.ac.ni.pmf.ana.XOXMultiplayer.model.dto;

import lombok.Builder;
import lombok.Value;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.GameEndReason;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.PlayerResult;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.Symbol;

import java.time.LocalDateTime;

// Završena partija iz ugla igrača koji je traži
@Value
@Builder
public class PlayedGameDto
{
	Long id;
	String opponentUsername;
	Symbol symbol;
	PlayerResult result;
	GameEndReason endReason;
	LocalDateTime startedAt;
	LocalDateTime endedAt;
}
