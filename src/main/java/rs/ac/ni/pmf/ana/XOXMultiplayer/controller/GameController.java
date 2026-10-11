package rs.ac.ni.pmf.ana.XOXMultiplayer.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.dto.GameStateDto;
import rs.ac.ni.pmf.ana.XOXMultiplayer.security.CustomUserDetails;
import rs.ac.ni.pmf.ana.XOXMultiplayer.service.GameService;

@RestController
@RequestMapping("/api/v1/games")
@RequiredArgsConstructor
public class GameController
{
	private final GameService _gameService;

	// Kad igrač nema partiju u toku, vraća se 204 bez tela, a ne 404, da se „nema partije“
	// ne bi mešalo sa greškom. Zato je ovde ResponseEntity, a ne DTO direktno.
	@GetMapping("/active")
	public ResponseEntity<GameStateDto> getActiveGame(@AuthenticationPrincipal final CustomUserDetails principal)
	{
		return _gameService.findActiveGame(principal.getId())
		                   .map(ResponseEntity::ok)
		                   .orElseGet(() -> ResponseEntity.noContent().build());
	}
}
