package rs.ac.ni.pmf.ana.XOXMultiplayer.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.dto.ChatMessageRequestDto;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.dto.MoveRequestDto;
import rs.ac.ni.pmf.ana.XOXMultiplayer.service.GameService;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class GameSocketController
{
	private final GameService _gameService;

	// Klijent šalje na /app/games/{gameId}/move. Odgovora nema: novo stanje stiže oboma na temu partije.
	// ErrorHandler ne važi za WebSocket, pa se odbijen potez samo zapiše u log i klijentu se ne vraća
	// ništa. To je svesno: front ne nudi nedozvoljen potez, a sledeće stanje ionako ispravi prikaz.
	@MessageMapping("/games/{gameId}/move")
	public void move(
			@DestinationVariable final Long gameId,
			@Valid @Payload final MoveRequestDto request,
			final Principal principal)
	{
		_gameService.makeMove(gameId, principal.getName(), request.getBoardIndex(), request.getCellIndex());
	}

	// Klijent šalje na /app/games/{gameId}/surrender, bez tela. Završno stanje stiže oboma na temu partije.
	@MessageMapping("/games/{gameId}/surrender")
	public void surrender(@DestinationVariable final Long gameId, final Principal principal)
	{
		_gameService.surrender(gameId, principal.getName());
	}

	// Klijent šalje na /app/games/{gameId}/chat. Poruka stiže oboma na temu četa te partije.
	// Prazna i predugačka poruka padaju na validaciji i, kao odbijen potez, samo se zapišu u log.
	@MessageMapping("/games/{gameId}/chat")
	public void chat(
			@DestinationVariable final Long gameId,
			@Valid @Payload final ChatMessageRequestDto request,
			final Principal principal)
	{
		_gameService.sendChatMessage(gameId, principal.getName(), request.getText());
	}
}
