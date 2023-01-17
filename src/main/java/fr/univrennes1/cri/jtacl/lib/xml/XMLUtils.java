/*
 * Copyright (c) 2010, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.lib.xml;

import fr.univrennes1.cri.jtacl.core.exceptions.JtaclConfigurationException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * XML utility class
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class XMLUtils {

	/**
	 * Returns the XML document readed from filename in argument.
	 * @param fileName XML file to parse
	 * @return {@link Document} XML document.
	 * @throws JtaclConfigurationException if error occurs.
	 */
	public static Document getXMLDocument(String fileName) {

		FileInputStream fis;
		try {
			fis = new FileInputStream(fileName);
		} catch (FileNotFoundException ex) {
			throw new JtaclConfigurationException(ex.getMessage());
		}

		Document doc = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setCoalescing(true);
			DocumentBuilder builder;
			builder = factory.newDocumentBuilder();
			doc = builder.parse(fis);
		} catch (ParserConfigurationException | SAXException | IOException ex) {
			throw new JtaclConfigurationException(ex.getMessage());
		}
		return doc;
	}

	/**
	 * Returns the value of the tag 'tag' in element 'element'
	 * @param element element
	 * @param tag tag
	 * @return the value of the tag 'tag' in element 'element'.
	 * Null if not found.
	 */
	public static String getTagValue(Element element, String tag) {

		List<Element> elist = getDirectChildren(element, tag);
		if (elist.isEmpty())
			return null;
		Element e = elist.get(0);
		NodeList nlist = e.getChildNodes();
		if (nlist.getLength() != 1)
			return null;
		Node nvalue = (Node) nlist.item(0);
		String value = nvalue.getNodeValue();
		value = value.trim();
		return value;
	}

	/**
	 * Returns a list of the direct children of element 'element' with a given
	 * tag name
	 * @param element element
	 * @param tag tag
	 * @return the value of the tag 'tag' in element 'element'.
	 * Null if not found.
	 */
	public static List<Element> getDirectChildren(Element element, String tag) {

		LinkedList<Element> list = new LinkedList<>();

		for (Node child = element.getFirstChild(); child != null;
			child = child.getNextSibling()) {
			if (child.getNodeType() == Node.ELEMENT_NODE ) {
				String nodeName = child.getNodeName();
				if (tag.equals(nodeName)) {
					list.add((Element)child);
				}
			}
		}
		return list;
	}

	/**
	 * Returns a text representation of the XML element e
	 * @param indent level of indentation
	 * @param e element to output
	 * @return a text representation of the XML element e
	 */
	public static String elementToText(int indent, Element e) {

		String s = "";
		String name = e.getNodeName();
		for (int i = 0; i < indent; i++)
			s += "\t";
		s += "<" + name + ">";
		s += "\n";
		NodeList nlist = e.getChildNodes();
		if (nlist != null && nlist.getLength() > 0) {
			String v = "" + e.getChildNodes().item(0).getNodeValue();
			if (!v.equals("null")) {
				for (int i = 0; i < indent + 1; i++)
					s += "\t";
				s += v;
				s += "\n";
			}
		}
		for (Node child = e.getFirstChild(); child != null;
			child = child.getNextSibling()) {
			if (child.getNodeType() == Node.ELEMENT_NODE ) {
				s += elementToText(indent + 1, (Element) child);
			}
		}
		for (int i = 0; i < indent; i++)
			s += "\t";
		s += "</" + name + ">\n";
		return s;
	}

	/**
	 * Returns a text representation of the XML element e
	 * @param e element to output
	 * @return a text representation of the XML element e
	 */
	public static String elementToText(Element e) {
		return elementToText(0, e);
	}

}
