<script lang="ts">
    import {authStore} from '../lib/auth/auth.store';
    import {changePassword} from '../lib/api/users.api';

    const username = $derived($authStore.user?.username ?? '');

    let currentPassword = $state('');
    let newPassword = $state('');
    let confirmPassword = $state('');
    let changing = $state(false);
    let passwordError = $state<string | null>(null);
    let passwordSuccess = $state<string | null>(null);

    async function handlePasswordSubmit(e: SubmitEvent) {
        e.preventDefault();
        passwordError = null;
        passwordSuccess = null;

        // Ponovljena lozinka se proverava samo ovde, backend dobija trenutnu i novu
        if (newPassword !== confirmPassword) {
            passwordError = 'Lozinke se ne poklapaju';
            return;
        }

        changing = true;
        try {
            await changePassword({currentPassword, newPassword});
            // Token ostaje važeći, pa korisnik ostaje prijavljen; nova lozinka važi od sledeće prijave
            currentPassword = '';
            newPassword = '';
            confirmPassword = '';
            passwordSuccess = 'Lozinka je promenjena';
        } catch (err) {
            passwordError = err instanceof Error ? err.message : 'Promena lozinke nije uspela';
        } finally {
            changing = false;
        }
    }
</script>

<div class="row justify-content-center">
    <div class="col-md-8 col-lg-6">
        <div class="card">
            <div class="card-header">
                <i class="bi bi-gear me-2 text-primary"></i>Podešavanje profila
            </div>
            <div class="card-body">
                <h6 class="mb-3">Podaci o profilu</h6>

                <div class="d-flex align-items-center gap-3">
                    <span class="icon-circle flex-shrink-0 fw-bold">{username.charAt(0).toUpperCase()}</span>
                    <div class="overflow-hidden">
                        <div class="text-muted small">Korisničko ime</div>
                        <div class="fw-semibold text-truncate">{username}</div>
                    </div>
                </div>
                <div class="form-text">Korisničko ime ne može da se menja.</div>

                <hr class="my-4"/>

                <h6 class="mb-3">Promena lozinke</h6>

                {#if passwordError}
                    <div class="alert alert-danger d-flex align-items-center py-2 small">
                        <i class="bi bi-exclamation-triangle-fill me-2"></i>{passwordError}
                    </div>
                {/if}
                {#if passwordSuccess}
                    <div class="alert alert-success d-flex align-items-center py-2 small">
                        <i class="bi bi-check-circle-fill me-2"></i>{passwordSuccess}
                    </div>
                {/if}

                <form onsubmit={handlePasswordSubmit}>
                    <!-- Skriveno korisničko ime pomaže menadžeru lozinki da zna za koji nalog je lozinka -->
                    <input type="text" class="d-none" autocomplete="username" value={username} readonly/>

                    <div class="mb-3">
                        <label class="form-label" for="currentPassword">Trenutna lozinka</label>
                        <input id="currentPassword"
                               type="password"
                               class="form-control"
                               autocomplete="current-password"
                               bind:value={currentPassword}
                               required/>
                    </div>

                    <!-- maxlength i minlength prate @Size iz ChangePasswordRequestDto -->
                    <div class="mb-3">
                        <label class="form-label" for="newPassword">Nova lozinka</label>
                        <input id="newPassword"
                               type="password"
                               class="form-control"
                               autocomplete="new-password"
                               minlength="4"
                               maxlength="24"
                               bind:value={newPassword}
                               required/>
                        <div class="form-text">Između 4 i 24 znaka.</div>
                    </div>

                    <div class="mb-3">
                        <label class="form-label" for="confirmPassword">Ponovite novu lozinku</label>
                        <input id="confirmPassword"
                               type="password"
                               class="form-control"
                               autocomplete="new-password"
                               bind:value={confirmPassword}
                               required/>
                    </div>

                    <button type="submit" class="btn btn-primary" disabled={changing}>
                        {#if changing}
                            <span class="spinner-border spinner-border-sm me-2"></span>Promena...
                        {:else}
                            <i class="bi bi-shield-lock me-2"></i>Promeni lozinku
                        {/if}
                    </button>
                </form>
            </div>
        </div>
    </div>
</div>
