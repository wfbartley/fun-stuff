package com.wbartley.bridgetool.handconstraint;

import org.w3c.dom.Element;

import com.wbartley.bridgetool.Hand;
import com.wbartley.bridgetool.HandDirection;
import com.wbartley.bridgetool.Layout;

public class PointRangeConstraint implements HandConstraint {
	private HandDirection handDirection;
	private int minPoints = 0, maxPoints = 37;

	@Override
	public HandConstraint parseConstraint(Element element) throws ConstraintParseException {
		PointRangeConstraint result = new PointRangeConstraint();
		String dir = element.getAttribute("dir");
		if (dir.isEmpty()) {
			throw new ConstraintParseException("PointRangeConstraint requires dir (N,S,E or W) attribute");
		}
		try {
			result.handDirection = HandDirection.fromAbbreviation(dir);
		} catch (Exception e) {
			throw new ConstraintParseException("Unrecognized hand direction, " + dir + ". Should be one of N,S,E or W.");
		}
		String minPointsStr = element.getAttribute("min");
		if (!minPointsStr.isEmpty()) {
			try {
				result.minPoints = Integer.parseInt(minPointsStr);
				if (result.minPoints < 0 || result.minPoints > 40) {
					throw new ConstraintParseException("min must be an integer between 0 and 40");
				}
			}
			catch (Exception e) {
				throw new ConstraintParseException("min must be an integer between 0 and 40");
			}
		}
		String maxPointsStr = element.getAttribute("max");
		if (!maxPointsStr.isEmpty()) {
			try {
				result.maxPoints = Integer.parseInt(maxPointsStr);
				if (result.maxPoints < 0 || result.maxPoints > 40) {
					throw new ConstraintParseException("max must be an integer between 0 and 40");
				}
			}
			catch (Exception e) {
				throw new ConstraintParseException("max must be an integer between 0 and 40");
			}
		}
		if (result.minPoints > result.maxPoints) {
			throw new ConstraintParseException("min must be less than or equal to max.");
		}
		return result;
	}

	@Override
	public boolean meetsConstraint(Layout layout) {
		Hand hand = layout.getHands()[handDirection.ordinal()];
		int points = hand.getTotalPoints();
		return points >= minPoints && points <= maxPoints;
	}

}
