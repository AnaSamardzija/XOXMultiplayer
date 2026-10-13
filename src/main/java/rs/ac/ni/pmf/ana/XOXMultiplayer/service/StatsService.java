package rs.ac.ni.pmf.ana.XOXMultiplayer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.ni.pmf.ana.XOXMultiplayer.mapper.GameMapper;
import rs.ac.ni.pmf.ana.XOXMultiplayer.mapper.StatsMapper;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.GameStatus;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.PlayerWins;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.dto.LeaderboardEntryDto;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.dto.PlayedGameDto;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.dto.UserStatsDto;
import rs.ac.ni.pmf.ana.XOXMultiplayer.repository.GameRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsService
{
	private static final int LEADERBOARD_SIZE = 10;

	private final GameRepository _gameRepository;
	private final StatsMapper _statsMapper;
	private final GameMapper _gameMapper;

	// Računaju se samo završene partije. Izjednačeni igrači dele mesto, a sledeće se preskače: 1, 2, 2, 4.
	@Transactional(readOnly = true)
	public List<LeaderboardEntryDto> leaderboard()
	{
		final List<PlayerWins> topWinners =
				_gameRepository.findTopWinners(GameStatus.FINISHED, PageRequest.of(0, LEADERBOARD_SIZE));

		final List<LeaderboardEntryDto> entries = new ArrayList<>();
		int rank = 0;
		PlayerWins previous = null;

		for (final PlayerWins playerWins : topWinners)
		{
			if (previous == null || !playerWins.wins().equals(previous.wins()))
			{
				rank = entries.size() + 1;
			}

			entries.add(_statsMapper.toLeaderboardEntryDto(rank, playerWins));
			previous = playerWins;
		}

		return entries;
	}

	@Transactional(readOnly = true)
	public UserStatsDto userStats(final Long userId)
	{
		return _statsMapper.toUserStatsDto(_gameRepository.findStats(userId, GameStatus.FINISHED));
	}

	// Partije u toku se ne vraćaju
	@Transactional(readOnly = true)
	public List<PlayedGameDto> userGames(final Long userId)
	{
		return _gameRepository.findFinishedByPlayer(userId, GameStatus.FINISHED).stream()
		                      .map(gameEntity -> _gameMapper.toPlayedGameDto(gameEntity, userId))
		                      .toList();
	}
}
