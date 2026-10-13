export interface LobbyDto {
    // Igrači koji trenutno imaju živu vezu sa serverom, uključujući i prijavljenog
    onlinePlayers: number;
    activeGames: number;
    // null kad niko ne čeka
    waitingUsername: string | null;
}

// Poruka sa teme /topic/lobby
export interface LobbyWaitingDto {
    // null kad niko ne čeka
    waitingUsername: string | null;
}
