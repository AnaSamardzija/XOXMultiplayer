package rs.ac.ni.pmf.ana.XOXMultiplayer.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.dto.LeaderboardEntryDto;
import rs.ac.ni.pmf.ana.XOXMultiplayer.service.StatsService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController
{
	private final StatsService _statsService;

	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public List<LeaderboardEntryDto> getLeaderboard()
	{
		return _statsService.leaderboard();
	}
}
