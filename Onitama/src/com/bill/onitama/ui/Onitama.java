package com.bill.onitama.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;

import com.bill.onitama.engine.Card;
import com.bill.onitama.engine.Layout;
import com.bill.onitama.engine.Move;

public class Onitama implements ActionListener {
	private static final String NEW_GAME_MENU_ITEM = "New Game...";
	private static final String EXIT_MENU_ITEM = "Exit";
	private static final String FLIP_BOARD_MENU_ITEM = "Flip Board";
	private static final String SETTINGS_MENU_ITEM = "Settings...";
	private static final String ABOUT_MENU_ITEM = "About...";
	
	private static Onitama instance;
	
	private JFrame mainFrame;
	private Layout layout;
	
	private BoardPanel boardPanel;
	private CardPanel [] topCards = new CardPanel[2];
	private CardPanel commonCard;
	private CardPanel [] bottomCards = new CardPanel[2];
	private boolean boardFlipped = false;
	
	private JTextPane moveList;
	private JTextPane messages;
	
	public Onitama(Layout layout){
		this.layout = layout;
		buildAndShowGui();
	}
	
	private void buildAndShowGui(){
	    mainFrame = new JFrame("Menu Demo");
	    mainFrame.setSize(800, 600);

	    mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    JMenuBar mainMenu = new JMenuBar();

	    JMenu jmFile = new JMenu("File");
	    JMenuItem jmiNewGame = new JMenuItem(NEW_GAME_MENU_ITEM);
	    JMenuItem jmiExit = new JMenuItem(EXIT_MENU_ITEM);
	    jmFile.add(jmiNewGame);
	    jmFile.addSeparator();
	    jmFile.add(jmiExit);
	    mainMenu.add(jmFile);

	    JMenu jmOptions = new JMenu("Options");
	    JMenuItem jmiFlipBoard = new JMenuItem(FLIP_BOARD_MENU_ITEM);
	    JMenuItem jmiSettings = new JMenuItem(SETTINGS_MENU_ITEM);
	    jmOptions.add(jmiFlipBoard);
	    jmOptions.add(jmiSettings);
	    mainMenu.add(jmOptions);

	    JMenu jmHelp = new JMenu("Help");
	    JMenuItem jmiAbout = new JMenuItem(ABOUT_MENU_ITEM);
	    jmHelp.add(jmiAbout);
	    mainMenu.add(jmHelp);

	    jmiNewGame.addActionListener(this);
	    jmiExit.addActionListener(this);
	    jmiFlipBoard.addActionListener(this);
	    jmiSettings.addActionListener(this);
	    jmiAbout.addActionListener(this);
	    
	    mainFrame.setJMenuBar(mainMenu);
	    
	    boardPanel = new BoardPanel(layout.getRedPieces(), layout.getBluePieces());
	    mainFrame.getContentPane().add(boardPanel, BorderLayout.LINE_START);
	    
	    JPanel cardsPanel = new JPanel();
	    cardsPanel.setLayout(new BoxLayout(cardsPanel, BoxLayout.Y_AXIS));
	    
	    JPanel blueCardsPanel = new JPanel();
	    blueCardsPanel.setLayout(new BoxLayout(blueCardsPanel, BoxLayout.X_AXIS));
	    
	    blueCardsPanel.add(Box.createHorizontalStrut(20));
	    
	    Card [] blues = layout.getBlueCards();
	    for (int i = 0; i < 2; i++){
	    	topCards[i] = new CardPanel(blues[i], true);
	    	blueCardsPanel.add(topCards[i]);
	    	blueCardsPanel.add(Box.createHorizontalStrut(20));
	    }
	    
	    JPanel commonCardPanel = new JPanel();
	    commonCardPanel.setLayout(new BoxLayout(commonCardPanel, BoxLayout.X_AXIS));
	    commonCardPanel.add(Box.createHorizontalStrut(20));
	    commonCard = new CardPanel(layout.getCommonCard(), false);
	    commonCardPanel.add(commonCard);
	    commonCardPanel.add(Box.createHorizontalGlue());
	    	    
	    JPanel redCardsPanel = new JPanel();
	    redCardsPanel.setLayout(new BoxLayout(redCardsPanel, BoxLayout.X_AXIS));
    	redCardsPanel.add(Box.createHorizontalStrut(20));
	    Card [] reds = layout.getRedCards();
	    for (int i = 0; i < 2; i++){
	    	bottomCards[i] = new CardPanel(reds[i], false);
	    	redCardsPanel.add(bottomCards[i]);
	    	redCardsPanel.add(Box.createHorizontalStrut(20));
	    }
	    
	    cardsPanel.add(blueCardsPanel);
	    cardsPanel.add(Box.createVerticalGlue());
	    cardsPanel.add(commonCardPanel);
	    cardsPanel.add(Box.createVerticalGlue());
	    cardsPanel.add(redCardsPanel);
	    
	    mainFrame.getContentPane().add(cardsPanel, BorderLayout.CENTER);
	    
	    JPanel outputPanel = new JPanel();
	    outputPanel.setLayout(new BoxLayout(outputPanel, BoxLayout.Y_AXIS));
	    outputPanel.add(new JLabel("Move List"));
	    moveList = new JTextPane();
	    moveList.setEditable(false);
	    moveList.setPreferredSize(new Dimension(300, 400));
	    moveList.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createRaisedBevelBorder(),
				BorderFactory.createLoweredBevelBorder()));
	    outputPanel.add(moveList);
	    
	    outputPanel.add(new JLabel("Messages"));
	    messages = new JTextPane();
	    messages.setEditable(false);;
	    messages.setPreferredSize(new Dimension(300, 400));
	    messages.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createRaisedBevelBorder(),
				BorderFactory.createLoweredBevelBorder())); 
	    outputPanel.add(messages);
	    
	    mainFrame.getContentPane().add(outputPanel, BorderLayout.LINE_END);

	    mainFrame.pack();
	    mainFrame.setVisible(true);
	}
	
	public void setCards(Card [] reds, Card [] blues, Card commonCard){
		for (int i = 0; i < 2; i++){
			if (boardFlipped){
				topCards[i].setCard(reds[i]);
				bottomCards[i].setCard(blues[i]);
			}
			else{
				topCards[i].setCard(blues[i]);
				bottomCards[i].setCard(reds[i]);
			}
		}
		this.commonCard.setCard(commonCard);
	}
	
	public void updateDisplay(){
        mainFrame.repaint();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String actionCommand = e.getActionCommand();
		if (actionCommand.equals(EXIT_MENU_ITEM)){
			System.exit(0);
		}
		else if (actionCommand.equals(FLIP_BOARD_MENU_ITEM)){
			boardFlipped = !boardFlipped;
			boardPanel.flipBoard();
			setCards(layout.getRedCards(), layout.getBlueCards(), layout.getCommonCard());
			mainFrame.repaint();
		}
		else if (actionCommand.equals(SETTINGS_MENU_ITEM)){
			new SettingsDialog();
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater( new Runnable() {	
			@Override
			public void run() {
				instance = new Onitama(new Layout());
			}
		}
		);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) { }
		Move move;
		do {
			move = instance.layout.findBestMove();
			if (move != null){
				instance.layout.makeMove(move);
				instance.boardPanel.setup(instance.layout.getRedPieces(), instance.layout.getBluePieces());
				instance.setCards(instance.layout.getRedCards(), instance.layout.getBlueCards(), instance.layout.getCommonCard());
				SwingUtilities.invokeLater( new Runnable() {	
					@Override
					public void run() {
						instance.updateDisplay();
					}
				}
				);
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) { }
			}
		} while (move != null);
	}

}
