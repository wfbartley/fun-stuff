package com.wbartley.rushhour;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
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
	private int numThreads;
	private int numActiveThreads = 0;
	private ArrayBlockingQueue<Solver> finishedSolverQueue;
	private ThreadPoolExecutor executor;
	private long startTime;
	private int highWaterMark;
	private long numPositionsExamined = 0;
	private long numShortCircuited = 0;
	private long totalUnsolvedMaintenanceTime = 0;
	private boolean debug;
	private boolean pauseSearch = false;
	private boolean stopSearch = false;
	
	public PermutationSolver(int numThreads, int initialUnsolvablePositionsSetSize, Notification notification, int minNumberOfMoves, PuzzleDifficulty desiredDifficulty, boolean debug) {
		this.numThreads = numThreads;
		this.minNumberOfMoves = minNumberOfMoves;
		this.desiredDifficulty = desiredDifficulty;
		this.notification = notification;
		unsolvablePositions = Collections.newSetFromMap(new WeakHashMap<ParkingLotLayout, Boolean>(initialUnsolvablePositionsSetSize));
		highWaterMark = 9 * initialUnsolvablePositionsSetSize / 10;
		finishedSolverQueue = new ArrayBlockingQueue<Solver>(numThreads+1);
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
		System.out.println("Num positions examined = " + numPositionsExamined + ", short circuited = " + numShortCircuited);
		System.out.println("Positions per second = " + numPositionsExamined * 1000 / getRunDuration());
		System.out.println("Total time unsolved add = " + totalUnsolvedMaintenanceTime / 1000000 + "ms / " + getRunDuration() + "ms");
		System.out.println("Num unsolved in set = " + unsolvablePositions.size());
	}
	
	public void resetUnsolvablePositions() {
		if (debug) {
			dumpStats();
		}
		unsolvablePositions.clear();
		startTime = System.currentTimeMillis();
		numPositionsExamined = 0;
		numShortCircuited = 0;
		totalUnsolvedMaintenanceTime = 0;
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
		
		public SolverThreadRunnable(ParkingLotLayout layout) {
			solver = new Solver(layout);
		}
		
		public SolverThreadRunnable(ParkingLotLayout layout, Solver solver) {
			this.solver = solver;
			solver.reset(layout);
		}

		@Override
		public void run() {
			solver.solve();
			finishedSolverQueue.offer(solver);
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
		else {
			long start = System.nanoTime();
			if (unsolvablePositions.size() > highWaterMark) {
				unsolvablePositions.clear();
			}
			unsolvablePositions.addAll(solver.getPositionsExamined());
			totalUnsolvedMaintenanceTime += System.nanoTime() - start;
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
		if (isInUnsolvables(layout)) {
			numShortCircuited++;
			return true;
		}
		if (numActiveThreads < numThreads) {
			numActiveThreads++;
			executor.execute(new SolverThreadRunnable(layout));
		}
		else {
			Solver solver = null;
			try {
				solver = finishedSolverQueue.take();
				if (processSolverResult(solver)) {
					// processSolverResult returns true when it finds the desired solution difficulty
					return false;
				}
			} catch (InterruptedException e) {}
			executor.execute(new SolverThreadRunnable(layout, solver));
		}
		return true;
	}
	
	@Override
	public void progressUpdate(int percentComplete) {
		notification.progressUpdate(percentComplete);
	}
			
}

