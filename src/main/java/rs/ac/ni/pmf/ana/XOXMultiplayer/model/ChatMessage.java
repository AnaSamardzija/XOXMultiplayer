package rs.ac.ni.pmf.ana.XOXMultiplayer.model;

// Poruka četa onako kako stoji u memoriji uz partiju. Redni broj kreće od 1 i raste po partiji.
public record ChatMessage(int number, String senderUsername, String text)
{
}
