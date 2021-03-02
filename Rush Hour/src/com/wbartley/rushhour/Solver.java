package com.wbartley.rushhour;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Solver {
	private class LeafNode {
		public ParkingLotLayout layout;
		public MoveList moveList;
		public LeafNode(ParkingLotLayout layout, MoveList moveList) {
			this.layout = layout;
			this.moveList = moveList;
		}
	}
	private ParkingLotLayout originalLayout;
	private Set<ParkingLotLayout> uniquePositions;
	private MoveList bestSolution = null;
	
	public Solver() {
	}
	
	public Solver(ParkingLotLayout layout) {
		bestSolution = null;
		originalLayout = new ParkingLotLayout(layout);
		uniquePositions = new HashSet<ParkingLotLayout>();
		uniquePositions.add(originalLayout);
	}
	
	public void reset(ParkingLotLayout layout) {
		originalLayout = new ParkingLotLayout(layout);
		bestSolution = null;
		uniquePositions = new HashSet<ParkingLotLayout>();
		uniquePositions.add(originalLayout);
	}
	
	private void checkForBestSolution(MoveList moveList) {
		if (moveList.isShorterThan(bestSolution)) {
			bestSolution = moveList;
		}
	}
	
	private ParkingLotLayout tryMove(ParkingLotLayout layout, MoveList moveList, int pieceIdx, boolean upOrLeft, int distance, ArrayList<LeafNode> leafNodes) {
		byte move = Move.create(pieceIdx, upOrLeft, distance);
		ParkingLotLayout result;
		if ((result = layout.tryMove(move)) != null) {
			MoveList newMoveList = moveList.add(move);
			if (result.redCarCanExit()) {
				checkForBestSolution(newMoveList);
			}
			else {
				if (uniquePositions.add(result)) {
					leafNodes.add(new LeafNode(result, newMoveList));
				}
			}
		}
		return result;
	}
		
	public void solve() {
		ArrayList<LeafNode> leafNodes1 = new ArrayList<LeafNode>(500);
		ArrayList<LeafNode> leafNodes2 = new ArrayList<LeafNode>(500);
		ArrayList<LeafNode> leafNodes = leafNodes1;
		solve(originalLayout, new MoveList(0), leafNodes);
		while (bestSolution == null && !leafNodes.isEmpty()) {
			for (LeafNode entry : leafNodes) {
				solve(entry.layout, entry.moveList, (leafNodes == leafNodes1) ? leafNodes2 : leafNodes1);
			}
			leafNodes.clear();
			leafNodes = (leafNodes == leafNodes1) ? leafNodes2 : leafNodes1;
		}
	}
	
	private void solve(ParkingLotLayout layout, MoveList moveList, ArrayList<LeafNode> leafNodes) {
		int lastPieceMoved = moveList.getLastMoveIdx();
		for (int i = 0; i < layout.getNumPieces(); i++) {
			if (i != lastPieceMoved) {
				int distance = 1;
				while (tryMove(layout, moveList, i, true, distance, leafNodes) != null) {
					distance++;
				}
				distance = 1;
				while (tryMove(layout, moveList, i, false, distance, leafNodes) != null) {
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
	
	public ParkingLotLayout getLayout() {
		return originalLayout;
	}
				
}
