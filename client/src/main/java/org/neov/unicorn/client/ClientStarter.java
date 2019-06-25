package org.neov.unicorn.client;

import lombok.extern.slf4j.Slf4j;
import org.neov.unicorn.ActiveMQConfig;
import org.neov.unicorn.client.services.TrafficLightService;
import org.neov.unicorn.common.models.TrafficLight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Arrays;

@Slf4j
//@RestController
@Import(value = {ActiveMQConfig.class})
@EnableScheduling
@SpringBootApplication
public class ClientStarter {

	@Autowired
	private JmsTemplate jmsTemplate;
	@Autowired
	private TrafficLightService trafficLightService;

	public static void main(String[] args) {
		SpringApplication.run(ClientStarter.class, args);
	}

	@EventListener
	public void onApplicationEvent(ContextRefreshedEvent event) {
		log.info("~".repeat(30));
		TrafficLight tl = this.trafficLightService.getTrafficLight();
		tl.setLastAction(LocalDateTime.now());
		tl.setLastSynk(LocalDateTime.now());
		jmsTemplate.convertAndSend(ActiveMQConfig.SERVER_QUEUE, tl);
	}
	/*@PostConstruct
	public void init() {
		log.info("*".repeat(50));
	}*/
/*
	@Autowired
	private JmsTemplate jmsTemplate;
	@Autowired
	private TrafficLightService trafficLightService;

	@RequestMapping(path = "/")
	public String register(){
		TrafficLight trafficLight = new TrafficLight("127.0.0.1", trafficLightService.getUuid().toString(), TrafficType.T3, Direction.LATITUDE, Color.OFF, "none.gif");
		jmsTemplate.convertAndSend(ActiveMQConfig.SERVER_QUEUE, trafficLight);
		return "oK";
	}*/



}
