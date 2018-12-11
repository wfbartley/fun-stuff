package com.wbartley.bridgetool.stats;

import org.w3c.dom.Element;

import com.wbartley.bridgetool.HandDirection;
import com.wbartley.bridgetool.RunConfig;
import com.wbartley.bridgetool.Suit;

public class DistStatistic implements Statistic {
	private HandDirection handDirection = null;
	private Suit suit = null;

	@Override
	public Statistic parse(Element element) throws StatisticParseException {
		DistStatistic result = new DistStatistic();
		String dirStr = element.getAttribute("dir");
		if (!dirStr.isEmpty()) {
			result.handDirection = HandDirection.fromAbbreviation(dirStr);
			if (result.handDirection == null) {
				throw new StatisticParseException("dir must be one of N, S, E or W");
			}
		}
		String suitStr = element.getAttribute("suit");
		if (!suitStr.isEmpty()) {
			result.suit = Suit.fromAbbreviation(suitStr);
			if (result.suit == null) {
				throw new StatisticParseException("suit must be one of S, H, D, or C");
			}
		}
		return result;
	}

	@Override
	public String output(RunStats runStats, RunConfig runConfig) {
		StringBuilder builder = new StringBuilder();
		double [] [] avgDist = runStats.getAvgDist();
		if (handDirection == null) {
			for (HandDirection dir : HandDirection.values()) {
				if (suit == null) {
					builder.append(String.format(dir + " Avg " + suit + " Distribution %.2f\n", avgDist[dir.ordinal()][suit.ordinal()]));
				}
				else {
					builder.append(dir + " Avg Distribution ");
					for (Suit suitVal : Suit.values()) {
						builder.append(String.format("%.2f ", avgDist[dir.ordinal()][suitVal.ordinal()]));
					}
					builder.append("\n");
				}
			}
		}
		else {
			if (suit == null) {
				builder.append(String.format(handDirection + " Avg " + suit + " Distribution %.2f\n", avgDist[handDirection.ordinal()][suit.ordinal()]));
			}
			else {
				builder.append(handDirection + " Avg Distribution ");
				for (Suit suitVal : Suit.values()) {
					builder.append(String.format("%.2f ", avgDist[handDirection.ordinal()][suitVal.ordinal()]));
				}
				builder.append("\n");
			}
		}
		return builder.toString();
	}

	@Override
	public boolean requiresDdAnalysis() {
		return false;
	}

}
