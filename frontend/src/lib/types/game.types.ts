// Nije Symbol, jer to ime već postoji u JavaScript-u
export type PlayerSymbol = 'X' | 'O';

export interface MatchFoundDto {
    gameId: number;
    opponentUsername: string;
    symbol: PlayerSymbol;
}
