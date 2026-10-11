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

    // Kad stigne nova poruka, lista se spušta na dno
    $effect(() => {
        if (list && messages.length > 0) list.scrollTop = list.scrollHeight;
    });

    // Poruka se ne dodaje u listu ovde, nego tek kad stigne sa teme četa, kao i potez na tabli
    function submit(e: SubmitEvent) {
        e.preventDefault();
        if (disabled || text.trim() === '') return;

        onSend(text.trim());
        text = '';
    }
</script>

<div class="card">
    <div class="card-header"><i class="bi bi-chat-dots me-2"></i>Čet</div>
    <div class="card-body chat-messages" bind:this={list}>
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
    <div class="card-footer bg-white">
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
