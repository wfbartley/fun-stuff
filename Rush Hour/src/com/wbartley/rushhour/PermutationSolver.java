package com.wbartley.rushhour;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.jar.Pack200.Packer;

public class PermutationSolver implements LayoutPermuter.PermutationListener {
	public interface Notification {
		public void goodPuzzleFound(ParkingLotLayout layout, MoveList solution);
		public void nonTrivialPuzzleFound(ParkingLotLayout layout, MoveList solution);
		public void progressUpdate(int percentComplete);
	}
	private MoveList maxSolution = null;
	private int unsolvablePositionsSize;
	private List<HashSet<ParkingLotLayout>> unsolvablePositions;
	private int curUnsolvableHashSet = 0;
	private Notification notification;
	private PuzzleDifficulty desiredDifficulty;
	private int minNumberOfMoves;
	private boolean desiredDifficultyPuzzleFound = false;
	private int numThreads;
	private int numActiveThreads = 0;
	private ArrayBlockingQueue<Solver> finishedSolverQueue;
	private ThreadPoolExecutor executor;
	private long startTime;
	private long numPositionsExamined = 0;
	private long numShortCircuited = 0;
	private long totalUnsolvedMaintenanceTime = 0;
	private boolean debug;
	
	public PermutationSolver(int numThreads, int initialUnsolvablePositionsSetSize, Notification notification, int minNumberOfMoves, PuzzleDifficulty desiredDifficulty, boolean debug) {
		this.numThreads = numThreads;
		this.minNumberOfMoves = minNumberOfMoves;
		this.desiredDifficulty = desiredDifficulty;
		this.notification = notification;
		this.unsolvablePositionsSize = initialUnsolvablePositionsSetSize;
		unsolvablePositions = new ArrayList<HashSet<ParkingLotLayout>>(4);
		for (int i = 0; i < 4; i++) {
			unsolvablePositions.add(new HashSet<ParkingLotLayout>(initialUnsolvablePositionsSetSize));
		}
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
	
	public void dumpStats() {
		System.out.println("Num positions examined = " + numPositionsExamined + ", short circuited = " + numShortCircuited);
		System.out.println("Positions per second = " + numPositionsExamined * 1000 / getRunDuration());
		System.out.println("Total time unsolved add = " + totalUnsolvedMaintenanceTime / 1000000 + "ms / " + getRunDuration() + "ms");
		int totalUnsolvable = 0;
		for (HashSet<ParkingLotLayout> up : unsolvablePositions) {
			totalUnsolvable += up.size();
		}
		System.out.println("Num unsolved in set = " + totalUnsolvable);
	}
	
	public void resetUnsolvablePositions() {
		if (debug) {
			dumpStats();
		}
		for (HashSet<ParkingLotLayout> us : unsolvablePositions) {
			us.clear();
		}
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
			HashSet<ParkingLotLayout> unsolvable = unsolvablePositions.get(curUnsolvableHashSet);
			unsolvable.addAll(solver.getPositionsExamined());
			if (unsolvable.size() * 100 / unsolvablePositionsSize > 90){
				curUnsolvableHashSet++;
				if (curUnsolvableHashSet == unsolvablePositions.size()) curUnsolvableHashSet = 0;
				unsolvablePositions.get(curUnsolvableHashSet).clear();
			}
			totalUnsolvedMaintenanceTime += System.nanoTime() - start;
		}
		return false;
	}
	
	public boolean isInUnsolvables(ParkingLotLayout layout) {
		int i = curUnsolvableHashSet;
		do {
			HashSet<ParkingLotLayout> unsolvable = unsolvablePositions.get(i);
			if (unsolvable.contains(layout)) return true;
			i--;
			if (i < 0) {
				i = unsolvablePositions.size() - 1;
			}
		} while (i != curUnsolvableHashSet);
		return false;
	}
	
	@Override
	public boolean processLayout(ParkingLotLayout layout) {
		numPositionsExamined++;
		if (debug && numPositionsExamined % 200000 == 0) {
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

