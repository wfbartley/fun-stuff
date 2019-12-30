package com.bill.onitama.ui;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.bill.onitama.engine.Layout;

public class SettingsDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private JPanel mainPanel;

	public SettingsDialog(){
		super(new JFrame(), "Settings", false);
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createRaisedBevelBorder(),
				BorderFactory.createLoweredBevelBorder()));
		
		getContentPane().add(mainPanel, BorderLayout.LINE_START);
		
		mainPanel.add(Box.createVerticalStrut(20));
		
		JLabel redPlayerLabel = new JLabel("Red Player");
		JPanel redPlayerLabelPanel = new JPanel();
		redPlayerLabelPanel.setLayout(new BoxLayout(redPlayerLabelPanel, BoxLayout.X_AXIS));
		redPlayerLabelPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		redPlayerLabelPanel.add(Box.createHorizontalStrut(20));
		redPlayerLabelPanel.add(redPlayerLabel);
		redPlayerLabelPanel.add(Box.createHorizontalStrut(20));
		redPlayerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		mainPanel.add(redPlayerLabelPanel);
		
		ButtonGroup redPlayerButtonGroup = new ButtonGroup();
		JRadioButton redPlayerHumanButton = new JRadioButton("Human");
		JRadioButton redPlayerComputer = new JRadioButton("Computer");
		redPlayerButtonGroup.add(redPlayerHumanButton);
		redPlayerButtonGroup.add(redPlayerComputer);
		JPanel redPlayerButtonPanel = new JPanel();
		redPlayerButtonPanel.setLayout(new BoxLayout(redPlayerButtonPanel, BoxLayout.X_AXIS));
		redPlayerButtonPanel.add(Box.createHorizontalStrut(20));
		redPlayerButtonPanel.add(redPlayerHumanButton);
		redPlayerButtonPanel.add(redPlayerComputer);
		redPlayerButtonPanel.add(Box.createHorizontalStrut(20));
		redPlayerButtonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		mainPanel.add(redPlayerButtonPanel);
		
		mainPanel.add(Box.createVerticalStrut(20));
		
		JLabel bluePlayerLabel = new JLabel("Blue Player");
		JPanel bluePlayerLabelPanel = new JPanel();
		bluePlayerLabelPanel.setLayout(new BoxLayout(bluePlayerLabelPanel, BoxLayout.X_AXIS));
		bluePlayerLabelPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		bluePlayerLabelPanel.add(Box.createHorizontalStrut(20));
		bluePlayerLabelPanel.add(bluePlayerLabel);
		bluePlayerLabelPanel.add(Box.createHorizontalStrut(20));
		mainPanel.add(bluePlayerLabelPanel);
		
		ButtonGroup bluePlayerButtonGroup = new ButtonGroup();
		JRadioButton bluePlayerHumanButton = new JRadioButton("Human");
		JRadioButton bluePlayerComputer = new JRadioButton("Computer");
		bluePlayerButtonGroup.add(bluePlayerHumanButton);
		bluePlayerButtonGroup.add(bluePlayerComputer);
		JPanel bluePlayerButtonPanel = new JPanel();
		bluePlayerButtonPanel.setLayout(new BoxLayout(bluePlayerButtonPanel, BoxLayout.X_AXIS));
		bluePlayerButtonPanel.add(Box.createHorizontalStrut(20));
		bluePlayerButtonPanel.add(bluePlayerHumanButton);
		bluePlayerButtonPanel.add(bluePlayerComputer);
		bluePlayerButtonPanel.add(Box.createHorizontalStrut(20));
		bluePlayerButtonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		mainPanel.add(bluePlayerButtonPanel);
		
		mainPanel.add(Box.createVerticalStrut(20));
		
		JLabel difficultyLevelLabel = new JLabel("Difficulty Level");
		JPanel difficultLevelLabelPanel = new JPanel();
		difficultLevelLabelPanel.setLayout(new BoxLayout(difficultLevelLabelPanel, BoxLayout.X_AXIS));
		difficultLevelLabelPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		difficultLevelLabelPanel.add(Box.createHorizontalStrut(20));
		difficultLevelLabelPanel.add(difficultyLevelLabel);
		difficultLevelLabelPanel.add(Box.createHorizontalStrut(20));
		difficultLevelLabelPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		mainPanel.add(difficultLevelLabelPanel);
		
		ButtonGroup levelButtonGroup = new ButtonGroup();
		JRadioButton easyButton = new JRadioButton("Easy");
		JRadioButton intermediateButton = new JRadioButton("Intermediate");
		JRadioButton hardButton = new JRadioButton("Hard");
		levelButtonGroup.add(easyButton);
		levelButtonGroup.add(intermediateButton);
		levelButtonGroup.add(hardButton);
		JPanel levelButtonPanel = new JPanel();
		levelButtonPanel.setLayout(new BoxLayout(levelButtonPanel, BoxLayout.X_AXIS));
		levelButtonPanel.add(Box.createHorizontalStrut(20));
		levelButtonPanel.add(easyButton);
		levelButtonPanel.add(intermediateButton);
		levelButtonPanel.add(hardButton);
		levelButtonPanel.add(Box.createHorizontalStrut(20));
		levelButtonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		mainPanel.add(levelButtonPanel);
		
		mainPanel.add(Box.createVerticalStrut(20));
		
		// display main panel
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}
}
