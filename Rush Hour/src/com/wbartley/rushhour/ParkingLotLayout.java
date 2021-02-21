package com.wbartley.rushhour;

import java.io.Serializable;
import java.util.Random;

public class ParkingLotLayout implements Comparable<ParkingLotLayout>, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int CAR_LENGTH = 2;
	public static final int TRUCK_LENGTH = 3;
	public static final int numRowsAndCols = 6;
	public static final int exitRow = 2;
	private static final int redCarIdx = 0;
	private static long [] exitRowMasks = new long[numRowsAndCols-CAR_LENGTH+1];
	static {
		// build a mask of the entire exit row
		long fullExitRowMask = 0x3f << (numRowsAndCols * 2);
		// erase a little bit of the mask as the red car moves to the right
		for (int i = 0; i < numRowsAndCols-CAR_LENGTH+1; i++) {
			long mask = Piece.getMask(Piece.get(exitRow, i, false, false));
			fullExitRowMask &= ~mask;
			exitRowMasks[i] = fullExitRowMask;
		}
	}
	private long firstEightPieces, secondEightPieces;
	private int numPieces;
	private long grid;
	
	public ParkingLotLayout() {
		reset();
	}
	
	public ParkingLotLayout(long grid, long firstEightPieces, long secondEightPieces, int numPieces) {
		this.grid = grid;
		this.firstEightPieces = firstEightPieces;
		this.secondEightPieces = secondEightPieces;
		this.numPieces = numPieces;
	}
		
	public ParkingLotLayout(ParkingLotLayout copyMe) {
		grid = copyMe.grid;
		firstEightPieces = copyMe.firstEightPieces;
		secondEightPieces = copyMe.secondEightPieces;
		numPieces = copyMe.numPieces;
	}
	
	public void reset() {
		grid = firstEightPieces = secondEightPieces = numPieces = 0;
	}
	
	public byte getPiece(int idx) {
		if (idx < 8) {
			return (byte)((firstEightPieces >> idx*8) & 0xff);
		}
		else {
			return (byte)((secondEightPieces >> (idx-8)*8) & 0xff);
		}
	}
	
	public boolean redCarCanExit() {
		return (grid & exitRowMasks[Piece.getCol(getPiece(redCarIdx))]) == 0;
	}
	
	public boolean isUnusable() {
		int [] colCount = { 0, 0, 0, 0, 0, 0 };
		int [] rowCount = { 0, 0, 0, 0, 0, 0 };
		for (int i = 1; i < numPieces; i++) {
			byte piece = getPiece(i);
			if (Piece.isVert(piece)) {
				int col = Piece.getCol(piece);
				if (Piece.isTruck(piece)) {
					if (Piece.getRow(piece) < 2) {
						colCount[col] += 4;
					}
					else {
						colCount[col] += 3;
					}
				}
				else {
					colCount[col] += 2;
				}
			}
			else {
				int row = Piece.getRow(piece);
				if (Piece.isCar(piece)) {
					rowCount[row] += 2;
				}
				else {
					rowCount[row] += 3;
				}
			}
		}
		for (int i = 0; i < 6; i++) {
			if (rowCount[i] > 5 || colCount[i] > 5) return true;
		}
		return false;
	}
	
	public int getPieceOnSquare(int row, int col) {
		for (int i = 0; i < numPieces; i++) {
			byte piece = getPiece(i);
			long mask = Piece.getMask(piece);
			if ((mask & (1L << (row * numRowsAndCols + col))) != 0) {
				return i;
			}
		}
		return -1;
	}
			
	private boolean placePiece(byte piece) {
		long pieceMask = Piece.getMask(piece);
		if ((grid & pieceMask) == 0) {
			grid |= pieceMask;
			return true;
		}
		else {
			return false;
		}
	}
	
	private void pickUpPiece(byte piece) {
		long pieceMask = Piece.getMask(piece);
		grid &= ~pieceMask;
	}
	
	public boolean addPiece(byte piece) {
		if (placePiece(piece)) {
			if (numPieces < 8) {
				firstEightPieces |= (long)piece << (numPieces * 8);
			}
			else {
				secondEightPieces |= (long)piece << ((numPieces-8)*8);
			}
			numPieces++;
			return true;
		}
		else {
			return false;
		}
	}
	
	public void removePiece(int index) {
		byte piece = getPiece(index);
		pickUpPiece(piece);
		numPieces--;
		if (index < 8) {
			int offset = index * 8;
			long firstItemInSecondEight = ((secondEightPieces & 0xff) << 56);
			secondEightPieces >>= 8;
			long divisor = 1L << offset;
			long keepBottom = firstEightPieces % divisor;
			long clearMask = ~(0xffL << offset);
			long keepTop = (firstEightPieces & clearMask ^ keepBottom) >> 8;
			firstEightPieces = firstItemInSecondEight | keepTop | keepBottom;
		}
		else {
			int offset = (index-8)*8;
			long divisor = 1L << offset;
			long keepBottom = secondEightPieces % divisor;
			long clearMask = ~(0xffL << offset);
			long keepTop = (secondEightPieces & clearMask ^ keepBottom) >> 8;
			secondEightPieces = keepTop | keepBottom;
		}
	}
	
	public void removeLastPiece() {
		numPieces--;
		byte piece = getPiece(numPieces);
		pickUpPiece(piece);
		if (numPieces < 8) {
			firstEightPieces &= ~(0xffL << (numPieces * 8));
		}
		else {
			secondEightPieces &= ~(0xffL << ((numPieces-8) * 8));
		}
	}
	
	public ParkingLotLayout tryMove(byte move) {
		int pieceIdx = Move.getPieceIndex(move);
		byte curPiece = getPiece(pieceIdx);
		byte afterMove = Piece.makeMove(curPiece, move);
		if (afterMove != Piece.INVALID) {
			long curPieceMask = Piece.getMask(curPiece);
			long afterMask = Piece.getMask(afterMove);
			long gridWithCurPieceRemoved = grid ^ curPieceMask;
			if ((gridWithCurPieceRemoved & afterMask) == 0) {
				long newGrid = gridWithCurPieceRemoved | afterMask;
				if (pieceIdx < 8) {
					int offset = pieceIdx * 8;
					long newFirstEightPieces = firstEightPieces & ~(0xffL << offset) | ((long)afterMove << offset);
					return new ParkingLotLayout(newGrid, newFirstEightPieces, secondEightPieces, numPieces);
				}
				else {
					int offset = (pieceIdx - 8) * 8;
					long newSecondEightPieces = secondEightPieces & ~(0xffL << offset) | ((long)afterMove << offset);
					return new ParkingLotLayout(newGrid, firstEightPieces, newSecondEightPieces, numPieces);
				}
			}
		}
 		return null;
	}
	
	public Vehicle getVehicleFromIndex(int index) {
		int carIdx = 0;
		int truckIdx = Vehicle.getMaxCars()+1;
		for (int i = 0; i < index; i++) {
			byte piece = getPiece(i);
			if (Piece.isCar(piece)) {
				carIdx++;
			}
			else {
				truckIdx++;
			}
		}
		byte piece = getPiece(index);
		if (Piece.isCar(piece)) {
			return Vehicle.values()[carIdx];
		}
		else {
			return Vehicle.values()[truckIdx];
		}
	}
		
	public static ParkingLotLayout generateRandom(int numCars, int numTrucks, int totalNumVehicles) {
		Random random = new Random(System.currentTimeMillis());
		ParkingLotLayout result = new ParkingLotLayout();
		boolean done = false;
		do {
			byte redCar = Piece.get(exitRow, (int)(random.nextDouble() * (numRowsAndCols - 2)), false, false);
			result.addPiece(redCar);
			int numTries = 0;
			for (int i = 0; i < numCars; i++) {
				boolean added;
				numTries = 0;
				do {
					boolean isVert = random.nextDouble() < 0.5;
					int row, col;
					if (isVert) {
						row = (int)(random.nextDouble() * 5);
						col = (int)(random.nextDouble() * 6);
					}
					else {
						row = (int)(random.nextDouble() * 5);
						if (row >= 2) row++;
						col = (int)(random.nextDouble() * 5);
					}
					byte nextPiece = Piece.get(row, col, false, isVert);
					added = result.addPiece(nextPiece);
					numTries++;
				} while (!added && numTries < 100);
				if (numTries == 100) break;
			}
			for (int i = 0; i < numTrucks; i++) {
				boolean added;
				numTries = 0;
				do {
					boolean isVert = random.nextDouble() < 0.5;
					int row, col;
					if (isVert) {
						row = (int)(random.nextDouble() * 4);
						col = (int)(random.nextDouble() * 6);
					}
					else {
						row = (int)(random.nextDouble() * 5);
						if (row >= 2) row++;
						col = (int)(random.nextDouble() * 4);
					}
					byte nextPiece = Piece.get(row, col, true, isVert);
					added = result.addPiece(nextPiece);
					numTries++;
				} while (!added && numTries < 100);
				if (numTries == 100) break;
			}
			if (numTries < 100 && !result.isUnusable() && !result.redCarCanExit()) {
				done = true;
			}
			else {
				result.reset();
			}
		} while (!done);
		return result;
	}
			
	public long getGrid() {
		return grid;
	}
	
	public int getNumPieces() {
		return numPieces;
	}
	
	public int getNumCars() {
		int result = 0;
		for (int i = 1; i < numPieces; i++) {
			if (Piece.isCar(getPiece(i))){
				result++;
			}
		}
		return result;
	}
	
	public int getNumTrucks() {
		int result = 0;
		for (int i = 1; i < numPieces; i++) {
			if (!Piece.isCar(getPiece(i))){
				result++;
			}
		}
		return result;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		ParkingLotLayout other = (ParkingLotLayout)o;
		return firstEightPieces == other.firstEightPieces && secondEightPieces == other.secondEightPieces;
	}
	
	@Override
	public int hashCode() {
		return (int)grid;
	}
			
	@Override
	public int compareTo(ParkingLotLayout o) {
		if (firstEightPieces != o.firstEightPieces) {
			if (firstEightPieces < o.firstEightPieces) {
				return -1;
			}
			else {
				return 1;
			}
		};
		if (secondEightPieces != o.secondEightPieces) {
			if (secondEightPieces < o.secondEightPieces) {
				return -1;
			}
			else {
				return 1;
			}
		}
		return 0;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (int row = 0; row < 6; row++) {
			for (int col = 0; col < 6; col++) {
				long rowColMask = 1L << row * 6 + col;
				if ((grid & rowColMask) == 0) {
					builder.append('_');
				}
				else {
					for (int i = 0; i < numPieces; i++) {
						byte piece = getPiece(i);
						if ((Piece.getMask(piece) & rowColMask & grid) != 0) {
							Vehicle vehicle = getVehicleFromIndex(i);
							builder.append(vehicle.getNickname());
							break;
						}
					}
				}
			}
			builder.append('\n');
		}
		return builder.toString();
	}


}
