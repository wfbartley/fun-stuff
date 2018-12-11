package com.wbartley.bridgetool.handconstraint;

public enum ConstraintEnum {
	OR(new OrConstraint()),
	AND(new AndConstraint()),
	NOT(new NotConstraint()),
	HCP(new HcpRangeConstraint()),
	DIST(new DistributionConstraint()),
	FIT(new FitConstraint()),
	POINTS(new PointRangeConstraint()),
	DEFENSE(new DefensiveTrickConstraint()),
	MAKES(new MakesConstraint()),
	DOWN(new DownConstraint()),
	M_OF_TOP_N(new MofTopNHonorsConstraint());
	
	private HandConstraint handConstraint;
	
	private ConstraintEnum(HandConstraint handConstraint) {
		this.handConstraint = handConstraint;
	}
	
	public HandConstraint getHandConstraint() {
		return handConstraint;
	}
}
