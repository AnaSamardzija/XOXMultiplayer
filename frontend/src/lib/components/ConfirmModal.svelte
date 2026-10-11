<script lang="ts">
    // Potvrda pre radnje koja ne može da se poništi, kao što je predaja partije
    let {
        show = $bindable(false),
        title = 'Potvrda',
        message = 'Da li ste sigurni?',
        confirmLabel = 'Potvrdi',
        onConfirm
    }: {
        show: boolean;
        title?: string;
        message?: string;
        confirmLabel?: string;
        onConfirm: () => void;
    } = $props();

    // Kao Bootstrap modal: pri otvaranju dobija fokus, pa se zatvara tasterom Escape, a i klikom na tamnu pozadinu oko prozora
    function handleKeydown(e: KeyboardEvent) {
        if (e.key === 'Escape') show = false;
    }

    function handleBackdropClick(e: MouseEvent) {
        if (e.target === e.currentTarget) show = false;
    }
</script>

{#if show}
    <div class="modal d-block modal-app" tabindex="-1" role="dialog" aria-modal="true" aria-labelledby="confirm-title"
         onclick={handleBackdropClick} onkeydown={handleKeydown} {@attach node => node.focus()}>
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="confirm-title">
                        <i class="bi bi-exclamation-triangle-fill text-danger me-2"></i>{title}
                    </h5>
                    <button type="button" class="btn-close" aria-label="Zatvori" onclick={() => show = false}></button>
                </div>
                <div class="modal-body">
                    <p class="mb-0">{message}</p>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-outline-secondary" onclick={() => show = false}>Otkaži</button>
                    <button type="button" class="btn btn-danger" onclick={() => { onConfirm(); show = false; }}>{confirmLabel}</button>
                </div>
            </div>
        </div>
    </div>
{/if}
