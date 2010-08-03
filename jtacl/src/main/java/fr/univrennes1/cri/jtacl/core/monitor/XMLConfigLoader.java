/*
 * Copyright (c) 2010, Université de Rennes 1
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

package fr.univrennes1.cri.jtacl.core.monitor;

import fr.univrennes1.cri.jtacl.core.exceptions.JtaclConfigurationException;
import fr.univrennes1.cri.jtacl.lib.ip.IPIcmp;
import fr.univrennes1.cri.jtacl.lib.ip.IPIcmp4;
import fr.univrennes1.cri.jtacl.lib.ip.IPIcmp6;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class XMLConfigLoader {

	protected Monitor _monitor;

	/**
	 * Creates a new {@link XMLConfigLoader} instance.
	 * @param monitor {@link Monitor} monitor to be associated to this instance.
	 */
	public XMLConfigLoader(Monitor monitor) {
		_monitor = monitor;
	}

	public void loadConfiguration(Document doc) {
		NodeList list = doc.getElementsByTagName("services");
		if (list.getLength() > 0) {
			Element e = (Element) list.item(0);
			String fileName = e.getAttribute("filename");

			if (fileName.isEmpty())
				throw new JtaclConfigurationException("Missing services filename");

			InputStream stream =null;
			try {
				stream = new FileInputStream(fileName);
			} catch (FileNotFoundException ex) {
				throw new JtaclConfigurationException("services file not found: " +
						fileName);
			}
			_monitor.readServices(stream);
		}

		list = doc.getElementsByTagName("protocols");
		if (list.getLength() > 0) {
			Element e = (Element) list.item(0);
			String fileName = e.getAttribute("filename");

			if (fileName.isEmpty())
				throw new JtaclConfigurationException("Missing protocols filename");

			InputStream stream =null;
			try {
				stream = new FileInputStream(fileName);
			} catch (FileNotFoundException ex) {
				throw new JtaclConfigurationException("protocols file not found: " +
						fileName);
			}
			_monitor.readProtocols(stream);
		}

		list = doc.getElementsByTagName("icmp4");
		if (list.getLength() > 0) {
			Element e = (Element) list.item(0);
			String fileName = e.getAttribute("filename");

			if (fileName.isEmpty())
				throw new JtaclConfigurationException("Missing icmp filename");

			InputStream stream =null;
			try {
				stream = new FileInputStream(fileName);
			} catch (FileNotFoundException ex) {
				throw new JtaclConfigurationException("icmp file not found: " +
						fileName);
			}
			IPIcmp icmp = IPIcmp4.getInstance();
			_monitor.readIcmp(icmp, stream);
		}

		list = doc.getElementsByTagName("icmp6");
		if (list.getLength() > 0) {
			Element e = (Element) list.item(0);
			String fileName = e.getAttribute("filename");

			if (fileName.isEmpty())
				throw new JtaclConfigurationException("Missing icmp filename");

			InputStream stream =null;
			try {
				stream = new FileInputStream(fileName);
			} catch (FileNotFoundException ex) {
				throw new JtaclConfigurationException("icmp file not found: " +
						fileName);
			}
			IPIcmp icmp = IPIcmp6.getInstance();
			_monitor.readIcmp(icmp, stream);
		}

	}

	public void loadTopology(Document doc) {

		NodeList list = doc.getElementsByTagName("tlink");
		for (int i = 0; i < list.getLength(); i++) {
			Element e = (Element) list.item(i);
			String sTopology = e.getAttribute("topology");
			String sNetwork = e.getAttribute("network");
			String sBorder = e.getAttribute("border");

			String s = "Topology: " + sTopology + " network: " + sNetwork +
				" border: " + sBorder;

			if (sBorder.isEmpty())
				sBorder = "false";

			if (sTopology.isEmpty())
				throw new JtaclConfigurationException("Missing topology: " + s);

			if (sNetwork.isEmpty())
				throw new JtaclConfigurationException("Missing network: " + s);

			boolean border = Boolean.parseBoolean(sBorder);
			_monitor.getTopology().addTopologicalLink(border, sNetwork, sTopology);

		}
	}

	public void loadEquipments(Document doc) {

		NodeList list = doc.getElementsByTagName("equipment");
		for (int i = 0; i < list.getLength(); i++) {
			Element e = (Element) list.item(i);
			String className = e.getAttribute("classname");
			String name = e.getAttribute("name");
			String comment = e.getAttribute("comment");
			String fileName = e.getAttribute("filename");

			String s = "className: " + className + " name: " + name +
					" comment: " + comment + " filename: " + fileName;
			if (className.isEmpty())
				throw new JtaclConfigurationException("Missing equipment classname: " + s);

			if (name.isEmpty())
				throw new JtaclConfigurationException("Missing equipment name: " + s);

			if (comment.isEmpty())
				throw new JtaclConfigurationException("Missing equipment comment: " + s);

			if (fileName.isEmpty())
				throw new JtaclConfigurationException("Missing equipment configuration file: " + s);

			_monitor.addEquipment(className, name, comment, fileName);
		}
	}

	public void loadDefines(Document doc) {
		NodeList list = doc.getElementsByTagName("define");
		for (int i = 0; i < list.getLength(); i++) {
			Element e = (Element) list.item(i);
			String name = e.getAttribute("name");
			String value = e.getAttribute("value");
			if (name.isEmpty())
				throw new JtaclConfigurationException("Missing define name");

			if (value.isEmpty())
				throw new JtaclConfigurationException("Missing define value");

			_monitor.getDefines().put(name, value);
		}
	}

}
