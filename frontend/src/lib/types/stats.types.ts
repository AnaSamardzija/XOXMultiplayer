import type {GameEndReason, PlayerSymbol} from './game.types';

export interface LeaderboardEntryDto {
    // Izjednačeni igrači dele mesto, a sledeće se preskače: 1, 2, 2, 4
    rank: number;
    username: string;
    wins: number;
}

// Računaju se samo završene partije
export interface UserStatsDto {
    played: number;
    wins: number;
    losses: number;
    draws: number;
}

// Ishod partije iz ugla prijavljenog igrača
export type PlayerResult = 'WIN' | 'LOSS' | 'DRAW';

// Završena partija iz ugla prijavljenog igrača
export interface PlayedGameDto {
    id: number;
    opponentUsername: string;
    symbol: PlayerSymbol;
    result: PlayerResult;
    endReason: GameEndReason;
    // LocalDateTime sa backend-a, bez zone, npr. „2026-10-13T19:47:07“
    startedAt: string;
    endedAt: string;
}
