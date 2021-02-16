package com.wbartley.rushhour;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.BoxLayout;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

public class RushHour implements ActionListener, MouseListener {
	private static final String SAVE_FILES_DIRECTORY_NAME = "saved";
	private static final String NEW_LAYOUT_MENU_ITEM = "New Layout";
	private static final String OPEN_MENU_ITEM = "Open..";
	private static final String SAVE_MENU_ITEM = "Save..";
	private static final String SAVE_AS_MENU_ITEM = "Save As..";
	private static final String RANDOM_PUZZLE_MENU_ITEM = "Random Puzzle..";
	private static final String SET_UP_LAYOUT_MENU_ITEM = "Set Up Layout..";
	private static final String FIND_HARDEST_PUZZLE_MENU_ITEM = "Find Hardest Puzzle..";
	private static final String SOLVE_PUZZLE_MENU_ITEM = "Solve Puzzle";
	private static final String SHOW_SOLUTION_MENU_ITEM = "Show Solution";
	private static final String ANIMATE_SOLUTION_MENU_ITEM = "Animate Solution";
	private static final String EXIT_MENU_ITEM = "Exit";
	private static final String ABOUT_MENU_ITEM = "About...";
	
	private static RushHour instance;
	private File saveFilesDirectory;
	private JFrame mainFrame;
	private MoveList solution = null;
	private PuzzleDifficulty puzzleDifficulty = null;
	private int curMove = -1;
	private JCheckBoxMenuItem showSolution;
	private String supplementalMessage = null;
	private ParkingLotPanel boardPanel;
	private boolean setUpLayoutActive = false;
	private int curAddVehicleSize = 0;
	private JEditorPane messagesTextArea;
	private File currentLayoutFile = null;
	
	public RushHour() {
		saveFilesDirectory = new File(SAVE_FILES_DIRECTORY_NAME);
		saveFilesDirectory.mkdir();
	}
	
	public void buildAndShowGui(){
	    mainFrame = new JFrame("Rush Hour");
	    mainFrame.setSize(600, 600);
	    mainFrame.setLocationRelativeTo(null);

	    mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    JMenuBar mainMenu = new JMenuBar();

	    JMenu jmFile = new JMenu("File");
	    JMenuItem newLayout = new JMenuItem(NEW_LAYOUT_MENU_ITEM);
	    JMenuItem open = new JMenuItem(OPEN_MENU_ITEM);
	    JMenuItem save = new JMenuItem(SAVE_MENU_ITEM);
	    JMenuItem saveAs = new JMenuItem(SAVE_AS_MENU_ITEM);
	    JMenuItem exit = new JMenuItem(EXIT_MENU_ITEM);
	    jmFile.add(newLayout);
	    jmFile.add(open);
	    jmFile.add(save);
	    jmFile.add(saveAs);
	    jmFile.addSeparator();
	    jmFile.add(exit);
	    mainMenu.add(jmFile);
	    
	    JMenu jmActions = new JMenu("Actions");
	    JMenuItem generatePuzzle = new JMenuItem(RANDOM_PUZZLE_MENU_ITEM);
	    JMenuItem inputPuzzle = new JMenuItem(SET_UP_LAYOUT_MENU_ITEM);
	    JMenuItem findLongestPuzzle = new JMenuItem(FIND_HARDEST_PUZZLE_MENU_ITEM);
	    JMenuItem solvePuzzle = new JMenuItem(SOLVE_PUZZLE_MENU_ITEM);
	    showSolution = new JCheckBoxMenuItem(SHOW_SOLUTION_MENU_ITEM);
	    JMenuItem animateSolution = new JMenuItem(ANIMATE_SOLUTION_MENU_ITEM);
	    jmActions.add(generatePuzzle);
	    jmActions.add(inputPuzzle);
	    jmActions.add(findLongestPuzzle);
	    jmActions.add(solvePuzzle);
	    jmActions.add(showSolution);
	    jmActions.add(animateSolution);
	    mainMenu.add(jmActions);

	    JMenu jmHelp = new JMenu("Help");
	    JMenuItem jmiAbout = new JMenuItem(ABOUT_MENU_ITEM);
	    jmHelp.add(jmiAbout);
	    mainMenu.add(jmHelp);

	    newLayout.addActionListener(this);
	    open.addActionListener(this);
	    save.addActionListener(this);
	    saveAs.addActionListener(this);
	    exit.addActionListener(this);
	    generatePuzzle.addActionListener(this);
	    inputPuzzle.addActionListener(this);
	    findLongestPuzzle.addActionListener(this);
	    solvePuzzle.addActionListener(this);
	    showSolution.addActionListener(this);
	    animateSolution.addActionListener(this);
	    jmiAbout.addActionListener(this);
	    
	    mainFrame.setJMenuBar(mainMenu);
	    
	    JPanel mainLayout = new JPanel();
	    mainLayout.setLayout(new BoxLayout(mainLayout, BoxLayout.Y_AXIS));
	    mainFrame.getContentPane().add(mainLayout, BorderLayout.LINE_START);
	    
	    boardPanel = new ParkingLotPanel();
	    mainLayout.add(boardPanel);
	    
	    messagesTextArea = new JEditorPane("text/html", "");
	    messagesTextArea.setEditable(false);
	    messagesTextArea.setPreferredSize(new Dimension(0, 150));
	    mainLayout.add(messagesTextArea);
	    
	    mainFrame.pack();
	    mainFrame.setVisible(true);
	}
	
	private class AnimateSolutionRunnable implements Runnable {

		@Override
		public void run() {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {}
			if (solution != null) {
				ParkingLotLayout startingLayout = boardPanel.getParkingLotLayout();
				ParkingLotLayout layout = new ParkingLotLayout(startingLayout);
				byte [] moveList = solution.getMoves();
				for (int i = 0; i < solution.length(); i++) {
					byte move = moveList[i];
					curMove++;
					layout = layout.tryMove(move);
					setLayoutSolutionAndMessage(layout, solution, null);
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {}
				}
				try {
					Thread.sleep(4000);
				} catch (InterruptedException e) {}
				curMove = -1;
				setLayoutSolutionAndMessage(startingLayout, solution, null);
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String actionCommand = e.getActionCommand();
		if (actionCommand.equals(EXIT_MENU_ITEM)){
			System.exit(0);
		}
		else if (actionCommand.equals(RANDOM_PUZZLE_MENU_ITEM)){
			new GenerateRandomPuzzleDialog(this);
		}
		else if (actionCommand.equals(SET_UP_LAYOUT_MENU_ITEM)){ 
			if (!setUpLayoutActive) {
				new SetUpLayoutDialog(this);
			}
		}
		else if (actionCommand.equals(FIND_HARDEST_PUZZLE_MENU_ITEM)){
			new FindHardestPuzzleDialog(this);
		}
		else if (actionCommand.equals(SOLVE_PUZZLE_MENU_ITEM)) {
			ParkingLotLayout layout = boardPanel.getParkingLotLayout();
			if (layout.getNumPieces() == 0) {
				setSupplementalMessage("Cannot solve layout without any pieces!");
			}
			else if (layout.redCarCanExit()) {
				setSupplementalMessage("Puzzle is already solved!");
			}
			else {
				Solver solver = new Solver(layout);
				solver.solve();
				solution = solver.getBestSolution();
				if (solution == null) {
					setSupplementalMessage("Puzzle is unsolvable!");
				}
				else {
					showSolution.setSelected(true);
					setLayoutSolutionAndMessage(layout, solution, "Puzzle is solved!");
				}
			}
		}
		else if (actionCommand.equals(SHOW_SOLUTION_MENU_ITEM)) {
			updateMessage();
		}
		else if (actionCommand.equals(ANIMATE_SOLUTION_MENU_ITEM)){
			new Thread(new AnimateSolutionRunnable()).start();
		}
		else if (actionCommand.equals(ABOUT_MENU_ITEM)) {
			new AboutRushHourDialog();
		}
		else if (actionCommand.equals(NEW_LAYOUT_MENU_ITEM)) {
			currentLayoutFile = null;
			setLayoutSolutionAndMessage(new ParkingLotLayout(), null, "");
		}
		else if (actionCommand.equals(OPEN_MENU_ITEM)) {
			loadFromFile();
		}
		else if (actionCommand.equals(SAVE_MENU_ITEM)) {
			saveToFile();
		}
		else if (actionCommand.equals(SAVE_AS_MENU_ITEM)) {
			File savedLayoutFile = currentLayoutFile;
			currentLayoutFile = null;
			saveToFile();
			if (currentLayoutFile == null) {
				currentLayoutFile = savedLayoutFile;
			}
		}
	}
	
	private void loadFromFile() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(saveFilesDirectory);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Rush Hour File","rhf");
		fileChooser.addChoosableFileFilter(filter);
		fileChooser.setDialogTitle("Open Layout File");
		int result = fileChooser.showOpenDialog(mainFrame);
		if (result == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			ParkingLotLayout layout = null;
			ObjectInputStream ois = null;
			try {
				ois = new ObjectInputStream(new FileInputStream(file));
				layout = (ParkingLotLayout)ois.readObject();
			} catch (ClassNotFoundException e1) {
				setSupplementalMessage("Error loading from file, " + file.getName());
			} catch (IOException e1) {
				setSupplementalMessage("Error loading from file, " + file.getName());
			} finally {
				if (ois != null) {
					try {
						ois.close();
					} catch (IOException e1) {}
				}
			}
			if (layout != null) {
				currentLayoutFile = file;
				setLayoutSolutionAndMessage(layout, null, "");
			}
		}
	}
	
	private void saveToFile() {
		if (currentLayoutFile == null) {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setCurrentDirectory(saveFilesDirectory);
			FileNameExtensionFilter filter = new FileNameExtensionFilter("Rush Hour File","rhf");
			fileChooser.addChoosableFileFilter(filter);
			fileChooser.setFileFilter(filter);
			fileChooser.setDialogTitle("Save Layout File");
			int result = fileChooser.showSaveDialog(mainFrame);
			if (result != JFileChooser.APPROVE_OPTION) {
				return;
			}
			currentLayoutFile = fileChooser.getSelectedFile();
			if (!currentLayoutFile.getPath().endsWith(".rhf")) {
				currentLayoutFile = new File(currentLayoutFile.getPath() + ".rhf");
			}
		}
		ObjectOutputStream oos = null;
		try {
			ParkingLotLayout layout = boardPanel.getParkingLotLayout();
			if (layout == null) return;
			oos = new ObjectOutputStream(new FileOutputStream(currentLayoutFile));
			oos.writeObject(layout);
			setSupplementalMessage("Saved!");
		} catch (IOException e1) {
			setSupplementalMessage("Error loading from file, " + currentLayoutFile.getName());
		} finally {
			try {
				if (oos != null) {
					oos.close();
				}
			} catch (IOException e) {}
		}
	}
	
	public Component getMainFrame() {
		return mainFrame;
	}
	
	public void setLayoutSolutionAndMessage(ParkingLotLayout layout, MoveList solution, String supplementalMessage) {
		if (layout == null) {
			layout = new ParkingLotLayout();
			this.solution = null;
			this.puzzleDifficulty = null;
		}
		else {
			this.solution = solution;
			if (solution != null) {
				this.puzzleDifficulty = solution.getPuzzleDifficulty();
			}
			else {
				puzzleDifficulty = null;
			}
			this.supplementalMessage = supplementalMessage;
		}
		updateMessage();
		boardPanel.setParkingLotLayout(layout);
	}
		
	public void setSolution(MoveList solution) {
		this.solution = solution;
	}
	
	public void setSupplementalMessage(String supplementalMessage) {
		this.supplementalMessage = supplementalMessage;
		updateMessage();
	}
	
	public void updateMessage() {
		SwingUtilities.invokeLater( new Runnable() {	
			@Override
			public void run() {
				StringBuilder builder = new StringBuilder();
				ParkingLotLayout layout = boardPanel.getParkingLotLayout();
				if (puzzleDifficulty != null) {
					builder.append("Puzzle difficulty: " + puzzleDifficulty + "<br>");
				}
				if (solution != null && showSolution.isSelected()) {
					builder.append("Solution:<br>");
					byte [] moveList = solution.getMoves();
					for (int i = 0; i < solution.length(); i++) {
						byte move = moveList[i];
						int pieceIdx = Move.getPieceIndex(move);
						byte piece = layout.getPiece(pieceIdx);
						Vehicle vehicle = layout.getVehicleFromIndex(pieceIdx);
						if (i == curMove) {
							builder.append("<b>" + Move.toString(vehicle, Piece.isHoriz(piece), move) + "</b> ");
						}
						else {
							builder.append(Move.toString(vehicle, Piece.isHoriz(piece), move) + " ");
						}
					}
					builder.append("<br>");
				}
				if (supplementalMessage != null){
					builder.append(supplementalMessage + "<br>");
				}
					
				messagesTextArea.setText(builder.toString());
			}
		});
	}
	
	public ParkingLotPanel getBoardPanel() {
		return boardPanel;
	}
	
	public void setCurrentAddVehicleSize(int size) {
		curAddVehicleSize = size;
		boardPanel.setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));
	}
	
	public void startLayoutSetUp() {
		setUpLayoutActive = true;
		boardPanel.addMouseListener(this);
	}
	
	public void stopLayoutSetup() {
		setUpLayoutActive = false;
		boardPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		boardPanel.removeMouseListener(this);
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		Cursor cursor = boardPanel.getCursor();
		ParkingLotLayout layout = boardPanel.getParkingLotLayout();
		if (e.getButton() == MouseEvent.BUTTON1) {
			if (cursor.getType() == Cursor.CROSSHAIR_CURSOR) {
				int row = ParkingLotPanel.getRowColClicked(e.getY());
				int col = ParkingLotPanel.getRowColClicked(e.getX());
				int clickedOnIndex = layout.getPieceOnSquare(row, col);
				if (clickedOnIndex != -1) {
					if (clickedOnIndex == 0 && layout.getNumPieces() != 1) {
						setSupplementalMessage("Cannot remove red car until all other cars have been removed");
					}
					else {
						layout.removePiece(clickedOnIndex);
						setLayoutSolutionAndMessage(layout, null, "");
						if (layout.getNumPieces() == 0) { // just removed the red car, enable piece entry
							setCurrentAddVehicleSize(Vehicle.carSize);
						}
					};
				}
				return;
			}
			int row = ParkingLotPanel.getUpperLeftRowCol(e.getY());
			int col = ParkingLotPanel.getUpperLeftRowCol(e.getX());
			boolean isTruck = curAddVehicleSize == 3;
			boolean isVert = (cursor.getType() == Cursor.N_RESIZE_CURSOR);
			if (layout.getNumPieces() == 0) { // Adding the red car
				if (row != 2 || col > 3 || isVert) {
					setSupplementalMessage("Red car must be placed horizontally on the third row and not right next to the exit");
					return;
				}
			}
			byte piece;
			if (!isVert) { // Horizontal piece entry
				if (col > 6 - curAddVehicleSize) {
					setSupplementalMessage("Piece is too far right");
					return;
				}
				else if (row == ParkingLotLayout.exitRow && layout.getNumPieces() != 0) {
					setSupplementalMessage("No horizontal pieces except red car allowed on third row");
					return;
				}
				piece = Piece.get(row, col, isTruck, isVert);
			}
			else { // Vertical piece entry
				if (row > 6 - curAddVehicleSize) {
					setSupplementalMessage("Piece is too low");
					return;
				}
				piece = Piece.get(row, col, isTruck, isVert);
			}
			if (layout.addPiece(piece)) {
				setLayoutSolutionAndMessage(layout, null, "");
			}
			else {
				setSupplementalMessage("Piece is obstructed");
			}
		}
		else if (e.getButton() == MouseEvent.BUTTON3) {
			if (layout.getNumPieces() != 0) { // If red car is being entered, don't change cursor
				if (cursor.getType() == Cursor.E_RESIZE_CURSOR) { // from EW to NS
					boardPanel.setCursor(new Cursor(Cursor.N_RESIZE_CURSOR));
				}
				else if (cursor.getType() == Cursor.N_RESIZE_CURSOR){ // NS to delete
					boardPanel.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
				}
				else { // from delete to EW
					boardPanel.setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));
				}
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// unused
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// unused
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// unused
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// unused
	}

	public static void main(String[] args) {
		instance = new RushHour();
		SwingUtilities.invokeLater( new Runnable() {	
			@Override
			public void run() {
				instance.buildAndShowGui();;
			}
		}
		);
	}
	
}
