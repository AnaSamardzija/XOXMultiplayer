import {get} from 'svelte/store';
import {gameInProgressStore} from './game.store';

// Dok partija traje, svaka druga ruta vraća igrača na ekran partije: dok je on na drugoj strani,
// server bi igrao nasumične poteze umesto njega
export function requireNoGameInProgress(): boolean {
    if (get(gameInProgressStore)) {
        window.location.href = '#/game';
        return false;
    }
    return true;
}
