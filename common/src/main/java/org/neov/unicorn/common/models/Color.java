package org.neov.unicorn.common.models;

import lombok.Getter;

@Getter
public enum Color {
	RED(1, "red", "error"),
	YELLOW(2, "yellow", "warning"),
	GREEN(3, "green", "success"),
	UNKNOWN(4, "blink", "blink"),
	OFF(0, "black", "contrast");
	private int colorId;
	private String colorName;
	private String styleName;

	Color(int colorId, String colorName, String styleName) {
		this.colorId = colorId;
		this.colorName = colorName;
		this.styleName = styleName;
	}
}
