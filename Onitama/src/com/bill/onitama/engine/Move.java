package com.bill.onitama.engine;

import com.bill.onitama.engine.Piece.Color;

public class Move {
	private final Piece piece;
	private Card cardPlayed;
	private final int cardIdx;
	private final Manuever manuever;
	private final Piece capturedPiece;
	
	public Move(Piece piece, Card cardPlayed, int cardIdx, Manuever manuever, Piece capturedPiece) {
		this.piece = piece;
		this.cardPlayed = cardPlayed;
		this.cardIdx = cardIdx;
		this.manuever = manuever;
		this.capturedPiece = capturedPiece;
	}

	public Piece getPiece(){
		return piece;
	}
	
	public Card getUseCard() {
		return cardPlayed;
	}
	
	public int getCardIdx(){
		return cardIdx;
	}

	public Manuever getManuever() {
		return manuever;
	}

	public Piece getCapturedPiece() {
		return capturedPiece;
	}
	
	@Override
	public String toString(){
		StringBuilder result = new StringBuilder();
		int row = piece.getRow();
		int col = piece.getCol();
		result.append(piece.getColor() + " plays " + cardPlayed + " ");
		if (piece.getColor() == Color.RED){
			result.append("(" + row + "," + col + ")-(" + (row+manuever.getRowOffset()) + "," + (col+manuever.getColOffset()) + ")");
		}
		else{
			result.append("(" + row + "," + col + ")-(" + (row-manuever.getRowOffset()) + "," + (col-manuever.getColOffset()) + ")");
		}
		return result.toString();
	}
	
}
