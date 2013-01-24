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
import fr.univrennes1.cri.jtacl.lib.ip.AddressFamily;
import fr.univrennes1.cri.jtacl.lib.ip.IPIcmpEnt;
import fr.univrennes1.cri.jtacl.lib.ip.IPNet;
import fr.univrennes1.cri.jtacl.lib.misc.Direction;
import fr.univrennes1.cri.jtacl.lib.misc.ParseContext;
import fr.univrennes1.cri.jtacl.lib.misc.StringTools;
import fr.univrennes1.cri.jtacl.lib.xml.XMLUtils;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.parboiled.Parboiled;
import org.parboiled.parserunners.BasicParseRunner;
import org.parboiled.support.ParsingResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Checkpoint Firewall
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class CpFw extends GenericEquipment {

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
	 * Parser
	 */
	protected CpParser _parser = Parboiled.createParser(CpParser.class);

	/**
	 * interfaces
	 */
	protected HashMap<String, CPfwIface> _cpfwIfaces
		= new HashMap<String, CPfwIface>();

	/**
	 * IPNet cross references
	 */
	protected Map<IPNet, IPNetCrossRef> _netCrossRef
			= new HashMap<IPNet, IPNetCrossRef>();

	/**
	 * parse context
	 */
	 protected ParseContext _parseContext = new ParseContext();

	/*
	 * services keyed by name
	 */
	protected HashMap<String, CpService> _services
			= new HashMap<String, CpService>();

	/**
	 * IPNet cross references
	 */
	Map<IPNet, IPNetCrossRef> getNetCrossRef() {
		return _netCrossRef;
	}

	/**
	 * Create a new {@link CpFw} with this name and this comment.<br/>
	 * @param monitor the {@link Monitor} monitor associated with this equipment.
	 * @param name the name of the equipment.
	 * @param comment a free comment for this equipment.
	 * @param configurationFileName name of the configuration file to use (may be null).
	 */
	public CpFw(Monitor monitor, String name, String comment, String configurationFileName) {
		super(monitor, name, comment, configurationFileName);
		CpFwShell shell = new CpFwShell(this);
		registerShell(shell);
	}

	protected CpPortItem parsePort(String sPorts) {

		ParsingResult<?> result = new BasicParseRunner(
			_parser.CpPortItem()).run(sPorts);
		if (!result.matched)
			throwCfgException("invalid port specification: " + sPorts, true);

		PortItemTemplate port = _parser.getPortItem();

		String sfirst = port.getFirstPort();
		String slast = port.getLastPort();
		int first = -1;
		int last = -1;
		try {
			first = Integer.parseInt(sfirst);
		} catch (NumberFormatException ex) {
			throwCfgException("invalid port number: " + sfirst, true);
		}
		if (slast != null ) {
			try {
				last = Integer.parseInt(slast);
			} catch (NumberFormatException ex) {
				throwCfgException("invalid port number: " + slast, true);
			}
		}

		String operator = port.getOperator();
		if (operator == null)
			operator = "=";

		CpPortItem portItem;
		if (last != -1) {
			portItem = new CpPortItem(operator, first, last);
		} else {
			portItem = new CpPortItem(operator, first);
		}
		return portItem;
	}

	protected CpService parseTcpUdpService(Element e) {

		String sName = XMLUtils.getTagValue(e, "Name");
		String sComment = XMLUtils.getTagValue(e, "comments");
		String sType = XMLUtils.getTagValue(e, "type");
		String sPorts = XMLUtils.getTagValue(e, "port");
		String sSourcePorts = XMLUtils.getTagValue(e, "src_port");
		String sInAny = XMLUtils.getTagValue(e, "include_in_any");

		/*
		 * ports
		 */
		CpPortItem portItem = parsePort(sPorts);
		CpPortItem srcPortItem =
			sSourcePorts == null ? null : parsePort(sSourcePorts);

		/*
		 * in any
		 */
		boolean inAny = Boolean.parseBoolean(sInAny);
		/*
		 * service type
		 */
		CpService service = null;
		if (sType.equalsIgnoreCase("tcp"))
			service = new CpTcpService(sName, sComment, portItem, srcPortItem,
				inAny);
		if (sType.equalsIgnoreCase("udp"))
			service = new CpUdpService(sName, sComment, portItem, srcPortItem,
				inAny);

		return service;
	}

	protected CpService parseIcmpService(Element e) {

		String sName = XMLUtils.getTagValue(e, "Name");
		String className = XMLUtils.getTagValue(e, "Class_Name");
		String sComment = XMLUtils.getTagValue(e, "comments");
		String sIcmpType = XMLUtils.getTagValue(e, "icmp_type");
		String sIcmpCode = XMLUtils.getTagValue(e, "icmp_code");

		/*
		 * icmp code and type
		 */
		int icmpCode = -1;
		if (sIcmpCode != null) {
			icmpCode = Integer.parseInt(sIcmpCode);
		}
		int icmpType = Integer.parseInt(sIcmpType);

		/*
		 * address family
		 */
		AddressFamily af = AddressFamily.INET;
		if (className.equalsIgnoreCase("icmpv6_service"))
			af = AddressFamily.INET6;

		CpService service = new CpIcmpService(sName, sComment, af,
			icmpType, icmpCode);

		return service;
	}

	protected CpService parseOtherService(Element e) {

		String sName = XMLUtils.getTagValue(e, "Name");
		String sComment = XMLUtils.getTagValue(e, "comments");
		String sProtocol = XMLUtils.getTagValue(e, "protocol");
		String sExp = XMLUtils.getTagValue(e, "exp");
		String sInAny = XMLUtils.getTagValue(e, "include_in_any");

		/*
		 * protocol
		 */
		Integer proto = sProtocol == null ? null : Integer.parseInt(sProtocol);

		/*
		 * in any
		 */
		boolean inAny = Boolean.parseBoolean(sInAny);

		CpService service =
			new CpOtherService(sName, sComment, proto, sExp, inAny);
		return service;
	}

	protected CpService parseServiceGroup(Element e) {
		String sName = XMLUtils.getTagValue(e, "Name");
		String sComment = XMLUtils.getTagValue(e, "comments");

		CpGroupService service = new CpGroupService(sName, sComment);

		/*
		 * members of this group (should be one member)
		 */
		List<Element> members = XMLUtils.getDirectChildren(e, "members");

		/*
		 * each reference
		 */
		List<Element> references =
			XMLUtils.getDirectChildren(members.get(0), "reference");
		for (Element reference: references) {
			String refName = XMLUtils.getTagValue(reference, "Name");
			service.addReference(refName, null);
		}

		return service;
	}

	protected void loadServices(String filename) {

		Document doc = XMLUtils.getXMLDocument(filename);
		doc.getDocumentElement().normalize();

		_parseContext = new ParseContext();
		CpService service;

		Element root = doc.getDocumentElement();
		List<Element> list = XMLUtils.getDirectChildren(root, "service");
		int i = 0;
		for (Element e: list) {
			_parseContext.set(filename, i,
				StringTools.stripWhiteSpacesCrLf(e.getTextContent()));
			String className = XMLUtils.getTagValue(e, "Class_Name");
			service = null;
			if (className.equalsIgnoreCase("tcp_service"))
				service = parseTcpUdpService(e);
			if (className.equalsIgnoreCase("udp_service"))
				service = parseTcpUdpService(e);
			if (className.equalsIgnoreCase("icmp_service"))
				service = parseIcmpService(e);
			if (className.equalsIgnoreCase("icmpv6_service"))
				service = parseIcmpService(e);
			if (className.equalsIgnoreCase("other_service"))
				service = parseOtherService(e);
			if (className.equalsIgnoreCase("service_group"))
				service = parseServiceGroup(e);
			i++;

			if (service != null)
				_services.put(service.getName(), service);

			if (service != null && Log.debug().isLoggable(Level.INFO)) {
			Log.debug().info("CpService: " + service.toString());
			}
		}
		linkServices();
	System.exit(0);
	}

	protected void linkServices() {
		/*
		 * each service
		 */
		for (String serviceName: _services.keySet()) {
			CpService service = _services.get(serviceName);
			if (service.getType() != CpServiceType.GROUP)
				continue;
			/*
			 * resolves the references of the group
			 */
			CpGroupService group = (CpGroupService) service;
			for (String refName: group.getReferencesName()) {
				CpService ref = _services.get(refName);
				if (ref == null) {
					warnConfig("cannot link service group: "
							+ serviceName + " to member: " + refName, false);
				} else {
					group.addReference(refName, ref);
				}
			}
		}
	}

	protected void loadConfiguration(Document doc) {

		/* services */
		NodeList list = doc.getElementsByTagName("services");
		if (list.getLength() < 1) {
			throw new JtaclConfigurationException(
					"At least one services file must be specified");
		}
		for (int i = 0; i < list.getLength(); i++) {
			Element e = (Element) list.item(i);
			String filename = e.getAttribute("filename");
			if (filename.isEmpty())
				throw new JtaclConfigurationException("Missing services file name");
			loadServices(filename);
			famAdd(filename);
		}

		/* network_objects */
		list = doc.getElementsByTagName("network_objects");
		if (list.getLength() < 1) {
			throw new JtaclConfigurationException(
					"At least one network_object file must be specified");
		}
		for (int i = 0; i < list.getLength(); i++) {
			Element e = (Element) list.item(i);
			String filename = e.getAttribute("filename");
			if (filename.isEmpty())
				throw new JtaclConfigurationException("Missing netork_objects file name");
		/* TODO parse file */
			famAdd(filename);
		}

		/* fwpolicies */
		list = doc.getElementsByTagName("fwpolicies");
		if (list.getLength() < 1) {
			throw new JtaclConfigurationException(
				"At least one fwpolicies file must be specified");
		}
		for (int i = 0; i < list.getLength(); i++) {
			Element e = (Element) list.item(i);
			String filename = e.getAttribute("filename");
			if (filename.isEmpty())
				throw new JtaclConfigurationException("Missing fwpolicies file name");
			/* TODO parse file */
			famAdd(filename);
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

	protected IPNet parseIp(String ip) {

		IPNet ipnet = null;
		try {
			ipnet = new IPNet(ip);
		} catch (UnknownHostException ex) {
			throwCfgException("invalid IP address: " + ex.getMessage(), true);
		}
		return ipnet;
	}

	protected int parseService(String service, String protocol) {

		int port = _ipServices.serviceLookup(service, protocol);
		if (port == -1)
			throwCfgException("unknown service", true);
		return port;
	}

	protected int parseProtocol(String protocol) {
		int proto = _ipProtocols.protocolLookup(protocol);
		if (proto == -1)
			throwCfgException("unknown protocol", true);
		return proto;
	}

	protected IPIcmpEnt parseIcmp4(String icmpName) {
		IPIcmpEnt icmp = _ipIcmp4Types.icmpLookup(icmpName);
		if (icmp == null)
			throwCfgException("unknown icmp-type or message", true);
		return icmp;
	}

	protected IPIcmpEnt parseIcmp6(String icmpName) {
		IPIcmpEnt icmp = _ipIcmp6Types.icmpLookup(icmpName);
		if (icmp == null)
			throwCfgException("unknown icmp-type or message", true);
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
