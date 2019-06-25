package org.neov.unicorn.server;

import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.broker.BrokerService;
import org.neov.unicorn.ActiveMQConfig;
import org.neov.unicorn.common.models.Health;
import org.neov.unicorn.common.models.SchedulerSettings;
import org.neov.unicorn.common.models.TrafficLight;
import org.neov.unicorn.server.repository.SettingsRepository;
import org.neov.unicorn.server.repository.TrafficLightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.LocalDateTime;

@Slf4j
@EntityScan(basePackageClasses = {TrafficLight.class})
@EnableJpaRepositories(basePackageClasses = {TrafficLightRepository.class})
@Import(value = {ActiveMQConfig.class})
@SpringBootApplication
@EnableScheduling
public class ApplicationStarter /*implements ApplicationRunner */{

	@Autowired
	private JmsTemplate jmsTemplate;

	@Bean
	public CommandLineRunner loadData(TrafficLightRepository repository, SettingsRepository settingsRepository) {
		return (args) -> {
			//repository.save(new SchedulerSettings());
			//repository.save(new TrafficLight("127.0.0.1", "ToRemove", TrafficType.T3, Direction.LATITUDE, Color.OFF, "none.gif"));
			settingsRepository.save(new SchedulerSettings());
		};
	}

	/*@EventListener
	public void onApplicationEvent(ContextRefreshedEvent event) {
		log.info("PING on Start Application");
		log.info("~".repeat(30));
		jmsTemplate.convertAndSend(ActiveMQConfig.CLIENT_QUEUE, Health.PING);
	}*/

	public static void main(String[] args) {
		SpringApplication.run(ApplicationStarter.class, args);
		/*ConfigurableApplicationContext context = SpringApplication.run(ApplicationStarter.class, args);
		JmsTemplate jmsTemplate = context.getBean(JmsTemplate.class);

		// Send a message with a POJO - the template reuse the message converter
		System.out.println("Sending an HELLO message.");
		jmsTemplate.convertAndSend(ActiveMQConfig.CLIENT_QUEUE, Health.HELLO);*/
	}


	@ConditionalOnProperty(name = "spring.activemq.in-memory", havingValue = "true")
	@Bean
	public BrokerService broker() throws Exception {
		final BrokerService broker = new BrokerService();
		broker.addConnector("tcp://localhost:61616");
		//broker.addConnector("vm://localhost");
		broker.setPersistent(false);
		return broker;
	}
}
