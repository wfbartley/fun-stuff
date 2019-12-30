package com.bill.onitama.ui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import com.bill.onitama.engine.Card;

public class CardPanel extends JPanel {
	private static final String CARD_BACK_IMAGE = "images/CardBack.jpg";
	private static final long serialVersionUID = 1L;
	private BufferedImage img;
	private boolean rotated;
	
	public CardPanel(boolean rotated){
		this((Card)null, rotated);
	}
	
	private BufferedImage createBufferedImage(String filename, boolean rotated) {
		Image image = new ImageIcon(filename).getImage();
		int width = image.getWidth(null);
		int height = image.getHeight(null);
		BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = newImage.createGraphics();
		if (rotated){
			g.rotate(Math.toRadians(180), width / 2, height / 2);
		}
	    g.drawImage(image, 0, 0, null);
	    g.dispose();
		return newImage;
	}
    
	public CardPanel(Card card, boolean rotated) {
		this.rotated = rotated;
		img = createBufferedImage(card == null ? CARD_BACK_IMAGE : card.getImageFileName(), rotated);
		Dimension size = new Dimension(img.getWidth(), img.getHeight());
		setPreferredSize(size);
		setMinimumSize(size);
		setMaximumSize(size);
		setSize(size);
		setLayout(null);
	}

	public void setCard(Card card){
		img = createBufferedImage(card == null ? CARD_BACK_IMAGE : card.getImageFileName(), rotated);
	}

	public void paintComponent(Graphics g) {
		g.drawImage(img, 0, 0, null);
	}
}
