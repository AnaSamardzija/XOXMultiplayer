<script lang="ts">
    import {push} from 'svelte-spa-router';
    import {authStore} from '../auth/auth.store';
    import {logout} from '../auth/auth.service';
    import {gameInProgressStore} from '../stores/game.store';

    function handleLogout() {
        logout();
        push('/login');
    }
</script>

<nav class="navbar navbar-app" data-bs-theme="dark">
    <div class="container flex-nowrap">
        <!-- Tokom partije naziv vodi na ekran partije: klik tada ne menja adresu, pa se ekran ne pravi iznova -->
        <a class="navbar-brand d-flex align-items-center gap-2 flex-shrink-0" href={$gameInProgressStore ? '#/game' : '#/'}>
            <i class="bi bi-grid-3x3"></i>
            Ultimate XOX
        </a>

        <!-- Dok partija traje nema linkova ni odjave: sa partije se izlazi predajom ili krajem partije -->
        {#if !$gameInProgressStore}
            <!-- overflow-hidden dozvoljava da se dugo korisničko ime skrati umesto da prelomi traku -->
            <div class="d-flex align-items-center gap-2 overflow-hidden">
                <a class="btn btn-outline-light btn-sm flex-shrink-0" href="#/leaderboard" title="Rang-lista" aria-label="Rang-lista">
                    <i class="bi bi-trophy"></i><span class="d-none d-sm-inline ms-1">Rang-lista</span>
                </a>
                <a class="navbar-user text-truncate text-decoration-none" href="#/profile" title="Profil">
                    <i class="bi bi-person-circle me-1"></i>{$authStore.user?.username}
                </a>
                <button class="btn btn-outline-light btn-sm flex-shrink-0" onclick={handleLogout}>
                    <i class="bi bi-box-arrow-right"></i><span class="d-none d-sm-inline ms-1">Odjavi se</span>
                </button>
            </div>
        {/if}
    </div>
</nav>
