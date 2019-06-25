package org.neov.unicorn.server.components;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.Command;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.neov.unicorn.ActiveMQConfig;
import org.neov.unicorn.common.models.Color;
import org.neov.unicorn.common.models.Event;
import org.neov.unicorn.common.models.Health;
import org.neov.unicorn.common.models.SchedulerSettings;
import org.neov.unicorn.server.repository.SettingsRepository;
import org.neov.unicorn.server.views.MainView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;

@Slf4j
@Data
@Component
public class ChangeLightTasks {

	private final JmsTemplate jmsTemplate;
	private final SettingsRepository settingsRepository;

	private AttachEvent attachEvent;
	protected UI ui;
	private MainView view;
	private Integer clock = 10;

	@Autowired
	public ChangeLightTasks(JmsTemplate jmsTemplate, SettingsRepository settingsRepository) {
		this.jmsTemplate = jmsTemplate;
		this.settingsRepository = settingsRepository;
		changeLight();
	}

	public void addView(UI ui, MainView view) {
		this.ui = ui;
		this.view = view;
	}

	private void changeLight(){
		SchedulerSettings settings = settingsRepository.findById(1l).orElse(new SchedulerSettings());
		Integer defaultTime = settings.getTimeout();

		Timer mTimer = new Timer();
		final Color[] color = {Color.RED};

		TimerTask mMyTimerTask = new TimerTask() {
			@Override
			public void run() {
				clock--;
				if(ui != null && view != null){
					ui.access((Command) () -> view.getTimer().setText(clock.toString()));
				}

				if(clock == 3){
					log.info("Blink Light");
					jmsTemplate.convertAndSend(ActiveMQConfig.CLIENT_QUEUE, Color.YELLOW);
				}

				if(clock == 0){
					clock = defaultTime;
					log.info("Change Light to {}", color[0]);
					//change color
					jmsTemplate.convertAndSend(ActiveMQConfig.CLIENT_QUEUE, color[0]);
					if(color[0].equals(Color.GREEN)){
						color[0] = Color.RED;
					}else{
						color[0] = Color.GREEN;
					}

				}
			}
		};
		mTimer.schedule(mMyTimerTask, 1000, 1000);
	}

	public Color getColorStyle(Integer color){
		return switch (color) {
			case 1 -> Color.RED;
			case 2 -> Color.YELLOW;
			case 3 -> Color.GREEN;
			default -> Color.OFF;
		};
	}
}
