package rs.ac.ni.pmf.ana.XOXMultiplayer.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.GameStatus;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.PlayerStats;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.PlayerWins;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.entity.GameEntity;

import java.util.List;

public interface GameRepository extends JpaRepository<GameEntity, Long>
{
	// Nerešene partije nemaju pobednika, pa ih JOIN sam izbacuje. Izjednačeni se ređaju po korisničkom
	// imenu, pa kad se uzme prvih deset, jedanaesti ispada po imenu.
	@Query("""
			SELECT new rs.ac.ni.pmf.ana.XOXMultiplayer.model.PlayerWins(w.username, COUNT(g))
			FROM GameEntity g
			JOIN g.winner w
			WHERE g.status = :status
			GROUP BY w.id, w.username
			ORDER BY COUNT(g) DESC, w.username ASC
			""")
	List<PlayerWins> findTopWinners(@Param("status") GameStatus status, Pageable pageable);

	// COALESCE zato što je SUM bez ijedne partije null
	@Query("""
			SELECT new rs.ac.ni.pmf.ana.XOXMultiplayer.model.PlayerStats(
				COUNT(g),
				COALESCE(SUM(CASE WHEN g.winner.id = :userId THEN 1L ELSE 0L END), 0L),
				COALESCE(SUM(CASE WHEN g.winner.id <> :userId THEN 1L ELSE 0L END), 0L),
				COALESCE(SUM(CASE WHEN g.winner IS NULL THEN 1L ELSE 0L END), 0L))
			FROM GameEntity g
			WHERE g.status = :status AND (g.playerX.id = :userId OR g.playerO.id = :userId)
			""")
	PlayerStats findStats(@Param("userId") Long userId, @Param("status") GameStatus status);

	// JOIN FETCH učitava oba igrača istim upitom, inače bi za ime protivnika išao poseban upit po partiji.
	// Najnovije su prve; id razrešava partije završene u istoj sekundi.
	@Query("""
			SELECT g
			FROM GameEntity g
			JOIN FETCH g.playerX
			JOIN FETCH g.playerO
			WHERE g.status = :status AND (g.playerX.id = :userId OR g.playerO.id = :userId)
			ORDER BY g.endedAt DESC, g.id DESC
			""")
	List<GameEntity> findFinishedByPlayer(@Param("userId") Long userId, @Param("status") GameStatus status);
}
