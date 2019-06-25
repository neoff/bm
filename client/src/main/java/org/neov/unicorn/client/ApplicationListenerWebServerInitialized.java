package org.neov.unicorn.client;

import org.neov.unicorn.client.services.TrafficLightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationListenerWebServerInitialized implements ApplicationListener<WebServerInitializedEvent> {
	@Autowired
	private TrafficLightService trafficLightService;

	@Override
	public void onApplicationEvent(WebServerInitializedEvent webServerInitializedEvent) {
		System.out.println("*".repeat(50));
		System.out.println(webServerInitializedEvent.getWebServer().getPort());
		String ip = trafficLightService.getTrafficLight().getIp();
		trafficLightService.getTrafficLight().setIp(ip+":"+webServerInitializedEvent.getWebServer().getPort());
	}
}
