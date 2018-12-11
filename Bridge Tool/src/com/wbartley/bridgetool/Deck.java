package com.wbartley.bridgetool;

import java.util.Random;

public class Deck {
	public static final int NUM_CARDS_IN_DECK = 52;
	private long [] cards = new long [NUM_CARDS_IN_DECK];
	private int numUndealt;
	private Random random = new Random(System.currentTimeMillis());
	private int mark;
	
	public Deck() {
		reset();
	}
	
	public void reset() {
		long cardMask = 1;
		for (int i = 0; i < NUM_CARDS_IN_DECK; i++) {
			cards[i] = cardMask;
			cardMask <<= 1;
		}
		numUndealt = NUM_CARDS_IN_DECK;
	}
	
	public void shuffle() {
		numUndealt = NUM_CARDS_IN_DECK;
	}
	
	public long dealCard() {
		int idx = Math.abs(random.nextInt() % numUndealt);
		long result = cards[idx];
		numUndealt--;
		cards[idx] = cards[numUndealt];
		cards[numUndealt] = result;
		return result;
	}
	
	public void mark() {
		mark = numUndealt;
	}
	
	public void returnToMark() {
		numUndealt = mark;
	}
	
	public long [] removeSpecificCards(String specificCards) {
		long [] result = new long[specificCards.length()-1];
		String suitAbbreviation = specificCards.substring(0,1);
		Suit suit = Suit.fromAbbreviation(suitAbbreviation);
		for (int idx = 1; idx < specificCards.length(); idx++) {
			char denomination = specificCards.charAt(idx);
			int bitOffset;
			switch(denomination) {
			case 'A':
				bitOffset = 12;
				break;
			case 'K':
				bitOffset = 11;
				break;
			case 'Q':
				bitOffset = 10;
				break;
			case 'J':
				bitOffset = 9;
				break;
			case 'T':
				bitOffset = 8;
				break;
			case 'S': case 'H': case 'D': case 'C':
				suit = Suit.fromAbbreviation(String.valueOf(denomination));
				continue;
			default:
				bitOffset = denomination - '2';
			}
			result[idx-1] = 1L << suit.ordinal() * 13 + bitOffset;
			for (int i = 0; i < numUndealt; i++) {
				if (cards[i] == result[idx-1]) {
					numUndealt--;
					cards[i] = cards[numUndealt];
					cards[numUndealt] = result[idx-1];
					break;
				}
			}
		}
		return result;
	}
}
