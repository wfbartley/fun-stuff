package com.wbartley.bridgetool.stats;

public enum StatsEnum {
	HCP(new HcpStatistic()),
	POINTS(new PointsStatistic()),
	DIST(new DistStatistic()),
	CONTRACT(new ContractStatistic());
	
	private Statistic statistic;
	
	private StatsEnum(Statistic statistic) {
		this.statistic = statistic;
	}
	
	public Statistic getStatistic() {
		return statistic;
	}
}
