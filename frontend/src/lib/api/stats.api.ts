import {apiFetch, errorMessage} from './client';
import type {LeaderboardEntryDto, PlayedGameDto, UserStatsDto} from '../types/stats.types';

export async function getLeaderboard(): Promise<LeaderboardEntryDto[]> {
    const res = await apiFetch('/api/v1/leaderboard');
    if (!res.ok) throw new Error(await errorMessage(res, 'Učitavanje rang-liste nije uspelo'));
    return res.json();
}

export async function getMyStats(): Promise<UserStatsDto> {
    const res = await apiFetch('/api/v1/users/me/stats');
    if (!res.ok) throw new Error(await errorMessage(res, 'Učitavanje statistike nije uspelo'));
    return res.json();
}

// Samo završene partije, najnovije prve
export async function getMyGames(): Promise<PlayedGameDto[]> {
    const res = await apiFetch('/api/v1/users/me/games');
    if (!res.ok) throw new Error(await errorMessage(res, 'Učitavanje partija nije uspelo'));
    return res.json();
}
