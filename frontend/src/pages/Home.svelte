<script lang="ts">
    import {authStore} from '../lib/auth/auth.store';
    import {connectionState, type ConnectionState} from '../lib/api/websocket.store';
    import {send, subscribe} from '../lib/api/websocket.service';
    import type {EchoResponseDto} from '../lib/types/echo.types';

    // Privremeno: probna poruka iz zadatka 006, briše se kad stigne prva prava poruka
    const stateLabels: Record<ConnectionState, string> = {
        connected: 'Povezano',
        connecting: 'Povezivanje…',
        disconnected: 'Nije povezano'
    };

    const stateClasses: Record<ConnectionState, string> = {
        connected: 'text-bg-success',
        connecting: 'text-bg-warning',
        disconnected: 'text-bg-secondary'
    };

    let responses = $state<EchoResponseDto[]>([]);

    // Pretplata se pravi tek kad je veza otvorena, a posle ponovnog povezivanja efekat je obnovi
    $effect(() => {
        if ($connectionState !== 'connected') return;

        return subscribe<EchoResponseDto>('/user/queue/echo', (response) => {
            responses.push(response);
        });
    });

    function sendEcho() {
        send('/app/echo', {text: `Proba u ${new Date().toLocaleTimeString('sr-Latn')}`});
    }
</script>

<div class="card">
    <div class="card-body p-4">
        <h4 class="mb-1">Zdravo, {$authStore.user?.username}</h4>
        <p class="text-muted mb-0">Prijavljeni ste.</p>
    </div>
</div>

<div class="card mt-3">
    <div class="card-body p-4">
        <div class="d-flex flex-wrap align-items-center gap-2 mb-3">
            <span>Veza sa serverom:</span>
            <span class="badge {stateClasses[$connectionState]}">{stateLabels[$connectionState]}</span>
        </div>

        <button class="btn btn-primary" onclick={sendEcho} disabled={$connectionState !== 'connected'}>
            Pošalji probnu poruku
        </button>

        {#if responses.length > 0}
            <ul class="list-group mt-3">
                {#each responses as response}
                    <li class="list-group-item text-break">
                        <strong>{response.username}</strong> (sesija {response.sessionId}): {response.text}
                    </li>
                {/each}
            </ul>
        {/if}
    </div>
</div>
