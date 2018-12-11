package com.wbartley.bridgetool;

import java.util.HashMap;
import java.util.Map;

public class Hand {
	public static final int NUM_CARDS_IN_EACH_SUIT = 13;
	public static final int NUM_CARDS_IN_HAND = 13;
	private static final long HONOR_CARD_MASK = 0xFL << 9 | 0xFL << 22 | 0xFL << 35 | 0xFL << 48;
	private static final int [] SUIT_SHIFT = new int [] { 0, 13, 26, 39 };
	private static final long [] SUIT_MASKS = new long [] {0x1FFFL, 0x1FFFL << 13, 0x1FFFL << 26, 0x1FFFL << 39};
	private static final Map<Long, Integer> POINT_MAP = new HashMap<Long, Integer>() ;
	private static final int [] SUIT_POINT_COUNT = new int [] {0, 1, 2, 3, 3, 4, 5, 6, 4, 5, 6, 7, 7, 8, 9, 10};
	private static final char [] CARD_SYMBOL = new char [] {'A', 'K', 'Q', 'J', 'T', '9', '8', '7', '6', '5', '4', '3', '2'};
	private static final long DEFENSIVE_TRICK_MASK = 0x7L;
	private static final long DEFENSIVE_TRICK_SHIFT = 10;
	private static final int [] DEFENSIVE_TRICKS_BY_MASK = new int [] { 0, 0, 1, 2, 2, 3, 4, 4 };
	
	static {
		for (int clubMask = 0; clubMask < 0x10; clubMask++) {
			int clubPointCount = SUIT_POINT_COUNT[clubMask];
			for (int diamondMask = 0; diamondMask < 0x10; diamondMask++) {
				int diamondPointCount = SUIT_POINT_COUNT[diamondMask];
				for (int heartMask = 0; heartMask < 0x10; heartMask++) {
					int heartPointCount = SUIT_POINT_COUNT[heartMask];
					for (int spadeMask = 0; spadeMask < 0x10; spadeMask++) {
						int spadePointCount = SUIT_POINT_COUNT[spadeMask];
						long honorCardMask = (long)clubMask << 9 | (long)(diamondMask) << 22 | (long)(heartMask) << 35 | (long)(spadeMask) << 48;
						POINT_MAP.put(honorCardMask, clubPointCount + diamondPointCount + heartPointCount + spadePointCount);
					}
				}
			}
		}
	}
	
	private long cards = 0;
	private int numCards = 0;
	
	public Hand() {
		
	}
	
	public Hand(long cards) {
		this.cards = cards;
		long mask = 1;
		for (int i = 0; i < 52 && numCards < 13; i++) {
			if ((cards & mask) != 0) {
				numCards++;
			}
			mask <<=1;
		}
	}
	
	public Hand(Hand hand) {
		cards = hand.cards;
		numCards = hand.numCards;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(100);
		long curBit = 1L << (Suit.values().length * NUM_CARDS_IN_EACH_SUIT - 1);
		for (int i = 0; i < Suit.values().length; i++) {
			boolean foundCard = false;
			for (int j = 0; j < NUM_CARDS_IN_EACH_SUIT; j++) {
				if ((cards & curBit) != 0) {
					foundCard = true;
					builder.append(CARD_SYMBOL[j]);
				}
				curBit >>= 1;
			}
			if (!foundCard) {
				builder.append('-');
			}
			builder.append('\n');
		}
		return builder.toString();
	}
	
	public void outputSuitToStringBuilder(Suit suit, StringBuilder builder) {
		long curBit = 1L << ((suit.ordinal()+1) * NUM_CARDS_IN_EACH_SUIT -1);
		boolean foundCard = false;
		for (int i = 0; i < NUM_CARDS_IN_EACH_SUIT; i++) {
			if ((cards & curBit) != 0) {
				foundCard = true;
				builder.append(CARD_SYMBOL[i]);
			}
			curBit >>= 1;
		}
		if (!foundCard) {
			builder.append('-');
		}
	}
	
	public int getNumberOfTopHonors(Suit suit, int topHowMany) {
		int result = 0;
		long mask = 1L << (NUM_CARDS_IN_EACH_SUIT * (suit.ordinal() + 1) - 1);
		for (int i = 0; i < topHowMany; i++) {
			if ((cards & mask) != 0) result++;
			mask >>= 1;
		}
		return result;
	}
	
	public long getCards() {
		return cards;
	}
	
	public int getSuitNormalized(Suit suit, int lowestBitNumber) {
		int idx = suit.ordinal();
		int shiftAmount = SUIT_SHIFT[idx] - lowestBitNumber;
		if (shiftAmount > 0) {
			return (int)((cards & SUIT_MASKS[idx]) >> shiftAmount);
		}
		else {
			return (int)((cards & SUIT_MASKS[idx]) << -shiftAmount);
		}
	}
	
	public int getHCP() {
		long honorCards = cards & HONOR_CARD_MASK;
		return POINT_MAP.get(honorCards);
	}
	
	public int getHCP(Suit suit) {
		long honorCards = cards & SUIT_MASKS[suit.ordinal()] & HONOR_CARD_MASK;
		return POINT_MAP.get(honorCards);
	}
	
	public int getTotalPoints() {
		int result = getHCP();
		int [] dist = getDistribution();
		for (int i = 0; i < Suit.values().length; i++) {
			if (dist[i] < 3) {
				result += 3 - dist[i];
			}
		}
		return result;
	}
	
	public int getDefensiveTrickCount() {
		int result = 0;
		long cardsCopy = cards;
		for (int i = 0; i < 4; i++) {
			cardsCopy >>= DEFENSIVE_TRICK_SHIFT;
			result += DEFENSIVE_TRICKS_BY_MASK[(int)(cardsCopy & DEFENSIVE_TRICK_MASK)];
		}
		return result;
	}
	
	public int [] getDistribution() {
		int [] result = new int[Suit.values().length];
		long mask = 1;
		for (int i = 0; i < Suit.values().length; i++) {
			for (int j = 0; j < NUM_CARDS_IN_EACH_SUIT; j++) {
				if ((cards & mask) != 0) {
					result[i]++;
				}
				mask <<= 1;
			}
		}
		return result;
	}
		
	public void addCard(long card) {
		cards |= card;
		numCards++;
	}
	
	public void addCards(long [] cards) {
		for (long card : cards) {
			if (card != 0) {
				addCard(card);
			}
		}
	}
	
	public void removeCard(long card) {
		cards &= ~card;
		numCards--;
	}
	
	public int getNumCards() {
		return numCards;
	}
	
	public String toPbn() {
		StringBuilder builder = new StringBuilder(16);
		long cardMask = 1L << (Deck.NUM_CARDS_IN_DECK - 1);
		for (int i = 0; i < Deck.NUM_CARDS_IN_DECK; i++) {
			int posInSuit = i % Hand.NUM_CARDS_IN_EACH_SUIT;
			if ((cards & cardMask) != 0) {
				builder.append(CARD_SYMBOL[posInSuit]);
			}
			if (posInSuit == Hand.NUM_CARDS_IN_EACH_SUIT - 1 && i != Deck.NUM_CARDS_IN_DECK - 1) {
				builder.append('.');
			}
			cardMask >>= 1;
		}
		return builder.toString();
	}
	
	public String toLin() {
		StringBuilder builder = new StringBuilder();
		long cardMask = 1L << (Deck.NUM_CARDS_IN_DECK - 1);
		for (int i = 0; i < Deck.NUM_CARDS_IN_DECK; i++) {
			int posInSuit = i % Hand.NUM_CARDS_IN_EACH_SUIT;
			if (posInSuit == 0) {
				builder.append(Suit.values()[3-i/Hand.NUM_CARDS_IN_EACH_SUIT].getAbbreviation());
			}
			if ((cards & cardMask) != 0) {
				builder.append(CARD_SYMBOL[posInSuit]);
			}
			cardMask >>= 1;
		}
		return builder.toString();
	}
}
