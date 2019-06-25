package org.neov.unicorn.common.models;

import lombok.Getter;

@Getter
public enum Health {
	PING(1,"ping"), PONG(2,"pong"), HELLO(0,"hello");

	private final int code;
	private final String name;

	Health(int code, String name) {
		this.name = name;
		this.code = code;
	}
}
