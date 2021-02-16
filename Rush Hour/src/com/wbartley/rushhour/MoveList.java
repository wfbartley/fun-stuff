package com.wbartley.rushhour;

import java.util.Arrays;

public class MoveList {
	private byte [] moves;
	
	public MoveList(int maxMoves) {
		moves = new byte[maxMoves];
	}
	
	public MoveList(MoveList copyMe) {
		moves = Arrays.copyOf(copyMe.moves, copyMe.moves.length);
	}
	
	public MoveList(byte [] moves) {
		this.moves = moves;
	}
	
	public MoveList add(byte move) {
		byte [] newMoveList = Arrays.copyOf(moves, moves.length+1);
		newMoveList[moves.length] = move;
		return new MoveList(newMoveList);
	}
		
	public int length() {
		return moves.length;
	}
	
	public byte [] getMoves() {
		return moves;
	}
	
	public int getLastMoveIdx() {
		if (moves.length == 0) return -1;
		return Move.getPieceIndex(moves[moves.length-1]);
	}
		
	public int getDistance() {
		int result = 0;
		for (byte move : moves) {
			result += Move.getDistance(move);
		}
		return result;
	}
	
	public boolean isShorterThan(MoveList other) {
		if (other == null) return true;
		if (moves.length < other.moves.length) return true;
		return moves.length == other.moves.length && getDistance() < other.getDistance();
	}
	
	public PuzzleDifficulty getPuzzleDifficulty() {
		if (moves.length== 0) {
			return PuzzleDifficulty.UNSOLVABLE;
		}
		if (moves.length < 8) {
			return PuzzleDifficulty.TRIVIAL;
		}
		else if (moves.length < 16) {
			return PuzzleDifficulty.NOVICE;
		}
		else if (moves.length < 24) {
			return PuzzleDifficulty.INTERMEDIATE;
		}
		else if (moves.length < 32) {
			return PuzzleDifficulty.ADVANCED;
		}
		else if (moves.length < 40){
			return PuzzleDifficulty.EXPERT;
		}
		else {
			return PuzzleDifficulty.PRO;
		}
	}
}
