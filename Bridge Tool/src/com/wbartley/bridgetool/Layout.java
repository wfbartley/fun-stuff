package com.wbartley.bridgetool;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.wbartley.bridgetool.dds.DdTableDeal;
import com.wbartley.bridgetool.dds.DdTableResults;
import com.wbartley.bridgetool.dds.DdsDll;
import com.wbartley.bridgetool.handconstraint.HandConstraint;

public class Layout {
	private Hand [] hands = new Hand[HandDirection.values().length];
	private Hand [] initialLayout = new Hand[HandDirection.values().length];
	private Deck deck;
	private HandConstraint handConstraint;
	private static SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy.MM.dd");
	private DdTableResults ddResults = null;
	private long numAttempts = 0;
	private long numSuccesses = 0;
	
	public Layout(Deck deck, HandConstraint handConstraint, String specificCards) {
		this.deck = deck;
		this.handConstraint = handConstraint;
		initializeLayout(specificCards);
	}
	
	public void initializeLayout(String specificCards) {
		// First add the specific cards called out in constraints
		for (int i = 0; i < HandDirection.values().length; i++) {
			initialLayout[i] = new Hand();
		}
		if (specificCards != null && !specificCards.isEmpty()) {
			String [] directions = specificCards.split(",");
			for (String dir : directions) {
				String [] parts = dir.split(":");
				if (parts.length != 2) continue;
				HandDirection direction = HandDirection.fromAbbreviation(parts[0]);
				if (direction == null) continue;
				initialLayout[direction.ordinal()].addCards(deck.removeSpecificCards(parts[1]));
			}
		}
		deck.mark();
	}
	
	public void generate() {
		boolean done;
		do {
			numAttempts++;
			for (int i = 0; i < HandDirection.values().length; i++) {
				hands[i] = new Hand(initialLayout[i]);
				int numCardsNeeded = Hand.NUM_CARDS_IN_HAND - hands[i].getNumCards();
				for (int j = 0; j < numCardsNeeded; j++) {
					hands[i].addCard(deck.dealCard());
				}
			}
			ddResults = null;
			if (handConstraint != null) {
				done = handConstraint.meetsConstraint(this);
			}
			else {
				done = true;
			}
			deck.returnToMark();
		} while (!done);
		numSuccesses++;
	}
	
	private void writeNsHand(HandDirection hand, String nsLeadingSpace, StringBuilder builder) {
		for (int i = Suit.values().length - 1; i >= 0; i--) {
			builder.append(nsLeadingSpace);
			hands[hand.ordinal()].outputSuitToStringBuilder(Suit.values()[i], builder);
			builder.append('\n');
		}
	}
	
	public Hand [] getHands() {
		return hands;
	}
	
	public DdTableResults getDdResults() {
		if (ddResults == null) {
			DdsDll ddsDll = DdsDll.INSTANCE;
			DdTableDeal deal = new DdTableDeal();
			DdTableDeal.ByValue tableDeal = deal.new ByValue();
			DdTableResults res = new DdTableResults();
			DdTableResults.ByReference results = res.new ByReference();
			tableDeal.setCards(this);
			ddsDll.CalcDDtable(tableDeal, results);
			ddResults = results;
		}
		return ddResults;
	}
	
	public String toPbn(int boardNumber, HandDirection dealer, Vulnerability vulnerability) {
		/* Here is an example of what it looks like
		[Event "Computer generated hands"]
		[Site "Inside my computer"]
		[Date "2018.06.14"]
		[Board "1"]
		[West "Nobody"]
		[North "Nobody"]
		[East "Nobody"]
		[South "Nobody"]
		[Dealer "N"]
		[Vulnerable "None"]
		[Deal "N:.63.AKQ987.A9732 A8654.KQ5.T.QJT6 J973.J98742.3.K4 KQT2.AT.J6542.85"]
		[Scoring "Any"]
		[Declarer ""]
		[Contract "None"]
		[Result ""] */
		StringBuilder builder = new StringBuilder();
		builder.append("[Event \"Computer generated hands\"]\n");
		builder.append("[Site \"Inside my computer\"]\n");
		builder.append(String.format("[Date \"%s\"]\n", dateFmt.format(new Date())));
		builder.append(String.format("[Board \"%d\"]\n", boardNumber));
		builder.append("[West \"Nobody\"]\n");
		builder.append("[North \"Nobody\"]\n");
		builder.append("[East \"Nobody\"]\n");
		builder.append("[South \"Nobody\"]\n");
		builder.append("[Dealer \"" + dealer.getAbbreviation() + "\"]\n");
		builder.append("[Vulnerable \"" + vulnerability + "\"]\n");
		builder.append("[Deal \"" + dealer.getAbbreviation() + ":");
		int handIdx = dealer.ordinal();
		for (int i = 0; i < hands.length; i++) {
			builder.append(hands[handIdx].toPbn());
			handIdx++;
			if (handIdx >= hands.length) handIdx = 0;
			if (i != hands.length - 1) {
				builder.append(" ");
			}
		}
		builder.append("\"]\n");
		builder.append("[Scoring \"Any\"]\n");
		builder.append("[Declarer \"\"]\n");
		builder.append("[Contract \"None\"]\n");
		builder.append("[Result \"\"]\n");
		return builder.toString();
	}
	
	public String toLin(int handIndex, int boardNumber, HandDirection dealer, Vulnerability vulnerability) {
		StringBuilder builder = new StringBuilder();
		/*
		 * Here's an example of the output:
		 * qx|o1|md|2SQT7HKQJDK32CA752,SJ642HA5DQ98CK963,SAK985H842D7654CJ|rh||ah|Board 1|sv|N|pg||
		 */
		int linDealerId = ((dealer.ordinal() + 2) % HandDirection.values().length) + 1;
		builder.append("qx|o" + boardNumber + "|md|" + linDealerId);
		builder.append(hands[HandDirection.SOUTH.ordinal()].toLin() + ",");
		builder.append(hands[HandDirection.WEST.ordinal()].toLin() + ",");
		builder.append(hands[HandDirection.NORTH.ordinal()].toLin() + "|rh||ah|Board " + boardNumber + "|sv|" + vulnerability.getLinValue() + "|pg||");
		return builder.toString();
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		int [] northDistribution = hands[HandDirection.NORTH.ordinal()].getDistribution();
		int [] southDistribution = hands[HandDirection.SOUTH.ordinal()].getDistribution();
		int [] eastDistribution = hands[HandDirection.EAST.ordinal()].getDistribution();
		int [] westDistribution = hands[HandDirection.WEST.ordinal()].getDistribution();
		int longestSuitInEast = 0;
		int longestSuitInWest = 0;
		int longestSuitInNs = 0;
		for (int i = 0; i < Suit.values().length; i++) {
			if (eastDistribution[i] > longestSuitInEast) {
				longestSuitInEast = eastDistribution[i];
			}
			if (westDistribution[i] > longestSuitInWest) {
				longestSuitInWest = westDistribution[i];
			}
			if (northDistribution[i] > longestSuitInNs) {
				longestSuitInNs = northDistribution[i];
			}
			if (southDistribution[i] > longestSuitInNs) {
				longestSuitInNs = southDistribution[i];
			}
		}
		int ewSum = longestSuitInEast + longestSuitInWest + 4;
		if (ewSum < longestSuitInNs + 4) {
			ewSum = longestSuitInNs + 4;
		}
		int minEwSpace = ewSum - longestSuitInEast - longestSuitInWest;
		String nsLeadingSpace = "";
		for (int i = 0; i < (ewSum - longestSuitInNs)/2; i++) {
			nsLeadingSpace += " ";
		}
		
		// Output the North hand
		writeNsHand(HandDirection.NORTH, nsLeadingSpace, builder);
		
		// Output the West and East hands
		for (int i = Suit.SPADES.ordinal(); i >= Suit.CLUBS.ordinal(); i--) {
			hands[HandDirection.WEST.ordinal()].outputSuitToStringBuilder(Suit.values()[i], builder);
			int westFieldWidth = westDistribution[i];
			if (westFieldWidth == 0) {
				westFieldWidth = 1;
			}
			for (int j = 0; j < minEwSpace + longestSuitInWest - westFieldWidth; j++) {
				builder.append(' ');
			}
			hands[HandDirection.EAST.ordinal()].outputSuitToStringBuilder(Suit.values()[i], builder);
			builder.append('\n');
		}

		writeNsHand(HandDirection.SOUTH, nsLeadingSpace, builder);

		return builder.toString();
	}
	
	public long getNumAttempts() {
		return numAttempts;
	}
	
	public long getNumSuccesses() {
		return numSuccesses;
	}
}
