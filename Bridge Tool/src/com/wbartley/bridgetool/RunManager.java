package com.wbartley.bridgetool;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Date;

import com.wbartley.bridgetool.stats.RunStats;
import com.wbartley.bridgetool.stats.Statistic;

public class RunManager {
	private static final int MAX_HANDS_PER_LIN_FILE = 32;
    
	public static void main(String [] args) {
		RunConfig runConfig;
		try {
			runConfig = new RunConfig();
		} catch (Exception e1) {
			System.out.println("runConfig.xml is either missing or invalid.\n" + e1.getMessage());
			return;
		}
		Deck deck = new Deck();
		Layout layout = new Layout(deck, runConfig.getHandConstraint(), runConfig.getSpecificCards());
		RunStats stats = new RunStats();
		long start = System.currentTimeMillis();
		String pbnFilename = runConfig.getPbnFilename();
		String linFilename = runConfig.getLinFilename();
		StringBuilder builder = null;
		PrintStream pbnOutputStream = null;
		PrintStream linOutputStream = null;
		if (pbnFilename != null || linFilename != null) {
			builder = new StringBuilder();
		}
		for (int i = 0; i < runConfig.getRunLength(); i++) {
			layout.generate();
			if (!runConfig.getStatsToCollect().isEmpty()) {
				stats.addToSample(layout, runConfig.isRequiresDdAnalysis());
			}
			if (runConfig.isDumpEachSample()) {
				System.out.println(layout);
				System.out.println(layout.getDdResults());
			}
			int boardNumber = i+1;
			if (pbnFilename != null) {
				builder.append(layout.toPbn(boardNumber, runConfig.getDealer(), runConfig.getVulnerability()));
				if (i == 0) {
					try {
						pbnOutputStream = new PrintStream(new FileOutputStream(pbnFilename + ".pbn"));
						pbnOutputStream.println("% PBN 2.1");
						pbnOutputStream.println("% EXPORT");
						pbnOutputStream.println();
					} catch (FileNotFoundException e) { }
				}
				if (pbnOutputStream != null) {
					pbnOutputStream.println(builder);
					builder.setLength(0);
				}
			}
			if (linFilename != null) {
				int handIndex = i % MAX_HANDS_PER_LIN_FILE;
				builder.append(layout.toLin(handIndex + 1, boardNumber, runConfig.getDealer(), runConfig.getVulnerability()));
				if (handIndex == 0) {
					if (linOutputStream != null) {
						linOutputStream.close();
					}
					try {
						linOutputStream = new PrintStream(new FileOutputStream(linFilename + i / MAX_HANDS_PER_LIN_FILE + ".lin"));
						linOutputStream.println("%");
						linOutputStream.println("% LIN file created by BridgeTool on " + new Date());
						linOutputStream.println("%");
					}
					catch (FileNotFoundException e) {}
				}
				if (linOutputStream != null) {
					linOutputStream.println(builder);
					builder.setLength(0);
				}
			}
		}
		if (pbnOutputStream != null) {
			pbnOutputStream.close();
		}
		if (linOutputStream != null) {
			linOutputStream.close();
		}
		System.out.println("Run took " + (System.currentTimeMillis() - start) + " milliseconds.");
		System.out.println(layout.getNumSuccesses() + " out of " + layout.getNumAttempts() + " generated hands matched constraint");
		for (Statistic statistic : runConfig.getStatsToCollect()) {
			System.out.println(statistic.output(stats, runConfig));
		}
	}
}
