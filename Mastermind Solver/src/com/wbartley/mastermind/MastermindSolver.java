package com.wbartley.mastermind;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class MastermindSolver {
	public static String [] pegColors = new String [] {
		"BLACK",
		"WHITE",
		"BLUE",
		"YELLOW",
		"GREEN",
		"RED",
		"ORANGE",
		"BROWN"
	};
	public static int MAX_NUM_HOLES = 8;
	
	private static int HOLE_MASK = 0x7;
	private static int BITS_PER_HOLE = 3;
	
	private int numHoles;
	private int [] remainingCodes;
	private int numRemainingCodes;
	private int puzzleSolved;
	private int maximumScore;
	
	public MastermindSolver(int numHoles) throws Exception {
		if (numHoles <= 0 || numHoles > MAX_NUM_HOLES) throw new Exception("The maximum number of holes permitted is " + MAX_NUM_HOLES);
		this.numHoles = numHoles;
		puzzleSolved = numHoles * 10;
		maximumScore = (numHoles-1) * 10 + 1;
		reset();
	}
	
	private void reset() {
		numRemainingCodes = 1;
		for (int i = 0; i < numHoles; i++) {
			numRemainingCodes *= pegColors.length;
		}
		remainingCodes = new int [numRemainingCodes];
		for (int i = 0; i < numRemainingCodes; i++) {
			remainingCodes[i] = i;
		}
	}
	
	public int computeScore(int guess, int correctCode) {
		int result = 0;
		int numUnmatched = 0;
		final int [] unmatchedInGuess = new int [numHoles];
		final int [] unmatchedInCode = new int [numHoles];
		for (int i = 0; i < numHoles; i++) {
			int guessColor = guess & HOLE_MASK;
			int codeColor = correctCode & HOLE_MASK;
			if (guessColor == codeColor){
				result++;
			}
			else {
				unmatchedInGuess[numUnmatched] = guessColor;
				unmatchedInCode[numUnmatched] = codeColor;
				numUnmatched++;
			}
			guess >>= BITS_PER_HOLE;
			correctCode >>= BITS_PER_HOLE;
		}
		result *= 10;
		int remainingUnmatched = numUnmatched;
		for (int i = 0; i < numUnmatched; i++) {
			for (int j = 0; j < remainingUnmatched; j++) {
				if (unmatchedInGuess[i] == unmatchedInCode[j]) {
					result++;
					remainingUnmatched--;
					unmatchedInCode[j] = unmatchedInCode[remainingUnmatched];
					break;
				}
			}
		}
		return result;
	}
	
	public int generateInitialGuess() {
		int [] colorSet = new int[pegColors.length];
		for (int i = 0; i < pegColors.length; i++) {
			colorSet[i] = i;
		}
		int numColorsLeft = pegColors.length;
		int result = 0;
		for (int i = 0; i < numHoles; i++) {
			result <<= BITS_PER_HOLE;
			int randomColor = (int)(Math.random() * numColorsLeft);
			result += colorSet[randomColor];
			numColorsLeft--;
			colorSet[randomColor] = colorSet[numColorsLeft];
		}
		return result;
	}
	
	public boolean puzzleIsSolved(int score) {
		return score == puzzleSolved;
	}
	
	public int submitScoreForGuess(int guess, int score) {
		int i = 0;
		while (i < numRemainingCodes) {
			int scoreForCode = computeScore(guess, remainingCodes[i]);
			if (scoreForCode != score) {
				numRemainingCodes--;
				remainingCodes[i] = remainingCodes[numRemainingCodes];
			}
			else {
				i++;
			}
		}
		return i;
	}
	
	public String guessToString(int guess) {
		String [] result = new String [numHoles];
		for (int i = numHoles-1; i >= 0; i--) {
			result[i] = pegColors[guess & HOLE_MASK];
			guess >>= BITS_PER_HOLE;
		}
		return Arrays.toString(result);
	}
	
	public int makeMinimalSetGuess() {
		if (numRemainingCodes == 1) {
			return remainingCodes[0];
		}
		int currentBestGuess = -1;
		int currentBestWorstCase = Integer.MAX_VALUE;
		for (int i = 0; i < numRemainingCodes; i++) {
			int guess = remainingCodes[i];
			int [] numForEachScore = new int[maximumScore + 1];
			for (int j = 0; j < numRemainingCodes; j++) {
				if (i != j) {
					numForEachScore[computeScore(guess, remainingCodes[j])]++;
				}
			}
			int max = 0;
			for (int j = 0; j <= maximumScore; j++) {
				if (numForEachScore[j] > max) {
					max = numForEachScore[j];
				}
			}
			if (max < currentBestWorstCase) {
				currentBestWorstCase = max;
				currentBestGuess = guess;
			}
		}
		return currentBestGuess;
	}
	
	public int makeRandomGuess() {
		return remainingCodes[(int)(Math.random() * numRemainingCodes)];
	}
	
	private static void testGuessingMethods() {
		final int numRuns = 500;
		MastermindSolver solver;
		try {
			solver = new MastermindSolver(5);
		} catch (Exception e) { return; }
		int totalGuesses = 0;
		for (int i = 0; i < numRuns; i++) {
			int correctCode = solver.makeRandomGuess();
			boolean done = false;
			int numGuesses = 1;
			int guess = solver.generateInitialGuess();
			do {
				if (guess == correctCode) {
					done = true;
				}
				else {
					int score = solver.computeScore(guess, correctCode);
					solver.submitScoreForGuess(guess, score);
					guess = solver.makeRandomGuess();
					numGuesses++;
				}
			} while (!done);
			totalGuesses += numGuesses;
			solver.reset();
		}
		double averageNumRandomGuesses = (double)totalGuesses / numRuns;
		totalGuesses = 0;
		for (int i = 0; i < numRuns; i++) {
			int correctCode = solver.makeRandomGuess();
			boolean done = false;
			int numGuesses = 1;
			int guess = solver.generateInitialGuess();
			do {
				if (guess == correctCode) {
					done = true;
				}
				else {
					int score = solver.computeScore(guess, correctCode);
					solver.submitScoreForGuess(guess, score);
					guess = solver.makeMinimalSetGuess();
					numGuesses++;
				}
			} while (!done);
			totalGuesses += numGuesses;
			solver.reset();
		}
		double averageNumMinimalSetGuesses = (double)totalGuesses / numRuns;
		
		System.out.println("Average num random guesses = " + averageNumRandomGuesses);
		System.out.println("Average num minimal set guesses = " + averageNumMinimalSetGuesses);
	}
	
	public static void main(String [] args) {
		if (args.length != 1) {
			testGuessingMethods();
			return;
		}
		int numHoles = Integer.parseInt(args[0]);
		MastermindSolver solver;
		try {
			solver = new MastermindSolver(numHoles);
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
			return;
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Choose a " + numHoles + " peg code and press return when you are ready for me to begin guessing.");
		System.out.println("The available colors are, " + Arrays.toString(pegColors));
		try {
			reader.readLine();
		} catch (IOException e) {
			return;
		}
		int guess = solver.generateInitialGuess();
		System.out.println("I'm going to start guessing now.");
		System.out.println("Enter the score after each guess as a two-digit number.");
		System.out.println("The first digit is the number exactly right and the second");
		System.out.println("is the number correct but in the wrong place.");
		System.out.println("So, if I have 2 pegs exactly right and one right but in the wrong place, enter 21");
		System.out.println("If nothing is exactly right, you can just enter the number of right colored pegs in the wrong position,");
		System.out.println("even if that number is 0");
		System.out.println("When I guess right, just type enter without any score.");
		System.out.println("Okay! My first guess is, " + solver.guessToString(guess));
		boolean done = false;
		do {
			String scoreEntered;
			try {
				scoreEntered = reader.readLine();
			} catch (IOException e1) {
				return;
			}
			if (scoreEntered.isEmpty()) {
				break;
			}
			int score;
			try {
				score = Integer.parseInt(scoreEntered);
				if (score < 0) {
					System.out.println("Score should not be entered as a negative number.");
					continue;
				}
				int numExact = score / 10;
				int numCorrect = score % 10;
				if (numExact + numCorrect > numHoles) {
					System.out.println("The number of exact right plus the number correct must be less than the number of holes.");
					continue;
				}
			} catch (Exception e) {
				System.out.println("See above instructions for entering score");
				continue;
			}
			int numRemaining = solver.submitScoreForGuess(guess, score);
			if (numRemaining == 0) {
				System.out.println("You can't cheat me! One of your input scores is incorrect. Try me again sometime.");
				return;
			}
			if (numRemaining == 1) {
				System.out.println("There is only one possible solution left!");
			}
			else {
				System.out.println("There are " + numRemaining + " possible solutions remaining.");
			}
			guess = solver.makeMinimalSetGuess();
			System.out.println("My next guess is, " + solver.guessToString(guess));
		} while (!done);
		System.out.println("Hooray for me!");
	}
	
}
