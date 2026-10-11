package rs.ac.ni.pmf.ana.XOXMultiplayer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class SchedulerConfig
{
	// Zakazivač za rokove u partiji. WebSocket broker ima svoj TaskScheduler za interne potrebe,
	// pa bez @Primary Spring ne bi znao koji od dva da ubaci u servis.
	@Bean
	@Primary
	public TaskScheduler taskScheduler()
	{
		final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.setPoolSize(2);
		scheduler.setThreadNamePrefix("game-timer-");

		return scheduler;
	}
}
