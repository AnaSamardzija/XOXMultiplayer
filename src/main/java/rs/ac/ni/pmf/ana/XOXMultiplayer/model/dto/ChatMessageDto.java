package rs.ac.ni.pmf.ana.XOXMultiplayer.model.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ChatMessageDto
{
	// Redni broj poruke u partiji. Ista poruka može da stigne i sa teme četa i u odgovoru sa REST-a,
	// pa front po njemu odbacuje duplikat i slaže poruke po redu.
	int number;
	String senderUsername;
	String text;
}
