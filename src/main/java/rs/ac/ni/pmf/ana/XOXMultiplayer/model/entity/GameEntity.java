package rs.ac.ni.pmf.ana.XOXMultiplayer.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.GameEndReason;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.GameOutcome;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.GameStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "games")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameEntity
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "player_x_id", nullable = false)
	private UserEntity playerX;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "player_o_id", nullable = false)
	private UserEntity playerO;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private GameStatus status;

	// Ishod, pobednik, razlog završetka i vreme kraja su prazni dok partija traje
	@Enumerated(EnumType.STRING)
	@Column(length = 50)
	private GameOutcome outcome;

	// Prazan i kod nerešenog ishoda
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "winner_id")
	private UserEntity winner;

	@Enumerated(EnumType.STRING)
	@Column(name = "end_reason", length = 50)
	private GameEndReason endReason;

	// Red se upisuje kad se partija napravi, pa je vreme upisa i vreme početka
	@CreationTimestamp
	@Column(name = "started_at", nullable = false, updatable = false)
	private LocalDateTime startedAt;

	@Column(name = "ended_at")
	private LocalDateTime endedAt;
}
