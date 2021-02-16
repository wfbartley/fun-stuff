package com.wbartley.rushhour;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;

import com.wbartley.rushhour.PermutationSolver.Notification;

public class GenerateRandomPuzzleDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private RushHour mainApp;
	private JPanel mainPanel;
	private JRadioButton noviceRadio, intermediateRadio, advancedRadio, expertRadio, proRadio;
	private JTextField numCarsTextField, numTrucksTextField, minNumMovesTextField;

	public GenerateRandomPuzzleDialog(RushHour mainApp){
		super(new JFrame(), "Generate Random Puzzle", false);
		this.mainApp = mainApp;
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		mainPanel.add(Box.createVerticalStrut(20));
		
		JPanel difficultyLabelPanel = new JPanel();
		difficultyLabelPanel.setLayout(new BoxLayout(difficultyLabelPanel, BoxLayout.X_AXIS));
		JLabel difficultyLabel = new JLabel("Puzzle Difficulty");
		difficultyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		difficultyLabelPanel.add(difficultyLabel);
		mainPanel.add(difficultyLabelPanel);
		
		ButtonGroup difficultyGroup = new ButtonGroup();
		noviceRadio = new JRadioButton("Novice");
		intermediateRadio = new JRadioButton("Intermediate");
		advancedRadio = new JRadioButton("Advanced");
		expertRadio = new JRadioButton("Expert");
		proRadio = new JRadioButton("Pro");
		difficultyGroup.add(noviceRadio);
		difficultyGroup.add(intermediateRadio);
		difficultyGroup.add(advancedRadio);
		difficultyGroup.add(expertRadio);
		difficultyGroup.add(proRadio);
		JPanel difficultyPanel = new JPanel();
		difficultyPanel.setLayout(new BoxLayout(difficultyPanel, BoxLayout.X_AXIS));
		difficultyPanel.add(noviceRadio);
		difficultyPanel.add(intermediateRadio);
		difficultyPanel.add(advancedRadio);
		difficultyPanel.add(expertRadio);
		difficultyPanel.add(proRadio);
		noviceRadio.setAlignmentX(Component.LEFT_ALIGNMENT);
		noviceRadio.setSelected(true);
		intermediateRadio.setAlignmentX(Component.LEFT_ALIGNMENT);
		advancedRadio.setAlignmentX(Component.LEFT_ALIGNMENT);
		expertRadio.setAlignmentX(Component.LEFT_ALIGNMENT);
		proRadio.setAlignmentX(Component.LEFT_ALIGNMENT);
		mainPanel.add(difficultyPanel);
		
		mainPanel.add(Box.createVerticalStrut(10));
		
		JPanel numMovesPanel = new JPanel();
		numMovesPanel.setLayout(new BoxLayout(numMovesPanel, BoxLayout.X_AXIS));
		JLabel minNumMovesLabel = new JLabel("Minimum Number of Moves");
		numMovesPanel.add(Box.createHorizontalStrut(5));
		numMovesPanel.add(minNumMovesLabel);
		numMovesPanel.add(Box.createHorizontalStrut(5));
		minNumMovesTextField = new JTextField(2);
		MyIntFilter filter = new MyIntFilter();
		PlainDocument document = (PlainDocument)minNumMovesTextField.getDocument();
		document.setDocumentFilter(filter);
		numMovesPanel.add(minNumMovesTextField);
		numMovesPanel.add(Box.createHorizontalStrut(5));
		mainPanel.add(numMovesPanel);
		mainPanel.add(Box.createVerticalStrut(10));
		
		JPanel pieceConfigurationPanel = new JPanel();
		pieceConfigurationPanel.setLayout(new BoxLayout(pieceConfigurationPanel, BoxLayout.X_AXIS));
		JLabel numCarsLabel = new JLabel("Num non-Red Cars");
		pieceConfigurationPanel.add(Box.createHorizontalStrut(5));
		pieceConfigurationPanel.add(numCarsLabel);
		pieceConfigurationPanel.add(Box.createHorizontalStrut(5));
		numCarsTextField = new JTextField(2);
		numCarsTextField.setText("4");
		document = (PlainDocument)numCarsTextField.getDocument();
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
				setVisible(false);
				generatePuzzle();
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
		setVisible(true);
	}
	
	public class PuzzleGenerator implements Runnable, Notification {
		private int numCars, numTrucks;
		private int minNumMoves;
		private PuzzleDifficulty desiredDifficulty;
		private int bestPuzzleLength = 0;
		private int bestPuzzleDistance = 0;
		
		public PuzzleGenerator(int minNumMoves, PuzzleDifficulty desiredDifficulty, int numCars, int numTrucks) {
			this.minNumMoves = minNumMoves;
			this.desiredDifficulty = desiredDifficulty;
			this.numCars = numCars;
			this.numTrucks = numTrucks;
		}

		@Override
		public void run() {
			Random random = new Random(System.nanoTime());
			PermutationSolver permutationSolver = new PermutationSolver(3, 2000000, this, minNumMoves, desiredDifficulty, false);
			do {
				int remainingNumCars = numCars;
				int remainingNumTrucks = numTrucks;
				int subsetNumCars = 0, subsetNumTrucks = 0;
				int totalNumVehicles = numCars + numTrucks;
				int subsetLength = totalNumVehicles - 4;
				if (subsetLength < 4) {
					subsetLength = 4;
				}
				for (int i = 0; i < subsetLength; i++) {
					int offset = (int)(random.nextDouble() * (remainingNumCars + remainingNumTrucks));
					if (offset < remainingNumCars) {
						remainingNumCars--;
						subsetNumCars++;
					}
					else {
						remainingNumTrucks--;
						subsetNumTrucks++;
					}
				}
				ParkingLotLayout layout = ParkingLotLayout.generateRandom(subsetNumCars, subsetNumTrucks, totalNumVehicles);
				LayoutPermuter permuter = new LayoutPermuter(layout, remainingNumCars, remainingNumTrucks, permutationSolver);
				permuter.generatePermutations();
				permutationSolver.resetUnsolvablePositions();
			} while (!permutationSolver.isDesiredDifficultyPuzzleFound());
			permutationSolver.exit();
		}
		
		@Override
		public void goodPuzzleFound(ParkingLotLayout layout, MoveList solution) {
			mainApp.setLayoutSolutionAndMessage(layout, solution, "Search Complete");
		}

		@Override
		public void nonTrivialPuzzleFound(ParkingLotLayout layout, MoveList solution) {
			int puzzleLength = solution.length();
			int puzzleDistance = solution.getDistance();
			if (puzzleLength > bestPuzzleLength || puzzleLength == bestPuzzleLength && puzzleDistance > bestPuzzleDistance) {
				PuzzleDifficulty difficulty = solution.getPuzzleDifficulty();
				if (minNumMoves != 0 || difficulty.ordinal() < desiredDifficulty.ordinal()) {
					bestPuzzleLength = puzzleLength;
					bestPuzzleDistance = puzzleDistance;
					String message;
					if (minNumMoves != 0) {
						message = "Attempting to generate " + minNumMoves + " move puzzle.";
					}
					else {
						message = "Attempting to generate " + desiredDifficulty + " level puzzle.";
					}
					mainApp.setLayoutSolutionAndMessage(layout, solution, message);
				}
			}
		}

		@Override
		public void progressUpdate(int percentComplete) {
			// Does nothing
		}
	}
	
	public void generatePuzzle() {
		PuzzleDifficulty difficulty = PuzzleDifficulty.NOVICE;
		if (intermediateRadio.isSelected()) {
			difficulty = PuzzleDifficulty.INTERMEDIATE;
		}
		else if (advancedRadio.isSelected()) {
			difficulty = PuzzleDifficulty.ADVANCED;
		}
		else if (expertRadio.isSelected()) {
			difficulty = PuzzleDifficulty.EXPERT;
		}
		else if (proRadio.isSelected()) {
			difficulty = PuzzleDifficulty.PRO;
		}
		
		String minMovesText = minNumMovesTextField.getText();
		int minNumMoves = minMovesText.isEmpty() ? 0 : Integer.parseInt(minMovesText);
		
		String carsText = numCarsTextField.getText();
		String trucksText = numTrucksTextField.getText();
		int numCars = carsText.isEmpty() ? 0 : Integer.parseInt(carsText);
		int numTrucks = trucksText.isEmpty() ? 0 : Integer.parseInt(trucksText);
		
		if (numCars + numTrucks < 4) {
			mainApp.setSupplementalMessage("Puzzle must contain at least four non-red vehicles");
		}
		else if (numTrucks > Vehicle.getMaxTrucks()) {
			mainApp.setSupplementalMessage("Puzzle may contain no more than " + Vehicle.getMaxTrucks() + " trucks");
		}
		else if (numCars > Vehicle.getMaxCars()) {
			mainApp.setSupplementalMessage("Puzzle may contain no more than " + Vehicle.getMaxCars() + "non-red cars");
		}
		else {
			if (minNumMoves != 0) {
				mainApp.setSupplementalMessage("Attempting to generate " + minNumMoves + " move puzzle.");
			}
			else {
				mainApp.setSupplementalMessage("Attempting to generate " + difficulty + " level puzzle.");
			}
			Thread puzzleGeneratorThread = new Thread(new PuzzleGenerator(minNumMoves, difficulty, numCars, numTrucks));
			puzzleGeneratorThread.start();
		}
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
