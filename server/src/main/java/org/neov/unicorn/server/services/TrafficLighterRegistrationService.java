package org.neov.unicorn.server.services;

import lombok.extern.slf4j.Slf4j;
import org.neov.unicorn.ActiveMQConfig;
import org.neov.unicorn.common.models.Health;
import org.neov.unicorn.common.models.TrafficLight;
import org.neov.unicorn.server.repository.TrafficLightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.Optional;


@Slf4j
@Service
public class TrafficLighterRegistrationService {
	@Autowired
	private JmsTemplate jmsTemplate;

	@Autowired
	private TrafficLightRepository repository;


	@Scheduled(fixedRate = 10000)
	public void reportCurrentTime() {
		log.info("PING in {} {}", LocalDateTime.now(), Health.PING);
		jmsTemplate.convertAndSend(ActiveMQConfig.CLIENT_QUEUE, Health.PING);
	}

	@JmsListener(destination = ActiveMQConfig.SERVER_QUEUE)
	public void send(Message trafficLight) {
		if(trafficLight.getPayload() instanceof TrafficLight){
			TrafficLight tl = (TrafficLight) trafficLight.getPayload();
			log.info("get light <" + tl + ">");
			if (!ObjectUtils.isEmpty(tl.getId())){

				Optional<TrafficLight> finalTl = repository.findById(tl.getId());
				if(finalTl.isPresent() && finalTl.get().getName().equals(tl.getName())){
					tl = base2queueMapper(finalTl.get(), tl);
				} else {
					tl.setId(null);
				}

			}
			log.info("SAVE TL <" + tl + ">");
			repository.saveAndFlush(tl);
			jmsTemplate.convertAndSend(ActiveMQConfig.CLIENT_QUEUE, tl);
		}

	}

	public TrafficLight base2queueMapper(TrafficLight tlB, TrafficLight tlQ){
		tlB.setName(tlQ.getName());
		tlB.setLastSynk(LocalDateTime.now());
		tlB.setLastAction(tlQ.getLastAction());
		tlB.setDirection(tlQ.getDirection());
		tlB.setColor(tlQ.getColor());
		tlB.setImageName(tlQ.getImageName());
		tlB.setIp(tlQ.getIp());
		tlB.setStatus(tlQ.getStatus());
		tlB.setType(tlQ.getType());
		tlB.setRegistred(true);
		return tlB;
	}
}
