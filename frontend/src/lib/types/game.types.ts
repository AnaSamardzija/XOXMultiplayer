// Nije Symbol, jer to ime već postoji u JavaScript-u
export type PlayerSymbol = 'X' | 'O';

export interface MatchFoundDto {
    gameId: number;
    opponentUsername: string;
    symbol: PlayerSymbol;
}

export type MiniBoardState = 'OPEN' | 'X_WON' | 'O_WON' | 'DRAW';

export type GameOutcome = 'X_WON' | 'O_WON' | 'DRAW';

export type GameEndReason = 'THREE_IN_ROW' | 'ALL_BOARDS_RESOLVED' | 'SURRENDER' | 'DISCONNECT';

// Celo stanje partije, isto za oba igrača. Mini-table i polja imaju indekse od 0 do 8, red po red.
export interface GameStateDto {
    gameId: number;
    playerXUsername: string;
    playerOUsername: string;
    // cells[mini-tabla][polje], null je prazno polje
    cells: (PlayerSymbol | null)[][];
    boardStates: MiniBoardState[];
    currentPlayer: PlayerSymbol;
    // null znači slobodan potez
    forcedBoard: number | null;
    moveNumber: number;
    // Koliko je igraču na potezu ostalo do isteka vremena, null kad odbrojavanja nema
    turnRemainingMillis: number | null;
    finished: boolean;
    // Ishod, pobednik i razlog završetka su null dok partija traje
    outcome: GameOutcome | null;
    // null i kod nerešenog ishoda
    winnerUsername: string | null;
    endReason: GameEndReason | null;
}

// Namera igrača: polje u mini-tabli. Da li je potez ispravan odlučuje server.
export interface MoveRequestDto {
    boardIndex: number;
    cellIndex: number;
}
