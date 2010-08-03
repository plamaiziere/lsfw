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
import fr.univrennes1.cri.jtacl.core.exceptions.JtaclInternalException;
import fr.univrennes1.cri.jtacl.core.network.IfaceLink;
import fr.univrennes1.cri.jtacl.core.network.NetworkEquipment;
import fr.univrennes1.cri.jtacl.core.network.NetworkEquipmentsByName;
import fr.univrennes1.cri.jtacl.core.topology.NetworkLink;
import fr.univrennes1.cri.jtacl.core.topology.Topology;
import fr.univrennes1.cri.jtacl.lib.ip.IPIcmp;
import fr.univrennes1.cri.jtacl.lib.ip.IPIcmp4;
import fr.univrennes1.cri.jtacl.lib.ip.IPIcmp6;
import fr.univrennes1.cri.jtacl.lib.ip.IPNet;
import fr.univrennes1.cri.jtacl.lib.ip.IPProtocols;
import fr.univrennes1.cri.jtacl.lib.ip.IPServices;
import fr.univrennes1.cri.jtacl.lib.xml.XMLUtils;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.w3c.dom.Document;

/**
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class Monitor {

	protected static Monitor _instance = new Monitor();
	public static Monitor getInstance() {
		return _instance;
	}

	protected Document _xmlConfiguration;
	protected int _hopCount;
	protected Options _options;
	protected Map<String, String> _defines;
	protected NetworkEquipmentsByName _equipments;
	protected Topology _topology;
	protected Probing _probing;

	protected Monitor() {
		_options = new Options();
		_defines = new HashMap<String, String>();
		_equipments = new NetworkEquipmentsByName();
		_topology = new Topology();
		_probing = new Probing();
	}

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

	public void addEquipment(String className,	String equipmentName,
			String equipmentComment, String fileName) {

		String s = "className: " + className + " name: " + equipmentName +
				" comment: " + equipmentComment +
				" filename: " + fileName;
		if (_equipments.containsKey(equipmentName))
			throw new JtaclConfigurationException("duplicate equipment: " + s);
		NetworkEquipment eq = createNetworkEquipment(className, equipmentName,
				equipmentComment, fileName);

		_equipments.put(eq);
	}

	public NetworkEquipmentsByName getEquipments() {
		return _equipments;
	}

	public Probing getProbing() {
		return _probing;
	}

	public void configure(String fileName) {

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
		XMLConfigLoader config = new XMLConfigLoader(this);
		config.loadConfiguration(_xmlConfiguration);
		config.loadEquipments(_xmlConfiguration);
		config.loadDefines(_xmlConfiguration);
	}

	public void init() {
		
		for (String name : _equipments.keySet()) {
			NetworkEquipment eq = _equipments.get(name);
			eq.configure();
			_topology.registerNetworkequipment(eq);
		}

		XMLConfigLoader config = new XMLConfigLoader(this);
		config.loadTopology(_xmlConfiguration);
		_topology.makeTopology();
	}

	public Topology getTopology() {
		return _topology;
	}

	public void receiveProbe(IfaceLink link, Probe probe, IPNet nextHop) {

		if (Log.debug().isLoggable(Level.INFO))
			Log.debug().info(probe.uidToString() +
				" received from: " + link.getIface().getEquipment().getName() + " "
				+ link.toString() + " to: " + nextHop.toString(":i"));

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

		if (Log.debug().isLoggable(Level.INFO))
			Log.debug().info("network link: " + nlink.toString());

		/*
		 * If nextHop is the same as the network link, the probe has reached
		 * its destination
		 */
		if (nextHop.equals(nlink.getNetwork())) {
			probe.destinationReached("destination reached");
			return;
		}

		/*
		 * The "null" network designates all IP address
		 *
		 */
		if (nlink.getNetwork().isNullNetwork()) {
			probe.destinationReached("destination reached");
			return;
		}

		/*
		 * Search the next interface link
		 */
		IfaceLink nextHopLink = nlink.getIfaceLink(nextHop);
		if (nextHopLink == null) {
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
		 * Create a new child probe to probe the next equipment and send
		 * the probe.
		 */
		Probe nextProbe = probe.newChild();
		sendProbe(nextHopLink, nextProbe);
	}

	public void sendProbe(IfaceLink link, Probe probe) {

		/*
		 * If we have already seen this probe on this link, the probe is looping.
		 * So drop it.
		 */
		ProbesByUid probes = probe.getProbesTracker().probesByIfaceLinkAndProbe(link, probe);
		if (probe.getParentProbe() != null && !probes.isEmpty()) {

			if (Log.debug().isLoggable(Level.INFO)) {
				Log.debug().info("----- loop killer on " +
					link.getIface().getEquipment().getName() +
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
						link.getIface().getEquipment().getName() +
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
						link.getIface().getEquipment() +
						" link: " + link.toString());
		
		probe.setIncomingLink(link);
		link.incoming(probe);
	}

	public void resetProbing() {
		_probing.clear();
	}

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

	public void newProbing(IfaceLink link, IPNet sourceAddress,
			IPNet destinationAddress, ProbeRequest request) {
		
		ProbesTracker tracker = new ProbesTracker();
		Probe probe = new Probe(tracker, sourceAddress, destinationAddress, request);
		probe.setIncomingLink(link);
		tracker.setRootProbe(probe);
		_probing.add(tracker);
	}
	
	public Options getOptions() {
		return _options;
	}

	public Map<String, String> getDefines() {
		return _defines;
	}

}
