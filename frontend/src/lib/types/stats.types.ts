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
