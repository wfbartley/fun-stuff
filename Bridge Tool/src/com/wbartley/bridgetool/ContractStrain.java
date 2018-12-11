package com.wbartley.bridgetool;

import java.util.HashMap;
import java.util.Map;

public enum ContractStrain {
	SPADES("S"),
	HEARTS("H"),
	DIAMONDS("D"),
	CLUBS("C"),
	NOTRUMP("N");
	
	private String abbreviation;
	private static Map<String, ContractStrain> abbreviationToEnumMap;
	
	private static void addAbbreviationToMap(String abbreviation, ContractStrain contractStrain) {
		if (abbreviationToEnumMap == null) {
			abbreviationToEnumMap = new HashMap<String, ContractStrain>(); 
		}
		abbreviationToEnumMap.put(abbreviation, contractStrain);
	}
	
	private ContractStrain(String abbreviation) {
		this.abbreviation = abbreviation;
		addAbbreviationToMap(abbreviation, this);
	}
	
	public String getAbbreviation() {
		return abbreviation;
	}
	
	public static ContractStrain fromAbbreviation(String abbreviation) {
		return abbreviationToEnumMap.get(abbreviation);
	}
}
