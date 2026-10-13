<script lang="ts">
    import {push, router} from 'svelte-spa-router';
    import {authStore} from '../auth/auth.store';
    import {gameInProgressStore} from '../stores/game.store';
    import {pendingJoinStore, waitingUsernameStore} from '../stores/lobby.store';

    // Ime igrača čije je obaveštenje zatvoreno. Pamti se ime, a ne samo „zatvoreno“, da se obaveštenje
    // o drugom igraču prikaže i kad poruka da niko ne čeka nije stigla između njih.
    let dismissedUsername = $state<string | null>(null);

    // Čim više niko ne čeka, zatvaranje se zaboravlja: sledeće čekanje se ponovo prikazuje
    $effect(() => {
        if ($waitingUsernameStore === null) dismissedUsername = null;
    });

    // Ne vidi ga igrač koji sam čeka ni igrač u partiji. Na početnoj strani isto piše u kartici
    // sa dugmetom „Igraj“, pa se tamo ne ponavlja.
    const visible = $derived(
        $waitingUsernameStore !== null
        && $waitingUsernameStore !== $authStore.user?.username
        && $waitingUsernameStore !== dismissedUsername
        && !$gameInProgressStore
        && router.location !== '/');

    // U red se ne ulazi odavde: pretplata na lični kanal postoji samo na početnoj strani, a poruka
    // o uparivanju stiže jednom. Zato se samo ostavi namera, a početna uvodi igrača u red.
    function play() {
        pendingJoinStore.set(true);
        push('/');
    }
</script>

{#if visible}
    <div class="alert alert-info d-flex align-items-center gap-2" role="status">
        <i class="bi bi-hourglass-split flex-shrink-0"></i>
        <!-- Dugo korisničko ime se prelama u drugi red, a dugmad ostaju desno -->
        <span class="flex-grow-1 text-break">
            <span class="fw-semibold">{$waitingUsernameStore}</span> čeka protivnika
        </span>
        <button type="button" class="btn btn-primary btn-sm flex-shrink-0" onclick={play}>Igraj</button>
        <button type="button" class="btn-close flex-shrink-0" aria-label="Zatvori"
                onclick={() => dismissedUsername = $waitingUsernameStore}></button>
    </div>
{/if}
