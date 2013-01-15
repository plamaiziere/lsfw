/*
 * Copyright (c) 2013, Universite de Rennes 1
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

package fr.univrennes1.cri.jtacl.equipments.checkpoint;

import fr.univrennes1.cri.jtacl.analysis.IPNetCrossRef;
import fr.univrennes1.cri.jtacl.core.exceptions.JtaclConfigurationException;
import fr.univrennes1.cri.jtacl.core.monitor.Log;
import fr.univrennes1.cri.jtacl.core.monitor.Monitor;
import fr.univrennes1.cri.jtacl.core.network.Iface;
import fr.univrennes1.cri.jtacl.core.network.IfaceLink;
import fr.univrennes1.cri.jtacl.core.network.Route;
import fr.univrennes1.cri.jtacl.core.network.Routes;
import fr.univrennes1.cri.jtacl.core.probing.Probe;
import fr.univrennes1.cri.jtacl.equipments.generic.GenericEquipment;
import fr.univrennes1.cri.jtacl.lib.ip.IPIcmpEnt;
import fr.univrennes1.cri.jtacl.lib.ip.IPNet;
import fr.univrennes1.cri.jtacl.lib.misc.Direction;
import fr.univrennes1.cri.jtacl.lib.misc.ParseContext;
import fr.univrennes1.cri.jtacl.lib.misc.StringsList;
import fr.univrennes1.cri.jtacl.lib.xml.XMLUtils;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Checkpoint Firewall
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class CPfw extends GenericEquipment {

	protected class CPfwIface {
		protected Iface _iface;
		protected String _name;
		protected String _description;

		public CPfwIface(Iface iface) {
			_iface = iface;
		}

		public Iface getIface() {
			return _iface;
		}

		public void setIface(Iface iface) {
			_iface = iface;
		}

		public String getDescription() {
			return _description;
		}

		public void setDescription(String description) {
			_description = description;
		}

		public String getName() {
			return _name;
		}

		public void setName(String name) {
			_name = name;
		}
	}

	/**
	 * Configuration file, mapped into strings.
	 */
	protected class ConfigurationFile extends StringsList {

		protected String _fileName;
		public String getFileName() {
			return _fileName;
		}

		@Override
		public void readFromFile(String fileName)
				throws FileNotFoundException, IOException {
			super.readFromFile(fileName);
			_fileName = fileName;
		}
	}

	/**
	 * the list of configuration files, mapped into strings.
	 */
	protected ArrayList<ConfigurationFile> _configurations;

	/**
	 * interfaces
	 */
	protected HashMap<String, CPfwIface> _cpfwIfaces = new HashMap<String, CPfwIface>();

	/**
	 * IPNet cross references
	 */
	protected Map<IPNet, IPNetCrossRef> _netCrossRef =
			new HashMap<IPNet, IPNetCrossRef>();

	/**
	 * parse context
	 */
	 protected ParseContext _parseContext = new ParseContext();

	/**
	 * IPNet cross references
	 */
	Map<IPNet, IPNetCrossRef> getNetCrossRef() {
		return _netCrossRef;
	}

	/**
	 * Create a new {@link CiscoRouter} with this name and this comment.<br/>
	 * @param monitor the {@link Monitor} monitor associated with this equipment.
	 * @param name the name of the equipment.
	 * @param comment a free comment for this equipment.
	 * @param configurationFileName name of the configuration file to use (may be null).
	 */
	public CPfw(Monitor monitor, String name, String comment, String configurationFileName) {
		super(monitor, name, comment, configurationFileName);
		CPfwShell shell = new CPfwShell(this);
		registerShell(shell);
	}

	protected void loadConfiguration(Document doc) {
		_configurations = new ArrayList<ConfigurationFile>();

		NodeList list = doc.getElementsByTagName("file");
		for (int i = 0; i < list.getLength(); i++) {
			Element e = (Element) list.item(i);
			String filename = e.getAttribute("filename");
			if (filename.isEmpty())
				throw new JtaclConfigurationException("Missing file name");
			ConfigurationFile cfg = new ConfigurationFile();
			try {
				cfg.readFromFile(filename);
			} catch (FileNotFoundException ex) {
				throw new JtaclConfigurationException("File not found:" + filename);
			} catch (IOException ex) {
				throw new JtaclConfigurationException("Cannot read file :" + filename);
			}
			famAdd(filename);
			_configurations.add(cfg);
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
			CPfwIface cpfwIface;
			Iface iface = getIface(name);
			if (iface == null) {
				if (comment.isEmpty())
					throw new JtaclConfigurationException("Missing interface comment: " + s);
				iface = addIface(name, comment);
				cpfwIface = new CPfwIface(iface);
				_cpfwIfaces.put(name, cpfwIface);
			}
			iface.addLink(ip, network);
		}
	}

	protected void throwCfgException(String msg) {
		throw new JtaclConfigurationException(_parseContext.toString() + msg);
	}

	protected IPNet parseIp(String ip) {

		IPNet ipnet = null;
		try {
			ipnet = new IPNet(ip);
		} catch (UnknownHostException ex) {
			throwCfgException("invalid IP address: " + ex.getMessage());
		}
		return ipnet;
	}

	protected int parseService(String service, String protocol) {

		int port = _ipServices.serviceLookup(service, protocol);
		if (port == -1)
			throwCfgException("unknown service");
		return port;
	}

	protected int parseProtocol(String protocol) {
		int proto = _ipProtocols.protocolLookup(protocol);
		if (proto == -1)
			throwCfgException("unknown protocol");
		return proto;
	}

	protected IPIcmpEnt parseIcmp4(String icmpName) {
		IPIcmpEnt icmp = _ipIcmp4Types.icmpLookup(icmpName);
		if (icmp == null)
			throwCfgException("unknown icmp-type or message");
		return icmp;
	}

	protected IPIcmpEnt parseIcmp6(String icmpName) {
		IPIcmpEnt icmp = _ipIcmp6Types.icmpLookup(icmpName);
		if (icmp == null)
			throwCfgException("unknown icmp-type or message");
		return icmp;
	}

	@Override
	public void configure() {
		if (_configurationFileName.isEmpty())
			return;

		/*
		 * Read the XML configuration file
		 */
		famAdd(_configurationFileName);
		Document doc = XMLUtils.getXMLDocument(_configurationFileName);
		loadOptionsFromXML(doc);
		loadFiltersFromXML(doc);

		loadIfaces(doc);
		loadConfiguration(doc);

		/*
		 * routing
		 */
		routeDirectlyConnectedNetworks();
		loadRoutesFromXML(doc);
		/*
		 * compute cross reference
		 */
		ipNetCrossReference();
	}

	protected IPNetCrossRef getIPNetCrossRef(IPNet ipnet) {
		IPNetCrossRef ref = _netCrossRef.get(ipnet);
		if (ref == null) {
			ref = new IPNetCrossRef(ipnet);
			_netCrossRef.put(ipnet, ref);
		}
		return ref;
	}

	/**
	 * Compute IPNet cross references
	 */
	protected void ipNetCrossReference() {
		//TODO
	}

	@Override
	public void incoming(IfaceLink link, Probe probe) {

		if (Log.debug().isLoggable(Level.INFO))
			Log.debug().info("probe" + probe.uidToString() + " incoming on " + _name);

		probe.decTimeToLive();
		if (!probe.isAlive()) {
			probe.killError("TimeToLive expiration");
			return;
		}

		/*
		 * Filter in the probe
		 */
		packetFilter(link, Direction.IN, probe);

		/*
		 * Check if the destination of the probe is on this equipment.
		 */
		IfaceLink ilink = getIfaceLink(probe.getDestinationAddress());
		if (ilink != null) {
			/*
			 * Set the probe's final position and notify the monitor
			 */
			probe.setOutgoingLink(ilink, probe.getDestinationAddress());
			probe.destinationReached("destination reached");
			return;
		}

		/*
		 * Route the probe.
		 */
		Routes routes = _routingEngine.getRoutes(probe);
		if (routes.isEmpty()) {
			probe.killNoRoute("No route to " + probe.getDestinationAddress());
			return;
		}
		probe.routed(probe.getDestinationAddress().toString("i::"));

		if (Log.debug().isLoggable(Level.INFO)) {
			for (Route r: routes) {
				Log.debug().info("route: " + r.toString());
			}
		}

		/*
		 * if we have several routes for a destination, we have to probe these
		 * routes too because our goal is to know if a probe is able to
		 * reach a destination, regardless of the route taken.
		 */
		ArrayList<Probe> probes = new ArrayList<Probe>();
		probes.add(probe);

		/*
		 * Create copies of the incoming probe to describe the other routes.
		 */
		for (int i = 1; i < routes.size(); i ++) {
			probes.add(probe.newInstance());
		}

		/*
		 * Set the position of the probes.
		 */
		for (int i = 0; i < routes.size(); i ++) {
			Route<IfaceLink> route = routes.get(i);
			probes.get(i).setOutgoingLink(route.getLink(), route.getNextHop());
		}

		/*
		 * Filter out the probes
		 */
		for (Probe p: probes) {
			packetFilter(p.getOutgoingLink(), Direction.OUT, p);
		}

		/*
		 * Send the probes over the network.
		 */
		for (Probe p: probes) {
			/*
			 * Do not send the probe if the outgoing link is the same as the input.
			 */
			if (!p.getOutgoingLink().equals(link))
				outgoing(p.getOutgoingLink(), p, p.getNextHop());
			else
				probe.killLoop("same incoming and outgoing link");
		}
	}

	/**
	 * Packet filter
	 */
	protected void packetFilter (IfaceLink link, Direction direction, Probe probe) {
		//TODO
	}


}
