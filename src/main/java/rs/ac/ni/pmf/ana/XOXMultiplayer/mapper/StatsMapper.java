package rs.ac.ni.pmf.ana.XOXMultiplayer.mapper;

import org.springframework.stereotype.Component;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.PlayerStats;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.PlayerWins;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.dto.LeaderboardEntryDto;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.dto.UserStatsDto;

@Component
public class StatsMapper
{
	public LeaderboardEntryDto toLeaderboardEntryDto(final int rank, final PlayerWins playerWins)
	{
		return LeaderboardEntryDto.builder()
		                          .rank(rank)
		                          .username(playerWins.username())
		                          .wins(playerWins.wins())
		                          .build();
	}

	public UserStatsDto toUserStatsDto(final PlayerStats playerStats)
	{
		return UserStatsDto.builder()
		                   .played(playerStats.played())
		                   .wins(playerStats.wins())
		                   .losses(playerStats.losses())
		                   .draws(playerStats.draws())
		                   .build();
	}
}
