package com.wbartley.rushhour;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

public class ParkingLotPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private ParkingLotLayout parkingLotLayout;
	private static int lotSize = 400, lotSquareEdge;

	public ParkingLotPanel() {
		Dimension size = new Dimension(lotSize, lotSize);
		lotSquareEdge = (int)Math.round(size.getWidth()) / 6;
		setPreferredSize(size);
		setMinimumSize(size);
		setMaximumSize(size);
		setSize(size);
		setLayout(null);
	    Border blackline = BorderFactory.createLineBorder(Color.black);
	    setBorder(blackline);
	    parkingLotLayout = new ParkingLotLayout();
	}
		
	public void setParkingLotLayout(ParkingLotLayout parkingLotLayout) {
		this.parkingLotLayout = new ParkingLotLayout(parkingLotLayout);
		SwingUtilities.invokeLater( new Runnable() {	
			@Override
			public void run() {
				paintComponent(getGraphics());
			}
		}
		);
	}
	
	public ParkingLotLayout getParkingLotLayout() {
		return new ParkingLotLayout(parkingLotLayout);
	}
	
	public static int getUpperLeftRowCol(int offset) {
		return (offset + lotSquareEdge / 2) / lotSquareEdge;
	}
	
	public static int getRowColClicked(int offset) {
		return offset / lotSquareEdge;
	}
	
	private void drawCenteredString(Graphics g, String text, int x, int y, int width, int height, FontMetrics metrics) {
	    int xPos = x + (width - metrics.stringWidth(text)) / 2;
	    int yPos = y + ((height - metrics.getHeight()) / 2) + metrics.getAscent();
	    g.drawString(text, xPos, yPos);
	}
	
	public void paintComponent(Graphics g) {
		Font font = new Font("Arial", Font.BOLD, 18);
	    // Set the font
	    g.setFont(font);
	    // Get the FontMetrics
	    FontMetrics metrics = g.getFontMetrics(font);
	    
	    // Lot background
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, lotSize, lotSize);
	    g.setColor(Color.LIGHT_GRAY);
	    for (int row = 0; row < 6; row++) {
	    	for (int col = 0; col < 6; col++) {
	    		int x = col * lotSquareEdge + 2;
	    		int y = row * lotSquareEdge + 2;
	    		int width = lotSquareEdge - 4;
	    		int height = width;
	    		g.drawRect(x, y, width, height);
	    	}
	    }
		
		int carIdx = 0;
		int truckIdx = Vehicle.getMaxCars() + 1;
		for (int i = 0; i < parkingLotLayout.getNumPieces(); i++){
			byte piece = parkingLotLayout.getPiece(i);
			int row = Piece.getRow(piece);
			int col = Piece.getCol(piece);
			int x = col * lotSquareEdge;
			int y = row * lotSquareEdge;
			int width = lotSquareEdge;
			int height = lotSquareEdge;
			boolean isCar = Piece.isCar(piece);
			if (Piece.isVert(piece)) {
				height *= (isCar ? 2 : 3);
			}
			else {
				width *= (isCar ? 2 : 3);
			}
			Vehicle vehicle;
			if (isCar) {
				vehicle = Vehicle.values()[carIdx++];
			}
			else {
				vehicle = Vehicle.values()[truckIdx++];
			}
			g.setColor(vehicle.getColor());
			g.fillRect(x, y, width, height);
			g.setColor(Color.WHITE);
			drawCenteredString(g, vehicle.getNickname(), x, y, width, height, metrics);
		}
		
	}
}
