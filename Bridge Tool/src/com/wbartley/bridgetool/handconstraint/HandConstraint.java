package com.wbartley.bridgetool.handconstraint;

import org.w3c.dom.Element;

import com.wbartley.bridgetool.Layout;

public interface HandConstraint {
	public HandConstraint parseConstraint(Element element) throws ConstraintParseException;
	public boolean meetsConstraint(Layout layout);
}
