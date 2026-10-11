<script lang="ts">
    import type {GameStateDto, MiniBoardState, PlayerSymbol} from '../types/game.types';

    // canPlay kaže da li je ovaj igrač na potezu; bez toga su sva polja onemogućena
    let {game, canPlay, onMove}: {
        game: GameStateDto;
        canPlay: boolean;
        onMove: (boardIndex: number, cellIndex: number) => void;
    } = $props();

    // Front ne zna pravila: koje mini-table se ističu čita se iz onoga što je server poslao.
    // Obavezna mini-tabla je jedna, a kod slobodnog poteza server je ne šalje, pa se ističu sve otvorene.
    // Na završnom ekranu se ne ističe nijedna.
    function isPlayable(boardIndex: number): boolean {
        if (game.finished) return false;

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
                {#each cells as cell, cellIndex}
                    <button type="button" class="cell" class:symbol-x={cell === 'X'} class:symbol-o={cell === 'O'}
                            disabled={!canPlay || !isPlayable(boardIndex) || cell !== null}
                            onclick={() => onMove(boardIndex, cellIndex)}>
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
