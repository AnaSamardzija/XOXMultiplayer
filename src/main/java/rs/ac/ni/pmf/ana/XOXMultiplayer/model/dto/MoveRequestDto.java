package rs.ac.ni.pmf.ana.XOXMultiplayer.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;

// Namera igrača: polje u mini-tabli. Integer, a ne int, da izostavljeno polje ne postane 0,
// što je ispravan indeks. Opseg od 0 do 8 proverava klasa sa pravilima.
@Value
@Builder
@NoArgsConstructor(force = true, access = lombok.AccessLevel.PRIVATE)
@AllArgsConstructor
public class MoveRequestDto
{
	@NotNull(message = "Mini-tabla je obavezna")
	Integer boardIndex;

	@NotNull(message = "Polje je obavezno")
	Integer cellIndex;
}
