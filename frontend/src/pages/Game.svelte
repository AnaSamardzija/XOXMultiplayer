<script lang="ts">
    import {get} from 'svelte/store';
    import {push} from 'svelte-spa-router';
    import {authStore} from '../lib/auth/auth.store';
    import {connectionState} from '../lib/api/websocket.store';
    import {subscribe} from '../lib/api/websocket.service';
    import {getActiveGame} from '../lib/api/game.api';
    import {applyGameState, gameStateStore, matchStore} from '../lib/stores/game.store';
    import Board from '../lib/components/Board.svelte';
    import type {GameStateDto, PlayerSymbol} from '../lib/types/game.types';

    let error = $state('');

    // Privremeno: identifikator partije postoji samo u memoriji pregledača, pa posle osvežavanja strane
    // nema na koju temu da se pretplati. Povratak u partiju dolazi u kasnijem zadatku.
    if (!get(matchStore)) {
        push('/');
    }

    // U store-u može da ostane stanje prethodne partije dok ne stigne stanje ove
    const game = $derived($gameStateStore?.gameId === $matchStore?.gameId ? $gameStateStore : null);

    const mySymbol = $derived<PlayerSymbol>(game?.playerXUsername === $authStore.user?.username ? 'X' : 'O');

    const players = $derived<{symbol: PlayerSymbol; username: string}[]>(game ? [
        {symbol: 'X', username: game.playerXUsername},
        {symbol: 'O', username: game.playerOUsername}
    ] : []);

    // Redosled je bitan: prvo pretplata na temu partije, pa tek onda stanje preko REST-a. Poruka o
    // uparivanju ne nosi tablu, a stanje koje bi server objavio na temu pre pretplate bi se izgubilo.
    // Posle ponovnog povezivanja efekat se izvrši opet, pa se obnove i pretplata i stanje.
    $effect(() => {
        if ($connectionState !== 'connected' || !$matchStore) return;

        const unsubscribe = subscribe<GameStateDto>(`/topic/games/${$matchStore.gameId}`, applyGameState);
        loadGame();

        return unsubscribe;
    });

    async function loadGame() {
        error = '';

        try {
            const state = await getActiveGame();

            // Partije više nema na serveru
            if (!state) {
                matchStore.set(null);
                push('/');
                return;
            }

            applyGameState(state);
        } catch (e) {
            error = e instanceof Error ? e.message : 'Učitavanje partije nije uspelo';
        }
    }
</script>

{#if error}
    <div class="alert alert-danger">{error}</div>
{/if}

{#if game}
    <div class="d-flex justify-content-center gap-2 mb-2">
        {#each players as player}
            <div class="player-chip" class:player-chip-active={game.currentPlayer === player.symbol}>
                <strong class:symbol-x={player.symbol === 'X'} class:symbol-o={player.symbol === 'O'}>
                    {player.symbol}
                </strong>
                <span class="text-truncate">
                    {player.username}{#if player.symbol === mySymbol}&nbsp;(vi){/if}
                </span>
            </div>
        {/each}
    </div>

    <p class="text-center fw-semibold mb-3">
        {game.currentPlayer === mySymbol ? 'Vi ste na potezu' : 'Na potezu je protivnik'}
    </p>

    <Board {game}/>
{:else if !error}
    <div class="d-flex justify-content-center align-items-center gap-2">
        <div class="spinner-border spinner-border-sm text-primary" role="status"></div>
        <span>Učitavanje partije…</span>
    </div>
{/if}
