package org.neov.unicorn.server.views;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyNotifier;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.extern.slf4j.Slf4j;
import org.neov.unicorn.ActiveMQConfig;
import org.neov.unicorn.common.models.Color;
import org.neov.unicorn.common.models.Direction;
import org.neov.unicorn.common.models.TrafficLight;
import org.neov.unicorn.common.models.TrafficType;
import org.neov.unicorn.server.repository.TrafficLightRepository;
import org.neov.unicorn.server.services.TrafficLighterRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;

import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@SpringComponent
@UIScope
public class TrafficLightEditor extends VerticalLayout implements KeyNotifier {

	private final TrafficLightRepository repository;
	private final TrafficLighterRegistrationService trafficLighterRegistrationService;
	private final JmsTemplate jmsTemplate;

	private TrafficLight trafficLight;

	private TextField ip = new TextField("ip address");

	private Select<Color>  color = new Select<> ();
	private Select<TrafficType> type = new Select<>();
	private Select<Direction> direction = new Select<>();


	private TextField name = new TextField("Last name");
	private TextField imageName = new TextField("Last name");

	private Button delete = new Button("Delete", VaadinIcon.TRASH.create());
	private Button cancel = new Button("Cancel");
	private Button save = new Button("Save", VaadinIcon.CHECK.create());
	private HorizontalLayout actions = new HorizontalLayout(save, cancel, delete);

	private Binder<TrafficLight> binder = new Binder<>(TrafficLight.class);
	private ChangeHandler changeHandler;


	@Autowired
	public TrafficLightEditor(TrafficLightRepository repository, TrafficLighterRegistrationService trafficLighterRegistrationService, JmsTemplate jmsTemplate) {

		this.repository = repository;
		this.trafficLighterRegistrationService = trafficLighterRegistrationService;
		this.jmsTemplate = jmsTemplate;

		this.color.setItems(Stream.of(Color.OFF, Color.RED, Color.YELLOW, Color.GREEN));
		this.type.setItems(Stream.of(TrafficType.T2, TrafficType.T3));
		this.direction.setItems(Stream.of(Direction.LATITUDE, Direction.LONGITUDE));
		this.ip.setReadOnly(true);
		this.color.setReadOnly(true);


		add(ip, name, type, direction, color, imageName, actions);

		binder.bindInstanceFields(this);

		setSpacing(true);

		save.getElement().getThemeList().add("primary");
		delete.getElement().getThemeList().add("error");

		addKeyPressListener(Key.ENTER, e -> save());

		save.addClickListener(e -> save());
		delete.addClickListener(e -> delete());
		cancel.addClickListener(e -> changeHandler.onChange());
		setVisible(true);
	}


	@JmsListener(destination = ActiveMQConfig.CLIENT_QUEUE)
	private void registerNewTraficLight(Message reload) {
		if(reload.getPayload() instanceof TrafficLight){
			Optional<UI> ui = getUI();
			ui.ifPresent(value -> {
				if (value.isEnabled())
					value.access((Command) () -> changeHandler.onChange());
			});
		}
	}

	public void delete() {
		repository.delete(trafficLight);
		changeHandler.onChange();
	}

	public void save() {
		repository.save(trafficLight);
		jmsTemplate.convertAndSend(ActiveMQConfig.CLIENT_QUEUE, trafficLight);
		changeHandler.onChange();
	}

	public interface ChangeHandler {
		void onChange();
	}

	final void updateTrafficLight(TrafficLight c){
		trafficLight = repository.findById(c.getId()).get();
		trafficLighterRegistrationService.base2queueMapper(trafficLight, c);
		//save();
	}

	final void editEmployee(TrafficLight c) {
		if (c == null) {
			setVisible(false);
			return;
		}
		final boolean persisted = c.getId() != null;
		if (persisted) {
			trafficLight = repository.findById(c.getId()).get();
		} else {
			trafficLight = new TrafficLight();
		}

		cancel.setVisible(persisted);
		binder.setBean(trafficLight);
		setVisible(true);
		ip.focus();
	}

	void setChangeHandler(ChangeHandler h) {
		changeHandler = h;
	}

}