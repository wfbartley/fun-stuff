package com.wbartley.bridgetool.handconstraint;

import org.w3c.dom.Element;

import com.wbartley.bridgetool.ContractStrain;
import com.wbartley.bridgetool.HandDirection;
import com.wbartley.bridgetool.Layout;
import com.wbartley.bridgetool.dds.DdTableResults;

public class DownConstraint implements HandConstraint {
	private int contractNumTricks;
	private ContractStrain contractStrain;
	private HandDirection handDirections[];
	private int numDown;
	private boolean exactly;

	@Override
	public HandConstraint parseConstraint(Element element) throws ConstraintParseException {
		DownConstraint result = new DownConstraint();
		String dirStr = element.getAttribute("dirs");
		if (dirStr.isEmpty()) {
			throw new ConstraintParseException("dirs attribute is required for MakesConstraint.");
		}
		result.handDirections = new HandDirection[dirStr.length()];
		for (int i = 0; i < dirStr.length(); i++) {
			result.handDirections[i] = HandDirection.fromAbbreviation(dirStr.substring(i, i+1));
			if (result.handDirections[i] == null) {
				throw new ConstraintParseException("dirs must be 1-4 of N, S, E and W");
			}
		}
		String inputContractString = element.getAttribute("contract");
		if (inputContractString.isEmpty()) {
			throw new ConstraintParseException("contract attribute is required for MakesConstraint.");
		}
		if (inputContractString.length() != 2) {
			throw new ConstraintParseException("contract attribute must be in the form <1-7><S|H|D|C|N>.");
		}
		result.contractNumTricks = inputContractString.charAt(0) - '0';
		if (result.contractNumTricks < 1 || result.contractNumTricks > 7) {
			throw new ConstraintParseException("contract number of tricks must be in range 1-7.");
		}
		result.contractNumTricks += 6;
		result.contractStrain = ContractStrain.fromAbbreviation(inputContractString.substring(1));
		if (result.contractStrain == null) {
			throw new ConstraintParseException("contract strain must be one of S, H, D, C, or N.");
		}
		
		String numDownTricksString = element.getAttribute("numDown");
		try {
			int numDownTricks = Integer.parseInt(numDownTricksString);
			if (numDownTricks < 1 || numDownTricks > result.contractNumTricks) {
				throw new ConstraintParseException("numDown must be an integer between 1 and " + result.contractNumTricks);
			}
			result.numDown = numDownTricks;
		} catch (NumberFormatException e) {
			throw new ConstraintParseException("numDown must be an integer between 1 and " + result.contractNumTricks);
		}
		
		result.exactly = Boolean.parseBoolean(element.getAttribute("exactly"));
		return result;
	}

	@Override
	public boolean meetsConstraint(Layout layout) {
		DdTableResults results = layout.getDdResults();
		for (HandDirection direction : handDirections) {
			int numTricks = results.getNumTricks(contractStrain, direction) - contractNumTricks;
			if (exactly) {
				if (numTricks != -numDown) {
					return false;
				}
			}
			else {
				if (numTricks > -numDown) {
					return false;
				};
			}
		}
		return true;
	}

}
