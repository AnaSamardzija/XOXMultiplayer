package rs.ac.ni.pmf.ana.XOXMultiplayer.model;

import org.junit.jupiter.api.Test;
import rs.ac.ni.pmf.ana.XOXMultiplayer.exception.InvalidOperationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UltimateXoxGameTest
{
	@Test
	void shouldWinMiniBoardWithThreeInRow()
	{
		final UltimateXoxGame game = new UltimateXoxGame();

		// X popunjava gornji red mini-table 4, a O je svaki put vraća u nju.
		play(game,
				4, 0,  0, 4,
				4, 1,  1, 4,
				4, 2);

		for (int boardIndex = 0; boardIndex < 9; boardIndex++)
		{
			assertThat(game.getBoardStates()[boardIndex])
					.isEqualTo(boardIndex == 4 ? MiniBoardState.X_WON : MiniBoardState.OPEN);
		}

		assertThat(game.isFinished()).isFalse();
		assertThat(game.getOutcome()).isNull();
	}

	@Test
	void shouldWinGameWithThreeMiniBoardsInRow()
	{
		final UltimateXoxGame game = new UltimateXoxGame();

		// X osvaja mini-table 2, 5 i 8, desnu kolonu velike table.
		play(game,
				8, 2,  2, 5,  5, 4,  4, 5,  5, 3,  3, 5,  5, 5,  7, 0,
				0, 5,  8, 8,  8, 6,  6, 1,  1, 3,  3, 2,  2, 6,  6, 2,
				2, 2,  2, 8,  8, 4,  4, 2);

		assertThat(game.isFinished()).isFalse();

		play(game, 2, 4);

		assertThat(game.getBoardStates()[2]).isEqualTo(MiniBoardState.X_WON);
		assertThat(game.getBoardStates()[5]).isEqualTo(MiniBoardState.X_WON);
		assertThat(game.getBoardStates()[8]).isEqualTo(MiniBoardState.X_WON);
		assertThat(game.isFinished()).isTrue();
		assertThat(game.getOutcome()).isEqualTo(GameOutcome.X_WON);
		assertThatThrownBy(() -> game.applyMove(Symbol.O, 0, 0))
				.isInstanceOf(InvalidOperationException.class);
	}

	@Test
	void shouldEnforceForcedBoardAndFreeMoveWhenItIsClosed()
	{
		final UltimateXoxGame game = new UltimateXoxGame();

		// Prvi potez igrača X je slobodan.
		assertThat(game.getForcedBoard()).isNull();

		play(game, 4, 0);

		assertThat(game.getForcedBoard()).isEqualTo(0);
		assertThatThrownBy(() -> game.applyMove(Symbol.O, 1, 4))
				.isInstanceOf(InvalidOperationException.class);

		// X osvaja mini-tablu 4, pa ga O poljem 4 šalje nazad u nju.
		play(game,
				0, 4,
				4, 1,  1, 4,
				4, 2,  2, 4);

		assertThat(game.getBoardStates()[4]).isEqualTo(MiniBoardState.X_WON);
		assertThat(game.getForcedBoard()).isNull();

		// Polje 8 u osvojenoj mini-tabli 4 je prazno, ali se u nju više ne igra.
		assertThat(game.getCells()[4][8]).isNull();
		assertThatThrownBy(() -> game.applyMove(Symbol.X, 4, 8))
				.isInstanceOf(InvalidOperationException.class);

		play(game, 8, 8);

		assertThat(game.getCells()[8][8]).isEqualTo(Symbol.X);
		assertThat(game.getForcedBoard()).isEqualTo(8);
	}

	@Test
	void shouldEndInDrawWhenAllBoardsClosedWithoutLine()
	{
		final UltimateXoxGame game = new UltimateXoxGame();

		// Velika tabla na kraju:
		//   X O O
		//   O O X
		//   X X O
		play(game,
				0, 1,  1, 0,  0, 5,  5, 2,  2, 3,  3, 0,  0, 3,  3, 3,
				3, 2,  2, 7,  7, 8,  8, 3,  3, 8,  8, 5,  5, 7,  7, 2,
				2, 0,  0, 6,  6, 8,  8, 4,  4, 2,  2, 5,  5, 4,  4, 8,
				5, 1,  1, 4,  4, 5,  1, 8,  3, 1,  3, 6,  6, 0,  0, 0,
				0, 4,  4, 7,  7, 4,  4, 6,  6, 4,  7, 3,  7, 0,  2, 1,
				2, 2);

		assertThat(game.isFinished()).isFalse();

		play(game, 2, 4);

		assertThat(game.getBoardStates()).doesNotContain(MiniBoardState.OPEN);
		assertThat(game.isFinished()).isTrue();
		assertThat(game.getOutcome()).isEqualTo(GameOutcome.DRAW);
	}

	// Igra parove (mini-tabla, polje) redom, svaki za igrača koji je na potezu.
	private void play(final UltimateXoxGame game, final int... pairs)
	{
		for (int i = 0; i < pairs.length; i += 2)
		{
			game.applyMove(game.getCurrentPlayer(), pairs[i], pairs[i + 1]);
		}
	}
}
