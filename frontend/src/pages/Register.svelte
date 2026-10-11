<script lang="ts">
    import {onMount} from 'svelte';
    import {push} from 'svelte-spa-router';
    import {register} from '../lib/auth/auth.service';

    let username = $state('');
    let password = $state('');
    let confirmPassword = $state('');
    let error = $state<string | null>(null);
    let loading = $state(false);

    let usernameInput: HTMLInputElement;

    onMount(() => {
        usernameInput.focus();
    });

    async function handleSubmit(e: SubmitEvent) {
        e.preventDefault();
        error = null;

        // Ponovljena lozinka se proverava samo ovde, backend dobija jednu lozinku
        if (password !== confirmPassword) {
            error = 'Lozinke se ne poklapaju';
            return;
        }

        loading = true;
        try {
            await register({username, password});
            push('/');
        } catch (err) {
            error = err instanceof Error ? err.message : 'Registracija nije uspela';
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
                        <span class="icon-circle mb-2"><i class="bi bi-person-plus-fill"></i></span>
                        <h4 class="mb-1">Novi nalog</h4>
                        <p class="text-muted small mb-0">Korisničko ime će videti i drugi igrači</p>
                    </div>

                    {#if error}
                        <div class="alert alert-danger d-flex align-items-center py-2 small">
                            <i class="bi bi-exclamation-triangle-fill me-2"></i>{error}
                        </div>
                    {/if}

                    <form onsubmit={handleSubmit}>
                        <!-- maxlength i minlength prate @Size iz RegisterRequest -->
                        <div class="mb-3">
                            <label class="form-label" for="username">Korisničko ime</label>
                            <div class="input-group">
                                <span class="input-group-text"><i class="bi bi-person"></i></span>
                                <input id="username"
                                       class="form-control"
                                       autocomplete="username"
                                       maxlength="100"
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
                                       autocomplete="new-password"
                                       minlength="4"
                                       maxlength="24"
                                       bind:value={password}
                                       required/>
                            </div>
                            <div class="form-text">Između 4 i 24 znaka.</div>
                        </div>

                        <div class="mb-3">
                            <label class="form-label" for="confirmPassword">Ponovite lozinku</label>
                            <div class="input-group">
                                <span class="input-group-text"><i class="bi bi-lock-fill"></i></span>
                                <input id="confirmPassword"
                                       type="password"
                                       class="form-control"
                                       autocomplete="new-password"
                                       bind:value={confirmPassword}
                                       required/>
                            </div>
                        </div>

                        <button type="submit" class="btn btn-primary w-100" disabled={loading}>
                            {#if loading}
                                <span class="spinner-border spinner-border-sm me-2"></span>Registracija...
                            {:else}
                                <i class="bi bi-person-plus me-2"></i>Registruj se
                            {/if}
                        </button>
                    </form>

                    <p class="text-center text-muted small mt-3 mb-0">
                        Već imate nalog? <a href="#/login">Prijavite se</a>
                    </p>
                </div>
            </div>
        </div>
    </div>
</div>
