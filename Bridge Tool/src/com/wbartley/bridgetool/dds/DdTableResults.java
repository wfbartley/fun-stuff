package com.wbartley.bridgetool.dds;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;
import com.wbartley.bridgetool.ContractStrain;
import com.wbartley.bridgetool.HandDirection;

public class DdTableResults extends Structure {
	public int [] resTable = new int[20];
	
	public class ByReference extends DdTableResults implements Structure.ByReference {}
	
	public DdTableResults() {}
	
	public DdTableResults(DdTableResults o) {
		for (int i = 0; i < 20; i++) {
			resTable[i] = o.resTable[i];
		}
	}

	@Override
	protected List<String> getFieldOrder() {
		return Arrays.asList("resTable");
	}
	public int getNumTricks(int strain, int direction) {
		return resTable[strain * 4 + direction];
	}
	
	public int getNumTricks(ContractStrain strain, HandDirection direction) {
		return resTable[strain.ordinal() * 4 + direction.ordinal()];
	}
	
	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append("    N   E   S   W\n");
		for (int strain = 0; strain < 5; strain++) {
			result.append(DdsDll.suitSymbols[strain]);
			for (int direction = 0; direction < 4; direction++) {
				int numTricks = getNumTricks(strain, direction)-6;
				if (numTricks <= 0) {
					numTricks--;
				}
				result.append(String.format("%4d", numTricks));
			}
			result.append("\n");
		}
		return result.toString();
	}
	
}
