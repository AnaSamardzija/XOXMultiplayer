package rs.ac.ni.pmf.ana.XOXMultiplayer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.ni.pmf.ana.XOXMultiplayer.mapper.GameMapper;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.GameSession;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.GameStatus;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.dto.GameStateDto;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.entity.GameEntity;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.entity.UserEntity;
import rs.ac.ni.pmf.ana.XOXMultiplayer.repository.GameRepository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class GameService
{
	private final GameRepository _gameRepository;
	private final GameMapper _gameMapper;

	// Partije u toku žive samo u memoriji, po identifikatoru partije. ConcurrentHashMap zato što
	// poruke sa WebSocket-a obrađuje više niti.
	private final Map<Long, GameSession> _games = new ConcurrentHashMap<>();

	@Transactional
	public GameSession createGame(final UserEntity playerX, final UserEntity playerO)
	{
		final GameEntity gameEntity = GameEntity.builder()
		                                        .playerX(playerX)
		                                        .playerO(playerO)
		                                        .status(GameStatus.IN_PROGRESS)
		                                        .build();

		final GameEntity saved = _gameRepository.save(gameEntity);

		final GameSession session = new GameSession(
				saved.getId(),
				playerX.getId(),
				playerX.getUsername(),
				playerO.getId(),
				playerO.getUsername());

		_games.put(session.getId(), session);

		return session;
	}

	public boolean isPlaying(final Long userId)
	{
		return _games.values().stream().anyMatch(session -> session.hasPlayer(userId));
	}

	public Optional<GameStateDto> findActiveGame(final Long userId)
	{
		return _games.values().stream()
		             .filter(session -> session.hasPlayer(userId))
		             .findFirst()
		             .map(session ->
		             {
			             // Zaključavanje po partiji: čitanje stanja prolazi kroz isto zaključavanje kao potez,
			             // da se tabla ne pročita usred poteza.
			             synchronized (session)
			             {
				             return _gameMapper.toGameStateDto(session);
			             }
		             });
	}
}
