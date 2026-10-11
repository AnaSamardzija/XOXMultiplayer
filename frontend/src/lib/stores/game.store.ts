import {get, writable} from 'svelte/store';
import type {GameStateDto, MatchFoundDto} from '../types/game.types';

// Poruka o uparivanju stiže na početnoj strani, a čita je ekran partije
export const matchStore = writable<MatchFoundDto | null>(null);

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
