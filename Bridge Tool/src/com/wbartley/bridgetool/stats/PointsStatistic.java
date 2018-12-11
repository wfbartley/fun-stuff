package com.wbartley.bridgetool.stats;

import org.w3c.dom.Element;

import com.wbartley.bridgetool.HandDirection;
import com.wbartley.bridgetool.RunConfig;

public class PointsStatistic implements Statistic {
	private HandDirection handDirection = null;

	@Override
	public Statistic parse(Element element) throws StatisticParseException {
		PointsStatistic result = new PointsStatistic();
		String dirStr = element.getAttribute("dir");
		if (!dirStr.isEmpty()) {
			result.handDirection = HandDirection.fromAbbreviation(dirStr);
			if (result.handDirection == null) {
				throw new StatisticParseException("dir must be one of N, S, E or W");
			}
		}
		return result;
	}

	@Override
	public String output(RunStats runStats, RunConfig runConfig) {
		double [] avgPoints = runStats.getAvgPoints();
		if (handDirection == null) {
			return String.format("Avg Points N: %.2f, S: %.2f, E: %.2f, W: %.2f\n", 
					avgPoints[HandDirection.NORTH.ordinal()],
					avgPoints[HandDirection.SOUTH.ordinal()],
					avgPoints[HandDirection.WEST.ordinal()],
					avgPoints[HandDirection.NORTH.ordinal()]
				);
		}
		else {
			return String.format("Avg Points " + handDirection.getAbbreviation() + ": %.2f", avgPoints[handDirection.ordinal()]);
		}
	}

	@Override
	public boolean requiresDdAnalysis() {
		return false;
	}

}
