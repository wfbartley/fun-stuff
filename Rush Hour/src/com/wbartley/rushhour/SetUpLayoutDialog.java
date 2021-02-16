package com.wbartley.rushhour;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;


public class SetUpLayoutDialog extends JDialog implements ActionListener, WindowListener {
	private static final long serialVersionUID = 1L;
	private static final String ADD_CAR = "Add Car";
	private static final String ADD_TRUCK = "Add Truck";
	private RushHour mainApp;
	private JPanel mainPanel;

	public SetUpLayoutDialog(RushHour mainApp) {
		super(new JFrame(), "Set Up Layout", false);
		addWindowListener(this);
		this.mainApp = mainApp;
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		mainPanel.add(Box.createVerticalStrut(20));
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		mainPanel.add(buttonPanel);
		JButton addCarButton = new JButton(ADD_CAR);
		addCarButton.setActionCommand(ADD_CAR);
		addCarButton.addActionListener(this);
		JButton addTruckButton = new JButton(ADD_TRUCK);
		addTruckButton.setActionCommand(ADD_TRUCK);
		addTruckButton.addActionListener(this);
		buttonPanel.add(Box.createHorizontalStrut(20));
		buttonPanel.add(addCarButton);
		buttonPanel.add(Box.createHorizontalStrut(20));
		buttonPanel.add(addTruckButton);
		buttonPanel.add(Box.createHorizontalStrut(20));
		buttonPanel.add(Box.createHorizontalGlue());
				
		mainPanel.add(Box.createVerticalStrut(20));
		
		getContentPane().add(mainPanel, "Center");
				
		// display main panel
		pack();
		Component mainFrame = mainApp.getMainFrame();
		setLocationRelativeTo(mainFrame);
		setLocation(new Point(mainFrame.getX() + mainFrame.getWidth(), mainFrame.getY()));
		mainApp.startLayoutSetUp();
		if (mainApp.getBoardPanel().getParkingLotLayout().getNumPieces() == 0) {
			mainApp.setCurrentAddVehicleSize(Vehicle.carSize);
		}
		setVisible(true);
	}
			
	@Override
	public void actionPerformed(ActionEvent e) {
		ParkingLotLayout layout = mainApp.getBoardPanel().getParkingLotLayout();
		if (e.getActionCommand().equals(ADD_CAR)) {
			if (layout.getNumCars() < Vehicle.getMaxCars()) {
				mainApp.setCurrentAddVehicleSize(Vehicle.carSize);
			}
			else {
				mainApp.setSupplementalMessage("Cannot add any more cars.");
			}
		}
		else {
			if (layout.getNumPieces() != 0) {
				if (layout.getNumTrucks() < Vehicle.getMaxTrucks()) {
					mainApp.setCurrentAddVehicleSize(Vehicle.truckSize);
				}
				else {
					mainApp.setSupplementalMessage("Cannot add any more trucks.");
				}
			}
			else {
				mainApp.setSupplementalMessage("First vehicle added must be the red car");
			}
		}
	}

	@Override
	public void windowOpened(WindowEvent e) {
	}

	@Override
	public void windowClosing(WindowEvent e) {
		mainApp.stopLayoutSetup();
	}

	@Override
	public void windowClosed(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}

}
