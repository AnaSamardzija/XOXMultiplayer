package rs.ac.ni.pmf.ana.XOXMultiplayer.model.dto;

import lombok.Builder;
import lombok.Value;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.GameEndReason;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.GameOutcome;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.MiniBoardState;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.Symbol;

import java.util.List;

// Celo stanje partije. Isto je za oba igrača: svoj simbol front odredi po korisničkom imenu.
@Value
@Builder(toBuilder = true)
public class GameStateDto
{
	Long gameId;
	String playerXUsername;
	String playerOUsername;
	// Igrač trenutno nema nijednu živu vezu i teče mu rok za povratak
	boolean playerXAway;
	boolean playerOAway;
	// cells[mini-tabla][polje], null je prazno polje
	Symbol[][] cells;
	MiniBoardState[] boardStates;
	Symbol currentPlayer;
	// null znači slobodan potez
	Integer forcedBoard;
	int moveNumber;
	// Koliko je igraču na potezu ostalo do isteka vremena, null kad odbrojavanja nema.
	// Šalje se preostalo vreme, a ne trenutak isteka, da prikaz ne zavisi od sata na klijentu.
	Long turnRemainingMillis;
	boolean finished;
	// Ishod, pobednik i razlog završetka su prazni dok partija traje
	GameOutcome outcome;
	// Prazan i kod nerešenog ishoda
	String winnerUsername;
	GameEndReason endReason;
	// Dosadašnje poruke četa. Popunjeno je samo u odgovoru sa REST-a, da igrač posle osvežavanja
	// strane vidi ceo čet. U stanju koje ide na temu partije je null: nove poruke stižu na temu četa.
	List<ChatMessageDto> chatMessages;
}
