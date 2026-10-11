package rs.ac.ni.pmf.ana.XOXMultiplayer.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

/**
 * Partija u toku, onako kako je server drži u memoriji: ko igra koji simbol i stanje table.
 * Nije entitet ni Spring komponenta. Identifikator je isti kao id reda u tabeli games.
 */
@Getter
@RequiredArgsConstructor
public class GameSession
{
	private final Long id;
	private final Long playerXId;
	private final String playerXUsername;
	private final Long playerOId;
	private final String playerOUsername;
	private final UltimateXoxGame game = new UltimateXoxGame();

	// Zakazan istek vremena za igrača na potezu, čuva se da bi mogao da se otkaže kad potez stigne
	@Setter
	private ScheduledFuture<?> turnTimeout;
	// Trenutak u kome ističe vreme za potez, null kad odbrojavanja nema
	@Setter
	private Instant turnDeadline;
	// Zašto je partija završena, null dok traje. Ishod drži sama igra, a razlog ne može:
	// predaja i prekinuta veza nisu pravila igre.
	@Setter
	private GameEndReason endReason;
	// Zakazani rokovi za povratak igrača koji su trenutno bez veze. Igrač je odsutan dok za
	// njega ovde postoji rok.
	private final Map<Symbol, ScheduledFuture<?>> reconnectTimeouts = new EnumMap<>(Symbol.class);

	public boolean hasPlayer(final Long userId)
	{
		return playerXId.equals(userId) || playerOId.equals(userId);
	}

	public boolean isAway(final Symbol symbol)
	{
		return reconnectTimeouts.containsKey(symbol);
	}

	// Po korisničkom imenu, jer je to ono što WebSocket poruka nosi kao Principal.
	// Vraća null kad korisnik nije igrač ove partije.
	public Symbol symbolOf(final String username)
	{
		if (playerXUsername.equals(username))
		{
			return Symbol.X;
		}

		if (playerOUsername.equals(username))
		{
			return Symbol.O;
		}

		return null;
	}
}
