package com.wbartley.bridgetool;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wbartley.bridgetool.handconstraint.ConstraintEnum;
import com.wbartley.bridgetool.handconstraint.HandConstraint;
import com.wbartley.bridgetool.stats.Statistic;
import com.wbartley.bridgetool.stats.StatsEnum;

public class RunConfig {
	private static final String CONFIG_FILE_NAME = "runConfig.xml";
	private static final String RUN_LENGTH_ATTRIBUTE = "runLength";
	private static final String SPECIFIC_CARDS_ATTRIBUTE = "specificCards";
	private static final String PBN_FILENAME_ATTRIBUTE = "pbnFilename";
	private static final String LIN_FILENAME_ATTRIBUTE = "linFilename";
	private static final String DEALER_ATTRIBUTE = "dealer";
	private static final String VULNERABILITY_ATTRIBUTE = "vulnerability";
	private static final String CONSTRAINT_ELEMENT = "constraint";
	private static final String INCLUDE_ELEMENT = "include";
	private static final String FILENAME_ATTRIBUTE = "filename";
	private static final String COLLECT_STATS_ELEMENT = "collectStats";
	private static final String DUMP_EACH_SAMPLE_ATTRIBUTE = "dumpEachSample";
	
	private Document doc;
	private int runLength = 1;
	private String specificCards;
	private boolean dumpEachSample;
	private HandConstraint handConstraint = null;
	private boolean requiresDdAnalysis = false;
	private String pbnFilename = null;
	private String linFilename = null;
	private HandDirection dealer = HandDirection.SOUTH;
	private Vulnerability vulnerability = Vulnerability.None;
	private List<Statistic> statsToCollect = new ArrayList<Statistic>();
	
	public RunConfig() throws Exception {
		parseConfigXml();
	}
	
	private void substitutePropertyValuesInIncludedElements(NamedNodeMap replacementAttributes, Element element) {
		NamedNodeMap currentAttributes = element.getAttributes();
		if (currentAttributes.getLength() > 0) {
			for (int i = 0; i < replacementAttributes.getLength(); i++) {
				Node replacementAttribute = replacementAttributes.item(i);
				String valueToReplace = "%" + replacementAttribute.getNodeName() + "%";
				String newValue = replacementAttribute.getNodeValue();
				for (int j = 0; j < currentAttributes.getLength(); j++) {
					Node currentAttribute = currentAttributes.item(j);
					currentAttribute.setNodeValue(currentAttribute.getNodeValue().replace(valueToReplace, newValue));
				}
			}
		}
		NodeList children = element.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child instanceof Element) {
				substitutePropertyValuesInIncludedElements(replacementAttributes, (Element)child);
			}
		}
	}
	
	private void processIncludeElements(Node origNode) throws Exception {
		NodeList children = origNode.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child instanceof Element) {
				Element childElement = (Element)child;
				if (childElement.getNodeName().equals(INCLUDE_ELEMENT)) {
					NamedNodeMap attributes = childElement.getAttributes();
					Node filenameNode = attributes.removeNamedItem(FILENAME_ATTRIBUTE);
					String filename = filenameNode.getNodeValue();
					Document includeDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(filename));
					Element configuration = includeDoc.getDocumentElement();
					substitutePropertyValuesInIncludedElements(attributes, configuration);
					Node importedChild = doc.importNode(configuration, true);
					origNode.replaceChild(importedChild, child);
					processIncludeElements(origNode);
				}
				else {
					processIncludeElements(childElement);
				}
			}
		}
	}
	
	private void parseConfigXml() throws Exception {
		doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(CONFIG_FILE_NAME));
		Element configuration = doc.getDocumentElement();
		processIncludeElements(configuration);
		try {
			runLength = Integer.parseInt(configuration.getAttribute(RUN_LENGTH_ATTRIBUTE));
			if (runLength < 1) runLength = 1;
		}
		catch (Exception e) {}
		specificCards = configuration.getAttribute(SPECIFIC_CARDS_ATTRIBUTE);
		dumpEachSample = Boolean.parseBoolean(configuration.getAttribute(DUMP_EACH_SAMPLE_ATTRIBUTE));
		if (dumpEachSample) {
			requiresDdAnalysis = true;
		}
		String pbnFile = configuration.getAttribute(PBN_FILENAME_ATTRIBUTE);
		if (!pbnFile.isEmpty()) {
			pbnFilename = pbnFile;
		}
		String linFile = configuration.getAttribute(LIN_FILENAME_ATTRIBUTE);
		if (!linFile.isEmpty()) {
			linFilename = linFile;
		}
		String dealerStr = configuration.getAttribute(DEALER_ATTRIBUTE);
		if (dealerStr.equalsIgnoreCase("random")) {
			dealer = HandDirection.values()[(int)(Math.random() * HandDirection.values().length)];
		}
		else {
			HandDirection dir = HandDirection.fromAbbreviation(dealerStr);
			if (dir != null) {
				dealer = dir;
			}
		}
		
		String vulStr = configuration.getAttribute(VULNERABILITY_ATTRIBUTE);
		if (vulStr.equalsIgnoreCase("random")) {
			vulnerability = Vulnerability.values()[(int)(Math.random() * Vulnerability.values().length)];
		}
		else {
			vulnerability = Vulnerability.valueOf(configuration.getAttribute(VULNERABILITY_ATTRIBUTE));
		}
		
		NodeList constraintNodeList = configuration.getElementsByTagName(CONSTRAINT_ELEMENT);
		if (constraintNodeList.getLength() > 0) {
			NodeList children = constraintNodeList.item(0).getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				Node child = children.item(i);
				if (child instanceof Element) {
					Element constraintElement = (Element)child;
					String constraintType = constraintElement.getNodeName();
					ConstraintEnum constraintEnum = ConstraintEnum.valueOf(constraintType);
					handConstraint = constraintEnum.getHandConstraint().parseConstraint(constraintElement);
				}
			}
		}
		NodeList statsNodeList = configuration.getElementsByTagName(COLLECT_STATS_ELEMENT);
		if (statsNodeList.getLength() > 0) {
			NodeList children = statsNodeList.item(0).getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				Node child = children.item(i);
				if (child instanceof Element) {
					Element statsElement = (Element)child;
					String statType = statsElement.getNodeName();
					StatsEnum statisticEnum;
					statisticEnum = StatsEnum.valueOf(statType);
					Statistic statistic = statisticEnum.getStatistic().parse(statsElement);
					if (statistic.requiresDdAnalysis()) {
						requiresDdAnalysis = true;
					}
					statsToCollect.add(statistic);
				}
			}
		}
	}
	
	public int getRunLength() {
		return runLength;
	}
	
	public String getSpecificCards() {
		return specificCards;
	}
	
	public boolean isDumpEachSample() {
		return dumpEachSample;
	}
	
	public boolean isRequiresDdAnalysis() {
		return requiresDdAnalysis;
	}
	
	public String getPbnFilename() {
		return pbnFilename;
	}
	
	public String getLinFilename() {
		return linFilename;
	}
	
	public HandDirection getDealer() {
		return dealer;
	}
	
	public Vulnerability getVulnerability() {
		return vulnerability;
	}
	
	public HandConstraint getHandConstraint() {
		return handConstraint;
	}

	public List<Statistic> getStatsToCollect(){
		return statsToCollect;
	}
}
