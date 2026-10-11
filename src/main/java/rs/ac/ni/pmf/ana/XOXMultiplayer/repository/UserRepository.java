package rs.ac.ni.pmf.ana.XOXMultiplayer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.entity.UserEntity;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long>
{
	Optional<UserEntity> findByUsername(String username);

	boolean existsByUsername(String username);
}
