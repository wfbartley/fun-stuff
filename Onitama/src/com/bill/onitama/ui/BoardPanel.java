package com.bill.onitama.ui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import com.bill.onitama.engine.Piece;

public class BoardPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private Image backgroundImage;
	private Image blueMaster, blueNovice, redMaster, redNovice;
	private List<Piece> redPieces;
	private List<Piece> bluePieces;
	private boolean boardFlipped = false;

	public BoardPanel() {
		backgroundImage = new ImageIcon("images/Board.jpg").getImage();
		Dimension size = new Dimension(backgroundImage.getWidth(null), backgroundImage.getHeight(null));
		setPreferredSize(size);
		setMinimumSize(size);
		setMaximumSize(size);
		setSize(size);
		blueMaster = new ImageIcon("images/BlueMaster.png").getImage();
		blueNovice = new ImageIcon("images/BlueNovice.png").getImage();
		redMaster = new ImageIcon("images/RedMaster.png").getImage();
		redNovice = new ImageIcon("images/RedNovice.png").getImage();
		setLayout(null);
		
	}
	
	public BoardPanel(List<Piece> redPieces, List<Piece> bluePieces){
		this();
		setup(redPieces, bluePieces);
	}
	
	public void setup(List<Piece> redPieces, List<Piece> bluePieces){
		this.redPieces = redPieces;
		this.bluePieces = bluePieces;
	}
	
	public void flipBoard(){
		boardFlipped = !boardFlipped;
	}
	
	public void paintComponent(Graphics g) {
		g.drawImage(backgroundImage, 0, 0, null);
		
		if (redPieces == null || bluePieces == null) return;
		
		if (boardFlipped){
			for (Piece piece : redPieces){
				int row = 4-piece.getRow();
				int col = 4-piece.getCol();
				if (piece.isMaster()){
					g.drawImage(redMaster, 32+128*col, 228+111*row, null);
				}
				else{
					g.drawImage(redNovice, 32+128*col, 232+111*row, null);
				}
			}
			
			for (Piece piece : bluePieces){
				int row = 4-piece.getRow();
				int col = 4-piece.getCol();
				if (piece.isMaster()){
					g.drawImage(blueMaster, 29+128*col, 232+111*row, null);
				}
				else{
					g.drawImage(blueNovice, 28+128*col, 232+111*row, null);
				}
			}
		}
		else{
			for (Piece piece : redPieces){
				int row = piece.getRow();
				int col = piece.getCol();
				if (piece.isMaster()){
					g.drawImage(redMaster, 32+128*col, 228+111*row, null);
				}
				else{
					g.drawImage(redNovice, 32+128*col, 232+111*row, null);
				}
			}
			
			for (Piece piece : bluePieces){
				int row = piece.getRow();
				int col = piece.getCol();
				if (piece.isMaster()){
					g.drawImage(blueMaster, 29+128*col, 232+111*row, null);
				}
				else{
					g.drawImage(blueNovice, 28+128*col, 232+111*row, null);
				}
			}
		}
	}
}
