<script lang="ts">
    import {onMount} from 'svelte';
    import {authStore} from '../lib/auth/auth.store';
    import {getMyStats} from '../lib/api/stats.api';
    import type {UserStatsDto} from '../lib/types/stats.types';

    let stats = $state<UserStatsDto | null>(null);
    let loading = $state(true);
    let error = $state<string | null>(null);

    onMount(async () => {
        try {
            stats = await getMyStats();
        } catch (e) {
            error = e instanceof Error ? e.message : 'Učitavanje statistike nije uspelo';
        } finally {
            loading = false;
        }
    });
</script>

<div class="card mb-4">
    <div class="card-body d-flex align-items-center gap-3">
        <span class="icon-circle flex-shrink-0"><i class="bi bi-person"></i></span>
        <div class="overflow-hidden">
            <h4 class="mb-0 text-truncate">{$authStore.user?.username}</h4>
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
{:else if stats}
    <!-- Na telefonu su dve kartice u redu, a na velikom ekranu sve četiri -->
    <div class="row g-3">
        <div class="col-6 col-lg-3">
            <div class="card h-100">
                <div class="card-body">
                    <div class="stat-label"><i class="bi bi-grid-3x3 me-2"></i>Odigrano</div>
                    <div class="stat-value">{stats.played}</div>
                </div>
            </div>
        </div>
        <div class="col-6 col-lg-3">
            <div class="card h-100">
                <div class="card-body">
                    <div class="stat-label"><i class="bi bi-trophy me-2"></i>Pobede</div>
                    <div class="stat-value">{stats.wins}</div>
                </div>
            </div>
        </div>
        <div class="col-6 col-lg-3">
            <div class="card h-100">
                <div class="card-body">
                    <div class="stat-label"><i class="bi bi-x-circle me-2"></i>Porazi</div>
                    <div class="stat-value">{stats.losses}</div>
                </div>
            </div>
        </div>
        <div class="col-6 col-lg-3">
            <div class="card h-100">
                <div class="card-body">
                    <div class="stat-label"><i class="bi bi-dash-circle me-2"></i>Nerešeno</div>
                    <div class="stat-value">{stats.draws}</div>
                </div>
            </div>
        </div>
    </div>
{/if}
