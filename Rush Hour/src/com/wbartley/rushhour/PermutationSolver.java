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
	private ArrayBlockingQueue<SolverThreadRunnable> finishedThreadQueue;
	private int numThreads;
	private ArrayDeque<SolverThreadRunnable> availableThreads;
	private ThreadPoolExecutor executor;
	private long startTime;
	private long numPositionsExamined = 0;
	private long numShortCircuited = 0;
	private boolean debug;
	private boolean pauseSearch = false;
	private boolean stopSearch = false;
	
	public PermutationSolver(int numThreads, Notification notification, int minNumberOfMoves, PuzzleDifficulty desiredDifficulty, boolean debug) {
		this.numThreads = numThreads;
		availableThreads = new ArrayDeque<SolverThreadRunnable>(numThreads);
		for (int i = 0; i < numThreads; i++) {
			availableThreads.add(new SolverThreadRunnable());
		}
		this.minNumberOfMoves = minNumberOfMoves;
		this.desiredDifficulty = desiredDifficulty;
		this.notification = notification;
		finishedThreadQueue = new ArrayBlockingQueue<SolverThreadRunnable>(numThreads);
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
		if (debug) {
			System.out.println("Num positions examined = " + numPositionsExamined);
			System.out.println("Positions per second = " + numPositionsExamined * 1000 / getRunDuration());
		}
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
		private int numRuns = 0;
		private long startTime, endTime = 0, totalIdleTime = 0, totalActiveTime = 0;
		
		public SolverThreadRunnable() {
			solver = new Solver();
		}
		
		public Solver getSolver() {
			return solver;
		}
		
		public void reset(ParkingLotLayout layout) {
			solver.reset(layout);
		}
		
		public int getNumRuns() {
			return numRuns;
		}
		
		public long getTotalIdleTime() {
			return totalIdleTime;
		}
		
		public long getTotalActiveTime() {
			return totalActiveTime;
		}
		
		public void resetEndTime() {
			endTime = 0;
		}

		@Override
		public void run() {
			numRuns++;
			startTime = System.nanoTime();
			if (endTime != 0) {
				totalIdleTime += startTime - endTime;
			}
			solver.solve();
			endTime = System.nanoTime();
			totalActiveTime += endTime - startTime;
			finishedThreadQueue.offer(this);
		}
		
	}
		
	public boolean processSolverResult(Solver solver) {
		MoveList solution = solver.getBestSolution();
		if (solution != null) {
			if (minNumberOfMoves != 0) {
				if (solution.getMoves().length >= minNumberOfMoves) {
					desiredDifficultyPuzzleFound = true;
					notification.goodPuzzleFound(new ParkingLotLayout(solver.getLayout()), new MoveList(solution));
					return true;
				}
			}
			else if (desiredDifficulty != null) {
				PuzzleDifficulty difficulty = solution.getPuzzleDifficulty();
				if (difficulty == desiredDifficulty) {
					desiredDifficultyPuzzleFound = true;
					notification.goodPuzzleFound(new ParkingLotLayout(solver.getLayout()), new MoveList(solution));
					return true;
				}
				else if (difficulty != PuzzleDifficulty.TRIVIAL) {
					notification.nonTrivialPuzzleFound(new ParkingLotLayout(solver.getLayout()), new MoveList(solution));
				}
			}
			else {
				if (checkForMaxSolution(solution)) {
					notification.goodPuzzleFound(new ParkingLotLayout(solver.getLayout()), new MoveList(solution));
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
		if (pauseSearch) {
			emptyPendingThreads();
			while (pauseSearch) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {}
			}
		}
		numPositionsExamined++;
		if (numPositionsExamined % 1000000 == 0) {
			dumpStats();
		}
		SolverThreadRunnable solverThread;
		if ((solverThread = availableThreads.pollLast()) != null) {
			solverThread.reset(layout);
			executor.execute(solverThread);
		}
		else {
			try {
				solverThread = finishedThreadQueue.take();
				Solver solver = solverThread.getSolver();		
				if (processSolverResult(solver)) {
					availableThreads.add(solverThread);
					return false;
				}
				solverThread.reset(layout);
				executor.execute(solverThread);
				while ((solverThread = finishedThreadQueue.poll()) != null) {
					availableThreads.add(solverThread);
					solver = solverThread.getSolver();
					if (processSolverResult(solver)) {
						return false;
					}
				}
			} catch (InterruptedException e) {}
		}
		return true;
	}
	
	private void dumpThreadStats() {
		int i = 0;
		for (SolverThreadRunnable runnable : availableThreads) {
			i++;
			System.out.println("Thread" + i + " numRuns = " + runnable.getNumRuns() + ", activeTime = " + runnable.getTotalActiveTime() + ", idleTime = " + runnable.getTotalIdleTime());
			runnable.resetEndTime();
		}
	}
	
	public void emptyPendingThreads() {
		SolverThreadRunnable solverThread = null;
		try {
			while (availableThreads.size() < numThreads) {
				solverThread = finishedThreadQueue.take();
				availableThreads.add(solverThread);
				processSolverResult(solverThread.getSolver());
			}
		} catch (InterruptedException e) {}
		dumpThreadStats();
	}
	
	@Override
	public void progressUpdate(int percentComplete) {
		notification.progressUpdate(percentComplete);
	}
			
}

