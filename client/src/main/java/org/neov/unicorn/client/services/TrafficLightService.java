package org.neov.unicorn.client.services;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.neov.unicorn.ActiveMQConfig;
import org.neov.unicorn.client.ClientStarter;
import org.neov.unicorn.common.models.Color;
import org.neov.unicorn.common.models.Direction;
import org.neov.unicorn.common.models.Event;
import org.neov.unicorn.common.models.SchedulerSettings;
import org.neov.unicorn.common.models.Status;
import org.neov.unicorn.common.models.TrafficLight;
import org.neov.unicorn.common.models.TrafficType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.UUID;


@Slf4j
@Data
@Service
public class TrafficLightService {
	private final UUID uuid = UUID.randomUUID();
	private final JmsTemplate jmsTemplate;
	private final TrafficLight trafficLight;
	private final SchedulerSettings schedulerSettings = new SchedulerSettings();

	final ClientStarter clientStarter;
	Color lightColor = Color.OFF;

	@Autowired
	public TrafficLightService(JmsTemplate jmsTemplate, ClientStarter clientStarter) {
		String address = "127.0.0.1";
		try {
			address = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			//noone
		}
		this.trafficLight = new TrafficLight(address, uuid.toString(), TrafficType.UNKNOWN, Direction.UNSET, Color.OFF, Status.OFF, "none.gif", false, LocalDateTime.now(), null);
		this.jmsTemplate = jmsTemplate;
		this.clientStarter = clientStarter;
	}

	private Color switchColor(Direction direction, Color color ){
			return switch (color){
				case RED -> (direction.equals(Direction.LONGITUDE))?Color.GREEN:color;
				case GREEN -> (direction.equals(Direction.LONGITUDE))?Color.RED:color;
				default -> color;
			};
	}

	@JmsListener(destination = ActiveMQConfig.CLIENT_QUEUE)
	private void tmpChangeLight(Message reload) {
		if (reload.getPayload() instanceof Color) {
			Color color = (Color) reload.getPayload();
			if (!trafficLight.getStatus().equals(Status.OFF) && !trafficLight.getDirection().equals(Direction.UNSET)) {
				Color localLightColor = switchColor(trafficLight.getDirection(), color);

				if (color.equals(Color.YELLOW) && trafficLight.getType().equals(TrafficType.T3)) {
					lightColor = Color.YELLOW;
				}
				//set color
				if (trafficLight.getDirection().equals(Direction.LONGITUDE)) {
					if (color.equals(Color.RED)) {
						lightColor = Color.GREEN;
					} else if (color.equals(Color.GREEN)) {
						lightColor = Color.RED;
					}
				}
				else {
					if (color.equals(Color.RED)) {
						lightColor = Color.RED;
					} else if (color.equals(Color.GREEN)) {
						lightColor = Color.GREEN;
					}
				}


				trafficLight.setColor(lightColor);
			} else {
				if (trafficLight.getStatus().equals(Status.UNKNOWN))
					trafficLight.setColor(Color.UNKNOWN);
				else
					trafficLight.setColor(Color.OFF);
			}
			sendToLightbox();
			jmsTemplate.convertAndSend(ActiveMQConfig.SERVER_QUEUE, trafficLight);
		}
	}

	private void sendToLightbox() {
		log.info("Send <{}> color message to adafruit controller", trafficLight.getColor());
	}

/*@Scheduled(fixedRate = interval)
	private void reportCurrentTime() {
		log.info("The time is now {} ping", trafficLight.getColor().getColorName());
	}

	@JmsListener(destination = ActiveMQConfig.LIGHT_QUEUE)
	private void receiveLightMessage(Color color) {
		log.info(color.getColorName());
		trafficLight.setColor(color);
	}*/
}
