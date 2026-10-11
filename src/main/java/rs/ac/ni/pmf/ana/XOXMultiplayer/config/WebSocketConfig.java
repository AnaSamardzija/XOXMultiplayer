package rs.ac.ni.pmf.ana.XOXMultiplayer.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import rs.ac.ni.pmf.ana.XOXMultiplayer.security.StompAuthChannelInterceptor;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer
{
	private final StompAuthChannelInterceptor _stompAuthChannelInterceptor;

	@Override
	public void registerStompEndpoints(final StompEndpointRegistry registry)
	{
		// Bez SockJS-a, obična WebSocket veza. Spring podrazumevano prihvata samo vezu sa iste adrese:
		// localhost je tu zbog Vite-a, koji je u razvoju na drugom portu, a Railway domen zato što
		// Railway skida HTTPS ispred aplikacije, pa Spring vidi http zahtev sa https poreklom.
		registry.addEndpoint("/ws")
				.setAllowedOriginPatterns("http://localhost:*", "https://*.up.railway.app");
	}

	@Override
	public void configureMessageBroker(final MessageBrokerRegistry registry)
	{
		// /topic i /queue deli broker pretplaćenima, a poruke na /app idu u @MessageMapping metode
		registry.enableSimpleBroker("/topic", "/queue");
		registry.setApplicationDestinationPrefixes("/app");
	}

	@Override
	public void configureClientInboundChannel(final ChannelRegistration registration)
	{
		registration.interceptors(_stompAuthChannelInterceptor);
	}
}
