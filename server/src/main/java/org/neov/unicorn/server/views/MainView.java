package org.neov.unicorn.server.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.gridpro.GridPro;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.neov.unicorn.common.models.Color;
import org.neov.unicorn.common.models.Direction;
import org.neov.unicorn.common.models.Status;
import org.neov.unicorn.common.models.TrafficLight;
import org.neov.unicorn.common.models.TrafficType;
import org.neov.unicorn.server.components.ChangeLightTasks;
import org.neov.unicorn.server.repository.TrafficLightRepository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;


@Route
@Push
@Slf4j
//@Theme(value = Lumo.class)
@HtmlImport("frontend://bower_components/vaadin-lumo-styles/badge.html")
@StyleSheet("lumo-badge.css")
public class MainView /*extends UI*/ extends VerticalLayout {
	private final Html css = new Html("<custom-style><style include=\"lumo-badge\">" +
			                       "html {\n" +
			                       "\t--lumo-warning-color: hsl(53, 91%, 52%);\n" +
			                       "\t--lumo-warning-color-50pct: hsla(53, 91%, 52%, 0.5);\n" +
			                       "\t--lumo-warning-color-10pct: hsla(53, 91%, 52%, 0.1);\n" +
			                       "\t--lumo-warning-text-color: hsl(3, 92%, 53%);\n" +
			                       "\t--lumo-warning-contrast-color: #FFF;\n" +
			                       "}\n" +
			                       "\n" +
			                       "[theme~=\"badge\"][theme~=\"warning\"] {\n" +
			                       "  color: var(--lumo-warning-text-color);\n" +
			                       "        background-color: var(--lumo-warning-color-10pct);\n" +
			                       "}\n" +
			                       "\n" +
			                       "[theme~=\"badge\"][theme~=\"warning\"][theme~=\"primary\"] {\n" +
			                       "  color: var(--lumo-warning-contrast-color);\n" +
			                       "        background-color: var(--lumo-warning-color);\n" +
			                       "}\n" +
			                       "\n" +
			                       "[theme~=\"badge\"][theme~=\"warning\"]:not([icon]):empty {\n" +
			                       "  background-color: var(--lumo-warning-color);\n" +
			                       "}\n" +
			                       "</style></custom-style>");

	private final TrafficLightRepository trafficLightRepository;

	private final TrafficLightEditor editor;

	private final Grid<TrafficLight> grid = new Grid<>(TrafficLight.class);
	private final GridPro<TrafficLight> gridPro = new GridPro<>(TrafficLight.class);

	private Dialog dialog = new Dialog();

	private final TextField filter = new TextField();

	private final ChangeLightTasks changeLightTasks;

	private TrafficLightEditor.ChangeHandler changeHandler;
	@Getter
	private final Span timer = new Span(" ");

	private AttachEvent attachEvent;
	protected UI ui;

	@Override
	protected void onAttach(final AttachEvent attachEvent) {
		this.attachEvent = attachEvent;
		this.ui = attachEvent.getUI();
		changeLightTasks.addView(this.ui, this);
	}

	@Override
	protected void onDetach(DetachEvent detachEvent) {
		// Cleanup
		this.ui = null;
		changeLightTasks.addView(null, null);
	}

	public MainView(TrafficLightRepository repo, TrafficLightEditor employeeEditor, ChangeLightTasks changeLightTasks) {
		this.trafficLightRepository = repo;
		this.editor = employeeEditor;
		this.changeLightTasks = changeLightTasks;
		setHeightFull();

		HorizontalLayout layout = new HorizontalLayout(filter, new Span("Next change light: "), timer);
		add(css, layout, grid, editor);

		//grid.setHeight("100%");
		grid.setHeightFull();
		grid.setSelectionMode(Grid.SelectionMode.NONE);
		grid.setColumns("id", "ip");
		grid.getColumnByKey("id").setWidth("50px").setFlexGrow(0);


		//grid.addEditColumn(TrafficLight::getName).text((item, newValue) -> {}).setHeader("Name (editable)");
		grid.addColumn(new ComponentRenderer<>(tl -> {
			Div el = new Div(new Span(tl.getName()));
			TextField textField = new TextField();
			textField.setValue(tl.getName());
			final Component[] ret = {el};
			el.addClickListener(e -> ret[0] = textField);
			textField.addKeyPressListener(Key.ENTER, e -> {
				tl.setName(textField.getValue());
				this.editor.updateTrafficLight(tl);
			});
			return ret[0];
		})).setKey("name").setHeader("Name");

		//type
		createColumnType();

		//direction
		createColumnDirrection();

		//status
		createColumnStatus();

		//traffic lighters
		grid.addColumn(new ComponentRenderer<>(tl -> getLighter().get(tl.getColor().getColorId())))
				.setKey("color").setHeader("Color");

		grid.addColumn("imageName");

		dialog.add(new Label("Close me with the esc-key or an outside click"));
		/*Input input = new Input();

		dialog.add(input);
		dialog.setWidth("400px");
		dialog.setHeight("150px");*/
		grid.addColumn(new ComponentRenderer<>(tl -> {
			Icon ico = new Icon(VaadinIcon.PENCIL);
			ico.addClickListener(e -> dialog.open());
			return ico;
		})).setKey("edit").setHeader("Configure");



		filter.setPlaceholder("Filter by last name");
		filter.setValueChangeMode(ValueChangeMode.EAGER);
		filter.addValueChangeListener(e -> listTrafficLighters(e.getValue()));

		//save on click
		grid.addItemClickListener(e -> editor.save());

		//grid.asSingleSelect().addValueChangeListener(e -> dialog.open());

		//addNewBtn.addClickListener(e -> editor.editEmployee(new TrafficLight()));

		editor.setChangeHandler(() -> {
			editor.setVisible(false);
			listTrafficLighters(filter.getValue());
		});

		listTrafficLighters(null);
	}

	private void createColumnType(){
		grid.addColumn(new ComponentRenderer<>(tl -> {
			Icon ico = new Icon(VaadinIcon.BAN);
			TrafficType trafficType = TrafficType.T3;
			if (tl.getType().equals(TrafficType.T3)){
				trafficType = TrafficType.T2;
				ico = new Icon(VaadinIcon.CAR);
			} else if (tl.getType().equals(TrafficType.T2)){
				trafficType = TrafficType.T3;
				ico = new Icon(VaadinIcon.MALE);
			}
			TrafficType finalTrafficType = trafficType;
			ico.addClickListener(e -> {
				tl.setType(finalTrafficType);
				this.editor.updateTrafficLight(tl);
			});
			return ico;
		})).setKey("type").setHeader("Type");
	}
	private void createColumnDirrection(){
		grid.addColumn(new ComponentRenderer<>(tl -> {
			Icon ico = new Icon(VaadinIcon.ARROWS_CROSS);
			var ref = new Object() {
				Direction direction = Direction.LATITUDE;
			};
			if (tl.getDirection().equals(Direction.LATITUDE)) {
				ico = new Icon(VaadinIcon.ARROWS_LONG_H);
				ref.direction = Direction.LONGITUDE;
			}
			else if (tl.getDirection().equals(Direction.LONGITUDE)){
				ico = new Icon(VaadinIcon.ARROWS_LONG_V);
				ref.direction = Direction.LATITUDE;
			}
			ico.addClickListener(e -> {
				tl.setDirection(ref.direction);
				this.editor.updateTrafficLight(tl);
			});
			return ico;
		})).setKey("direction").setHeader("Direction");
	}

	private void createColumnStatus(){
		grid.addColumn(new ComponentRenderer<>(tl -> {
			Icon ico = new Icon(VaadinIcon.PLAY);
			var ref = new Object() {
				Status status = Status.ON;
			};
			if (tl.getStatus().equals(Status.OFF)) {
				ico = new Icon(VaadinIcon.UNLINK);
				ico.setColor("gray");
				ref.status = Status.ON;
			} else if (tl.getStatus().equals(Status.ON)) {
				ico = new Icon(VaadinIcon.LINK);
				ico.setColor("green");
				ref.status =Status.OFF;
			}
			ico.addClickListener(e -> {
				tl.setStatus(ref.status);
				this.editor.updateTrafficLight(tl);
			});
			return ico;
		})).setKey("status").setHeader("Status");
	}

	private void listTrafficLighters(String filterText) {
		List<TrafficLight> trafficLighters;
		if (StringUtils.isEmpty(filterText)) {
			trafficLighters = trafficLightRepository.findAll();
		} else {
			trafficLighters = trafficLightRepository.findByNameStartsWithIgnoreCase(filterText);
		}
		removeOldTrafficLighters(trafficLighters);

		//grid.getColumnByKey("color").getRenderer()
		/*grid.addColumn(new NativeButtonRenderer<>("Remove item", clickedItem -> {
			// remove the item
		}));*/
		grid.setItems(trafficLighters);
	}

	private void removeOldTrafficLighters(List<TrafficLight> trafficLighters) {
		int i = 0;
		Iterator<TrafficLight> iter = trafficLighters.iterator();
		while (iter.hasNext()) {
			TrafficLight ttl = iter.next();
			LocalDateTime datetime = ttl.getLastSynk();
			log.info("id {} last synk {}", ttl.getId(), datetime);
			if (Objects.isNull(datetime)) {
				iter.remove();
			} else {
				long until = ChronoUnit.SECONDS.between(datetime, LocalDateTime.now());
				if (until > 30) {
					log.info("remove old lighter {}", ttl);
					trafficLightRepository.delete(ttl);
					iter.remove();
				}
			}
			i++;
		}
	}

	private Map<Integer, HorizontalLayout> getLighter() {
		Map<Integer, HorizontalLayout> lights = new HashMap<>();
		List<String> defaultStyle = Arrays.asList("badge", "pill", "contrast");
		for (int i = 0; i < 4; i++) {
			Color style = changeLightTasks.getColorStyle(i);
			HorizontalLayout lighter = new HorizontalLayout(new Span("  "), new Span("  "), new Span("  "), new Span("  "));
			if (i > 0) {
				lighter.getComponentAt(i).getElement().getThemeList().addAll(Arrays.asList("badge", "pill", "primary", style.getStyleName()));
			}
			switch (i) {
				case 1 -> {
					lighter.getComponentAt(2).getElement().getThemeList().addAll(defaultStyle);
					lighter.getComponentAt(3).getElement().getThemeList().addAll(defaultStyle);
				}
				case 2 -> {
					lighter.getComponentAt(1).getElement().getThemeList().addAll(defaultStyle);
					lighter.getComponentAt(3).getElement().getThemeList().addAll(defaultStyle);
				}
				case 3 -> {
					lighter.getComponentAt(1).getElement().getThemeList().addAll(defaultStyle);
					lighter.getComponentAt(2).getElement().getThemeList().addAll(defaultStyle);
				}
				default -> {
					lighter.getComponentAt(2).getElement().getThemeList().addAll(defaultStyle);
					lighter.getComponentAt(1).getElement().getThemeList().addAll(defaultStyle);
					lighter.getComponentAt(3).getElement().getThemeList().addAll(defaultStyle);
				}
			}
			lights.put(i, lighter);
		}
		return lights;
	}
}
