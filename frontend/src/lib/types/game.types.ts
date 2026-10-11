// Nije Symbol, jer to ime već postoji u JavaScript-u
export type PlayerSymbol = 'X' | 'O';

export interface MatchFoundDto {
    gameId: number;
    opponentUsername: string;
    symbol: PlayerSymbol;
}

export type MiniBoardState = 'OPEN' | 'X_WON' | 'O_WON' | 'DRAW';

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
}
