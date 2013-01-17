/*
 * Copyright (c) 2010, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the ESUP-Portail license as published by the
 * ESUP-Portail consortium.
 *
 * Alternatively, this software may be distributed under the terms of BSD
 * license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.lib.xml;

import fr.univrennes1.cri.jtacl.core.exceptions.JtaclConfigurationException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
		} catch (ParserConfigurationException ex) {
			throw new JtaclConfigurationException(ex.getMessage());
		} catch (SAXException ex) {
			throw new JtaclConfigurationException(ex.getMessage());
		} catch (IOException ex) {
			throw new JtaclConfigurationException(ex.getMessage());
		}
		return doc;
	}

	public static String getTagValue(Element element, String tag) {

		NodeList elist = element.getElementsByTagName(tag);
		if (elist.getLength() != 1)
			return null;
		Element e = (Element) elist.item(0);
		NodeList nlist = e.getChildNodes();
		if (nlist.getLength() != 1)
			return null;
		Node nvalue = (Node) nlist.item(0);
		String value = nvalue.getNodeValue();
		value = value.trim();
		return value;
	}

}
