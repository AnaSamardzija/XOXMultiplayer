package rs.ac.ni.pmf.ana.XOXMultiplayer.mapper;

import org.springframework.stereotype.Component;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.ChatMessage;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.GameOutcome;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.GameSession;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.Symbol;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.UltimateXoxGame;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.dto.ChatMessageDto;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.dto.GameStateDto;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.dto.MatchFoundDto;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

@Component
public class GameMapper
{
	// Ista partija daje dve različite poruke, pa se prosleđuje i igrač kome je poruka namenjena
	public MatchFoundDto toMatchFoundDto(final GameSession session, final Long userId)
	{
		final boolean isPlayerX = session.getPlayerXId().equals(userId);

		return MatchFoundDto.builder()
		                    .gameId(session.getId())
		                    .opponentUsername(isPlayerX ? session.getPlayerOUsername() : session.getPlayerXUsername())
		                    .symbol(isPlayerX ? Symbol.X : Symbol.O)
		                    .build();
	}

	// Nizovi se kopiraju, jer se DTO pretvara u JSON tek posle izlaska iz zaključavanja partije,
	// a tada bi sledeći potez već mogao da menja originalne.
	public GameStateDto toGameStateDto(final GameSession session)
	{
		final UltimateXoxGame game = session.getGame();
		final Instant turnDeadline = session.getTurnDeadline();

		// Računa se u trenutku slanja, pa je tačno i za stanje koje se traži preko REST-a usred poteza
		final Long turnRemainingMillis = turnDeadline == null
				? null
				: Math.max(0, Duration.between(Instant.now(), turnDeadline).toMillis());

		return GameStateDto.builder()
		                   .gameId(session.getId())
		                   .playerXUsername(session.getPlayerXUsername())
		                   .playerOUsername(session.getPlayerOUsername())
		                   .playerXAway(session.isAway(Symbol.X))
		                   .playerOAway(session.isAway(Symbol.O))
		                   .cells(Arrays.stream(game.getCells()).map(Symbol[]::clone).toArray(Symbol[][]::new))
		                   .boardStates(game.getBoardStates().clone())
		                   .currentPlayer(game.getCurrentPlayer())
		                   .forcedBoard(game.getForcedBoard())
		                   .moveNumber(game.getMoveCount())
		                   .turnRemainingMillis(turnRemainingMillis)
		                   .finished(game.isFinished())
		                   .outcome(game.getOutcome())
		                   .winnerUsername(winnerUsername(session))
		                   .endReason(session.getEndReason())
		                   .build();
	}

	// Stanje za odgovor sa REST-a: isto kao ono za temu partije, uz dosadašnje poruke četa
	public GameStateDto toActiveGameStateDto(final GameSession session)
	{
		return toGameStateDto(session).toBuilder()
		                              .chatMessages(session.getChatMessages().stream().map(this::toChatMessageDto).toList())
		                              .build();
	}

	public ChatMessageDto toChatMessageDto(final ChatMessage chatMessage)
	{
		return ChatMessageDto.builder()
		                     .number(chatMessage.number())
		                     .senderUsername(chatMessage.senderUsername())
		                     .text(chatMessage.text())
		                     .build();
	}

	private String winnerUsername(final GameSession session)
	{
		final GameOutcome outcome = session.getGame().getOutcome();

		if (outcome == GameOutcome.X_WON)
		{
			return session.getPlayerXUsername();
		}

		if (outcome == GameOutcome.O_WON)
		{
			return session.getPlayerOUsername();
		}

		return null;
	}
}
