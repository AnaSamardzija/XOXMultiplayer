import {get, writable} from 'svelte/store';
import type {GameStateDto} from '../types/game.types';

// Identifikator partije na kojoj je korisnik. Upisuje se iz poruke o uparivanju na početnoj strani,
// a posle osvežavanja strane ili ponovnog povezivanja iz odgovora sa REST-a. Čita ga ekran partije.
export const activeGameIdStore = writable<number | null>(null);

// Stanje partije koje se trenutno prikazuje. Upisuje se samo kroz applyGameState.
export const gameStateStore = writable<GameStateDto | null>(null);

// Kroz ovo prolaze i odgovor sa REST-a i poruke sa teme partije, pa nije bitno šta stigne prvo.
// Poruke mogu da stignu u pogrešnom redosledu ili duplirano: stanje iste partije sa manjim rednim
// brojem poteza od prikazanog se odbacuje, inače bi tabla „trepnula“ unazad.
export function applyGameState(state: GameStateDto) {
    const current = get(gameStateStore);

    if (current && current.gameId === state.gameId) {
        if (state.moveNumber < current.moveNumber) return;

        // Posle završnog stanja ništa više ne važi. Predaja ne menja redni broj poteza, pa bi
        // zakasneli odgovor sa REST-a inače vratio prikaz na partiju u toku.
        if (current.finished) return;
    }

    gameStateStore.set(state);
}
