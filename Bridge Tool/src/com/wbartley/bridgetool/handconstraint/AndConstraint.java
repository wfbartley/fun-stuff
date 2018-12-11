package com.wbartley.bridgetool.handconstraint;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wbartley.bridgetool.Layout;

public class AndConstraint implements HandConstraint {
	private List<HandConstraint> subConstraints = new ArrayList<HandConstraint>();

	@Override
	public HandConstraint parseConstraint(Element element) throws ConstraintParseException {
		AndConstraint result = new AndConstraint();
		NodeList children = element.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node node = children.item(i);
			if (node instanceof Element) {
				Element childElement = (Element)node;
				String constraintType = childElement.getNodeName();
				try {
					ConstraintEnum constraint = ConstraintEnum.valueOf(constraintType);
					result.subConstraints.add(constraint.getHandConstraint().parseConstraint(childElement));
				}
				catch (Exception e) {
					throw new ConstraintParseException("Unrecognized constraint type, " + constraintType);
				}
			}
		}
		return result;
	}

	@Override
	public boolean meetsConstraint(Layout layout) {
		for (HandConstraint subConstraint : subConstraints) {
			if (!subConstraint.meetsConstraint(layout)) {
				return false;
			}
		}
		return true;
	}

}
