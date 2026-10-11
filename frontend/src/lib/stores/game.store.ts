import {writable} from 'svelte/store';
import type {MatchFoundDto} from '../types/game.types';

// Poruka o uparivanju stiže na početnoj strani, a čita je ekran partije
export const matchStore = writable<MatchFoundDto | null>(null);
