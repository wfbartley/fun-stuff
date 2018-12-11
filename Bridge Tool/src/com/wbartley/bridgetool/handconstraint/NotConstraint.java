package com.wbartley.bridgetool.handconstraint;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wbartley.bridgetool.Layout;

public class NotConstraint implements HandConstraint {
	private HandConstraint childConstraint;

	@Override
	public HandConstraint parseConstraint(Element element) throws ConstraintParseException {
		NotConstraint result = new NotConstraint();
		NodeList children = element.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node node = children.item(i);
			if (node instanceof Element) {
				Element childElement = (Element)node;
				String constraintType = childElement.getNodeName();
				try {
					ConstraintEnum constraint = ConstraintEnum.valueOf(constraintType);
					result.childConstraint = constraint.getHandConstraint().parseConstraint(childElement);
				}
				catch (Exception e) {
					throw new ConstraintParseException("Unrecognized constraint type, " + constraintType);
				}
				break;
			}
		}
		return result;
	}

	@Override
	public boolean meetsConstraint(Layout layout) {
		return !childConstraint.meetsConstraint(layout);
	}

}
