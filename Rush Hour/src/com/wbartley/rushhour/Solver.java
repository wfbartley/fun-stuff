package com.wbartley.rushhour;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class Solver {
	private class TerminalEntry {
		public ParkingLotLayout layout;
		public MoveList moveList;
		public TerminalEntry(ParkingLotLayout layout, MoveList moveList) {
			this.layout = layout;
			this.moveList = moveList;
		}
	}
	private ParkingLotLayout originalLayout;
	private Set<ParkingLotLayout> uniquePositions;
	private MoveList bestSolution = null;
	private static int maxPositionsExamined;
	
	public Solver(ParkingLotLayout layout) {
		bestSolution = null;
		this.originalLayout = layout;
		uniquePositions = new LinkedHashSet<ParkingLotLayout>(2048);
	}
	
	public void reset(ParkingLotLayout layout) {
		originalLayout = new ParkingLotLayout(layout);
		bestSolution = null;
		uniquePositions.clear();
	}
	
	private void checkForBestSolution(MoveList moveList) {
		if (moveList.isShorterThan(bestSolution)) {
			bestSolution = moveList;
		}
	}
	
	private ParkingLotLayout tryMove(ParkingLotLayout layout, MoveList moveList, int pieceIdx, boolean upOrLeft, int distance, ArrayList<TerminalEntry> terminalEntries) {
		byte move = Move.create(pieceIdx, upOrLeft, distance);
		ParkingLotLayout result;
		if ((result = layout.tryMove(move)) != null) {
			MoveList newMoveList = moveList.add(move);
			if (result.redCarCanExit()) {
				checkForBestSolution(newMoveList);
			}
			else {
				if (uniquePositions.add(result)) {
					terminalEntries.add(new TerminalEntry(result, newMoveList));
				}
			}
		}
		return result;
	}
		
	public void solve() {
		ArrayList<TerminalEntry> terminalEntries1 = new ArrayList<TerminalEntry>(500);
		ArrayList<TerminalEntry> terminalEntries2 = new ArrayList<TerminalEntry>(500);
		ArrayList<TerminalEntry> terminalEntries = terminalEntries1;
		solve(originalLayout, new MoveList(0), terminalEntries);
		while (bestSolution == null && !terminalEntries.isEmpty()) {
			for (TerminalEntry entry : terminalEntries) {
				solve(entry.layout, entry.moveList, (terminalEntries == terminalEntries1) ? terminalEntries2 : terminalEntries1);
			}
			terminalEntries.clear();
			terminalEntries = (terminalEntries == terminalEntries1) ? terminalEntries2 : terminalEntries1;
		}
		if (uniquePositions.size() > maxPositionsExamined) {
			maxPositionsExamined = uniquePositions.size();
		}
	}
	
	private void solve(ParkingLotLayout layout, MoveList moveList, ArrayList<TerminalEntry> terminalEntries) {
		int lastPieceMoved = moveList.getLastMoveIdx();
		for (int i = 0; i < layout.getNumPieces(); i++) {
			if (i != lastPieceMoved) {
				int distance = 1;
				while (tryMove(layout, moveList, i, true, distance, terminalEntries) != null) {
					distance++;
				}
				distance = 1;
				while (tryMove(layout, moveList, i, false, distance, terminalEntries) != null) {
					distance++;
				}
			}
		}
	}
	
	public int getNumPositionsExamined() {
		return uniquePositions.size();
	}
	
	public MoveList getBestSolution(){
		return bestSolution;
	}
	
	public Set<ParkingLotLayout> getPositionsExamined(){
		return uniquePositions;
	}
	
	public ParkingLotLayout getLayout() {
		return originalLayout;
	}
	
	public static int getMaxPositionsExamined() {
		return maxPositionsExamined;
	}
	
		
}
