package com.wbartley.bridgetool.stats;

import org.w3c.dom.Element;

import com.wbartley.bridgetool.RunConfig;

public interface Statistic {
	public Statistic parse(Element element) throws StatisticParseException;
	public String output(RunStats runStats, RunConfig config);
	public boolean requiresDdAnalysis();
}
