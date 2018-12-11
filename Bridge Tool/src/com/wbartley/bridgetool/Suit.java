package com.wbartley.bridgetool;

import java.util.HashMap;
import java.util.Map;

public enum Suit {
	CLUBS("C"),
	DIAMONDS("D"),
	HEARTS("H"),
	SPADES("S");
	
	private String abbreviation;
	private static Map<String, Suit> abbreviationToEnumMap;
	
	private static void addAbbreviationToMap(String abbreviation, Suit suit) {
		if (abbreviationToEnumMap == null) {
			abbreviationToEnumMap = new HashMap<String, Suit>(); 
		}
		abbreviationToEnumMap.put(abbreviation, suit);
	}
	
	private Suit(String abbreviation) {
		this.abbreviation = abbreviation;
		addAbbreviationToMap(abbreviation, this);
	}
	
	public String getAbbreviation() {
		return abbreviation;
	}
	
	public static Suit fromAbbreviation(String abbreviation) {
		return abbreviationToEnumMap.get(abbreviation);
	}
}
