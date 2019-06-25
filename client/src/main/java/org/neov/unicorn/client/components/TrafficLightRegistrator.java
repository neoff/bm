package org.neov.unicorn.client.components;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.neov.unicorn.ActiveMQConfig;
import org.neov.unicorn.client.services.TrafficLightService;
import org.neov.unicorn.common.models.Health;
import org.neov.unicorn.common.models.TrafficLight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Getter
@Setter
@Component
public class TrafficLightRegistrator {

	private final JmsTemplate jmsTemplate;

	private final TrafficLightService trafficLightService;

	@Autowired
	public TrafficLightRegistrator(JmsTemplate jmsTemplate, TrafficLightService trafficLightService) {
		this.jmsTemplate = jmsTemplate;
		this.trafficLightService = trafficLightService;
	}

	/*@JmsListener(destination = ActiveMQConfig.CLIENT_QUEUE)
	public void receiveSettingsMessage(Message message) {
		if (message.getPayload() instanceof SchedulerSettings) {
			SchedulerSettings schedulerSettings = (SchedulerSettings) message.getPayload();
			this.trafficLightService.getSchedulerSettings().setLastAction(schedulerSettings.getLastAction());
			this.trafficLightService.getSchedulerSettings().setTimeout(schedulerSettings.getTimeout());
		}

	}*/

	@JmsListener(destination = ActiveMQConfig.CLIENT_QUEUE)
	public void receiveTrafficLightMessage(Message message) {
		if (message.getPayload() instanceof TrafficLight) {
			TrafficLight tl = (TrafficLight) message.getPayload();
			if(tl.getName().equals(this.trafficLightService.getTrafficLight().getName()) ||
					tl.getId().equals(this.trafficLightService.getTrafficLight().getId())) {

				this.trafficLightService.getTrafficLight().setId(tl.getId());
				this.trafficLightService.getTrafficLight().setName(tl.getName());
				this.trafficLightService.getTrafficLight().setDirection(tl.getDirection());
				this.trafficLightService.getTrafficLight().setType(tl.getType());
				this.trafficLightService.getTrafficLight().setStatus(tl.getStatus());
				this.trafficLightService.getTrafficLight().setLastAction(LocalDateTime.now());
				this.trafficLightService.getTrafficLight().setLastSynk(LocalDateTime.now());
			}

		}

	}

	@JmsListener(destination = ActiveMQConfig.CLIENT_QUEUE)
	public void receiveHealthMessage(Message message) {
		if (message.getPayload() instanceof Health && message.getPayload() == Health.PING) {
			TrafficLight tl = this.trafficLightService.getTrafficLight();
			tl.setLastAction(LocalDateTime.now());
			tl.setLastSynk(LocalDateTime.now());
			jmsTemplate.convertAndSend(ActiveMQConfig.SERVER_QUEUE, tl);
		}

	}



	/*@JmsListener(destination = ActiveMQConfig.ORDER_QUEUE)
	private void receiveLightMessage(TrafficLight light) {
		log.info("reseive {} {}",  light.getId(), light.getName());
		if(trafficLightService.getUuid().toString().equals(light.getName())){
			log.info("register {} {}",  light.getId(), light.getName());
			trafficLightService.setTrafficLight(light);
		}
	}*/
}
