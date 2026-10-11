<script lang="ts">
    import {get} from 'svelte/store';
    import {push} from 'svelte-spa-router';
    import {authStore} from '../lib/auth/auth.store';
    import {connectionState} from '../lib/api/websocket.store';
    import {send, subscribe} from '../lib/api/websocket.service';
    import {activeGameIdStore} from '../lib/stores/game.store';
    import type {MatchFoundDto} from '../lib/types/game.types';

    let waiting = $state(false);

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

        return () => {
            // Odlazak sa strane dok se čeka: igrač izlazi iz reda. Kad je veza pukla, server ga je
            // već sam izbacio, pa se samo vraća dugme „Igraj“.
            if (waiting && get(connectionState) === 'connected') {
                send('/app/matchmaking/leave');
            }
            waiting = false;
            unsubscribe();
        };
    });

    function joinQueue() {
        send('/app/matchmaking/join');
        waiting = true;
    }

    function leaveQueue() {
        send('/app/matchmaking/leave');
        waiting = false;
    }
</script>

<div class="card">
    <div class="card-body p-4 text-center">
        <h4 class="mb-3">Zdravo, {$authStore.user?.username}</h4>

        {#if waiting}
            <div class="d-flex justify-content-center align-items-center gap-2 mb-3">
                <div class="spinner-border spinner-border-sm text-primary" role="status"></div>
                <span>Čekanje protivnika…</span>
            </div>

            <button class="btn btn-outline-primary" onclick={leaveQueue}>Odustani</button>
        {:else}
            <p class="text-muted mb-3">Uđite u red čekanja i server će vam naći protivnika.</p>

            <button class="btn btn-primary btn-lg" onclick={joinQueue} disabled={$connectionState !== 'connected'}>
                Igraj
            </button>

            {#if $connectionState !== 'connected'}
                <p class="text-muted small mt-2 mb-0">Povezivanje sa serverom…</p>
            {/if}
        {/if}
    </div>
</div>
