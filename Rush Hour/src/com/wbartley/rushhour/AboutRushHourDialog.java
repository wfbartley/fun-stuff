package com.wbartley.rushhour;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class AboutRushHourDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private JPanel mainPanel;

	public AboutRushHourDialog(){
		super(new JFrame(), "About RushHour", false);
		mainPanel = new JPanel();
		BoxLayout layoutMgr = new BoxLayout(mainPanel, BoxLayout.Y_AXIS);
		mainPanel.setLayout(layoutMgr);
		
		getContentPane().add(mainPanel, "Center");
		
		mainPanel.add(Box.createVerticalStrut(10));
		
		JLabel aboutRushHourLabel = new JLabel("RushHour by Bill Bartley");
		aboutRushHourLabel.setBorder(new EmptyBorder(5, 20, 5, 20));
		mainPanel.add(aboutRushHourLabel);
				
		JLabel copyrightLabel = new JLabel("Copyright 2021");
		copyrightLabel.setBorder(new EmptyBorder(5, 20, 5, 20));
		mainPanel.add(copyrightLabel);
				
		mainPanel.add(Box.createVerticalStrut(10));
		
	    JPanel p2 = new JPanel();
	    JButton ok = new JButton("Ok");
	    p2.add(ok);
	    getContentPane().add(p2, "South");

		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				setVisible(false);
			}
		});
		// display main panel
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}
}
