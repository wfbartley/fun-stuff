package com.bill.onitama.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import com.bill.onitama.engine.Piece.Color;

public class Layout {
	private Piece [][] board = new Piece[5][5];
	private List<Piece> redPieces;
	private List<Piece> bluePieces;
	private Color colorOnMove;
	private Card [] redCards = new Card [2];
	private Piece redMaster, blueMaster;
	private Card [] blueCards = new Card [2];
	private Card commonCard;
	private int curMoveNum = 0;
	
	@SuppressWarnings("serial")
	private TreeMap<Integer, Integer> beginnerMap = new TreeMap<Integer, Integer>() {
		{
			put(0, 4);
		}
	};
	
	@SuppressWarnings("serial")
	private TreeMap<Integer, Integer> intermediateMap = new TreeMap<Integer, Integer>() {
		{
			put(0, 6);
		}
	};
	
	@SuppressWarnings("serial")
	private TreeMap<Integer, Integer> advancedMap = new TreeMap<Integer, Integer>() {
		{
			put(0, 10);
			put(4, 8);
			put(8, 6);
		}
	};
	
	private int [] bestScore = new int[20];
	
	
	public Layout(){
		shuffleAndDeal();
		setupDefaultInitialPosition();
	}
	
	public Layout(List<Piece> redPieces, List<Piece> bluePieces, Card [] redCards, Card [] blueCards, Card commonCard, Color colorOnMove) throws OnitamaException {
		
		validateCardSet(redCards, blueCards, commonCard);
		validatePiecePositions(redPieces, bluePieces);
		if (colorOnMove == null){
			throw new OnitamaException(OnitamaException.ExceptionType.InvalidInitialLayout);
		}
		
		this.colorOnMove = colorOnMove;
	}
	
	private Layout(Card [] redCards, Card [] blueCards, Card commonCard) throws OnitamaException {
		validateCardSet(redCards, blueCards, commonCard);
		setupDefaultInitialPosition();
	}
	
	private void validatePiecePositions(List<Piece> redPieces, List<Piece> bluePieces) throws OnitamaException {
		if (redPieces == null || bluePieces == null || 
			redPieces.isEmpty() || bluePieces.isEmpty() || 
			redPieces.size() > 5 || bluePieces.size() > 5){
			throw new OnitamaException(OnitamaException.ExceptionType.InvalidInitialLayout);
		}
		for (Piece piece : redPieces){
			if (piece.getColor() != Color.RED){
				throw new OnitamaException(OnitamaException.ExceptionType.InvalidInitialLayout);
			}
			if (piece.getRow() == 2 && piece.getCol() == 0){
				throw new OnitamaException(OnitamaException.ExceptionType.InvalidInitialLayout);
			}
			if (piece.isMaster()){
				redMaster = piece;
			}
			board[piece.getRow()][piece.getCol()] = piece;
		}
		for (Piece piece : bluePieces){
			if (piece.getColor() != Color.BLUE){
				throw new OnitamaException(OnitamaException.ExceptionType.InvalidInitialLayout);
			}
			if (piece.getRow() == 2 && piece.getCol() == 4){
				throw new OnitamaException(OnitamaException.ExceptionType.InvalidInitialLayout);
			}
			if (piece.isMaster()){
				blueMaster = piece;
			}
			board[piece.getRow()][piece.getCol()] = piece;
		}
		if (redMaster == null || blueMaster == null){
			throw new OnitamaException(OnitamaException.ExceptionType.InvalidInitialLayout);
		}
		this.redPieces = redPieces;
		this.bluePieces = bluePieces;
	}
	
	private void validateCardSet(Card [] redCards, Card [] blueCards, Card commonCard) throws OnitamaException {
		if (redCards.length != 2 || redCards[0] == null || redCards[1] == null ||
			blueCards.length != 2 || blueCards[0] == null || blueCards[1] == null || commonCard == null){
			throw new OnitamaException(OnitamaException.ExceptionType.InvalidInitialLayout);
		}
		this.blueCards = blueCards;
		this.redCards = redCards;
		this.commonCard = commonCard;
	}
	
	private void shuffleAndDeal(){
		int numCardsLeftInDeck = Card.values().length;
		int [] deck = new int [numCardsLeftInDeck];
		for (int i = 0; i < numCardsLeftInDeck; i++){
			deck[i] = i;
		}
		Card [] deal = new Card[5];
		for (int curCard = 0; curCard < 5; curCard++){
			int idx = (int)(Math.random() * numCardsLeftInDeck);
			deal[curCard] = Card.values()[deck[idx]];
			numCardsLeftInDeck--;
			deck[idx] = deck[numCardsLeftInDeck];
		}
		redCards[0] = deal[0];
		redCards[1] = deal[1];
		blueCards[0] = deal[2];
		blueCards[1] = deal[3];
		commonCard = deal[4];
	}
	
	private void setupDefaultInitialPosition(){
		colorOnMove = Color.RED;
		redPieces = new ArrayList<Piece>(5);
		bluePieces = new ArrayList<Piece>(5);
		for (int i = 0; i < 5; i++){
			Piece redPiece = new Piece(4, i, Color.RED, i == 2);
			if (redPiece.isMaster()){
				redMaster = redPiece;
			}
			board[4][i] = redPiece;
			redPieces.add(redPiece);
			Piece bluePiece = new Piece(0, i, Color.BLUE, i == 2);
			if (bluePiece.isMaster()){
				blueMaster = bluePiece;
			}
			board[0][i] = bluePiece;
			bluePieces.add(bluePiece);
		}
	}
	
	private void addMoveToListIfLegal(Piece piece, int cardIdx, Manuever manuever, List<Move> moveList){
		int row = piece.getRow();
		int col = piece.getCol();
		Card cardPlayed;
		if (piece.getColor() == Color.RED){
			row += manuever.getRowOffset();
			col += manuever.getColOffset();
			cardPlayed = redCards[cardIdx];
		}
		else{
			row -= manuever.getRowOffset();
			col -= manuever.getColOffset();
			cardPlayed = blueCards[cardIdx];
		}
		if (row > 4 || row < 0 || col > 4 || col < 0) return;
		Piece onSquare = board[row][col];
		if (onSquare != null && onSquare.getColor() == piece.getColor()) return;
		moveList.add(new Move(piece, cardPlayed, cardIdx, manuever, onSquare));
	}
	
	private List<Move> getPossibleMoves(){
		List<Move> result = new ArrayList<Move>();
		List<Piece> pieces;
		Card [] cards;
		if (colorOnMove == Color.RED){
			pieces = redPieces;
			cards = redCards;
		}
		else{
			pieces = bluePieces;
			cards = blueCards;
		}
		for (Piece piece : pieces){
			for (int i = 0; i < 2; i++){
				for (Manuever manuever : cards[i].getManuevers()){
					addMoveToListIfLegal(piece, i, manuever, result);
				}
			}
		}
		if (result.isEmpty()){
			// This case happens when there are no legal moves. The player must still exchange one of his cards for the common card
			if (colorOnMove == Color.RED){
				result.add(new Move(null, redCards[0], 0, null, null));
				result.add(new Move(null, redCards[1], 1, null, null));
			}
			else{
				result.add(new Move(null, blueCards[0], 2, null, null));
				result.add(new Move(null, blueCards[1], 3, null, null));
			}
		}
		return result;
	}
	
	private void toggleOnMove(){
		if (colorOnMove == Color.RED){
			colorOnMove = Color.BLUE;
		}
		else{
			colorOnMove = Color.RED;
		}
	}
	
	private void swapCard(Card [] playerCards, int cardIdx){
		Card played = playerCards[cardIdx];
		playerCards[cardIdx] = commonCard;
		commonCard = played;
	}
	
	public void makeMove(Move move){
		curMoveNum++;
		toggleOnMove();
		Piece piece = move.getPiece();
		int cardIdx = move.getCardIdx();
		Card [] playerCards = redCards;
		if (piece == null){
			if (cardIdx >= 2){
				cardIdx -= 2;
				playerCards = blueCards;
			}
		}
		else{
			if (piece.getColor() == Color.BLUE){
				playerCards = blueCards;
			}
		}
		swapCard(playerCards, cardIdx);
		if (piece == null) return;
		
		board[piece.getRow()][piece.getCol()] = null;
		piece.doManuever(move.getManuever());
		board[piece.getRow()][piece.getCol()] = piece;
		
		Piece captured = move.getCapturedPiece();
		if (captured != null){
			if (piece.getColor() == Color.RED){
				bluePieces.remove(move.getCapturedPiece());
			}
			else{
				redPieces.remove(move.getCapturedPiece());
			}
		}
	}
	
	public void unmakeMove(Move move){
		curMoveNum--;
		toggleOnMove();
		Piece piece = move.getPiece();
		int cardIdx = move.getCardIdx();
		Card [] playerCards = redCards;
		if (piece == null){
			if (cardIdx >= 2){
				cardIdx -= 2;
				playerCards = blueCards;
			}
		}
		else{
			if (piece.getColor() == Color.BLUE){
				playerCards = blueCards;
			}
		}
		swapCard(playerCards, cardIdx);
		if (piece == null) return;
		
		Manuever manuever = move.getManuever();
		Piece captured = move.getCapturedPiece();
		board[piece.getRow()][piece.getCol()] = captured;
		piece.undoManuever(manuever);
		board[piece.getRow()][piece.getCol()] = piece;
		if (captured != null){
			if (piece.getColor() == Color.RED){
				bluePieces.add(captured);
			}
			else{
				redPieces.add(captured);
			}
		}
	}
	
	private int score(int curDepth, int searchDepth){
		List<Move> possibleMoves = getPossibleMoves();
		if (colorOnMove == Color.RED){
			for (Move possibleMove : possibleMoves){
				Piece capturedPiece = possibleMove.getCapturedPiece();
				if (capturedPiece != null && capturedPiece.isMaster()){
					return 10000-curDepth;
				}
				Manuever manuever = possibleMove.getManuever();
				Piece piece = possibleMove.getPiece();
				if (piece != null && piece.isMaster() && piece.getRow() + manuever.getRowOffset() == 0 && piece.getCol() + manuever.getColOffset() == 2){
					return 10000-curDepth;
				}
			}
		}
		else{
			for (Move possibleMove : possibleMoves){
				Piece capturedPiece = possibleMove.getCapturedPiece();
				if (capturedPiece != null && capturedPiece.isMaster()){
					return -10000+curDepth;
				}
				Manuever manuever = possibleMove.getManuever();
				Piece piece = possibleMove.getPiece();
				if (piece != null && piece.isMaster() && piece.getRow() + manuever.getRowOffset() == 4 && piece.getCol() + manuever.getColOffset() == 2){
					return -10000+curDepth;
				}
			}
		}
		if (curDepth == searchDepth){
			int redSum = 0;
			for (Piece piece : redPieces){
				int advancement = 4 - piece.getRow();
				if (piece.isMaster()){
					redSum += advancement;
				}
				else{
					redSum += 2 * advancement;
				}
			}
			int blueSum = 0;
			for (Piece piece : bluePieces){
				if (piece.isMaster()){
					blueSum += piece.getRow();
				}
				else{
					blueSum += 2 * piece.getRow();
				}
			}
			return 100 * (redPieces.size() - bluePieces.size()) + (redSum - blueSum);
		}
		if (colorOnMove == Color.RED){
			bestScore[curDepth] = -10000;
			for (Move move : possibleMoves){
				makeMove(move);
				int moveScore = score(curDepth+1, searchDepth);
				unmakeMove(move);
				if (moveScore > bestScore[curDepth]){
					bestScore[curDepth] = moveScore;
					if (bestScore[curDepth] > bestScore[curDepth-1]) break;
				}
			}
		}
		else {
			bestScore[curDepth] = 10000;
			for (Move move : possibleMoves){
				makeMove(move);
				int moveScore = score(curDepth+1, searchDepth);
				unmakeMove(move);
				if (moveScore < bestScore[curDepth]){
					bestScore[curDepth] = moveScore;
					if (bestScore[curDepth] < bestScore[curDepth-1]) break;
				}
			}
		}
		return bestScore[curDepth];
	}
	
	
	public Move findBestMove(){
		List<Move> possibleMoves = getPossibleMoves();
		int searchDepth = advancedMap.floorEntry(possibleMoves.size()).getValue();
		Move bestMove = null;
		if (colorOnMove == Color.RED){
			bestScore[0] = -9999;
			for (Move move : possibleMoves){
				makeMove(move);
				int moveScore = score(1, searchDepth);
				unmakeMove(move);
				if (moveScore > bestScore[0]){
					bestScore[0] = moveScore;
					bestMove = move;
				}
			}
		}
		else {
			bestScore[0] = 9999;
			for (Move move : possibleMoves){
				makeMove(move);
				int moveScore = score(1, searchDepth);
				unmakeMove(move);
				if (moveScore < bestScore[0]){
					bestScore[0] = moveScore;
					bestMove = move;
				}
			}
		}
		if (Math.abs(bestScore[0]) == 9999){
			return null;
		}
		return bestMove;
	}
	
	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append(blueCards[0] + " " + blueCards[1] + "\n");
		for (int row = 0; row < 5; row++){
			for (int col = 0; col < 5; col++){
				Piece piece = board[row][col];
				if (piece == null){
					builder.append("--");
				}
				else{
					builder.append(piece);
				}
				builder.append(" ");
			}
			if (row == 2){
				builder.append(commonCard);
			}
			builder.append("\n");
		}
		builder.append(redCards[0] + " " + redCards[1] + "\n");
		return builder.toString();
	}
	
	public Color getColorOnMove(){
		return colorOnMove;
	}
	
	public int getCurMoveNum(){
		return curMoveNum;
	}
	
	public List<Piece> getRedPieces(){
		return redPieces;
	}
	
	public List<Piece> getBluePieces(){
		return bluePieces;
	}
	
	public Card [] getRedCards(){
		return redCards;
	}
	
	public Card [] getBlueCards(){
		return blueCards;
	}
	
	public Card getCommonCard(){
		return commonCard;
	}
	
	public static void main(String [] args){
		Layout layout = new Layout();
		System.out.println(layout);
		System.out.println();
		Move move;
		do {
			move = layout.findBestMove();
			if (move != null){
				layout.makeMove(move);
				System.out.println(layout);
				System.out.println();
			}
		} while (move != null);
		if (layout.getColorOnMove() == Color.BLUE){
			System.out.print("RED wins after ");
		}
		else{
			System.out.print("BLUE wins after ");
		}
		System.out.print(layout.getCurMoveNum());
		System.out.println(" moves");
	}
}
