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
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;

import com.wbartley.rushhour.PermutationSolver.Notification;

public class FindHardestPuzzleDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private RushHour mainApp;
	private JPanel mainPanel;
	private JTextField numCarsTextField, numTrucksTextField;
	private PuzzleGenerator puzzleGenerator = null;

	public FindHardestPuzzleDialog(RushHour mainApp){
		super(new JFrame(), "Find Hardest Puzzle", false);
		this.mainApp = mainApp;
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		mainPanel.add(Box.createVerticalStrut(20));
		
		JPanel pieceConfigurationPanel = new JPanel();
		pieceConfigurationPanel.setLayout(new BoxLayout(pieceConfigurationPanel, BoxLayout.X_AXIS));
		JLabel numCarsLabel = new JLabel("Num non-Red Cars");
		pieceConfigurationPanel.add(Box.createHorizontalStrut(5));
		pieceConfigurationPanel.add(numCarsLabel);
		pieceConfigurationPanel.add(Box.createHorizontalStrut(5));
		numCarsTextField = new JTextField(2);
		numCarsTextField.setText("4");
		PlainDocument document = (PlainDocument)numCarsTextField.getDocument();
		MyIntFilter filter = new MyIntFilter();
		document.setDocumentFilter(filter);
		pieceConfigurationPanel.add(numCarsTextField);
		pieceConfigurationPanel.add(Box.createHorizontalStrut(15));
		JLabel numTrucksLabel = new JLabel("Num Trucks");
		pieceConfigurationPanel.add(numTrucksLabel);
		pieceConfigurationPanel.add(Box.createHorizontalStrut(5));
		numTrucksTextField = new JTextField(2);
		numTrucksTextField.setText("3");
		document = (PlainDocument)numTrucksTextField.getDocument();
		document.setDocumentFilter(filter);
		pieceConfigurationPanel.add(numTrucksTextField);
		pieceConfigurationPanel.add(Box.createHorizontalStrut(5));
		mainPanel.add(pieceConfigurationPanel);
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
		bottomPanel.add(Box.createVerticalStrut(10));
		mainPanel.add(Box.createVerticalGlue());
		mainPanel.add(bottomPanel);
				
		getContentPane().add(mainPanel, "Center");
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(Box.createHorizontalStrut(5));
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
	    JButton ok = new JButton("Ok");
	    buttonPanel.add(ok);
		buttonPanel.add(Box.createHorizontalStrut(20));
	    JButton cancel = new JButton("Cancel");
	    buttonPanel.add(cancel);
	    getContentPane().add(buttonPanel, "South");
			    
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (generatePuzzles()) {
					mainApp.setHardestPuzzleSearchInProgress(true);
					setVisible(false);
				}
			}
		});
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				setVisible(false);
			}
		});
		
		// display main panel
		pack();
		setLocationRelativeTo(mainApp.getMainFrame());
	}
	
	public class PuzzleGenerator implements Runnable, Notification {
		private int numCars, numTrucks, percentComplete = 0;
		private PermutationSolver permutationSolver;
		
		public PuzzleGenerator(int numCars, int numTrucks) {
			this.numCars = numCars;
			this.numTrucks = numTrucks;
		}
		
		public void pauseSearch(boolean pause) {
			permutationSolver.pauseSearch(pause);
		}
		
		public void stopSearch() {
			permutationSolver.stopSearch();
		}

		@Override
		public void run() {
			permutationSolver = new PermutationSolver(12, this, 0, null, true);
			LayoutPermuter permuter = new LayoutPermuter(new ParkingLotLayout(), numCars, numTrucks, permutationSolver);
			permuter.generatePermutations();
			permutationSolver.emptyPendingThreads();
			permutationSolver.exit();
			mainApp.setHardestPuzzleSearchInProgress(false);
		}
		
		@Override
		public void goodPuzzleFound(ParkingLotLayout layout, MoveList solution) {
			mainApp.setLayoutSolutionAndMessage(layout, solution, percentComplete + "%");
		}

		@Override
		public void nonTrivialPuzzleFound(ParkingLotLayout layout, MoveList solution) {
		}

		@Override
		public void progressUpdate(int percentComplete) {
			this.percentComplete = percentComplete;
			if (percentComplete != 100) {
				mainApp.setSupplementalMessage(percentComplete + "%");
			}
			else {
				mainApp.setSupplementalMessage("100% Finished!");
			}
		}
	}
	
	public boolean generatePuzzles() {
		String carsText = numCarsTextField.getText();
		String trucksText = numTrucksTextField.getText();
		int numCars = carsText.isEmpty() ? 0 : Integer.parseInt(carsText);
		int numTrucks = trucksText.isEmpty() ? 0 : Integer.parseInt(trucksText);
		if (numCars + numTrucks < 4) {
			mainApp.setSupplementalMessage("Puzzle must contain at least four non-red vehicles");
			return false;
		}
		else if (numTrucks > Vehicle.getMaxTrucks()) {
			mainApp.setSupplementalMessage("Puzzle may contain no more than " + Vehicle.getMaxTrucks() + " trucks");
			return false;
		}
		else if (numCars > Vehicle.getMaxCars()) {
			mainApp.setSupplementalMessage("Puzzle may contain no more than " + Vehicle.getMaxCars() + "non-red cars");
			return false;
		}
		else {
			mainApp.setSupplementalMessage("Generating puzzles..");
			puzzleGenerator = new PuzzleGenerator(numCars, numTrucks);
			Thread puzzleGeneratorThread = new Thread(puzzleGenerator);
			puzzleGeneratorThread.start();
			return true;
		}
	}
	
	public void pauseSearch(boolean pause) {
		puzzleGenerator.pauseSearch(pause);
		if (pause) {
			mainApp.setSupplementalMessage("Search paused");
		}
		else {
			mainApp.setSupplementalMessage("Generating puzzles..");
		}
	}
	
	public void stopSearch() {
		puzzleGenerator.stopSearch();
		mainApp.setSupplementalMessage("Search terminated");
	}

	class MyIntFilter extends DocumentFilter {
		@Override
		public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
				throws BadLocationException {

			Document doc = fb.getDocument();
			StringBuilder sb = new StringBuilder();
			sb.append(doc.getText(0, doc.getLength()));
			sb.insert(offset, string);

			if (test(sb.toString())) {
				super.insertString(fb, offset, string, attr);
			}
		}

		private boolean test(String text) {
			if (text.isEmpty()) return true;
			try {
				int result = Integer.parseInt(text);
				return result >= 0;
			} catch (NumberFormatException e) {
				return false;
			}
		}

		@Override
		public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
				throws BadLocationException {

			Document doc = fb.getDocument();
			StringBuilder sb = new StringBuilder();
			sb.append(doc.getText(0, doc.getLength()));
			sb.replace(offset, offset + length, text);

			if (test(sb.toString())) {
				super.replace(fb, offset, length, text, attrs);
			}
		}

		@Override
		public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
			Document doc = fb.getDocument();
			StringBuilder sb = new StringBuilder();
			sb.append(doc.getText(0, doc.getLength()));
			sb.delete(offset, offset + length);

			if (test(sb.toString())) {
				super.remove(fb, offset, length);
			}
		}
	}

}
