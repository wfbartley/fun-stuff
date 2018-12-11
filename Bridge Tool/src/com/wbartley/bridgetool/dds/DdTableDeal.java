package com.wbartley.bridgetool.dds;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;
import com.wbartley.bridgetool.Hand;
import com.wbartley.bridgetool.HandDirection;
import com.wbartley.bridgetool.Layout;
import com.wbartley.bridgetool.Suit;

public class DdTableDeal extends Structure {
	public class ByValue extends DdTableDeal implements Structure.ByValue {}

	@Override
	protected List<String> getFieldOrder() {
		// TODO Auto-generated method stub
		return Arrays.asList("cards");
	}
	public int [] cards = new int[16];
	
	public void setCards(Layout layout) {
		Hand [] hands = layout.getHands();
		for (int handIdx = 0; handIdx < 4; handIdx++) {
			Hand hand = null;
			switch (handIdx) {
				case DdsDll.HAND_NORTH:
					hand = hands[HandDirection.NORTH.ordinal()];
					break;
				case DdsDll.HAND_EAST:
					hand = hands[HandDirection.EAST.ordinal()];
					break;
				case DdsDll.HAND_SOUTH:
					hand = hands[HandDirection.SOUTH.ordinal()];
					break;
				case DdsDll.HAND_WEST:
					hand = hands[HandDirection.WEST.ordinal()];
					break;
			}
			for (int suitIdx = 0; suitIdx < 4; suitIdx++) {
				int cardMask = 0;
				switch (suitIdx) {
				case DdsDll.SUIT_SPADES:
					cardMask = hand.getSuitNormalized(Suit.SPADES, 2);
					break;
				case DdsDll.SUIT_HEARTS:
					cardMask = hand.getSuitNormalized(Suit.HEARTS, 2);
					break;
				case DdsDll.SUIT_DIAMONDS:
					cardMask = hand.getSuitNormalized(Suit.DIAMONDS, 2);
					break;
				case DdsDll.SUIT_CLUBS:
					cardMask = hand.getSuitNormalized(Suit.CLUBS, 2);
					break;
				}
				cards[handIdx * 4 + suitIdx] = cardMask;
			}
		}
	}
}
