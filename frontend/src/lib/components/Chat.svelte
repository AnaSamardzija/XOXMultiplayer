<script lang="ts">
    import type {ChatMessageDto} from '../types/game.types';

    // Ograničenje je isto kao na serveru, koji predugačku poruku odbija
    const MAX_LENGTH = 200;

    // disabled je kad nema veze sa serverom, jer tada poruka ne može da se pošalje
    let {messages, myUsername, disabled, onSend}: {
        messages: ChatMessageDto[];
        myUsername: string;
        disabled: boolean;
        onSend: (text: string) => void;
    } = $props();

    let text = $state('');
    let list = $state<HTMLDivElement>();

    // Čet je sakriven dok se ne klikne na kružić u uglu, a onda izlazi sa desne strane
    let open = $state(false);
    // Koliko je poruka bilo kad je čet poslednji put bio otvoren: tuđe poruke posle toga su nepročitane
    let seenCount = $state(0);

    const unreadCount = $derived(
        messages.slice(seenCount).filter((message) => message.senderUsername !== myUsername).length);

    // Kad stigne nova poruka ili se čet otvori, lista se spušta na dno
    $effect(() => {
        if (!open) return;

        seenCount = messages.length;
        if (list) list.scrollTop = list.scrollHeight;
    });

    // Poruka se ne dodaje u listu ovde, nego tek kad stigne sa teme četa, kao i potez na tabli
    function submit(e: SubmitEvent) {
        e.preventDefault();
        if (disabled || text.trim() === '') return;

        onSend(text.trim());
        text = '';
    }
</script>

<button type="button" class="btn btn-primary chat-toggle" aria-label="Otvori čet" onclick={() => open = true}>
    <i class="bi bi-chat-dots"></i>
    {#if unreadCount > 0}
        <span class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger">
            {unreadCount}
        </span>
    {/if}
</button>

<div class="chat-panel" class:chat-panel-open={open}>
    <div class="d-flex justify-content-between align-items-center p-3 border-bottom fw-semibold">
        <span><i class="bi bi-chat-dots me-2"></i>Čet</span>
        <button type="button" class="btn-close" aria-label="Zatvori čet" onclick={() => open = false}></button>
    </div>
    <div class="chat-messages p-3" bind:this={list}>
        {#each messages as message (message.number)}
            <div class="chat-message" class:chat-message-mine={message.senderUsername === myUsername}>
                {#if message.senderUsername !== myUsername}
                    <div class="chat-message-sender">{message.senderUsername}</div>
                {/if}
                {message.text}
            </div>
        {:else}
            <p class="text-muted small text-center m-auto">Još nema poruka.</p>
        {/each}
    </div>
    <div class="p-3 border-top">
        <form class="input-group" onsubmit={submit}>
            <input type="text" class="form-control" placeholder="Napišite poruku" aria-label="Poruka"
                   maxlength={MAX_LENGTH} autocomplete="off" bind:value={text}/>
            <button type="submit" class="btn btn-primary" aria-label="Pošalji"
                    disabled={disabled || text.trim() === ''}>
                <i class="bi bi-send"></i>
            </button>
        </form>
    </div>
</div>
