<script lang="ts">
    import {get} from 'svelte/store';
    import Router, {push, router} from 'svelte-spa-router';
    import {routes} from './routes';
    import {authStore} from './lib/auth/auth.store';
    import Navbar from './lib/components/Navbar.svelte';
    import WaitingNotice from './lib/components/WaitingNotice.svelte';
    import {connect, disconnect, subscribe} from './lib/api/websocket.service';
    import {connectionState} from './lib/api/websocket.store';
    import {getActiveGame} from './lib/api/game.api';
    import {getLobby} from './lib/api/lobby.api';
    import {activeGameIdStore, applyGameState} from './lib/stores/game.store';
    import {waitingUsernameStore} from './lib/stores/lobby.store';
    import type {LobbyWaitingDto} from './lib/types/lobby.types';

    // WebSocket veza prati prijavu: otvara se čim postoji token, a zatvara se na odjavu
    $effect(() => {
        if ($authStore.accessToken) {
            connect();
        } else {
            disconnect();
        }
    });

    // Povratak u partiju u toku. Veza postaje 'connected' pri učitavanju aplikacije i posle svakog
    // ponovnog povezivanja, pa se oba slučaja pokrivaju ovde. Server se pita preko REST-a, a ne čeka
    // se njegova poruka: u trenutku povezivanja pretplate na lični kanal još nema i poruka bi se izgubila.
    $effect(() => {
        if ($connectionState !== 'connected') return;

        restoreActiveGame();
    });

    // Ko čeka protivnika. Pretplata je ovde, a ne na početnoj strani, jer se obaveštenje vidi na svakoj
    // strani. Server na temu objavljuje samo promene, pa se stanje zatečeno pri povezivanju pita preko
    // REST-a, i to tek posle pretplate, da se promena između ta dva ne izgubi.
    $effect(() => {
        if ($connectionState !== 'connected') return;

        // Poruka sa teme je uvek novija od odgovora sa REST-a koji stigne posle nje, pa se on tada odbacuje
        let received = false;

        const unsubscribe = subscribe<LobbyWaitingDto>('/topic/lobby', (message) => {
            received = true;
            waitingUsernameStore.set(message.waitingUsername);
        });

        loadWaitingUsername(() => received);

        return () => {
            // Bez veze se ne zna ko čeka; odgovor sa REST-a koji je još na putu više ne važi
            received = true;
            unsubscribe();
            waitingUsernameStore.set(null);
        };
    });

    async function loadWaitingUsername(isOutdated: () => boolean) {
        try {
            const lobby = await getLobby();

            if (!isOutdated()) waitingUsernameStore.set(lobby.waitingUsername);
        } catch {
            // Sledeća poruka sa teme donosi stanje
        }
    }

    async function restoreActiveGame() {
        try {
            const state = await getActiveGame();

            if (state) {
                applyGameState(state);
                activeGameIdStore.set(state.gameId);
                push('/game');
            } else if (router.location === '/game' && get(activeGameIdStore) === null) {
                // Ekran partije je otvoren, a partije nema, na primer strana je osvežena pošto je
                // rok za povratak istekao
                push('/');
            }
        } catch {
            // Sledeće povezivanje pita ponovo
        }
    }
</script>

<!-- Ljuska strane: prijavljen korisnik dobija zaglavlje i sadržaj ispod njega,
     a gost (prijava, registracija) sadržaj na sredini ekrana -->
{#if $authStore.accessToken}
    <Navbar/>
{/if}

<main class={$authStore.accessToken ? 'container py-4' : 'auth-page'}>
    {#if $authStore.accessToken}
        <WaitingNotice/>
    {/if}

    <Router {routes}/>
</main>
