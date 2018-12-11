package com.wbartley.bridgetool.handconstraint;

import org.w3c.dom.Element;

import com.wbartley.bridgetool.Hand;
import com.wbartley.bridgetool.HandDirection;
import com.wbartley.bridgetool.Layout;
import com.wbartley.bridgetool.Suit;

public class HcpRangeConstraint implements HandConstraint {
	private HandDirection handDirection;
	private Suit suit = null;
	private int minHcp = 0, maxHcp = 37;

	@Override
	public HandConstraint parseConstraint(Element element) throws ConstraintParseException {
		HcpRangeConstraint result = new HcpRangeConstraint();
		String dir = element.getAttribute("dir");
		if (dir.isEmpty()) {
			throw new ConstraintParseException("HcpRangeConstraint requires dir (N,S,E or W) attribute");
		}
		try {
			result.handDirection = HandDirection.fromAbbreviation(dir);
		} catch (Exception e) {
			throw new ConstraintParseException("Unrecognized hand direction, " + dir + ". Should be one of N,S,E or W.");
		}
		String suitStr = element.getAttribute("suit");
		if (!suitStr.isEmpty()) {
			try {
				result.suit = Suit.fromAbbreviation(suitStr);
			}
			catch (Exception e) {
				throw new ConstraintParseException("Unrecognized suit, " + suitStr + ". Should be one of S,H,D or C");
			}
		}
		String minHcpStr = element.getAttribute("min");
		if (!minHcpStr.isEmpty()) {
			try {
				result.minHcp = Integer.parseInt(minHcpStr);
				if (result.minHcp < 0 || result.minHcp > 37) {
					throw new ConstraintParseException("min must be an integer between 0 and 37");
				}
			}
			catch (Exception e) {
				throw new ConstraintParseException("min must be an integer between 0 and 37");
			}
		}
		String maxHcpStr = element.getAttribute("max");
		if (!maxHcpStr.isEmpty()) {
			try {
				result.maxHcp = Integer.parseInt(maxHcpStr);
				if (result.maxHcp < 0 || result.maxHcp > 37) {
					throw new ConstraintParseException("max must be an integer between 0 and 37");
				}
			}
			catch (Exception e) {
				throw new ConstraintParseException("max must be an integer between 0 and 37");
			}
		}
		if (result.minHcp > result.maxHcp) {
			throw new ConstraintParseException("min must be less than or equal to max.");
		}
		return result;
	}

	@Override
	public boolean meetsConstraint(Layout layout) {
		Hand hand = layout.getHands()[handDirection.ordinal()];
		int hcp;
		if (suit == null) {
			hcp = hand.getHCP();
		}
		else {
			hcp = hand.getHCP(suit);
		}
		return hcp >= minHcp && hcp <= maxHcp;
	}

}
