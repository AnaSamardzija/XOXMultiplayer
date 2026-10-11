<script lang="ts">
    import type {GameStateDto, MiniBoardState, PlayerSymbol} from '../types/game.types';

    let {game}: {game: GameStateDto} = $props();

    // Front ne zna pravila: koje mini-table se ističu čita se iz onoga što je server poslao.
    // Obavezna mini-tabla je jedna, a kod slobodnog poteza server je ne šalje, pa se ističu sve otvorene.
    function isPlayable(boardIndex: number): boolean {
        return game.forcedBoard !== null
            ? game.forcedBoard === boardIndex
            : game.boardStates[boardIndex] === 'OPEN';
    }

    function winnerOf(boardState: MiniBoardState): PlayerSymbol | null {
        if (boardState === 'X_WON') return 'X';
        if (boardState === 'O_WON') return 'O';
        return null;
    }
</script>

<div class="board-wrap">
    <div class="board">
        {#each game.cells as cells, boardIndex}
            {@const winner = winnerOf(game.boardStates[boardIndex])}

            <div class="mini-board"
                 class:mini-board-playable={isPlayable(boardIndex)}
                 class:mini-board-draw={game.boardStates[boardIndex] === 'DRAW'}>
                {#each cells as cell}
                    <button type="button" class="cell" class:symbol-x={cell === 'X'} class:symbol-o={cell === 'O'} disabled>
                        {cell ?? ''}
                    </button>
                {/each}

                <!-- Veliki simbol ide preko polja, a odigrana polja ispod ostaju vidljiva -->
                {#if winner}
                    <div class="mini-board-winner" class:symbol-x={winner === 'X'} class:symbol-o={winner === 'O'}>
                        {winner}
                    </div>
                {/if}
            </div>
        {/each}
    </div>
</div>
