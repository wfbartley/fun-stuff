package com.wbartley.bridgetool.handconstraint;

import org.w3c.dom.Element;

import com.wbartley.bridgetool.Hand;
import com.wbartley.bridgetool.HandDirection;
import com.wbartley.bridgetool.Layout;
import com.wbartley.bridgetool.Orientation;
import com.wbartley.bridgetool.Suit;

public class FitConstraint implements HandConstraint {
	private Orientation orientation;
	private Suit suit;
	private int minFit = 0, maxFit = 13;

	@Override
	public HandConstraint parseConstraint(Element element) throws ConstraintParseException {
		FitConstraint result = new FitConstraint();
		String orient = element.getAttribute("orientation");
		if (orient.isEmpty()) {
			throw new ConstraintParseException("FitConstraint requires orientation (NS or EW) attribute");
		}
		try {
			result.orientation = Orientation.valueOf(orient);
		} catch (Exception e) {
			throw new ConstraintParseException("Unrecognized orientation, " + orient + ". Should be one of NS or EW.");
		}
		String suitStr = element.getAttribute("suit");
		if (suitStr.isEmpty()) {
			throw new ConstraintParseException("FitConstraint requires suit (C,D,H or S) attribute");
		}
		try {
			result.suit = Suit.fromAbbreviation(suitStr);
		}
		catch (Exception e) {
			throw new ConstraintParseException("Unrecognized suit, " + suitStr + ". Should be one of S,H,D or C");
		}
		String minDistStr = element.getAttribute("min");
		if (!minDistStr.isEmpty()) {
			try {
				result.minFit = Integer.parseInt(minDistStr);
				if (result.minFit < 0 || result.minFit > 13) {
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
				result.maxFit = Integer.parseInt(maxDistStr);
				if (result.maxFit < 0 || result.maxFit > 13) {
					throw new ConstraintParseException("max must be an integer between 0 and 13");
				}
			}
			catch (Exception e) {
				throw new ConstraintParseException("max must be an integer between 0 and 13");
			}
		}
		if (result.minFit > result.maxFit) {
			throw new ConstraintParseException("min must be less than or equal to max.");
		}
		return result;
	}

	@Override
	public boolean meetsConstraint(Layout layout) {
		Hand hand1, hand2;
		if (orientation == Orientation.NS) {
			hand1 = layout.getHands()[HandDirection.NORTH.ordinal()];
			hand2 = layout.getHands()[HandDirection.SOUTH.ordinal()];
		}
		else {
			hand1 = layout.getHands()[HandDirection.EAST.ordinal()];
			hand2 = layout.getHands()[HandDirection.WEST.ordinal()];
		}
		int fit = hand1.getDistribution()[suit.ordinal()] + hand2.getDistribution()[suit.ordinal()];
		return fit >= minFit && fit <= maxFit;
	}

}
