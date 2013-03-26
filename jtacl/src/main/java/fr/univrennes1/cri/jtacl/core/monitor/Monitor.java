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

package fr.univrennes1.cri.jtacl.core.monitor;

import fr.univrennes1.cri.jtacl.core.exceptions.JtaclConfigurationException;
import fr.univrennes1.cri.jtacl.core.exceptions.JtaclInternalException;
import fr.univrennes1.cri.jtacl.core.network.IfaceLink;
import fr.univrennes1.cri.jtacl.core.network.NetworkEquipment;
import fr.univrennes1.cri.jtacl.core.network.NetworkEquipmentsByName;
import fr.univrennes1.cri.jtacl.core.probing.AclResult;
import fr.univrennes1.cri.jtacl.core.probing.Probe;
import fr.univrennes1.cri.jtacl.core.probing.ProbeRequest;
import fr.univrennes1.cri.jtacl.core.probing.ProbesByUid;
import fr.univrennes1.cri.jtacl.core.probing.ProbesTracker;
import fr.univrennes1.cri.jtacl.core.probing.Probing;
import fr.univrennes1.cri.jtacl.core.topology.NetworkLink;
import fr.univrennes1.cri.jtacl.core.topology.Topology;
import fr.univrennes1.cri.jtacl.lib.ip.IPIcmp;
import fr.univrennes1.cri.jtacl.lib.ip.IPIcmp4;
import fr.univrennes1.cri.jtacl.lib.ip.IPIcmp6;
import fr.univrennes1.cri.jtacl.lib.ip.IPNet;
import fr.univrennes1.cri.jtacl.lib.ip.IPProtocols;
import fr.univrennes1.cri.jtacl.lib.ip.IPRangeable;
import fr.univrennes1.cri.jtacl.lib.ip.IPServices;
import fr.univrennes1.cri.jtacl.lib.xml.XMLUtils;
import fr.univrennes1.cri.jtacl.policies.Policies;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * The Monitor is the main class of Jtacl.<br/>
 * It creates network equipments according to the configuration.<br/>
 * Builds the topology.<br/>
 * Handles define and option.<br/>
 * Handles name databases.<br/>
 * Dispatches probes between network equipments.<br/>
 * Does the probing.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class Monitor {

	/**
	 * singleton
	 */
	protected static Monitor _instance = new Monitor();

	/**
	 * Returns the singleton instance.
	 * @return the singleton instance.
	 */
	public static Monitor getInstance() {
		return _instance;
	}

	/**
	 * configuration XML document
	 */
	protected Document _xmlConfiguration;

	/**
	 * configuration file name
	 */
	protected String _configurationFileName;

	/**
	 * Hop counter
	 */
	protected int _hopCount;

	/**
	 * Jtacl options
	 */
	protected Options _options;

	/**
	 * defined variable
	 */
	protected Map<String, String> _defines;

	/**
	 * Map of the network equipments.
	 */
	protected NetworkEquipmentsByName _equipments;

	/**
	 * The topology of the network
	 */
	protected Topology _topology;

	/**
	 * Probing collection.
	 */
	protected Probing _probing;

	protected Monitor() {
		_options = new Options();
		_defines = new HashMap<String, String>();
		_equipments = new NetworkEquipmentsByName();
		_topology = new Topology();
		_probing = new Probing();
	}

	/**
	 * Reads the protocols database.
	 * @param stream stream to read.
	 */
	protected void readProtocols(InputStream stream) {
		try {
			IPProtocols.getInstance().readProtocols(stream);
		} catch (IOException ex) {
			throw new JtaclConfigurationException("Error: cannot read protocols " +
				ex.getMessage());
		} catch (NumberFormatException ex) {
			throw new JtaclConfigurationException("Error in protocols file " +
				 ex.getMessage());
		}
	}

	/**
	 * Reads the services database.
	 * @param stream stream to read.
	 */
	protected void readServices(InputStream stream) {
		try {
			IPServices.getInstance().readServices(stream);
		} catch (IOException ex) {
			throw new JtaclConfigurationException("Error: cannot read services " +
				ex.getMessage());
		} catch (NumberFormatException ex) {
			throw new JtaclConfigurationException("Error in services file " +
				 ex.getMessage());
		}
	}

	/**
	 * Reads the icmp-types and messages database.
	 * @param ipIcmp database to be loaded.
	 * @param stream stream to read.
	 */
	protected void readIcmp(IPIcmp ipIcmp, InputStream stream) {
		try {
			ipIcmp.readIcmp(stream);
		} catch (IOException ex) {
			throw new JtaclConfigurationException("Error: cannot read icmp " +
					"definition " + ex.getMessage());
		} catch (NumberFormatException ex) {
			throw new JtaclConfigurationException("Error in icmp file " +
				 ex.getMessage());
		}
	}

	protected void loadConfiguration(Document doc) {
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
			readServices(stream);
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
			readProtocols(stream);
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
			readIcmp(icmp, stream);
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
			readIcmp(icmp, stream);
		}

		list = doc.getElementsByTagName("policies");
		Policies.clear();
		if (list.getLength() > 0) {
			Element e = (Element) list.item(0);
			String fileName = e.getAttribute("filename");

			if (fileName.isEmpty())
				throw new JtaclConfigurationException("Missing policies filename");

			InputStream stream =null;
			try {
				stream = new FileInputStream(fileName);
			} catch (FileNotFoundException ex) {
				throw new JtaclConfigurationException("Policies file not found: " +
						fileName);
			}
			Policies.loadPolicies(fileName);
		}
	}

	protected void loadTopology(Document doc, Topology topology) {

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
			topology.addTopologicalLink(border, sNetwork, sTopology);

		}
	}

	protected void loadEquipments(Document doc) {

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

			addEquipment(className, name, comment, fileName);
		}
	}

	protected void loadDefines(Document doc) {
		NodeList list = doc.getElementsByTagName("define");
		for (int i = 0; i < list.getLength(); i++) {
			Element e = (Element) list.item(i);
			String name = e.getAttribute("name");
			String value = e.getAttribute("value");
			if (name.isEmpty())
				throw new JtaclConfigurationException("Missing define name");

			if (value.isEmpty())
				throw new JtaclConfigurationException("Missing define value");

			getDefines().put(name, value);
		}
	}

	/**
	 * Creates a new instance of the network equipment described in argument.
	 * @param className className of the equipment.
	 * @param equipmentName the name of the equipment.
	 * @param equipmentComment the comment of the equipment.
	 * @param fileName the filename of the equipment.
	 * @return the newly created equipment.
	 * @throws JtaclConfigurationException on error.
	 */
	public NetworkEquipment createNetworkEquipment(String className,
			String equipmentName,
			String equipmentComment,
			String fileName) {

		NetworkEquipment equipment = null;

		/*
		 * Retrieve the class for "className"
		 */
		Class cl;
		try {
			cl = Class.forName(className);
		} catch (ClassNotFoundException ex) {
			throw new JtaclConfigurationException(ex.getMessage());
		}

		/*
		 * Search the constructor of the Equipment
		 */
		Class str = String.class;
		Class monitor = Monitor.class;
		Constructor constructor;
		try {
			constructor = cl.getConstructor(monitor, str, str, str);
		} catch (NoSuchMethodException ex) {
			throw new JtaclConfigurationException(ex.getMessage());
		} catch (SecurityException ex) {
			throw new JtaclConfigurationException(ex.getMessage());
		}
		/*
		 * Create the equipment.
		 */
		try {
			equipment =  (NetworkEquipment)
					constructor.newInstance(this, equipmentName, equipmentComment, fileName);
		} catch (InstantiationException ex) {
			throw new JtaclConfigurationException(ex.getMessage());
		} catch (IllegalAccessException ex) {
			throw new JtaclConfigurationException(ex.getMessage());
		} catch (IllegalArgumentException ex) {
			throw new JtaclConfigurationException(ex.getMessage());
		} catch (InvocationTargetException ex) {
			throw new JtaclConfigurationException(ex.getMessage());
		}
		return equipment;
	}

	/**
	 * Creates and adds a new instance of the network equipment described in argument.
	 * @param className className of the equipment.
	 * @param equipmentName the name of the equipment.
	 * @param equipmentComment the comment of the equipment.
	 * @param fileName the filename of the equipment.
	 * @return the newly created network equipment.
	 * @throws JtaclConfigurationException on error.
	 */
	public NetworkEquipment addEquipment(String className,	String equipmentName,
			String equipmentComment, String fileName) {

		String s = "className: " + className + " name: " + equipmentName +
				" comment: " + equipmentComment +
				" filename: " + fileName;
		if (_equipments.containsKey(equipmentName))
			throw new JtaclConfigurationException("duplicate equipment: " + s);
		NetworkEquipment eq = createNetworkEquipment(className, equipmentName,
				equipmentComment, fileName);

		_equipments.put(eq);
		return eq;
	}

	/**
	 * Returns a map of the network equipments handled by the monitor.
	 * @return a map of the network equipments handled by the monitor.
	 */
	public NetworkEquipmentsByName getEquipments() {
		return _equipments;
	}

	/**
	 * Returns the probing done by the monitor.
	 * @return the probing done by the monitor.
	 */
	public Probing getProbing() {
		return _probing;
	}

	/**
	 * Configures the monitor.
	 * @param fileName filename of the configuration file.
	 */
	public void configure(String fileName) {

		_configurationFileName = fileName;

		/*
		 * Read the protocols from ressources.
		 */
		IPProtocols protocols = IPProtocols.getInstance();
		InputStream stream = protocols.getClass().getResourceAsStream("/ip/protocols");
		readProtocols(stream);

		/*
		 * Read the services from ressources.
		 */
		IPServices services = IPServices.getInstance();
		stream = services.getClass().getResourceAsStream("/ip/services");
		readServices(stream);

		/*
		 * Read the icmp-type from ressources.
		 */
		IPIcmp icmp = IPIcmp4.getInstance();
		stream = icmp.getClass().getResourceAsStream("/ip/icmp");
		readIcmp(icmp, stream);

		icmp = IPIcmp6.getInstance();
		stream = icmp.getClass().getResourceAsStream("/ip/icmp6");
		readIcmp(icmp, stream);

		/*
		 * Read the XML configuration file.
		 */
		_xmlConfiguration = XMLUtils.getXMLDocument(fileName);
		loadConfiguration(_xmlConfiguration);
		loadEquipments(_xmlConfiguration);
		loadDefines(_xmlConfiguration);
	}

	/**
	 * Initializes the monitor.
	 */
	public void init() {

		for (String name : _equipments.keySet()) {
			NetworkEquipment eq = _equipments.get(name);
			eq.configure();
			_topology.registerNetworkequipment(eq);
		}

		loadTopology(_xmlConfiguration, _topology);
		_topology.makeTopology();
	}

	/**
	 * Reload the monitor
	 */
	public void reload() {
		_equipments = new NetworkEquipmentsByName();
		_topology = new Topology();
		_probing = new Probing();
		configure(_configurationFileName);
		init();
	}

	/**
	 * Reload an equipment
	 */
	public void reloadEquipment(NetworkEquipment equipment) {

		/*
		 * new configuration
		 */
		Topology topology = new Topology();
		NetworkEquipmentsByName equipments = new NetworkEquipmentsByName();

		/*
		 * create a new equipment to replace the older
		 */
		NetworkEquipment newEq = createNetworkEquipment(
				equipment.getClass().getName(),
				equipment.getName(),
				equipment.getComment(),
				equipment.getConfigurationFileName()
			);
		newEq.configure();

		/*
		 * build the topology
		 */
		topology.registerNetworkequipment(newEq);
		equipments.put(newEq);
		for (NetworkEquipment eq: _equipments.values()) {
			if (eq == equipment)
				continue;
			topology.registerNetworkequipment(eq);
			equipments.put(eq);
		}

		Document xmlConfiguration = XMLUtils.getXMLDocument(_configurationFileName);
		loadTopology(xmlConfiguration, topology);
		topology.makeTopology();

		_topology = topology;
		_equipments = equipments;
	}

	/**
	 * Returns the topology.
	 * @return the topology.
	 */
	public Topology getTopology() {
		return _topology;
	}

	/**
	 * Receives a probe coming from an equipment.
	 * @param link probe's incoming link
	 * @param probe the probe received
	 * @param nextHop the nexthop to use. Can be null.
	 */
	public void receiveProbe(IfaceLink link, Probe probe, IPNet nextHop) {

		if (Log.debug().isLoggable(Level.INFO))
			Log.debug().info(probe.uidToString() +
				" received from: " + link.getEquipmentName() + " "
				+ link.toString() + " to: " + nextHop.toString("::i"));

		/*
		 * Ensure that the probing is done
		 */
		if (!probe.isProbingDone())
			throw new JtaclInternalException("probe" + probe.uidToString() +
				" probe must be in probingDone state");

		/*
		 * Forward the probe to the nextHop.
		 */
		NetworkLink nlink = link.getNetworkLink();

		IfaceLink hostLink = null;

		if (Log.debug().isLoggable(Level.INFO))
			Log.debug().info("network link: " + nlink.toString());

		/*
		 * If nextHop is the same as the network link, the probe has reached
		 * its destination
		 */
		if (nextHop.equals(nlink.getNetwork())) {
			hostLink = nlink.getIfaceLink(probe.getDestinationAddress().nearestNetwork());
			if (hostLink == null) {
				probe.destinationReached("destination reached");
				return;
			}
		}

		/*
		 * The "null" network designates all IP address
		 *
		 */
		if (hostLink == null && nlink.getNetwork().isNullNetwork()) {
			probe.destinationReached("destination reached");
			return;
		}

		/*
		 * Search the next interface link
		 */
		IfaceLink nextHopLink = nlink.getIfaceLink(nextHop);
		if (hostLink == null && nextHopLink == null) {
			/*
			 * If the network link is a border, we always accept the probe
			 * regardeless of the next hop.
			 */
			if (nlink.isBorderLink()) {
				probe.destinationReached("destination (border) reached");
				return;
			}
			probe.killNoRoute("host not found: " + nextHop.toString("i::"));
			return;
		}

		/*
		 * quick deny: stop if the probe is denied
		 */
		if (probe.getRequest().getProbeOptions().hasQuickDeny()) {
			AclResult aclresult = probe.getResults().getAclResult();
			if (aclresult.isCertainlyDeny()) {
				probe.destinationReached("quick denied");
				return;
			}
		}

		/*
		 * Create a new child probe to probe the next equipment and send
		 * the probe.
		 */
		Probe nextProbe = probe.newChild();
		if (hostLink == null)
			sendProbe(nextHopLink, nextProbe);
		else
			sendProbe(hostLink, nextProbe);
	}

	/**
	 * Sends a probe to a link.
	 * @param link link to use.
	 * @param probe probe to send.
	 */
	public void sendProbe(IfaceLink link, Probe probe) {

		/*
		 * If we have already seen this probe on this link, the probe is looping.
		 * So drop it.
		 */
		ProbesByUid probes = probe.getProbesTracker().probesByIfaceLinkAndProbe(link, probe);
		if (probe.getParentProbe() != null && !probes.isEmpty()) {

			if (Log.debug().isLoggable(Level.INFO)) {
				Log.debug().info("----- loop killer on " +
					link.getEquipmentName() +
					" probe" + probe.uidToString());
				Log.debug().info("probe: " + probe.showSimplePath());
			}

			boolean loop = false;
			for (Probe p: probes.values()) {
				if (Log.debug().isLoggable(Level.INFO))
					Log.debug().info("ppath: " + p.showSimplePath());

				if (p.getPositions().sameAs(probe.getParentsPositions()))
					loop = true;
			}
			if (loop) {
				Log.notifier().warning("Loop detected on " +
						link.getEquipmentName() +
						" probe" + probe.uidToString());
				probe.setIncomingLink(link);
				probe.killLoop("loop detected");
				return;
			}
		}

		if (_hopCount == 0) {
			probe.setIncomingLink(link);
			probe.killError("max hop count reached");
			Log.notifier().warning("probing stoped: max hop count reached");
			return;
		} else if (_hopCount > 0)
			_hopCount--;

		/*
		 * pass the probe to the link
		 */
		if (Log.debug().isLoggable(Level.INFO))
			Log.debug().info("probe" + probe.uidToString() +
						" probing on " +
						link.getEquipment() +
						" link: " + link.toString());

		probe.setIncomingLink(link);
		link.incoming(probe);
	}

	/**
	 * Resets the probing queue.
	 */
	public void resetProbing() {
		_probing.clear();
	}

	/**
	 * Starts the probing.
	 * @return the result of the probing.
	 */
	public Probing startProbing() {

		Probing result = new Probing();

		while (!_probing.isEmpty()) {
			ProbesTracker tracker = _probing.get(0);
			_hopCount = _options.getMaxHop();
			IfaceLink link = tracker.getRootProbe().getIncomingLink();
			sendProbe(link, tracker.getRootProbe());

			/*
			 * The path of final probes should unique
			 */
			if (!tracker.checkFinalProbePath())
				Log.notifier().warning("Resulting paths should be unique");
			_probing.remove(0);
			result.add(tracker);
		}
		return result;
	}

	/**
	 * Queues a new probing.
	 * @param link link to use.
	 * @param sourceAddress source IP range addresses of the probing.
	 * @param destinationAddress destination IP range addresses of the probing.
	 * @param request the request of the probing.
	 */
	public void newProbing(IfaceLink link, IPRangeable sourceAddress,
			IPRangeable destinationAddress, ProbeRequest request) {

		ProbesTracker tracker = new ProbesTracker();
		Probe probe = new Probe(tracker, sourceAddress, destinationAddress, request);
		probe.setIncomingLink(link);
		tracker.setRootProbe(probe);
		_probing.add(tracker);
	}

	/**
	 * Returns the options.
	 * @return the options.
	 */
	public Options getOptions() {
		return _options;
	}

	/**
	 * Returns the defined variables.
	 * @return the defined variables.
	 */
	public Map<String, String> getDefines() {
		return _defines;
	}

}
