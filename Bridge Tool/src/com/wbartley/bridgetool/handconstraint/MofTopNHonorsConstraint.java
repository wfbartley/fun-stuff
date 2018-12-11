package com.wbartley.bridgetool.handconstraint;

import org.w3c.dom.Element;

import com.wbartley.bridgetool.Hand;
import com.wbartley.bridgetool.HandDirection;
import com.wbartley.bridgetool.Layout;
import com.wbartley.bridgetool.Suit;

public class MofTopNHonorsConstraint implements HandConstraint {
	private HandDirection handDirection;
	private Suit suit;
	private int m, n;

	@Override
	public HandConstraint parseConstraint(Element element) throws ConstraintParseException {
		MofTopNHonorsConstraint result = new MofTopNHonorsConstraint();
		String dir = element.getAttribute("dir");
		if (dir.isEmpty()) {
			throw new ConstraintParseException("MofTopNHonorsConstraint requires dir (N,S,E or W) attribute");
		}
		try {
			result.handDirection = HandDirection.fromAbbreviation(dir);
		} catch (Exception e) {
			throw new ConstraintParseException("Unrecognized hand direction, " + dir + ". Should be one of N,S,E or W.");
		}
		String suitStr = element.getAttribute("suit");
		if (suitStr.isEmpty()) {
			throw new ConstraintParseException("MofTopNHonorsConstraint requires suit (S,H,D or C) attribute");
		}
		result.suit = Suit.fromAbbreviation(suitStr);
		if (result.suit == null) {
			throw new ConstraintParseException("Unrecognized suit, " + suitStr + ". Should be one of S,H,D or C");
		}
		String mStr = element.getAttribute("m");
		if (mStr.isEmpty()) {
			throw new ConstraintParseException("MofTopNHonorsConstraint requires m (0-11) attribute");
		}
		result.m = Integer.parseInt(mStr);
		if (result.m <= 0 || result.m > 11) {
			throw new ConstraintParseException("m value must be greater than zero and less than 12");
		}
		String nStr = element.getAttribute("n");
		if (nStr.isEmpty()) {
			throw new ConstraintParseException("MofTopNHonorsConstraint requires n (0-12) attribute");
		}
		result.n = Integer.parseInt(nStr);
		if (result.n <= 0 || result.n > 12) {
			throw new ConstraintParseException("n value must be greater than zero and less than 13");
		}
		if (result.m >= result.n) {
			throw new ConstraintParseException("m value must be less than n value");
		}
		return result;
	}

	@Override
	public boolean meetsConstraint(Layout layout) {
		Hand hand = layout.getHands()[handDirection.ordinal()];
		int numTopHonors = hand.getNumberOfTopHonors(suit, n);
		return numTopHonors >= m;
	}

}
