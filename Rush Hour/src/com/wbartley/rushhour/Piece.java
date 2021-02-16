package com.wbartley.rushhour;

public class Piece {
	public static byte INVALID = (byte)0xff;
	private static long [] pieceMasks = new long [128];
	private static short truckMask = 0x40;
	private static byte vertMask = 0x20;
	private static byte rowColMask = 0x1f;
	private static int maxCarRowColValue = 29;
	private static int maxTruckRowColValue = 23;
	
	static {
		long horizCar = 0x3;
		long vertCar = 0x41;	
		long horizTruck = 0x7;
		long vertTruck = 0x1041;
		for (int row = 0; row < ParkingLotLayout.numRowsAndCols; row++) {
			for (int col = 0; col < ParkingLotLayout.numRowsAndCols; col++) {
				if (col < ParkingLotLayout.numRowsAndCols-1) { // Can place horizontal car
					pieceMasks[get(row, col, false, false)] = horizCar;
				}
				if (col < ParkingLotLayout.numRowsAndCols-2) { // Can place horizontal truck
					pieceMasks[get(row, col, true, false)] = horizTruck;
				}
				if (row < ParkingLotLayout.numRowsAndCols-1) { // Can place vertical car
					pieceMasks[get(row, col, false, true)] = vertCar;
				}
				if (row < ParkingLotLayout.numRowsAndCols-2) { // Can place vertical truck
					pieceMasks[get(row, col, true, true)] = vertTruck;
				}
				horizCar <<= 1;
				horizTruck <<= 1;
				vertCar <<= 1;
				vertTruck <<= 1;
			}
		}
	}
	
	public static boolean isTruck(byte piece) {
		return (piece & truckMask) != 0;
	}
	
	public static boolean isCar(byte piece) {
		return (piece & truckMask) == 0;
	}
	
	public static boolean isHoriz(byte piece) {
		return (piece & vertMask) == 0;
	}
	
	public static boolean isVert(byte piece) {
		return (piece & vertMask) != 0;
	}
	
	public static byte get(int row, int col, boolean isTruck, boolean isVert) {
		if (isVert) {
			return (byte)((row * 6 + col) | (isTruck ? truckMask : 0) | vertMask);
		}
		else {
			return (byte)((col * 6 + row) | (isTruck ? truckMask : 0));
		}
	}
	
	public static int getRow(byte piece) {
		if (isVert(piece)) {
			return (piece & rowColMask) / 6;
		}
		else {
			return (piece & rowColMask) % 6;
		}
	}
	
	public static int getCol(byte piece) {
		if (isVert(piece)) {
			return (piece & rowColMask) % 6;
		}
		else {
			return (piece & rowColMask) / 6;
		}
	}
		
	public static long getMask(byte piece) {
		return pieceMasks[piece];
	}
	
	public static byte makeMove(byte piece, byte move) {
		int newLocation = (piece & rowColMask) + 6 * Move.getOffset(move);
		if (newLocation >= 0) {
			if (isCar(piece)) {
				if (newLocation <= maxCarRowColValue) {
					return (byte)(piece & ~rowColMask | newLocation);
				}
			}
			else {
				if (newLocation <= maxTruckRowColValue) {
					return (byte)(piece & ~rowColMask | newLocation);
				}
			}
		}
		return INVALID;
	}
		
}
