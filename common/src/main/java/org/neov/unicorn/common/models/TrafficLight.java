package org.neov.unicorn.common.models;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Data
@Entity
public class TrafficLight {
	@Id
	@GeneratedValue
	private Long id;

	private String ip;
	private String name;
	private TrafficType type;
	private Direction direction;
	private Color color;
	private String imageName;
	private Status status;
	private boolean registred;
	private LocalDateTime lastAction;
	private LocalDateTime lastSynk;

	public TrafficLight() {
	}

	public TrafficLight(String ip, String name, TrafficType type, Direction direction, Color color, Status status) {
		this.ip = ip;
		this.name = name;
		this.type = type;
		this.direction = direction;
		this.color = color;
		this.status = status;
	}

	public TrafficLight(String ip, String name, TrafficType type, Direction direction, Color color, Status status, String imageName, boolean registred, LocalDateTime lastAction, LocalDateTime lastSynk) {
		this(ip, name, type, direction, color, status);

		this.imageName = imageName;
		this.registred = registred;
		this.lastAction = lastAction;
		this.lastSynk = lastSynk;
	}
}
