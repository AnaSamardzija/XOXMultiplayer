package rs.ac.ni.pmf.ana.XOXMultiplayer.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.dto.ChangePasswordRequestDto;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.dto.PlayedGameDto;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.dto.UserStatsDto;
import rs.ac.ni.pmf.ana.XOXMultiplayer.security.CustomUserDetails;
import rs.ac.ni.pmf.ana.XOXMultiplayer.service.StatsService;
import rs.ac.ni.pmf.ana.XOXMultiplayer.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController
{
	private final StatsService _statsService;
	private final UserService _userService;

	@GetMapping("/me/stats")
	@ResponseStatus(HttpStatus.OK)
	public UserStatsDto getMyStats(@AuthenticationPrincipal final CustomUserDetails principal)
	{
		return _statsService.userStats(principal.getId());
	}

	@GetMapping("/me/games")
	@ResponseStatus(HttpStatus.OK)
	public List<PlayedGameDto> getMyGames(@AuthenticationPrincipal final CustomUserDetails principal)
	{
		return _statsService.userGames(principal.getId());
	}

	@PutMapping("/me/password")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void changePassword(@AuthenticationPrincipal final CustomUserDetails principal,
	                           @RequestBody @Valid final ChangePasswordRequestDto request)
	{
		_userService.changePassword(principal.getId(), request.getCurrentPassword(), request.getNewPassword());
	}
}
