<script lang="ts">
    import {onMount} from 'svelte';
    import {get} from 'svelte/store';
    import {push} from 'svelte-spa-router';
    import {authStore} from '../lib/auth/auth.store';
    import {connectionState} from '../lib/api/websocket.store';
    import {send, subscribe} from '../lib/api/websocket.service';
    import {activeGameIdStore} from '../lib/stores/game.store';
    import {getLobby} from '../lib/api/lobby.api';
    import {getLeaderboard, getMyStats} from '../lib/api/stats.api';
    import HowToPlay from '../lib/components/HowToPlay.svelte';
    import type {MatchFoundDto} from '../lib/types/game.types';
    import type {LobbyDto} from '../lib/types/lobby.types';
    import type {LeaderboardEntryDto, UserStatsDto} from '../lib/types/stats.types';

    const QUEUE_TIMEOUT_SECONDS = 60;
    const LOBBY_REFRESH_MILLIS = 5000;

    let waiting = $state(false);
    let waitedSeconds = $state(0);
    let queueMessage = $state('');
    let lobby = $state<LobbyDto | null>(null);

    const TOP_PLAYERS_COUNT = 3;

    let stats = $state<UserStatsDto | null>(null);
    let statsError = $state('');
    let topPlayers = $state<LeaderboardEntryDto[] | null>(null);
    let topPlayersError = $state('');

    const myUsername = $derived($authStore.user?.username);

    // Statistika i rang-lista se učitavaju nezavisno: ako jedna padne, greška se vidi samo u njenoj
    // celini, a ostatak strane i dugme „Igraj“ rade
    onMount(() => {
        loadStats();
        loadTopPlayers();
    });

    async function loadStats() {
        try {
            stats = await getMyStats();
        } catch (e) {
            statsError = e instanceof Error ? e.message : 'Učitavanje statistike nije uspelo';
        }
    }

    async function loadTopPlayers() {
        try {
            topPlayers = (await getLeaderboard()).slice(0, TOP_PLAYERS_COUNT);
        } catch (e) {
            topPlayersError = e instanceof Error ? e.message : 'Učitavanje rang-liste nije uspelo';
        }
    }

    // Pretplata na lični kanal postoji čim je veza otvorena, dakle pre klika na „Igraj“: server poruku
    // o uparivanju šalje samo jednom i izgubila bi se da pretplate još nema.
    // Posle ponovnog povezivanja efekat se izvrši opet i pretplata se obnovi.
    $effect(() => {
        if ($connectionState !== 'connected') return;

        const unsubscribe = subscribe<MatchFoundDto>('/user/queue/match', (match) => {
            waiting = false;
            activeGameIdStore.set(match.gameId);
            push('/game');
        });

        // Broj igrača na mreži se učitava tek kad je veza otvorena, a ne čim se strana otvori:
        // inače bi odgovor mogao da stigne pre nego što server upiše vezu ovog igrača.
        // Zatim se osvežava dok god je početna strana otvorena, da se vidi kad se neko prijavi.
        loadLobby();
        const lobbyInterval = setInterval(loadLobby, LOBBY_REFRESH_MILLIS);

        return () => {
            clearInterval(lobbyInterval);

            // Odlazak sa strane dok se čeka: igrač izlazi iz reda. Kad je veza pukla, server ga je
            // već sam izbacio, pa se samo vraća dugme „Igraj“.
            if (waiting && get(connectionState) === 'connected') {
                send('/app/matchmaking/leave');
            }
            waiting = false;
            unsubscribe();
        };
    });

    // Rok za čekanje vodi front: server igrača drži u redu neograničeno, a izlazak iz reda već postoji.
    // Efekat se gasi čim waiting postane false (nađen protivnik, „Odustani“, pukla veza) i kad se
    // ode sa strane, pa se odbrojavanje u svim tim slučajevima prekida na jednom mestu.
    $effect(() => {
        if (!waiting) return;

        const startedAt = Date.now();
        const tick = () => {
            // Lokalna promenljiva, da efekat ne čita waitedSeconds: inače bi se na svaku promenu
            // pokrenuo iznova i brojanje bi krenulo od nule
            const seconds = Math.floor((Date.now() - startedAt) / 1000);
            waitedSeconds = seconds;

            if (seconds >= QUEUE_TIMEOUT_SECONDS) {
                leaveQueue();
                queueMessage = 'Trenutno nema slobodnih igrača. Pokušajte ponovo kasnije.';
            }
        };

        tick();
        const interval = setInterval(tick, 250);

        return () => clearInterval(interval);
    });

    async function loadLobby() {
        try {
            lobby = await getLobby();
        } catch {
            // Podatak je sporedan: ostaje poslednji učitan, a sledeće osvežavanje pokušava ponovo
        }
    }

    // „1 igrač“, „2 igrača“, „11 igrača“, „21 igrač“
    function playersText(count: number): string {
        return `${count} ${count % 10 === 1 && count % 100 !== 11 ? 'igrač' : 'igrača'}`;
    }

    // „1 partija“, „2 partije“, „5 partija“, „12 partija“, „22 partije“
    function gamesText(count: number): string {
        const few = count % 10 >= 2 && count % 10 <= 4 && (count % 100 < 12 || count % 100 > 14);
        return `${count} ${few ? 'partije' : 'partija'} u toku`;
    }

    function joinQueue() {
        queueMessage = '';
        send('/app/matchmaking/join');
        waiting = true;
    }

    function leaveQueue() {
        send('/app/matchmaking/leave');
        waiting = false;
    }
</script>

<div class="card mb-4">
    <div class="card-body p-4 text-center">
        <h4 class="mb-3">Zdravo, {$authStore.user?.username}</h4>

        {#if waiting}
            <div class="d-flex justify-content-center align-items-center gap-2 mb-3">
                <div class="spinner-border spinner-border-sm text-primary" role="status"></div>
                <span>Čekanje protivnika… {waitedSeconds} s</span>
            </div>

            <button class="btn btn-outline-primary" onclick={leaveQueue}>Odustani</button>
        {:else}
            {#if queueMessage}
                <div class="alert alert-warning">{queueMessage}</div>
            {/if}

            <p class="text-muted mb-3">Uđite u red čekanja i server će vam naći protivnika.</p>

            <button class="btn btn-primary btn-lg" onclick={joinQueue} disabled={$connectionState !== 'connected'}>
                Igraj
            </button>

            {#if $connectionState !== 'connected'}
                <p class="text-muted small mt-2 mb-0">Povezivanje sa serverom…</p>
            {/if}
        {/if}

        {#if lobby}
            <p class="text-muted small mt-3 mb-0">
                Na mreži: {playersText(lobby.onlinePlayers)} · {gamesText(lobby.activeGames)}
            </p>
        {/if}
    </div>
</div>

<!-- Na telefonu su kartice jedna ispod druge. Na velikom ekranu su „Vaše partije“ i „Najbolji igrači“
     jedna pored druge, a „Kako se igra“ ispod njih, u punoj širini. -->
<div class="row g-4">
    <div class="col-12 col-lg-6">
        <div class="card h-100">
            <div class="card-header d-flex justify-content-between align-items-center">
                <span><i class="bi bi-bar-chart me-2 text-primary"></i>Vaše partije</span>
                <a class="small fw-normal" href="#/profile">Profil</a>
            </div>
            <!-- Kartica je visoka koliko i susedna, pa su brojevi na sredini po visini -->
            <div class="card-body d-flex flex-column justify-content-center">
                {#if statsError}
                    <div class="alert alert-danger mb-0">{statsError}</div>
                {:else if stats}
                    <!-- Tri broja ostaju u jednom redu i na telefonu -->
                    <div class="row text-center">
                        <div class="col-4">
                            <div class="stat-value">{stats.wins}</div>
                            <div class="stat-label">Pobede</div>
                        </div>
                        <div class="col-4">
                            <div class="stat-value">{stats.losses}</div>
                            <div class="stat-label">Porazi</div>
                        </div>
                        <div class="col-4">
                            <div class="stat-value">{stats.draws}</div>
                            <div class="stat-label">Nerešeno</div>
                        </div>
                    </div>
                {:else}
                    <div class="text-center">
                        <div class="spinner-border spinner-border-sm text-primary" role="status"></div>
                    </div>
                {/if}
            </div>
        </div>
    </div>

    <div class="col-12 col-lg-6">
        <div class="card h-100">
            <div class="card-header d-flex justify-content-between align-items-center">
                <span><i class="bi bi-trophy me-2 text-primary"></i>Najbolji igrači</span>
                <a class="small fw-normal" href="#/leaderboard">Cela rang-lista</a>
            </div>

            {#if topPlayersError}
                <div class="card-body">
                    <div class="alert alert-danger mb-0">{topPlayersError}</div>
                </div>
            {:else if topPlayers === null}
                <div class="card-body text-center">
                    <div class="spinner-border spinner-border-sm text-primary" role="status"></div>
                </div>
            {:else if topPlayers.length === 0}
                <div class="card-body text-center text-muted">
                    Još nema završenih partija sa pobednikom.
                </div>
            {:else}
                <ul class="list-group list-group-flush">
                    {#each topPlayers as entry (entry.username)}
                        <li class="list-group-item d-flex align-items-center gap-3">
                            <span class="rank-badge flex-shrink-0"
                                  class:rank-1={entry.rank === 1}
                                  class:rank-2={entry.rank === 2}
                                  class:rank-3={entry.rank === 3}>{entry.rank}</span>
                            <span class="flex-grow-1 text-truncate">
                                <span class="fw-semibold">{entry.username}</span>
                                {#if entry.username === myUsername}
                                    <span class="badge text-bg-primary ms-1">Vi</span>
                                {/if}
                            </span>
                            <span class="fw-bold flex-shrink-0">{entry.wins}</span>
                        </li>
                    {/each}
                </ul>
            {/if}
        </div>
    </div>

    <div class="col-12">
        <HowToPlay/>
    </div>
</div>
