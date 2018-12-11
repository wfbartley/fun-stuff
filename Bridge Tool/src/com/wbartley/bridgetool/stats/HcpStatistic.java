package com.wbartley.bridgetool.stats;

import org.w3c.dom.Element;

import com.wbartley.bridgetool.HandDirection;
import com.wbartley.bridgetool.RunConfig;

public class HcpStatistic implements Statistic {
	private HandDirection handDirection = null;

	@Override
	public Statistic parse(Element element) throws StatisticParseException {
		HcpStatistic result = new HcpStatistic();
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
		double [] avgHcp = runStats.getAvgHcp();
		if (handDirection == null) {
			return String.format("Avg HCP N: %.2f, S: %.2f, E: %.2f, W: %.2f\n", 
					avgHcp[HandDirection.NORTH.ordinal()],
					avgHcp[HandDirection.SOUTH.ordinal()],
					avgHcp[HandDirection.WEST.ordinal()],
					avgHcp[HandDirection.NORTH.ordinal()]
				);
		}
		else {
			return String.format("Avg HCP " + handDirection.getAbbreviation() + ": %.2f", avgHcp[handDirection.ordinal()]);
		}
	}

	@Override
	public boolean requiresDdAnalysis() {
		return false;
	}

}
