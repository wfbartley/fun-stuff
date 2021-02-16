package com.wbartley.rushhour;

public class Move {
	public static byte create(int pieceIndex, boolean upOrLeft, int distance) {
		if (upOrLeft) { 
			return (byte)(-(pieceIndex | (distance << 4)));
		}
		else {
			return (byte)(pieceIndex | (distance << 4));
		}
	}
	
	public static boolean isUpOrLeft(byte move) {
		return move < 0;
	}
	
	public static int getDistance(byte move) {
		if (move < 0) {
			return (-move >> 4) & 0x7;
		}
		return (move >> 4) & 0x7;
	}
	
	public static int getOffset(byte move) {
		return (move < 0) ? -((-move >> 4) & 0x7) : ((move >> 4) & 0x7);
	}
	
	public static int getPieceIndex(byte move) {
		if (move < 0) return -move & 0xf;
		return move & 0xf;
	}
	
	public static String toString(Vehicle vehicle, boolean isHoriz, byte move) {
		if (isHoriz) {
			if (isUpOrLeft(move)) {
				return vehicle.getNickname() + "L" + getDistance(move);
			}
			else {
				return vehicle.getNickname() + "R" + getDistance(move);
			}
		}
		else {
			if (isUpOrLeft(move)) {
				return vehicle.getNickname() + "U" + getDistance(move);
			}
			else {
				return vehicle.getNickname() + "D" + getDistance(move);
			}
		}
	}
}
