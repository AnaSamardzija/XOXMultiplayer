<script lang="ts">
    import {get} from 'svelte/store';
    import {push} from 'svelte-spa-router';
    import {matchStore} from '../lib/stores/game.store';

    // Privremeno: podaci o partiji postoje samo u memoriji pregledača, pa posle osvežavanja strane
    // nema šta da se prikaže. Zadatak 008 ovde dovodi tablu i stanje sa servera.
    if (!get(matchStore)) {
        push('/');
    }
</script>

{#if $matchStore}
    <div class="card">
        <div class="card-body p-4 text-center">
            <h4 class="mb-3">Partija #{$matchStore.gameId}</h4>
            <p class="mb-2">Protivnik: <strong>{$matchStore.opponentUsername}</strong></p>
            <p class="mb-0">Vaš simbol: <strong>{$matchStore.symbol}</strong></p>
        </div>
    </div>
{/if}
