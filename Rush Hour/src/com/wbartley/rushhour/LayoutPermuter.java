package com.wbartley.rushhour;

public class LayoutPermuter {
	public interface PermutationListener {
		public void progressUpdate(int percentComplete);
		public boolean processLayout(ParkingLotLayout layout);
	}
	
	private PermutationListener listener;
	private ParkingLotLayout layout;
	private boolean keepGoing = true;
	private int firstTruck;
	private int numVehicles;
		
	public LayoutPermuter(ParkingLotLayout initialLayout, int numCars, int numTrucks, PermutationListener listener) {
		this.layout = initialLayout;
		this.listener = listener;
		numVehicles = numCars + numTrucks;
		firstTruck = numCars;
	}
	
	// vehicles array must not contain RED_CAR which is assumed to be present in every valid layout
	public LayoutPermuter(PermutationListener listener, int numCars, int numTrucks) {
		this(new ParkingLotLayout(), numCars, numTrucks, listener);
	}
	
	public void generatePermutations() {
		if (layout.getNumPieces() == 0) {
			for (int col = 0; keepGoing && col < 4; col++) {
				byte piece = Piece.get(ParkingLotLayout.exitRow, col, false, false);
				layout.addPiece(piece);
				generatePermutations(0, 0);
				layout.removeLastPiece();
			}
		}
		else {
			generatePermutations(0,0);
		}
		listener.progressUpdate(100);
	}
	
	private void generatePermutations(int curVehicle, int startIdx) {
		boolean isTruck = curVehicle >= firstTruck;
		int vehicleLength = isTruck ? 3 : 2;
		for (int i = startIdx; keepGoing && i < 36; i++) {
			int nextVehicle = curVehicle + 1;
			int row = i / 6;
			int col = i % 6;
			if (row != 2 && col < 7 - vehicleLength) {
				byte piece = Piece.get(row, col, isTruck, false);
				if (layout.addPiece(piece)) {
					if (nextVehicle == numVehicles) {
						if (!layout.redCarCanExit() && !layout.isUnusable()) {
							keepGoing = listener.processLayout(layout);
						}
					}
					else {
						if (nextVehicle == firstTruck) {
							generatePermutations(nextVehicle, 0);
						}
						else {
							generatePermutations(nextVehicle, i+vehicleLength);
						}
					}
					layout.removeLastPiece();
				}
			}
			if (keepGoing && row < 7 - vehicleLength) {
				byte piece = Piece.get(row, col, isTruck, true);
				if (layout.addPiece(piece)) {
					if (nextVehicle == numVehicles) {
						if (!layout.redCarCanExit()) {
							keepGoing = listener.processLayout(layout);
						}
					}
					else {
						if (nextVehicle == firstTruck) {
							generatePermutations(nextVehicle, 0);
						}
						else {
							generatePermutations(nextVehicle, i+1);
						}
					}
					layout.removeLastPiece();
				}
			}
		}
	}
}
