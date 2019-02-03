package com.sh.converter;

public enum PathsEnum {
	
	GENPATH ("generation");
	
	private final String path;
	
	private PathsEnum(String value) {
		this.path = value;
	}
	
	public String getValue() {
		return this.path;
	}
}
