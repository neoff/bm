package org.neov.unicorn.common.models;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Data
@Entity
public class SchedulerSettings {
	@Id
	@GeneratedValue
	private Long id;

	private Integer timeout = 10;
	private boolean blink = false;
	private LocalDateTime lastAction = LocalDateTime.now();
}
