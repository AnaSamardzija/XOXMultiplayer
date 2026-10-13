<script lang="ts">
    import {push} from 'svelte-spa-router';
    import {authStore} from '../auth/auth.store';
    import {logout} from '../auth/auth.service';
    import {gameInProgressStore} from '../stores/game.store';

    let menuOpen = $state(false);
    let menuElement = $state<HTMLElement>();

    // Meni se zatvara klikom bilo gde van njega; klik na stavku ga zatvara sam
    function handleWindowClick(e: MouseEvent) {
        if (menuOpen && !menuElement?.contains(e.target as Node)) {
            menuOpen = false;
        }
    }

    function handleLogout() {
        menuOpen = false;
        logout();
        push('/login');
    }
</script>

<svelte:window onclick={handleWindowClick}/>

<nav class="navbar navbar-app" data-bs-theme="dark">
    <div class="container flex-nowrap">
        <!-- Tokom partije naziv vodi na ekran partije: klik tada ne menja adresu, pa se ekran ne pravi iznova -->
        <a class="navbar-brand d-flex align-items-center gap-2 flex-shrink-0" href={$gameInProgressStore ? '#/game' : '#/'}>
            <i class="bi bi-grid-3x3"></i>
            Ultimate XOX
        </a>

        <!-- Dok partija traje nema linkova ni menija: sa partije se izlazi predajom ili krajem partije,
             pa odjava usred partije nije moguća -->
        {#if !$gameInProgressStore}
            <div class="navbar-actions d-flex align-items-center gap-2">
                <a class="btn btn-outline-light btn-sm flex-shrink-0" href="#/leaderboard" title="Rang-lista" aria-label="Rang-lista">
                    <i class="bi bi-trophy"></i><span class="d-none d-sm-inline ms-1">Rang-lista</span>
                </a>

                <!-- Padajući meni je čist Svelte: Bootstrap-ov JavaScript se ne koristi -->
                <div class="navbar-user-menu" bind:this={menuElement}>
                    <button type="button"
                            class="navbar-user d-flex align-items-center mw-100 border-0 bg-transparent p-0"
                            aria-expanded={menuOpen}
                            onclick={() => menuOpen = !menuOpen}>
                        <i class="bi bi-person-circle me-1"></i>
                        <!-- Dugo korisničko ime se skraćuje umesto da prelomi traku -->
                        <span class="text-truncate">{$authStore.user?.username}</span>
                        <i class="bi bi-chevron-down ms-1 small"></i>
                    </button>

                    {#if menuOpen}
                        <div class="dropdown-menu show" data-bs-theme="light">
                            <a class="dropdown-item" href="#/my-games" onclick={() => menuOpen = false}>
                                <i class="bi bi-clock-history me-2"></i>Moje partije
                            </a>
                            <a class="dropdown-item" href="#/settings" onclick={() => menuOpen = false}>
                                <i class="bi bi-gear me-2"></i>Podešavanje profila
                            </a>
                            <div class="dropdown-divider"></div>
                            <button type="button" class="dropdown-item" onclick={handleLogout}>
                                <i class="bi bi-box-arrow-right me-2"></i>Odjavi se
                            </button>
                        </div>
                    {/if}
                </div>
            </div>
        {/if}
    </div>
</nav>
