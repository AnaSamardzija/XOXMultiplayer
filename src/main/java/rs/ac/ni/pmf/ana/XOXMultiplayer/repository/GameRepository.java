package rs.ac.ni.pmf.ana.XOXMultiplayer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.entity.GameEntity;

public interface GameRepository extends JpaRepository<GameEntity, Long>
{
}
