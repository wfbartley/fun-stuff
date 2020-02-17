package com.wbartley.bridgetool;

import java.util.HashMap;
import java.util.Map;

public enum HandDirection {
	NORTH("N", true),
	EAST("E", false),
	SOUTH("S", true),
	WEST("W", false);
	
	private String abbreviation;
	private static Map<String, HandDirection> abbreviationToEnumMap;
	private boolean isNS;
	
	private static void addAbbreviationToMap(String abbreviation, HandDirection suit) {
		if (abbreviationToEnumMap == null) {
			abbreviationToEnumMap = new HashMap<String, HandDirection>();
		}
		abbreviationToEnumMap.put(abbreviation, suit);
	}
	
	private HandDirection(String abbreviation, boolean isNS) {
		this.abbreviation = abbreviation;
		this.isNS = isNS;
		addAbbreviationToMap(abbreviation, this);
	}
	
	public static HandDirection fromBoardNumber(int boardNumber) {
		return values()[(boardNumber - 1) % 4];
	}
	
	public String getAbbreviation() {
		return abbreviation;
	}
	
	public boolean isNS() {
		return isNS;
	}
	
	public static HandDirection fromAbbreviation(String abbreviation) {
		return abbreviationToEnumMap.get(abbreviation);
	}
}
