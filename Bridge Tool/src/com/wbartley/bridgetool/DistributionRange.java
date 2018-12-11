package com.wbartley.bridgetool;

public class DistributionRange {
	private int min;
	private int max;
	
	public DistributionRange() {
		min = 0;
		max = Hand.NUM_CARDS_IN_HAND;
	}
	
	public DistributionRange(int min, int max) {
		this.min = min;
		this.max = max;
	}
	
	public DistributionRange(int min) {
		this.min = min;
		this.max = Hand.NUM_CARDS_IN_HAND;
	}

	public int getMin() {
		return min;
	}

	public int getMax() {
		return max;
	}
	
	public boolean inRange(int value) {
		return value >= min && value <= max;
	}

	@Override
	public String toString() {
		return "(" + min + ", " + max + ")";
	}
}
