import {apiFetch, errorMessage} from './client';
import type {GameStateDto} from '../types/game.types';

const BASE = '/api/v1/games';

// Backend vraća 204 bez tela kad igrač nema partiju u toku, pa se tada vraća null
export async function getActiveGame(): Promise<GameStateDto | null> {
    const res = await apiFetch(`${BASE}/active`);
    if (res.status === 204) return null;
    if (!res.ok) throw new Error(await errorMessage(res, 'Učitavanje partije nije uspelo'));
    return res.json();
}
