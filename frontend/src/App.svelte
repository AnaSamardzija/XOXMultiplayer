<script lang="ts">
    import Router from 'svelte-spa-router';
    import {routes} from './routes';
    import {authStore} from './lib/auth/auth.store';
    import Navbar from './lib/components/Navbar.svelte';
</script>

<!-- Ljuska strane: prijavljen korisnik dobija zaglavlje i sadržaj ispod njega,
     a gost (prijava, registracija) sadržaj na sredini ekrana -->
{#if $authStore.accessToken}
    <Navbar/>
{/if}

<main class={$authStore.accessToken ? 'container py-4' : 'auth-page'}>
    <Router {routes}/>
</main>
