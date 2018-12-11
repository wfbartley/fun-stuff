package com.wbartley.bridgetool.dds;

import com.sun.jna.Library;
import com.sun.jna.Native;

public interface DdsDll extends Library {
	public static final int SUIT_SPADES = 0;
	public static final int SUIT_HEARTS = 1;
	public static final int SUIT_DIAMONDS = 2;
	public static final int SUIT_CLUBS = 3;
	public static final int SUIT_NT = 4;
	
	public static final int HAND_NORTH = 0;
	public static final int HAND_EAST = 1;
	public static final int HAND_SOUTH = 2;
	public static final int HAND_WEST = 3;
	
	public static final String [] suitSymbols = {"S", "H", "D", "C", "N"};
	public static final String [] directionSymbols = {"N", "E", "S", "W" };
	
    DdsDll INSTANCE = (DdsDll) Native.loadLibrary("dds", DdsDll.class);
    // it's possible to check the platform on which program runs, for example purposes we assume that there's a linux port of the library (it's not attached to the downloadable project)
    void FreeMemory();
    void CalcDDtable(DdTableDeal.ByValue tableDeal, DdTableResults.ByReference response);
}

