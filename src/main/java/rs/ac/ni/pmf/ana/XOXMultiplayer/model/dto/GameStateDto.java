package rs.ac.ni.pmf.ana.XOXMultiplayer.model.dto;

import lombok.Builder;
import lombok.Value;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.MiniBoardState;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.Symbol;

// Celo stanje partije. Isto je za oba igrača: svoj simbol front odredi po korisničkom imenu.
@Value
@Builder
public class GameStateDto
{
	Long gameId;
	String playerXUsername;
	String playerOUsername;
	// cells[mini-tabla][polje], null je prazno polje
	Symbol[][] cells;
	MiniBoardState[] boardStates;
	Symbol currentPlayer;
	// null znači slobodan potez
	Integer forcedBoard;
	int moveNumber;
}
