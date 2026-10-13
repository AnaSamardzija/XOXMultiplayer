<script lang="ts">
    import {onMount} from 'svelte';
    import {router} from 'svelte-spa-router';
    import {authStore} from '../lib/auth/auth.store';
    import {getMyGames} from '../lib/api/stats.api';
    import type {PlayedGameDto, PlayerResult} from '../lib/types/stats.types';

    const FILTERS: {value: PlayerResult | null; label: string}[] = [
        {value: null, label: 'Sve'},
        {value: 'WIN', label: 'Pobede'},
        {value: 'LOSS', label: 'Porazi'},
        {value: 'DRAW', label: 'Nerešeno'}
    ];

    const RESULT_TEXT: Record<PlayerResult, string> = {
        WIN: 'Pobeda',
        LOSS: 'Poraz',
        DRAW: 'Nerešeno'
    };

    let games = $state<PlayedGameDto[]>([]);
    let loading = $state(true);
    let error = $state<string | null>(null);

    // Filter je u adresi, posle „?“ (npr. #/my-games?filter=WIN), pa pločice i dugmad samo linkuju na
    // njega. Promena filtera ne pravi stranu iznova, pa se partije ne učitavaju ponovo.
    const filter = $derived.by<PlayerResult | null>(() => {
        const value = new URLSearchParams(router.querystring ?? '').get('filter');
        return value === 'WIN' || value === 'LOSS' || value === 'DRAW' ? value : null;
    });

    const filteredGames = $derived(filter ? games.filter((game) => game.result === filter) : games);

    const username = $derived($authStore.user?.username ?? '');

    // Brojevi na pločicama se računaju iz učitane liste, pa nema posebnog poziva za statistiku.
    // Klik na pločicu bira isti filter kao dugme ispod.
    const tiles = $derived<{label: string; icon: string; value: number; filter: PlayerResult | null}[]>([
        {label: 'Odigrano', icon: 'bi-grid-3x3', value: games.length, filter: null},
        {label: 'Pobede', icon: 'bi-trophy', value: countOf('WIN'), filter: 'WIN'},
        {label: 'Porazi', icon: 'bi-x-circle', value: countOf('LOSS'), filter: 'LOSS'},
        {label: 'Nerešeno', icon: 'bi-dash-circle', value: countOf('DRAW'), filter: 'DRAW'}
    ]);

    function countOf(result: PlayerResult): number {
        return games.filter((game) => game.result === result).length;
    }

    function filterHref(value: PlayerResult | null): string {
        return value ? `#/my-games?filter=${value}` : '#/my-games';
    }

    onMount(async () => {
        try {
            games = await getMyGames();
        } catch (e) {
            error = e instanceof Error ? e.message : 'Učitavanje partija nije uspelo';
        } finally {
            loading = false;
        }
    });

    // Server šalje samo razlog završetka, a rečenica iz ugla igrača se sklapa ovde. Predaja i prekid
    // veze uvek imaju pobednika, pa ko nije pobedio, taj je predao ili prekinuo vezu.
    function endReasonText(game: PlayedGameDto): string {
        switch (game.endReason) {
            case 'SURRENDER':
                return game.result === 'WIN' ? 'Protivnik je predao' : 'Predali ste';
            case 'DISCONNECT':
                return game.result === 'WIN' ? 'Protivnik je prekinuo vezu' : 'Prekinuli ste vezu';
            case 'THREE_IN_ROW':
                return 'Tri mini-table u nizu';
            case 'ALL_BOARDS_RESOLVED':
                return 'Sve mini-table su rešene bez tri u nizu';
        }
    }

    // „13. okt 2026.“
    function formatDate(value: string): string {
        return new Date(value).toLocaleDateString('sr-Latn-RS', {day: 'numeric', month: 'short', year: 'numeric'});
    }

    // „45 s“, „3 min 20 s“
    function formatDuration(game: PlayedGameDto): string {
        const seconds = Math.max(0, Math.round((Date.parse(game.endedAt) - Date.parse(game.startedAt)) / 1000));
        return seconds < 60 ? `${seconds} s` : `${Math.floor(seconds / 60)} min ${seconds % 60} s`;
    }
</script>

<div class="card mb-4">
    <div class="card-body d-flex align-items-center gap-3">
        <span class="icon-circle flex-shrink-0 fw-bold">{username.charAt(0).toUpperCase()}</span>
        <div class="overflow-hidden">
            <h4 class="mb-0 text-truncate">{username}</h4>
            <div class="text-muted small">Statistika završenih partija</div>
        </div>
    </div>
</div>

{#if loading}
    <div class="text-center py-5">
        <div class="spinner-border text-primary" role="status"></div>
    </div>
{:else if error}
    <div class="alert alert-danger">{error}</div>
{:else}
    <!-- Na telefonu su dve pločice u redu, a na velikom ekranu sve četiri -->
    <div class="row g-3 mb-4">
        {#each tiles as tile (tile.label)}
            <div class="col-6 col-lg-3">
                <a class="card h-100 stat-card" class:stat-card-active={filter === tile.filter} href={filterHref(tile.filter)}>
                    <div class="card-body">
                        <div class="stat-label"><i class="bi {tile.icon} me-2"></i>{tile.label}</div>
                        <div class="stat-value">{tile.value}</div>
                    </div>
                </a>
            </div>
        {/each}
    </div>

    <div class="card">
        <div class="card-header">
            <i class="bi bi-clock-history me-2 text-primary"></i>Moje partije
        </div>

        <div class="card-body d-flex flex-wrap gap-2">
            {#each FILTERS as item (item.label)}
                <a class="btn btn-sm"
                   class:btn-primary={filter === item.value}
                   class:btn-outline-primary={filter !== item.value}
                   href={filterHref(item.value)}>{item.label}</a>
            {/each}
        </div>

        {#if filteredGames.length === 0}
            <div class="card-body text-center text-muted py-5">
                {filter ? 'Nema partija za izabrani filter.' : 'Još nema završenih partija.'}
            </div>
        {:else}
            <!-- Lista, a ne tabela: red ima tri dela koji staju i na telefon, a dug tekst se prelama ili skraćuje -->
            <ul class="list-group list-group-flush">
                {#each filteredGames as game (game.id)}
                    <li class="list-group-item d-flex align-items-center gap-3">
                        <span class="badge result-badge flex-shrink-0"
                              class:text-bg-success={game.result === 'WIN'}
                              class:text-bg-danger={game.result === 'LOSS'}
                              class:text-bg-secondary={game.result === 'DRAW'}>{RESULT_TEXT[game.result]}</span>

                        <div class="flex-grow-1 overflow-hidden">
                            <div class="text-truncate">
                                <strong class:symbol-x={game.symbol === 'X'} class:symbol-o={game.symbol === 'O'}>
                                    {game.symbol}
                                </strong>
                                protiv <span class="fw-semibold">{game.opponentUsername}</span>
                            </div>
                            <div class="text-muted small">{endReasonText(game)}</div>
                        </div>

                        <div class="text-end text-muted small flex-shrink-0">
                            <div>{formatDate(game.endedAt)}</div>
                            <div>{formatDuration(game)}</div>
                        </div>
                    </li>
                {/each}
            </ul>
        {/if}
    </div>
{/if}
