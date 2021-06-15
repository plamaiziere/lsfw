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
import fr.univrennes1.cri.jtacl.core.monitor.Log;
import fr.univrennes1.cri.jtacl.core.monitor.Monitor;
import fr.univrennes1.cri.jtacl.core.network.NetworkEquipment;
import fr.univrennes1.cri.jtacl.core.network.NetworkEquipmentsByName;
import fr.univrennes1.cri.jtacl.equipments.generic.GenericEquipment;
import fr.univrennes1.cri.jtacl.lib.misc.ParseContext;
import fr.univrennes1.cri.jtacl.lib.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PxHost extends GenericEquipment {

	NetworkEquipmentsByName _guests = new NetworkEquipmentsByName();

	/**
	 * parse context
	 */
	 protected ParseContext _parseContext = new ParseContext();


	/**
	 * Create a new {@link PxHost} with this name and this comment.<br/>
	 * @param monitor the {@link Monitor} monitor associated with this equipment.
	 * @param name the name of the equipment.
	 * @param comment a free comment for this equipment.
	 * @param configurationFileName name of the configuration file to use (may be null).
	 */
	public PxHost(Monitor monitor, String name, String comment, String configurationFileName) {
		super(monitor, name, comment, configurationFileName);
	}

	protected void throwCfgException(String msg, boolean context) {
		String s = "Equipment: " + _name + " ";
		if (context)
			s += _parseContext + msg;
		else
			s += msg;

		throw new JtaclConfigurationException(s);
	}

	protected void warnConfig(String msg, boolean context) {
		String s = "Equipment: " + _name + " ";
		if (context)
			s += _parseContext + msg;
		else
			s += msg;

		Log.config().warning(s);
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
		loadConfiguration(doc);
		loadGuests(doc);

		return _guests;
/*

		loadIfaces(doc);
		// loopback interface
        Iface iface = addLoopbackIface("loopback", "loopback");
        _fgFwIfaces.put("loopback", new FgFw.FgIface(iface));

		loadConfiguration(doc);
		linkServices();
		linkNetworkObjects();

		*/
/*
		 * routing
		 *//*

		routeDirectlyConnectedNetworks();
		loadRoutesFromXML(doc);
		*/
/*
		 * compute cross reference
		 *//*

		if (_monitorOptions.getXref())
			CrossReferences();

		return null;
*/
	}

	protected void loadConfiguration(Document doc) {

         /* policy */
		NodeList list = doc.getElementsByTagName("policy");
		if (list.getLength() < 1) {
			throwCfgException("At least one policy must be specified", false);
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
	}

	protected void loadGuests(Document doc) {

         /* guests */
		NodeList list = doc.getElementsByTagName("guests");
		if (list.getLength() < 1) {
			throwCfgException("At least one guests must be specified", false);
		}

		List<String> filenames = new ArrayList<>();
		for (int i = 0; i < list.getLength(); i++) {
            Element e = (Element) list.item(i);
            String filename = e.getAttribute("directory");
            if (!filename.isEmpty()) {
                filenames.add(filename);
            }
        }

        if (filenames.isEmpty()) {
            throwCfgException("Missing guests directory file name", false);
        }

        for( String f: filenames) {
        	_guests.putAll(createGuestsEquipments(f));
        	_fam.addFile(f);
		}

	}

	protected NetworkEquipmentsByName createGuestsEquipments(String directory) {

		NetworkEquipmentsByName equipments = new NetworkEquipmentsByName();
		File file = new File(directory);
		File[] files = file.listFiles();
		if (files == null) {
			throwCfgException("Cannot read directory " + directory, false);
		}
		for (File f: files) {
				if (f.isFile()) {
					NetworkEquipment guest = new PxGuest(_monitor, getName()+ "." + f.getName(), f.getName(), f.getAbsolutePath());
					guest.configure();
					equipments.put(guest);
				}
		}
		return equipments;
	}
}
