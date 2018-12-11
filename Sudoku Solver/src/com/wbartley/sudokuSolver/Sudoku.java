package com.wbartley.sudokuSolver;

import java.io.FileReader;
import java.io.BufferedReader;
import java.lang.String;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;

public class Sudoku {
	private int[] layout = new int[81];
	private Integer [] emptySquares = new Integer[81];
	private int numEmptySquares = 0;
	private HashSet<Integer> filledSquares = new HashSet<Integer>(81);
	private int [][] relatedPositions = new int[81][20];
	private int [][] availableMoves = new int[0x1ff][];
	private int [] bitMask = { 0, 0x1, 0x2, 0x4, 0x8, 0x10, 0x20, 0x40, 0x80, 0x100 };
	private long numTries = 0;
	
	public Sudoku() {
		// Initialize related positions
		for (int position = 0; position < 81; position++) {
			int numRelatedPositions = 0;
			int row = position / 9;
			int column = position % 9;
			for (int c = 0; c < 9; c++) {
				if (c != column) {
					relatedPositions[position][numRelatedPositions] = row * 9 + c;
					numRelatedPositions++;
				}
			}
			for (int r = 0; r < 9; r++) {
				if (r != row) {
					relatedPositions[position][numRelatedPositions] = r * 9 + column;
					numRelatedPositions++;
				}
			}
			int startRow = (row / 3) * 3;
			int startCol = (column / 3) * 3;
			for (int r = startRow; r < startRow + 3; r++) {
				for (int c = startCol; c < startCol + 3; c++) {
					if (r != row && c != column) {
						relatedPositions[position][numRelatedPositions] = r * 9 + c;
						numRelatedPositions++;
					}
				}
			}
		}
		
		// Initialize available moves
		for (int bitArray = 0; bitArray < 511; bitArray++) {
			int numAvailable = 0;
			for (int value = 1; value < 10; value++) {
				if ((bitMask[value] & bitArray) == 0){
					numAvailable++;
				}
			}
			int [] available = new int[numAvailable];
			availableMoves[bitArray] = available;
			numAvailable = 0;
			for (int value = 1; value < 10; value++) {
				if ((bitMask[value] & bitArray) == 0){
					available[numAvailable] = value;
					numAvailable++;
				}
			}
		}
	}
	
	private class SortEmptySquares implements Comparator<Integer>{

		@Override
		public int compare(Integer arg0, Integer arg1) {
			int bits1 = layout[arg0];
			int bits2 = layout[arg1];
			int allowed1 = availableMoves[bits1].length;
			int allowed2 = availableMoves[bits2].length;
			return allowed1 - allowed2;
		}

	}
	
	private void orderEmptySquares() {
		Arrays.sort(emptySquares, 0, numEmptySquares, new SortEmptySquares());
	}

	public void readInitialPositionFromFile(String pathname) throws Exception {

		// Read in .txt file containing Sudoku puzzle
		FileReader readInitialPuzzle = new FileReader(pathname);
		BufferedReader readPuzzleLines = new BufferedReader(readInitialPuzzle);
		int [] dummy = new int[20];

		for (int row = 0; row < 9; row++) {
			String line = readPuzzleLines.readLine();

			// If there is a line missing
			if (line == null) {
				readPuzzleLines.close();
				throw new Exception("There must be 9 lines in the puzzle file");
			}

			// If there is more or less than 9 lines
			if (line.length() != 9) {
				readPuzzleLines.close();
				throw new Exception("There must be 9 lines in the puzzle file");
			}

			for (int column = 0; column < 9; column++) {
				char squareVal = line.charAt(column);
				int position = row * 9 + column;

				if (squareVal == '-') {
					emptySquares[numEmptySquares] = position;
					numEmptySquares++;
				} else if (squareVal >= '1' && squareVal <= '9') {
					int intValue = squareVal - '0';
					if (!setSquareValue(position, intValue, dummy)) {
						readPuzzleLines.close();
						throw new Exception("Puzzle initial position is inconsistent");
					}
				} else {
					readPuzzleLines.close();
					throw new Exception("Only characters '-' and digits 1-9 are allowed");
				}
			}
		}
		orderEmptySquares();
		readPuzzleLines.close();
	}
	
	private boolean setSquareValue(int position, int value, int [] previousPosition) {
		if ((layout[position] & bitMask[value]) != 0){
			return false;
		}
		numTries++;
		boolean result = true;
		int [] related = relatedPositions[position];
		for (int i = 0; i < 20; i++) {
			previousPosition[i] = layout[related[i]];
		}
		layout[position] = value;
		filledSquares.add(position);
		for (int i = 0; i < 20; i++) {
			int relatedPos = related[i];
			if (!filledSquares.contains(relatedPos)) {
				layout[relatedPos] |= bitMask[value];
				if (layout[related[i]] == 0x1ff) {
					result = false;
				}
			}
		}
		return result;
	}
	
	public void setNumTries(long numTries) {
		this.numTries = numTries;
	}
	
	public long getNumTries() {
		return numTries;
	}
	
	public boolean tryToSolve(int moveNumber) {
		int position = emptySquares[moveNumber];
		int currentValue = layout[position];
		int [] available = availableMoves[currentValue];
		int [] previousPosition = new int[20];
		for (int value : available) {
			if (setSquareValue(position, value, previousPosition)) {
				if (filledSquares.size() == 81) {
					return true;
				}
				else if (tryToSolve(moveNumber+1)) {
					return true;
				}
			}
			int [] related = relatedPositions[position];
			for (int i = 0; i < 20; i++) {
				layout[related[i]] = previousPosition[i];
			}
		}
		// Couldn't find a winning path from here
		layout[position] = currentValue;
		filledSquares.remove(position);
		return false;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(100);
		for (int i = 0; i < 81; i++) {
			if (filledSquares.contains(i)) {
				builder.append(String.valueOf(layout[i]));
			}
			else {
				builder.append("-");
			}
			if (i % 9 == 8) {
				builder.append("\n");
			}
		}
		return builder.toString();
	}

	public static void main(String[] args) {
		Sudoku tester = new Sudoku();
		try {
			tester.readInitialPositionFromFile(args[0]);
			System.out.println("Initial position is:");
			System.out.println(tester);
			tester.setNumTries(0L);
			if (tester.tryToSolve(0)) {
				System.out.println("The solution is:");
				System.out.println(tester);
				System.out.println("Solution took " + tester.getNumTries() + " moves.");
			}
			else {
				System.out.println("No solution");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
