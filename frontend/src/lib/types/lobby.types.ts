export interface LobbyDto {
    // Igrači koji trenutno imaju živu vezu sa serverom, uključujući i prijavljenog
    onlinePlayers: number;
    activeGames: number;
}
