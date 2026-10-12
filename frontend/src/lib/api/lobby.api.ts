import {apiFetch, errorMessage} from './client';
import type {LobbyDto} from '../types/lobby.types';

export async function getLobby(): Promise<LobbyDto> {
    const res = await apiFetch('/api/v1/lobby');
    if (!res.ok) throw new Error(await errorMessage(res, 'Učitavanje broja igrača nije uspelo'));
    return res.json();
}
