<script lang="ts">
    import {onMount} from 'svelte';
    import {authStore} from '../lib/auth/auth.store';
    import {getLeaderboard} from '../lib/api/stats.api';
    import type {LeaderboardEntryDto} from '../lib/types/stats.types';

    let entries = $state<LeaderboardEntryDto[]>([]);
    let loading = $state(true);
    let error = $state<string | null>(null);

    const myUsername = $derived($authStore.user?.username);

    onMount(async () => {
        try {
            entries = await getLeaderboard();
        } catch (e) {
            error = e instanceof Error ? e.message : 'Učitavanje rang-liste nije uspelo';
        } finally {
            loading = false;
        }
    });
</script>

<div class="card">
    <div class="card-header">
        <i class="bi bi-trophy me-2 text-primary"></i>Rang-lista
    </div>

    {#if loading}
        <div class="card-body text-center py-5">
            <div class="spinner-border text-primary" role="status"></div>
        </div>
    {:else if error}
        <div class="card-body">
            <div class="alert alert-danger mb-0">{error}</div>
        </div>
    {:else if entries.length === 0}
        <div class="card-body text-center text-muted py-5">
            Još nema završenih partija sa pobednikom.
        </div>
    {:else}
        <!-- Prva tri mesta su obojena, a red prijavljenog korisnika istaknut -->
        <div class="table-responsive">
            <table class="table align-middle mb-0">
                <thead>
                <tr>
                    <th class="text-center">Mesto</th>
                    <th>Igrač</th>
                    <th class="text-end">Pobede</th>
                </tr>
                </thead>
                <tbody>
                {#each entries as entry (entry.username)}
                    <tr class:row-me={entry.username === myUsername}>
                        <td class="text-center">
                            <span class="rank-badge"
                                  class:rank-1={entry.rank === 1}
                                  class:rank-2={entry.rank === 2}
                                  class:rank-3={entry.rank === 3}>{entry.rank}</span>
                        </td>
                        <td class="text-break">
                            <span class="fw-semibold">{entry.username}</span>
                            {#if entry.username === myUsername}
                                <span class="badge text-bg-primary ms-1">Vi</span>
                            {/if}
                        </td>
                        <td class="text-end fw-bold">{entry.wins}</td>
                    </tr>
                {/each}
                </tbody>
            </table>
        </div>
    {/if}
</div>
