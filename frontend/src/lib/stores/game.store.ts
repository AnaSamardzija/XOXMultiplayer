import {get, writable} from 'svelte/store';
import type {ChatMessageDto, GameStateDto} from '../types/game.types';

// Identifikator partije na kojoj je korisnik. Upisuje se iz poruke o uparivanju na početnoj strani,
// a posle osvežavanja strane ili ponovnog povezivanja iz odgovora sa REST-a. Čita ga ekran partije.
export const activeGameIdStore = writable<number | null>(null);

// Stanje partije koje se trenutno prikazuje. Upisuje se samo kroz applyGameState.
export const gameStateStore = writable<GameStateDto | null>(null);

// Poruke četa, uz identifikator partije kojoj pripadaju. Upisuje se samo kroz applyChatMessages
// i applyGameState.
export const chatStore = writable<{gameId: number | null; messages: ChatMessageDto[]}>({gameId: null, messages: []});

// Kroz ovo prolaze i istorija iz odgovora sa REST-a i pojedinačne poruke sa teme četa, pa nije bitno
// šta stigne prvo. Ista poruka može da stigne na oba načina: po rednom broju se izbacuje duplikat,
// a poruke se slažu po redu. Poruke druge partije se ne mešaju sa ovima, nego se kreće od praznog četa.
export function applyChatMessages(gameId: number, messages: ChatMessageDto[]) {
    const current = get(chatStore);
    const byNumber = new Map<number, ChatMessageDto>();

    if (current.gameId === gameId) {
        current.messages.forEach((message) => byNumber.set(message.number, message));
    }

    messages.forEach((message) => byNumber.set(message.number, message));

    chatStore.set({gameId, messages: [...byNumber.values()].sort((a, b) => a.number - b.number)});
}

// Kroz ovo prolaze i odgovor sa REST-a i poruke sa teme partije, pa nije bitno šta stigne prvo.
// Poruke mogu da stignu u pogrešnom redosledu ili duplirano: stanje iste partije sa manjim rednim
// brojem poteza od prikazanog se odbacuje, inače bi tabla „trepnula“ unazad.
export function applyGameState(state: GameStateDto) {
    const current = get(gameStateStore);
    const sameGame = current !== null && current.gameId === state.gameId;

    // Posle završnog stanja ništa više ne važi. Predaja ne menja redni broj poteza, pa bi
    // zakasneli odgovor sa REST-a inače vratio prikaz na partiju u toku.
    if (sameGame && current.finished) return;

    // Pre provere rednog broja poteza: i kad je tabla iz odgovora sa REST-a već zastarela,
    // istorija četa iz njega i dalje važi
    if (state.chatMessages) applyChatMessages(state.gameId, state.chatMessages);

    if (sameGame && state.moveNumber < current.moveNumber) return;

    // Čet traje samo dok traje partija
    if (state.finished) chatStore.set({gameId: state.gameId, messages: []});

    gameStateStore.set(state);
}
