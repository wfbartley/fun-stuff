package com.wbartley.bridgetool.stats;

import java.util.List;

import org.w3c.dom.Element;

import com.wbartley.bridgetool.ContractStrain;
import com.wbartley.bridgetool.HandDirection;
import com.wbartley.bridgetool.RunConfig;
import com.wbartley.bridgetool.Vulnerability;
import com.wbartley.bridgetool.dds.DdTableResults;

public class ContractStatistic implements Statistic {
	private static final int ntGameNumTricks = 9;
	private static final int majorSuitGameNumTricks = 10;
	private static final int minorSuitGameNumTricks = 11;
	private static final int slamNumTricks = 12;
	private static final int grandSlamNumTricks = 13;
	private static final int [] vulDownTricks = new int[] { 0, 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000, 1100, 1200, 1300 };
	private static final int [] nonVulDownTricks = new int [] { 0, 50, 100, 150, 200, 250, 300, 350, 400, 450, 500, 550, 600, 650 };
	private static final int [] vulDblDownTricks = new int [] { 0, 200, 500, 800, 1100, 1400, 1700, 2000, 2300, 2600, 2900, 3200, 3500, 3800 };
	private static final int [] nonVulDblDownTricks = new int [] { 0, 100, 300, 500, 800, 1100, 1400, 1700, 2000, 2300, 2600, 2900, 3200, 3500 };
	private static final int ntFirstTrick = 10;
	private static final int majorSuitPerTrick = 30;
	private static final int minorSuitPerTrick = 20;
	private static final int partScoreBonus = 50;
	private static final int vulGameBonus = 500;
	private static final int nonVulGameBonus = 300;
	private static final int vulSlamBonus = 750;
	private static final int nonVulSlamBonus = 500;
	private static final int vulGrandSlamBonus = 1500;
	private static final int nonVulGrandSlamBonus = 1000;
	
	private int [] contractNumTricks;
	private ContractStrain [] contractStrain;
	private HandDirection handDirection;
	private String inputContractString;
	private boolean computeNsScore;
	private int numTricksToDouble = 0;
	private boolean exactly;

	@Override
	public Statistic parse(Element element) throws StatisticParseException {
		ContractStatistic result = new ContractStatistic();
		String dirStr = element.getAttribute("dir");
		if (dirStr.isEmpty()) {
			throw new StatisticParseException("dir attribute is required for ContractStatistic.");
		}
		result.handDirection = HandDirection.fromAbbreviation(dirStr);
		if (result.handDirection == null) {
			throw new StatisticParseException("dir must be one of N, S, E or W");
		}
		result.inputContractString = element.getAttribute("contract");
		if (result.inputContractString.isEmpty()) {
			throw new StatisticParseException("contract attribute is required for ContractStatistic.");
		}
		String [] inputContracts = result.inputContractString.split(",");
		result.contractNumTricks = new int [inputContracts.length];
		result.contractStrain = new ContractStrain[inputContracts.length];
		for (int i = 0; i < inputContracts.length; i++) {
			String inputContract = inputContracts[i];
			if (inputContract.length() != 2) {
				throw new StatisticParseException("contract attribute must be in the form <1-7><S|H|D|C|N>.");
			}
			result.contractNumTricks[i] = inputContract.charAt(0) - '0';
			if (result.contractNumTricks[i] < 1 || result.contractNumTricks[i] > 7) {
				throw new StatisticParseException("contract number of tricks must be in range 1-7.");
			}
			result.contractNumTricks[i] += 6;
			result.contractStrain[i] = ContractStrain.fromAbbreviation(inputContract.substring(1));
			if (result.contractStrain[i] == null) {
				throw new StatisticParseException("contract strain must be one of S, H, D, C, or N.");
			}
		}
		String exactlyStr = element.getAttribute("exactly");
		result.computeNsScore = Boolean.parseBoolean(element.getAttribute("computeNsScore"));
		String tricksToDouble = element.getAttribute("numTricksToDouble");
		if (!tricksToDouble.isEmpty()) {
			result.numTricksToDouble = Integer.parseInt(tricksToDouble);
		}
		if (result.contractStrain.length != 1 && result.computeNsScore) {
			throw new StatisticParseException("NS score can only be computed when exactly one contract is specified.");
		}
		result.exactly = Boolean.parseBoolean(exactlyStr);
		return result;
	}
	
	private int calculateScore(ContractStrain contractStrain, int contractNumTricks, Vulnerability vulnerability, int numTricksTaken) {
		int result = 0;
		boolean declarerIsVulnerable;
		if (handDirection.isNS()) {
			declarerIsVulnerable = vulnerability.isNsVul();
		}
		else {
			declarerIsVulnerable = vulnerability.isEwVul();
		}
		if (numTricksTaken >= contractNumTricks) {
			int made = numTricksTaken - 6;
			boolean isGame = false;
			if (contractStrain == ContractStrain.NOTRUMP) {
				if (contractNumTricks >= ntGameNumTricks) {
					isGame = true;
				}
				result = made * majorSuitPerTrick + ntFirstTrick;
			}
			else if (contractStrain == ContractStrain.HEARTS || contractStrain == ContractStrain.SPADES) {
				if (contractNumTricks >= majorSuitGameNumTricks) {
					isGame = true;
				}
				result = made * majorSuitPerTrick;
			}
			else {
				if (contractNumTricks >= minorSuitGameNumTricks) {
					isGame = true;
				}
				result = made * minorSuitPerTrick;
			}
			if (isGame) {
				if (declarerIsVulnerable) {
					result += vulGameBonus;
				}
				else {
					result += nonVulGameBonus;
				}
				if (contractNumTricks == slamNumTricks) {
					if (declarerIsVulnerable) {
						result += vulSlamBonus;
					}
					else {
						result += nonVulSlamBonus;
					}
				}
				else if (contractNumTricks == grandSlamNumTricks){
					if (declarerIsVulnerable) {
						result += vulGrandSlamBonus;
					}
					else {
						result += nonVulGrandSlamBonus;
					}
				}
			}
			else {
				result += partScoreBonus;
			}
		}
		else {
			// contract went down
			int numTricksDown = contractNumTricks - numTricksTaken;
			boolean doubled = (numTricksToDouble != 0 && numTricksDown >= numTricksToDouble);
			if (declarerIsVulnerable) {
				if (doubled) {
					result = -vulDblDownTricks[numTricksDown];
				}
				else {
					result = -vulDownTricks[numTricksDown];
				}
			}
			else {
				if (doubled) {
					result = -nonVulDblDownTricks[numTricksDown];
				}
				else {
					result = -nonVulDownTricks[numTricksDown];
				}
			}
		}
		if (!handDirection.isNS()) {
			result = -result;
		}
		return result;
	}

	@Override
	public String output(RunStats runStats, RunConfig config) {
		List<DdTableResults> tableResultsList = runStats.getDdTableResults();
		int sumMakes = 0;
		int totalNsScore = 0;
		int sumMadeTricks = 0;
		int sumMakesExactly = 0;
		int sumDownTricks = 0;
		for (DdTableResults tableResults : tableResultsList) {
			boolean made = false;
			int minDownTricks = -14;
			for (int i = 0; i < contractNumTricks.length && !made; i++) {
				int tricksTaken = tableResults.getNumTricks(contractStrain[i], handDirection);
				if (tricksTaken >= contractNumTricks[i]) {
					sumMakes++;
					sumMadeTricks += tricksTaken - 6;
					if (tricksTaken == contractNumTricks[i]) {
						sumMakesExactly++;
					}
					made = true;
				}
				else {
					int downTricks = tricksTaken - contractNumTricks[i];
					if (downTricks > minDownTricks) {
						minDownTricks = downTricks;
					}
				}
				if (computeNsScore) {
					totalNsScore += calculateScore(contractStrain[i], contractNumTricks[i], config.getVulnerability(), tricksTaken);
				}
			}
			if (!made) {
				sumDownTricks += minDownTricks;
			}
		}
		if (exactly) {
			return String.format(inputContractString + " exactly made %.2f%% of the time",
					(double)sumMakesExactly / tableResultsList.size() * 100);
		}
		else {
			String result = String.format(inputContractString + " made %.2f%% of the time. ",
					(double)sumMakes / tableResultsList.size() * 100);
			if (sumMakes != 0) {
				result += String.format("Average make tricks: %.2f. ", (double)sumMadeTricks / sumMakes);
			}
			int numDown = tableResultsList.size() - sumMakes;
			if (numDown != 0) {
				result += String.format("Average down tricks: %.2f.", (double)sumDownTricks / numDown);
			}
			if (computeNsScore) {
				result += String.format("Average NS score: %.2f.", (double)totalNsScore / tableResultsList.size());
			}
			return result;
		}
	}

	@Override
	public boolean requiresDdAnalysis() {
		return true;
	}

}
