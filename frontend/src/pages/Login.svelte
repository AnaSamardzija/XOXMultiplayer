<script lang="ts">
    import {onMount} from 'svelte';
    import {push} from 'svelte-spa-router';
    import {login} from '../lib/auth/auth.service';

    let username = $state('');
    let password = $state('');
    let error = $state<string | null>(null);
    let loading = $state(false);

    let usernameInput: HTMLInputElement;

    onMount(() => {
        usernameInput.focus();
    });

    async function handleSubmit(e: SubmitEvent) {
        e.preventDefault();
        error = null;
        loading = true;

        try {
            await login(username, password);
            push('/');
        } catch (err) {
            error = err instanceof Error ? err.message : 'Prijava nije uspela';
        } finally {
            loading = false;
        }
    }
</script>

<div class="container">
    <div class="row justify-content-center">
        <div class="col-sm-9 col-md-6 col-lg-4">
            <div class="card">
                <div class="card-body p-4">
                    <div class="text-center mb-4">
                        <span class="icon-circle mb-2"><i class="bi bi-grid-3x3"></i></span>
                        <h4 class="mb-1">Ultimate XOX</h4>
                        <p class="text-muted small mb-0">Prijavite se da biste igrali</p>
                    </div>

                    {#if error}
                        <div class="alert alert-danger d-flex align-items-center py-2 small">
                            <i class="bi bi-exclamation-triangle-fill me-2"></i>{error}
                        </div>
                    {/if}

                    <form onsubmit={handleSubmit}>
                        <div class="mb-3">
                            <label class="form-label" for="username">Korisničko ime</label>
                            <div class="input-group">
                                <span class="input-group-text"><i class="bi bi-person"></i></span>
                                <input id="username"
                                       class="form-control"
                                       autocomplete="username"
                                       bind:value={username}
                                       bind:this={usernameInput}
                                       required/>
                            </div>
                        </div>

                        <div class="mb-3">
                            <label class="form-label" for="password">Lozinka</label>
                            <div class="input-group">
                                <span class="input-group-text"><i class="bi bi-lock"></i></span>
                                <input id="password"
                                       type="password"
                                       class="form-control"
                                       autocomplete="current-password"
                                       bind:value={password}
                                       required/>
                            </div>
                        </div>

                        <button type="submit" class="btn btn-primary w-100" disabled={loading}>
                            {#if loading}
                                <span class="spinner-border spinner-border-sm me-2"></span>Prijava...
                            {:else}
                                <i class="bi bi-box-arrow-in-right me-2"></i>Prijavi se
                            {/if}
                        </button>
                    </form>

                    <p class="text-center text-muted small mt-3 mb-0">
                        Nemate nalog? <a href="#/register">Registrujte se</a>
                    </p>
                </div>
            </div>
        </div>
    </div>
</div>
