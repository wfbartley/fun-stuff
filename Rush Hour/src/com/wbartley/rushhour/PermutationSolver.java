package com.wbartley.rushhour;

import java.util.ArrayDeque;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class PermutationSolver implements LayoutPermuter.PermutationListener {
	public interface Notification {
		public void goodPuzzleFound(ParkingLotLayout layout, MoveList solution);
		public void nonTrivialPuzzleFound(ParkingLotLayout layout, MoveList solution);
		public void progressUpdate(int percentComplete);
	}
	private MoveList maxSolution = null;
	private Set<ParkingLotLayout> unsolvablePositions;
	private Notification notification;
	private PuzzleDifficulty desiredDifficulty;
	private int minNumberOfMoves;
	private boolean desiredDifficultyPuzzleFound = false;
	private ArrayBlockingQueue<SolverThreadRunnable> finishedSolverQueue;
	private int numThreads;
	private ArrayDeque<SolverThreadRunnable> availableRunnables;
	private ThreadPoolExecutor executor;
	private long startTime;
	private long numPositionsExamined = 0;
	private long numShortCircuited = 0;
	private boolean debug;
	private boolean pauseSearch = false;
	private boolean stopSearch = false;
	
	public PermutationSolver(int numThreads, Notification notification, int minNumberOfMoves, PuzzleDifficulty desiredDifficulty, boolean debug) {
		this.numThreads = numThreads;
		availableRunnables = new ArrayDeque<SolverThreadRunnable>(numThreads);
		for (int i = 0; i < numThreads; i++) {
			availableRunnables.add(new SolverThreadRunnable());
		}
		this.minNumberOfMoves = minNumberOfMoves;
		this.desiredDifficulty = desiredDifficulty;
		this.notification = notification;
		finishedSolverQueue = new ArrayBlockingQueue<SolverThreadRunnable>(numThreads+1);
		executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(numThreads);
		startTime = System.currentTimeMillis();
		this.debug = debug;
	}
	
	public boolean isDesiredDifficultyPuzzleFound() {
		return desiredDifficultyPuzzleFound;
	}
	
	public void exit() {
		executor.shutdown();
	}
	
	public long getRunDuration() {
		return System.currentTimeMillis() - startTime;
	}
	
	public long getNumPositionsExamined() {
		return numPositionsExamined;
	}

	public long getNumShortCircuited() {
		return numShortCircuited;
	}
		
	public void pauseSearch(boolean pause) {
		pauseSearch = pause;
	}
	
	public void stopSearch() {
		pauseSearch = false;
		stopSearch = true;
	}
	
	public void dumpStats() {
		System.out.println("Num positions examined = " + numPositionsExamined);
		System.out.println("Positions per second = " + numPositionsExamined * 1000 / getRunDuration());
	}
	
	public void resetUnsolvablePositions() {
		if (debug) {
			dumpStats();
		}
		unsolvablePositions.clear();
		startTime = System.currentTimeMillis();
		numPositionsExamined = 0;
		numShortCircuited = 0;
	}
	
	private boolean checkForMaxSolution(MoveList solution) {
		if (maxSolution == null || maxSolution.isShorterThan(solution)) {
			maxSolution = new MoveList(solution);
			return true;
		}
		return false;
	}
	
	private class SolverThreadRunnable implements Runnable {
		private Solver solver;
		
		public SolverThreadRunnable() {
			solver = new Solver();
		}
		
		public Solver getSolver() {
			return solver;
		}
		
		public void reset(ParkingLotLayout layout) {
			solver.reset(layout);
		}

		@Override
		public void run() {
			solver.solve();
			finishedSolverQueue.offer(this);
		}
		
	}
		
	public boolean processSolverResult(Solver solver) {
		MoveList solution = solver.getBestSolution();
		if (solution != null) {
			if (minNumberOfMoves != 0 && solution.getMoves().length >= minNumberOfMoves) {
				desiredDifficultyPuzzleFound = true;
				notification.goodPuzzleFound(new ParkingLotLayout(solver.getLayout()), new MoveList(solution));
				return true;
			}
			else if (desiredDifficulty == null) {
				if (checkForMaxSolution(solution)) {
					notification.goodPuzzleFound(new ParkingLotLayout(solver.getLayout()), new MoveList(solution));
				}
			}
			else {
				PuzzleDifficulty difficulty = solution.getPuzzleDifficulty();
				if (minNumberOfMoves == 0 && difficulty == desiredDifficulty) {
					desiredDifficultyPuzzleFound = true;
					notification.goodPuzzleFound(new ParkingLotLayout(solver.getLayout()), new MoveList(solution));
					return true;
				}
				else if (difficulty != PuzzleDifficulty.TRIVIAL) {
					notification.nonTrivialPuzzleFound(new ParkingLotLayout(solver.getLayout()), new MoveList(solution));
				}
			}
		}
		return stopSearch;
	}
	
	public boolean isInUnsolvables(ParkingLotLayout layout) {
		return unsolvablePositions.contains(layout);
	}
	
	@Override
	public boolean processLayout(ParkingLotLayout layout) {
		while (pauseSearch) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {}
		}
		numPositionsExamined++;
		if (debug && numPositionsExamined % 1000000 == 0) {
			dumpStats();
		}
		if (!availableRunnables.isEmpty()) {
			SolverThreadRunnable runnable = availableRunnables.pollLast();
			runnable.reset(layout);
			executor.execute(runnable);
		}
		else {
			SolverThreadRunnable solverThread = null;
			try {
				solverThread = finishedSolverQueue.take();
				availableRunnables.add(solverThread);
				Solver solver = solverThread.getSolver();
				
				if (processSolverResult(solver)) {
					// processSolverResult returns true when it finds the desired solution difficulty
					return false;
				}
			} catch (InterruptedException e) {}
		}
		return true;
	}
	
	public void emptyPendingThreads() {
		SolverThreadRunnable solverThread = null;
		try {
			while (availableRunnables.size() < numThreads) {
				solverThread = finishedSolverQueue.take();
				availableRunnables.add(solverThread);
				processSolverResult(solverThread.getSolver());
			}
		} catch (InterruptedException e) {}
	}
	
	@Override
	public void progressUpdate(int percentComplete) {
		notification.progressUpdate(percentComplete);
	}
			
}

