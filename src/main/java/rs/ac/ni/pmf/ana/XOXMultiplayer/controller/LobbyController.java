package rs.ac.ni.pmf.ana.XOXMultiplayer.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.dto.LobbyDto;
import rs.ac.ni.pmf.ana.XOXMultiplayer.service.LobbyService;

@RestController
@RequestMapping("/api/v1/lobby")
@RequiredArgsConstructor
public class LobbyController
{
	private final LobbyService _lobbyService;

	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public LobbyDto getLobby()
	{
		return _lobbyService.lobby();
	}
}
