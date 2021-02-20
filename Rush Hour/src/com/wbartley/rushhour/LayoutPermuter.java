package com.wbartley.rushhour;

public class LayoutPermuter {
	public interface PermutationListener {
		public void progressUpdate(int percentComplete);
		public boolean processLayout(ParkingLotLayout layout);
	}
	
	private static long [][] totalPermutationsArray = new long[Vehicle.getMaxCars()+1][Vehicle.getMaxTrucks()+1];
	
	static {
		totalPermutationsArray[0][0]=0;
		totalPermutationsArray[0][1]=30L;
		totalPermutationsArray[0][2]=816L;
		totalPermutationsArray[0][3]=8589L;
		totalPermutationsArray[0][4]=45353L;
		totalPermutationsArray[1][0]=20L;
		totalPermutationsArray[1][1]=1826L;
		totalPermutationsArray[1][2]=34840L;
		totalPermutationsArray[1][3]=280655L;
		totalPermutationsArray[1][4]=1136209L;
		totalPermutationsArray[2][0]=888L;
		totalPermutationsArray[2][1]=44508L;
		totalPermutationsArray[2][2]=628672L;
		totalPermutationsArray[2][3]=3871030L;
		totalPermutationsArray[2][4]=11812915L;
		totalPermutationsArray[3][0]=17498L;
		totalPermutationsArray[3][1]=598513L;
		totalPermutationsArray[3][2]=6370278L;
		totalPermutationsArray[3][3]=29670472L;
		totalPermutationsArray[3][4]=66641829L;
		totalPermutationsArray[4][0]=202164L;
		totalPermutationsArray[4][1]=5038316L;
		totalPermutationsArray[4][2]=40414694L;
		totalPermutationsArray[4][3]=139790082L;
		totalPermutationsArray[4][4]=223812575L;
		totalPermutationsArray[5][0]=1524383L;
		totalPermutationsArray[5][1]=28252509L;
		totalPermutationsArray[5][2]=168901742L;
		totalPermutationsArray[5][3]=422437622L;
		totalPermutationsArray[5][4]=461895347L;
		totalPermutationsArray[6][0]=7902887L;
		totalPermutationsArray[6][1]=108912723L;
		totalPermutationsArray[6][2]=475364506L;
		totalPermutationsArray[6][3]=828790851L;
		totalPermutationsArray[6][4]=583742605L;
		totalPermutationsArray[7][0]=28913554L;
		totalPermutationsArray[7][1]=292449571L;
		totalPermutationsArray[7][2]=903974559L;
		totalPermutationsArray[7][3]=1045096514L;
		totalPermutationsArray[7][4]=436556024L;
		totalPermutationsArray[8][0]=75432687L;
		totalPermutationsArray[8][1]=546753678L;
		totalPermutationsArray[8][2]=1147214649L;
		totalPermutationsArray[8][3]=820356013L;
		totalPermutationsArray[8][4]=179572299L;
		totalPermutationsArray[9][0]=140169603L;
		totalPermutationsArray[9][1]=702805126L;
		totalPermutationsArray[9][2]=943881476L;
		totalPermutationsArray[9][3]=377400709L;
		totalPermutationsArray[9][4]=35354984L;
		totalPermutationsArray[10][0]=183392609L;
		totalPermutationsArray[10][1]=605336522L;
		totalPermutationsArray[10][2]=478751241L;
		totalPermutationsArray[10][3]=91530873L;
		totalPermutationsArray[10][4]=2477879L;
		totalPermutationsArray[11][0]=165176858L;
		totalPermutationsArray[11][1]=334816963L;
		totalPermutationsArray[11][2]=137733400L;
		totalPermutationsArray[11][3]=9570059L;
		totalPermutationsArray[11][4]=23857L;
	};
	
	private PermutationListener listener;
	private ParkingLotLayout layout;
	private boolean keepGoing = true;
	private int firstTruck;
	private int numVehicles, percentComplete;
	private long permutationCount, totalPermutations;
		
	public LayoutPermuter(ParkingLotLayout initialLayout, int numCars, int numTrucks, PermutationListener listener) {
		this.layout = initialLayout;
		this.listener = listener;
		numVehicles = numCars + numTrucks;
		firstTruck = numCars;
		permutationCount = 0L;
		percentComplete = 0;
		totalPermutations = totalPermutationsArray[numCars][numTrucks];
	}
	
	// vehicles array must not contain RED_CAR which is assumed to be present in every valid layout
	public LayoutPermuter(PermutationListener listener, int numCars, int numTrucks) {
		this(new ParkingLotLayout(), numCars, numTrucks, listener);
	}
	
	public void generatePermutations() {
		if (layout.getNumPieces() == 0) {
			for (int col = 3; keepGoing && col >= 0; col--) {
				byte piece = Piece.get(ParkingLotLayout.exitRow, col, false, false);
				layout.addPiece(piece);
				generatePermutations(0, 0);
				layout.removeLastPiece();
			}
		}
		else {
			generatePermutations(0,0);
		}
	}
	
	private void checkForProgressUpdate() {
		permutationCount++;
		int percent = (int)(permutationCount * 100L / totalPermutations);
		if (percent != percentComplete) {
			percentComplete = percent;
			listener.progressUpdate(percentComplete);
		}
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
					if (!layout.isUnusable()) {
						if (nextVehicle == numVehicles) {
							if (!layout.redCarCanExit()) {
								keepGoing = listener.processLayout(layout);
								checkForProgressUpdate();
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
					}
					layout.removeLastPiece();
				}
			}
			if (keepGoing && row < 7 - vehicleLength) {
				byte piece = Piece.get(row, col, isTruck, true);
				if (layout.addPiece(piece)) {
					if (!layout.isUnusable()) {
						if (nextVehicle == numVehicles) {
							if (!layout.redCarCanExit()) {
								keepGoing = listener.processLayout(layout);
								checkForProgressUpdate();
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
					}
					layout.removeLastPiece();
				}
			}
		}
	}
}
