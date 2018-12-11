package com.wbartley.bridgetool.stats;

import java.util.ArrayList;
import java.util.List;

import com.wbartley.bridgetool.Hand;
import com.wbartley.bridgetool.HandDirection;
import com.wbartley.bridgetool.Layout;
import com.wbartley.bridgetool.Suit;
import com.wbartley.bridgetool.dds.DdTableResults;

public class RunStats {
	private int numSamples;
	private double [] sumHcp;
	private double [] sumPoints;
	private double [] [] sumDist;
	private int [] [] [] suitCount;
	private List<DdTableResults> ddResults;

	public RunStats() {
		reset();
	}
	
	public void reset() {
		numSamples = 0;
		sumHcp = new double [HandDirection.values().length];
		sumPoints = new double [HandDirection.values().length];
		sumDist = new double [HandDirection.values().length][Suit.values().length];
		suitCount = new int [HandDirection.values().length][Suit.values().length][Hand.NUM_CARDS_IN_EACH_SUIT];
		ddResults = new ArrayList<DdTableResults>();
	}
	
	public void addToSample(Layout layout, boolean requiresDdAnalysis) {
		Hand [] hands = layout.getHands();
		for (int i = 0; i < HandDirection.values().length; i++) {
			int hcp = hands[i].getHCP();
			int points = hands[i].getTotalPoints();
			int [] dist = hands[i].getDistribution();
			sumHcp[i] += hcp;
			sumPoints[i] += points;
			for (int j = 0; j < Suit.values().length; j++) {
				sumDist[i][j] += dist[j];
				suitCount[i][j][dist[j]]++;
			}
		}
		if (requiresDdAnalysis) {
			// clone this guy and add to results list
			ddResults.add(layout.getDdResults());
		}
		numSamples++;
	}
	
	public double [] getAvgHcp() {
		double [] result = new double [HandDirection.values().length];
		for (int i = 0; i < HandDirection.values().length; i++) {
			result [i] = sumHcp[i] / numSamples;
		}
		return result;
	}
	
	public double [] getAvgPoints() {
		double [] result = new double [HandDirection.values().length];
		for (int i = 0; i < HandDirection.values().length; i++) {
			result [i] = sumPoints[i] / numSamples;
		}
		return result;
	}
	
	public double [] [] getAvgDist() {
		double [] [] result = new double [HandDirection.values().length][Suit.values().length];
		for (int i = 0; i < HandDirection.values().length; i++) {
			for (int j = 0; j < Suit.values().length; j++) {
				result [i] [j] = sumDist[i][j] / numSamples;
			}
		}
		return result;
	}
	
	public List<DdTableResults> getDdTableResults(){
		return ddResults;
	}
	
}
