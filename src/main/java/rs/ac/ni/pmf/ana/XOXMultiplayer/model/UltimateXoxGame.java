package rs.ac.ni.pmf.ana.XOXMultiplayer.model;

import lombok.Getter;
import rs.ac.ni.pmf.ana.XOXMultiplayer.exception.InvalidOperationException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Pravila igre Ultimate XOX i stanje jedne partije. Nije Spring komponenta:
 * za svaku partiju se pravi nova. Mini-table i polja imaju indekse od 0 do 8,
 * red po red (indeks = red * 3 + kolona).
 */
@Getter
public class UltimateXoxGame
{
	private static final int SIZE = 9;

	// Iste linije važe i za polja u mini-tabli i za mini-table na velikoj tabli.
	private static final int[][] WIN_LINES = {
			{0, 1, 2}, {3, 4, 5}, {6, 7, 8},
			{0, 3, 6}, {1, 4, 7}, {2, 5, 8},
			{0, 4, 8}, {2, 4, 6}
	};

	private static final Random RANDOM = new Random();

	// cells[mini-tabla][polje], null je prazno polje
	private final Symbol[][] cells = new Symbol[SIZE][SIZE];
	private final MiniBoardState[] boardStates = new MiniBoardState[SIZE];
	private Symbol currentPlayer = Symbol.X;
	// null znači slobodan potez, kao što je prvi potez igrača X
	private Integer forcedBoard;
	// Redni broj poslednjeg odigranog poteza. Ide uz svako stanje koje server pošalje,
	// da klijent odbaci stanje starije od onog koje već prikazuje.
	private int moveCount;
	private boolean finished;
	private GameOutcome outcome;

	public UltimateXoxGame()
	{
		Arrays.fill(boardStates, MiniBoardState.OPEN);
	}

	public boolean isMoveAllowed(final int boardIndex, final int cellIndex)
	{
		if (finished)
		{
			return false;
		}

		if (boardIndex < 0 || boardIndex >= SIZE || cellIndex < 0 || cellIndex >= SIZE)
		{
			return false;
		}

		// Osvojena mini-tabla je zatvorena i kad u njoj ima praznih polja.
		if (boardStates[boardIndex] != MiniBoardState.OPEN)
		{
			return false;
		}

		if (forcedBoard != null && forcedBoard != boardIndex)
		{
			return false;
		}

		return cells[boardIndex][cellIndex] == null;
	}

	public void applyMove(final Symbol symbol, final int boardIndex, final int cellIndex)
	{
		if (finished)
		{
			throw new InvalidOperationException("Partija je završena");
		}

		if (symbol != currentPlayer)
		{
			throw new InvalidOperationException("Nisi na potezu");
		}

		if (!isMoveAllowed(boardIndex, cellIndex))
		{
			throw new InvalidOperationException("Potez nije dozvoljen");
		}

		cells[boardIndex][cellIndex] = symbol;
		moveCount++;
		resolveMiniBoard(boardIndex, symbol);

		if (hasBigBoardLine(symbol))
		{
			finish(symbol == Symbol.X ? GameOutcome.X_WON : GameOutcome.O_WON);
			return;
		}

		if (Arrays.stream(boardStates).noneMatch(state -> state == MiniBoardState.OPEN))
		{
			finish(GameOutcome.DRAW);
			return;
		}

		// Obavezna mini-tabla se određuje tek posle primene poteza, jer potez može
		// da zatvori baš tablu u koju šalje, i tada protivnik igra slobodno.
		forcedBoard = boardStates[cellIndex] == MiniBoardState.OPEN ? cellIndex : null;
		currentPlayer = symbol == Symbol.X ? Symbol.O : Symbol.X;
	}

	// Predati sme i igrač koji nije na potezu. Redni broj poteza se ne menja, jer predaja nije potez.
	public void surrender(final Symbol symbol)
	{
		if (finished)
		{
			throw new InvalidOperationException("Partija je završena");
		}

		finish(symbol == Symbol.X ? GameOutcome.O_WON : GameOutcome.X_WON);
	}

	/**
	 * Nasumičan dozvoljen potez za igrača na potezu, za slučaj kad mu istekne vreme.
	 * Potez se samo vraća, a primenjuje se kroz applyMove kao i ručni.
	 */
	public Move randomMove()
	{
		final List<Move> allowedMoves = new ArrayList<>();

		for (int boardIndex = 0; boardIndex < SIZE; boardIndex++)
		{
			for (int cellIndex = 0; cellIndex < SIZE; cellIndex++)
			{
				if (isMoveAllowed(boardIndex, cellIndex))
				{
					allowedMoves.add(new Move(boardIndex, cellIndex));
				}
			}
		}

		if (allowedMoves.isEmpty())
		{
			throw new InvalidOperationException("Partija je završena");
		}

		return allowedMoves.get(RANDOM.nextInt(allowedMoves.size()));
	}

	private void resolveMiniBoard(final int boardIndex, final Symbol symbol)
	{
		final Symbol[] board = cells[boardIndex];

		for (final int[] line : WIN_LINES)
		{
			if (board[line[0]] == symbol && board[line[1]] == symbol && board[line[2]] == symbol)
			{
				boardStates[boardIndex] = symbol == Symbol.X ? MiniBoardState.X_WON : MiniBoardState.O_WON;
				return;
			}
		}

		// Popunjena mini-tabla bez pobednika ne pripada nikome.
		if (Arrays.stream(board).noneMatch(cell -> cell == null))
		{
			boardStates[boardIndex] = MiniBoardState.DRAW;
		}
	}

	private boolean hasBigBoardLine(final Symbol symbol)
	{
		final MiniBoardState won = symbol == Symbol.X ? MiniBoardState.X_WON : MiniBoardState.O_WON;

		for (final int[] line : WIN_LINES)
		{
			if (boardStates[line[0]] == won && boardStates[line[1]] == won && boardStates[line[2]] == won)
			{
				return true;
			}
		}

		return false;
	}

	private void finish(final GameOutcome gameOutcome)
	{
		finished = true;
		outcome = gameOutcome;
		forcedBoard = null;
	}
}
