package com.bill.onitama.engine;

public class Manuever {
	private final int colOffset;
	private final int rowOffset;
	
	public Manuever(int colOffset, int rowOffset) {
		this.colOffset = colOffset;
		this.rowOffset = rowOffset;
	}

	public int getColOffset() {
		return colOffset;
	}
	
	public int getRowOffset() {
		return rowOffset;
	}

}
