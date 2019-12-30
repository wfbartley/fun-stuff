package com.bill.onitama.engine;

public class Piece {
	public enum Color {
		RED,
		BLUE
	}
	
	private int row;
	private int col;
	private final Color color;
	private final boolean master;
	
	
	public Piece(int row, int col, Color color, boolean master) {
		this.row = row;
		this.col = col;
		this.color = color;
		this.master = master;
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getCol() {
		return col;
	}

	public void setCol(int col) {
		this.col = col;
	}
	
	public Color getColor(){
		return color;
	}
	
	public boolean isMaster(){
		return master;
	}
	
	public void doManuever(Manuever manuever){
		if (color == Color.RED){
			row += manuever.getRowOffset();
			col += manuever.getColOffset();
		}
		else{
			row -= manuever.getRowOffset();
			col -= manuever.getColOffset();
		}
	}
	
	public void undoManuever(Manuever manuever){
		if (color == Color.RED){
			row -= manuever.getRowOffset();
			col -= manuever.getColOffset();
		}
		else{
			row += manuever.getRowOffset();
			col += manuever.getColOffset();
		}
	}
	
	@Override
	public String toString(){
		if (color == Color.RED){
			if (master){
				return "RM";
			}
			else{
				return "RP";
			}
		}
		else {
			if (master){
				return "BM";
			}
			else{
				return "BP";
			}
		}
	}
			
}
