<script lang="ts">
    import {get} from 'svelte/store';
    import {push} from 'svelte-spa-router';
    import {authStore} from '../lib/auth/auth.store';
    import {connectionState} from '../lib/api/websocket.store';
    import {send, subscribe} from '../lib/api/websocket.service';
    import {getActiveGame} from '../lib/api/game.api';
    import {
        activeGameIdStore,
        applyChatMessages,
        applyGameState,
        chatStore,
        gameStateStore
    } from '../lib/stores/game.store';
    import Board from '../lib/components/Board.svelte';
    import Chat from '../lib/components/Chat.svelte';
    import ConfirmModal from '../lib/components/ConfirmModal.svelte';
    import type {
        ChatMessageDto,
        ChatMessageRequestDto,
        GameEndReason,
        GameStateDto,
        MoveRequestDto,
        PlayerSymbol
    } from '../lib/types/game.types';

    const END_REASON_TEXT: Record<GameEndReason, string> = {
        THREE_IN_ROW: 'Tri mini-table u nizu',
        ALL_BOARDS_RESOLVED: 'Sve mini-table su rešene bez tri u nizu',
        SURRENDER: 'Predaja',
        DISCONNECT: 'Prekinuta veza'
    };

    let error = $state('');
    let secondsLeft = $state<number | null>(null);
    let showSurrenderModal = $state(false);

    // Posle osvežavanja strane identifikator partije još nije poznat: upisuje ga App.svelte kad od
    // servera sazna da partija postoji. Dotle se ovde vidi „Učitavanje partije…“.
    // U store-u može da ostane stanje prethodne partije dok ne stigne stanje ove.
    const game = $derived(
        $gameStateStore && $gameStateStore.gameId === $activeGameIdStore ? $gameStateStore : null);

    const mySymbol = $derived<PlayerSymbol>(game?.playerXUsername === $authStore.user?.username ? 'X' : 'O');

    const players = $derived<{symbol: PlayerSymbol; username: string; away: boolean}[]>(game ? [
        {symbol: 'X', username: game.playerXUsername, away: game.playerXAway},
        {symbol: 'O', username: game.playerOUsername, away: game.playerOAway}
    ] : []);

    const opponentAway = $derived(game ? (mySymbol === 'X' ? game.playerOAway : game.playerXAway) : false);

    // U store-u mogu da ostanu poruke prethodne partije
    const chatMessages = $derived(game && $chatStore.gameId === game.gameId ? $chatStore.messages : []);

    const resultTitle = $derived.by(() => {
        if (!game?.finished) return '';
        if (game.outcome === 'DRAW') return 'Nerešeno';
        return game.winnerUsername === $authStore.user?.username ? 'Pobedili ste!' : 'Izgubili ste';
    });

    // Redosled je bitan: prvo pretplata na temu partije, pa tek onda stanje preko REST-a. Poruka o
    // uparivanju ne nosi tablu, a stanje koje bi server objavio na temu pre pretplate bi se izgubilo.
    // Posle ponovnog povezivanja efekat se izvrši opet, pa se obnove i pretplata i stanje.
    $effect(() => {
        if ($connectionState !== 'connected' || $activeGameIdStore === null) return;

        const gameId = $activeGameIdStore;
        const unsubscribe = subscribe<GameStateDto>(`/topic/games/${gameId}`, applyGameState);
        // Čet ima svoju temu: na nju stiže jedna nova poruka, a dosadašnje su u odgovoru sa REST-a
        const unsubscribeChat = subscribe<ChatMessageDto>(
            `/topic/games/${gameId}/chat`, (message) => applyChatMessages(gameId, [message]));
        loadGame();

        return () => {
            unsubscribe();
            unsubscribeChat();
        };
    });

    // Odbrojavač služi samo za prikaz: o isteku vremena odlučuje server. Svako novo stanje nosi koliko je
    // ostalo do isteka, pa se odatle računa lokalni rok i odbrojavanje se tako usklađuje sa serverom.
    // Na nuli ostaje 0 dok ne stigne stanje sa potezom koji je server odigrao.
    $effect(() => {
        const remaining = game?.turnRemainingMillis;

        if (remaining === null || remaining === undefined) {
            secondsLeft = null;
            return;
        }

        const deadline = Date.now() + remaining;
        const tick = () => {
            secondsLeft = Math.max(0, Math.ceil((deadline - Date.now()) / 1000));
        };

        tick();
        const interval = setInterval(tick, 250);

        return () => clearInterval(interval);
    });

    async function loadGame() {
        error = '';

        try {
            const state = await getActiveGame();

            // Partije više nema na serveru
            if (!state) {
                // Završena je dok je ovaj ekran otvoren: završni ekran se crta iz poslednjeg primljenog
                // stanja, pa se ostaje na njemu
                const last = get(gameStateStore);
                if (last?.finished && last.gameId === get(activeGameIdStore)) return;

                activeGameIdStore.set(null);
                push('/');
                return;
            }

            applyGameState(state);
        } catch (e) {
            error = e instanceof Error ? e.message : 'Učitavanje partije nije uspelo';
        }
    }

    // Šalje se samo namera. Tabla se ne menja na klik, nego tek kad novo stanje stigne sa teme partije.
    function play(boardIndex: number, cellIndex: number) {
        if (!game) return;

        const move: MoveRequestDto = {boardIndex, cellIndex};
        send(`/app/games/${game.gameId}/move`, move);
    }

    function surrender() {
        if (!game) return;

        send(`/app/games/${game.gameId}/surrender`);
    }

    function sendChatMessage(text: string) {
        if (!game) return;

        const request: ChatMessageRequestDto = {text};
        send(`/app/games/${game.gameId}/chat`, request);
    }

    // Nova partija se traži sa početne strane, jer je tamo pretplata na poruku o uparivanju
    function newGame() {
        activeGameIdStore.set(null);
        push('/');
    }
</script>

{#if error}
    <div class="alert alert-danger">{error}</div>
{/if}

{#if game}
    <div class="d-flex justify-content-center gap-2 mb-2">
        {#each players as player}
            <!-- Dok partija traje istaknut je igrač na potezu, a na završnom ekranu pobednik -->
            <div class="player-chip"
                 class:player-chip-active={game.finished
                     ? game.winnerUsername === player.username
                     : game.currentPlayer === player.symbol}>
                <strong class:symbol-x={player.symbol === 'X'} class:symbol-o={player.symbol === 'O'}>
                    {player.symbol}
                </strong>
                <span class="text-truncate">
                    {player.username}{#if player.symbol === mySymbol}&nbsp;(vi){/if}
                </span>
                {#if player.away}
                    <span class="player-away">odsutan</span>
                {/if}
            </div>
        {/each}
    </div>

    {#if game.finished}
        <div class="text-center mb-3">
            <h4 class="mb-1">{resultTitle}</h4>
            <p class="mb-0">
                {#if game.winnerUsername}
                    Pobednik: <strong>{game.winnerUsername}</strong> ·
                {/if}
                {game.endReason ? END_REASON_TEXT[game.endReason] : ''}
            </p>
        </div>

        <Board {game} canPlay={false} onMove={play}/>

        <div class="text-center mt-3">
            <button class="btn btn-primary btn-lg" onclick={newGame}>Nova partija</button>
        </div>
    {:else}
        <p class="text-center fw-semibold" class:mb-3={!opponentAway} class:mb-1={opponentAway}>
            {game.currentPlayer === mySymbol ? 'Vi ste na potezu' : 'Na potezu je protivnik'}
            {#if secondsLeft !== null}
                · <span class:text-danger={secondsLeft <= 5}>{secondsLeft} s</span>
            {/if}
        </p>

        <!-- Tajmer poteza i dalje teče: dok je protivnik odsutan, server igra umesto njega -->
        {#if opponentAway}
            <p class="text-center text-muted small mb-3">Protivnik je odsutan, čeka se da se vrati.</p>
        {/if}

        <!-- Na širokom ekranu čet je pored table, a na telefonu ispod nje -->
        <div class="row g-3 justify-content-center">
            <div class="col-lg-8">
                <!-- Bez veze poruka ne može da se pošalje, pa su polja onemogućena dok se veza ne vrati -->
                <Board {game} canPlay={game.currentPlayer === mySymbol && $connectionState === 'connected'}
                       onMove={play}/>

                <div class="text-center mt-3">
                    <button class="btn btn-outline-danger btn-sm" onclick={() => showSurrenderModal = true}
                            disabled={$connectionState !== 'connected'}>
                        Predaj partiju
                    </button>
                </div>
            </div>

            <div class="col-lg-4 chat-column">
                <Chat messages={chatMessages} myUsername={$authStore.user?.username ?? ''}
                      disabled={$connectionState !== 'connected'} onSend={sendChatMessage}/>
            </div>
        </div>
    {/if}
{:else if !error}
    <div class="d-flex justify-content-center align-items-center gap-2">
        <div class="spinner-border spinner-border-sm text-primary" role="status"></div>
        <span>Učitavanje partije…</span>
    </div>
{/if}

<!-- Ako se partija završi dok je prozor otvoren, potvrda više nema smisla, pa prozor nestaje -->
{#if game && !game.finished}
    <ConfirmModal
        bind:show={showSurrenderModal}
        title="Predaja partije"
        message="Da li ste sigurni da želite da predate partiju? Pobeda se upisuje protivniku."
        confirmLabel="Predaj partiju"
        onConfirm={surrender}
    />
{/if}
