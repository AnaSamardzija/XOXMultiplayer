package rs.ac.ni.pmf.ana.XOXMultiplayer.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.dto.UserStatsDto;
import rs.ac.ni.pmf.ana.XOXMultiplayer.security.CustomUserDetails;
import rs.ac.ni.pmf.ana.XOXMultiplayer.service.StatsService;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController
{
	private final StatsService _statsService;

	@GetMapping("/me/stats")
	@ResponseStatus(HttpStatus.OK)
	public UserStatsDto getMyStats(@AuthenticationPrincipal final CustomUserDetails principal)
	{
		return _statsService.userStats(principal.getId());
	}
}
