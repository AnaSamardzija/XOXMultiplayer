<script lang="ts">
    import {get} from 'svelte/store';
    import Router, {push, router} from 'svelte-spa-router';
    import {routes} from './routes';
    import {authStore} from './lib/auth/auth.store';
    import Navbar from './lib/components/Navbar.svelte';
    import {connect, disconnect} from './lib/api/websocket.service';
    import {connectionState} from './lib/api/websocket.store';
    import {getActiveGame} from './lib/api/game.api';
    import {activeGameIdStore, applyGameState} from './lib/stores/game.store';

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
    <Router {routes}/>
</main>
