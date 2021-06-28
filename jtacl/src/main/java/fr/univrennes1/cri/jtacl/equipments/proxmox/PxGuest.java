/*
 * Copyright (c) 2021, Universite de Rennes 1
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
package fr.univrennes1.cri.jtacl.equipments.proxmox;

import fr.univrennes1.cri.jtacl.core.exceptions.JtaclConfigurationException;
import fr.univrennes1.cri.jtacl.core.monitor.Monitor;
import fr.univrennes1.cri.jtacl.core.network.Iface;
import fr.univrennes1.cri.jtacl.core.network.NetworkEquipmentsByName;
import fr.univrennes1.cri.jtacl.lib.ip.IPNet;
import fr.univrennes1.cri.jtacl.lib.misc.ParseContext;
import fr.univrennes1.cri.jtacl.lib.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class PxGuest extends PxEquipment {

	String _directory;

	@Override
	public boolean isHost() { return false; }

	/**
	 * parse context
	 */
	 protected ParseContext _parseContext = new ParseContext();

	/**
	 * Create a new {@link PxGuest} with this name and this comment.<br/>
	 * @param monitor the {@link Monitor} monitor associated with this equipment.
	 * @param name the name of the equipment.
	 * @param comment a free comment for this equipment.
	 * @param configurationFileName name of the configuration file to use (may be null).
	 */
	public PxGuest(Monitor monitor, String name, String comment, String directory, String configurationFileName
		,  PxEquipment host) {
		super(monitor, name, comment, configurationFileName, host);
		_directory = directory;
	}

	@Override
	public NetworkEquipmentsByName configure() {
		if (_configurationFileName.isEmpty())
			return null;

		/*
		 * Read the XML configuration file
		 */
		famAdd(_configurationFileName);
		Document doc = XMLUtils.getXMLDocument(_configurationFileName);
		loadOptionsFromXML(doc);
		loadFiltersFromXML(doc);
		loadIfaces(doc);
		// loopback interface
        Iface iface = addLoopbackIface("loopback", "loopback");
        _pxgIfaces.put("loopback", new PxgIface(iface));
		loadConfiguration(doc);
		routeDirectlyConnectedNetworks();
		loadRoutesFromXML(doc);

		return null;
	}

	protected void loadConfiguration(Document doc) {

         /* policy */
		NodeList list = doc.getElementsByTagName("policy");
		if (list.getLength() != 1) {
			throwCfgException("One policy must be specified", false);
		}

		List<String> filenames = new ArrayList<>();
		for (int i = 0; i < list.getLength(); i++) {
            Element e = (Element) list.item(i);
            String filename = e.getAttribute("filename");
            if (!filename.isEmpty()) {
                filenames.add(filename);
            }
        }

        if (filenames.isEmpty()) {
            throwCfgException("Missing policy file name", false);
        }

        String fileName = _directory + "/" + filenames.get(0);
       	parsePolicy(fileName);
       	famAdd(fileName);
        if (_monitor.getOptions().getXref()) {
        	crossReference();
		}
	}

	protected void loadIfaces(Document doc) {

		NodeList list = doc.getElementsByTagName("iface");
		for (int i = 0; i < list.getLength(); i++) {
			Element e = (Element) list.item(i);
			String name = e.getAttribute("name");
			String comment = e.getAttribute("comment");
			String ifIp = e.getAttribute("ip");
			String ifNetwork = e.getAttribute("network");

			String s = "name: " + name + " comment: " + comment +
					" IP: " + ifIp + " network: " + ifNetwork;

			if (name.isEmpty())
				throw new JtaclConfigurationException("Missing interface name: " + s);

			if (ifIp.isEmpty())
				throw new JtaclConfigurationException("Missing interface IP: " + s);

			IPNet ip;
			try {
				ip = new IPNet(ifIp);
				ip = ip.hostAddress();
			} catch (UnknownHostException ex) {
				throw new JtaclConfigurationException("Invalid interface IP: " + s);
			}

			IPNet network;
			try {
				/*
				 * If network attribute is empty, use the network address of the IP
				 * instead.
				 */
				if (ifNetwork.isEmpty())
					network = new IPNet(ifIp);
				else
					network = new IPNet(ifNetwork);
				network = network.networkAddress();
			} catch (UnknownHostException ex) {
				throw new JtaclConfigurationException("Invalid interface network: " + s);
			}

			/*
			 * Append the link to an existing Iface or create a new one.
			 */
			PxgIface pxgIface;
			Iface iface = getIface(name);
			if (iface == null) {
				if (comment.isEmpty())
					throw new JtaclConfigurationException("Missing interface comment: " + s);
				iface = addIface(name, comment);
				pxgIface = new PxgIface(iface);
				_pxgIfaces.put(name, pxgIface);
			}
			iface.addLink(ip, network);
		}
	}

}
