<script lang="ts">
    import {push} from 'svelte-spa-router';
    import {authStore} from '../auth/auth.store';
    import {logout} from '../auth/auth.service';

    function handleLogout() {
        logout();
        push('/login');
    }
</script>

<nav class="navbar navbar-app" data-bs-theme="dark">
    <div class="container flex-nowrap">
        <a class="navbar-brand d-flex align-items-center gap-2 flex-shrink-0" href="#/">
            <i class="bi bi-grid-3x3"></i>
            Ultimate XOX
        </a>

        <!-- overflow-hidden dozvoljava da se dugo korisničko ime skrati umesto da prelomi traku -->
        <div class="d-flex align-items-center gap-2 overflow-hidden">
            <span class="navbar-user text-truncate">
                <i class="bi bi-person-circle me-1"></i>{$authStore.user?.username}
            </span>
            <button class="btn btn-outline-light btn-sm flex-shrink-0" onclick={handleLogout}>
                <i class="bi bi-box-arrow-right"></i><span class="d-none d-sm-inline ms-1">Odjavi se</span>
            </button>
        </div>
    </div>
</nav>
