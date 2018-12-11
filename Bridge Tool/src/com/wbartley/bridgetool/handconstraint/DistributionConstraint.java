package com.wbartley.bridgetool.handconstraint;

import org.w3c.dom.Element;

import com.wbartley.bridgetool.Hand;
import com.wbartley.bridgetool.HandDirection;
import com.wbartley.bridgetool.Layout;
import com.wbartley.bridgetool.Suit;

public class DistributionConstraint implements HandConstraint {
	private HandDirection handDirection;
	private Suit suit;
	private int minDist = 0, maxDist = 13;
	private int [] nonSuitSpecific = null;

	@Override
	public HandConstraint parseConstraint(Element element) throws ConstraintParseException {
		DistributionConstraint result = new DistributionConstraint();
		String dir = element.getAttribute("dir");
		if (dir.isEmpty()) {
			throw new ConstraintParseException("DistributionConstraint requires dir (N,S,E or W) attribute");
		}
		try {
			result.handDirection = HandDirection.fromAbbreviation(dir);
		} catch (Exception e) {
			throw new ConstraintParseException("Unrecognized hand direction, " + dir + ". Should be one of N,S,E or W.");
		}
		String nonSuitSpecificStr = element.getAttribute("nonSuitSpecific");
		int intValue;
		if (!nonSuitSpecificStr.isEmpty()) {
			if (nonSuitSpecificStr.length() == 4 && (intValue = Integer.parseInt(nonSuitSpecificStr)) != 0) {
				result.nonSuitSpecific = new int [4];
				int sum = 0;
				for (int i = 0; i < 4; i++) {
					int digit = intValue % 10;
					sum += digit;
					result.nonSuitSpecific[i] = digit;
					intValue /= 10;
				}
				if (sum != 13) {
					throw new ConstraintParseException("NonSuitSpecific distribution digits must add to 13, e.g., 4432 or 5440");
				}
			}
			else {
				throw new ConstraintParseException("NonSuitSpecific distribution must have four digits");
			}
		}
		else {
			String suitStr = element.getAttribute("suit");
			if (suitStr.isEmpty()) {
				throw new ConstraintParseException("DistributionConstraint requires suit (C,D,H or S) attribute");
			}
			try {
				result.suit = Suit.fromAbbreviation(suitStr);
			}
			catch (Exception e) {
				throw new ConstraintParseException("Unrecognized suit, " + suitStr + ". Should be one of S,H,D or C");
			}
		}
		String minDistStr = element.getAttribute("min");
		if (!minDistStr.isEmpty()) {
			try {
				result.minDist = Integer.parseInt(minDistStr);
				if (result.minDist < 0 || result.minDist > 13) {
					throw new ConstraintParseException("min must be an integer between 0 and 13");
				}
			}
			catch (Exception e) {
				throw new ConstraintParseException("min must be an integer between 0 and 13");
			}
		}
		String maxDistStr = element.getAttribute("max");
		if (!maxDistStr.isEmpty()) {
			try {
				result.maxDist = Integer.parseInt(maxDistStr);
				if (result.maxDist < 0 || result.maxDist > 13) {
					throw new ConstraintParseException("max must be an integer between 0 and 13");
				}
			}
			catch (Exception e) {
				throw new ConstraintParseException("max must be an integer between 0 and 13");
			}
		}
		if (result.minDist > result.maxDist) {
			throw new ConstraintParseException("min must be less than or equal to max.");
		}
		return result;
	}

	@Override
	public boolean meetsConstraint(Layout layout) {
		Hand hand = layout.getHands()[handDirection.ordinal()];
		int [] distribution = hand.getDistribution();
		if (nonSuitSpecific != null) {
			int numLeft = 4;
			for (int dist : nonSuitSpecific) {
				boolean found = false;
				for (int j = 0; j < numLeft && !found; j++) {
					if (distribution[j] == dist) {
						numLeft--;
						distribution[j] = distribution[numLeft];
						found = true;
					}
				}
				if (!found) break;
			}
			return (numLeft == 0);
		}
		else {
			int dist = distribution[suit.ordinal()];
			return dist >= minDist && dist <= maxDist;
		}
	}

}
