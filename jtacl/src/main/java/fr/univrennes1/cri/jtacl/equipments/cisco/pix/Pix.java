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

package fr.univrennes1.cri.jtacl.equipments.cisco.pix;

import fr.univrennes1.cri.jtacl.analysis.CrossRefContext;
import fr.univrennes1.cri.jtacl.analysis.IPNetCrossRef;
import fr.univrennes1.cri.jtacl.lib.misc.Direction;
import fr.univrennes1.cri.jtacl.core.exceptions.JtaclConfigurationException;
import fr.univrennes1.cri.jtacl.core.exceptions.JtaclInternalException;
import fr.univrennes1.cri.jtacl.core.probing.AclResult;
import fr.univrennes1.cri.jtacl.core.monitor.Log;
import fr.univrennes1.cri.jtacl.core.monitor.Monitor;
import fr.univrennes1.cri.jtacl.core.probing.Probe;
import fr.univrennes1.cri.jtacl.core.probing.ProbeRequest;
import fr.univrennes1.cri.jtacl.core.probing.ProbeResults;
import fr.univrennes1.cri.jtacl.core.network.Iface;
import fr.univrennes1.cri.jtacl.core.network.IfaceLink;
import fr.univrennes1.cri.jtacl.core.network.Route;
import fr.univrennes1.cri.jtacl.core.network.Routes;
import fr.univrennes1.cri.jtacl.equipments.generic.GenericEquipment;
import fr.univrennes1.cri.jtacl.lib.ip.IPIcmpEnt;
import fr.univrennes1.cri.jtacl.lib.ip.IPNet;
import fr.univrennes1.cri.jtacl.core.probing.MatchResult;
import fr.univrennes1.cri.jtacl.lib.ip.PortSpec;
import fr.univrennes1.cri.jtacl.lib.misc.ParseContext;
import fr.univrennes1.cri.jtacl.lib.misc.StringsList;
import fr.univrennes1.cri.jtacl.lib.xml.XMLUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.parboiled.Parboiled;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class Pix extends GenericEquipment implements GroupTypeSearchable {

	protected class CiscoIface {
		protected Iface _iface;
		protected String _name;
		protected String _description;
		protected boolean _shutdown;
		protected ArrayList<IPNet> _ipAddresses = new ArrayList<IPNet>();

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

		public boolean isShutdown() {
			return _shutdown;
		}

		public void setShutdown(boolean shutdown) {
			_shutdown = shutdown;
		}

		public ArrayList<IPNet> getIpAddresses() {
			return _ipAddresses;
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
	protected HashMap<String, CiscoIface> _ciscoIfaces = new HashMap<String, CiscoIface>();

	/**
	 * Names defined by the command name
	 */
	protected HashMap<String, PixName> _names = new HashMap<String, PixName>();

	/**
	 * network object group defined by the command object-group network
	 */
	protected HashMap<String, ObjectGroup> _networkGroups =
			new HashMap<String, ObjectGroup>();

	/**
	 * service object group defined by the command object-group service
	 */
	protected HashMap<String, ObjectGroup> _serviceGroups =
			new HashMap<String, ObjectGroup>();

	/**
	 * enhanced service object group defined by the command object-group
	 * service
	 */
	protected HashMap<String, ObjectGroup> _enhancedGroups =
			new HashMap<String, ObjectGroup>();

	/**
	 * protocol object group defined by the command object-group protocol
	 */
	protected HashMap<String, ObjectGroup> _protocolGroups =
			new HashMap<String, ObjectGroup>();

	/**
	 * icmp-type object group defined by the command object-group icmp-type
	 */
	protected HashMap<String, ObjectGroup> _icmpGroups =
			new HashMap<String, ObjectGroup>();

	/**
	 * access-group defined by the command access-group.
	 */
	protected HashMap<String, AccessGroup> _accessGroups =
			new HashMap<String, AccessGroup>();

	/**
	 * access list groups
	 */
	protected HashMap<String, AccessListGroup> _accessListGroups =
			new HashMap<String, AccessListGroup>();

	/**
	 * IPNet cross references
	 */
	protected Map<IPNet, IPNetCrossRef> _netCrossRef =
			new HashMap<IPNet, IPNetCrossRef>();

	/**
	 * parse context
	 */
	protected ParseContext _parseContext = new ParseContext();

	HashMap<String, AccessGroup> getAccessGroups() {
		return _accessGroups;
	}

	HashMap<String, AccessListGroup> getAccessListGroups() {
		return _accessListGroups;
	}

	ArrayList<ConfigurationFile> getConfigurations() {
		return _configurations;
	}

	HashMap<String, ObjectGroup> getEnhancedGroups() {
		return _enhancedGroups;
	}

	HashMap<String, ObjectGroup> getIcmpGroups() {
		return _icmpGroups;
	}

	HashMap<String, PixName> getNames() {
		return _names;
	}

	HashMap<String, ObjectGroup> getNetworkGroups() {
		return _networkGroups;
	}

	HashMap<String, ObjectGroup> getProtocolGroups() {
		return _protocolGroups;
	}

	HashMap<String, ObjectGroup> getServiceGroups() {
		return _serviceGroups;
	}

	/*
	 * IPNet cross reference
	 */
	Map<IPNet, IPNetCrossRef> getNetCrossRef() {
		return _netCrossRef;
	}

	/**
	 * Create a new {@link Pix} with this name and this comment.<br/>
	 * @param monitor the {@link Monitor} monitor associated with this equipment.
	 * @param name the name of the equipment.
	 * @param comment a free comment for this equipment.
	 * @param configurationFileName name of the configuration file to use (may be null).
	 */
	public Pix(Monitor monitor, String name, String comment, String configurationFileName) {
		super(monitor, name, comment, configurationFileName);
		PixShell shell = new PixShell(this);
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

	protected String nameLookup(String name) {
		PixName result = _names.get(name);
		if (result != null) {
			result.incRefCount();
			return result.getIpValue();
		}
		return name;
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

	protected void parseInterfaces(ConfigurationFile cfg) {
		boolean inInterface = false;
		ParsingResult<?> result;

		CiscoIface curCsIface = null;
		PixParser parser = Parboiled.createParser(PixParser.class);

		for (int i = 0; i < cfg.size(); i++) {
			String line = cfg.get(i);
			line = parser.stripWhiteSpaces(line);
			_parseContext = new ParseContext();
			_parseContext.set(cfg.getFileName(), i + 1, line);
			String lineCfg = parser.stripComment(line).trim();
			lineCfg = filter(lineCfg);
			if (inInterface) {
				result = ReportingParseRunner.run(parser.ExitInterface(), lineCfg);
				if (result.matched) {
					inInterface = false;
					curCsIface = null;
				} else {
					result = ReportingParseRunner.run(parser.InInterface(), lineCfg);
					if (result.matched) {
						String rule = parser.getRuleName();

						/*
						 * nameif
						 */
						if (rule.equals("nameif"))
							curCsIface.setName(parser.getName());

						/*
						 * ip address
						 */
						if (rule.equals("ip address")) {
							String ip = nameLookup(parser.getIpAddress());
							String ipNetmask = nameLookup(parser.getIpNetmask());
							IPNet ipnet = parseIp(ip + "/" + ipNetmask);
							curCsIface.getIpAddresses().add(ipnet);
						}

						/*
						 * ipv6 address
						 */
						if (rule.equals("ipv6 address")) {
							String ipv6Address = nameLookup(parser.getIpAddress());
							IPNet ipnet = parseIp(ipv6Address);
							curCsIface.getIpAddresses().add(ipnet);
						}
						dumpConfiguration(line);
					} else {
						/*
						 * check if we should match a rule
						 */
						if (parser.shouldMatchInInterface(lineCfg)) {
							Log.config().warning(_parseContext.toString() +
								"does not match any rule (but should).");
						}
					}
				}
			}
			if (lineCfg.startsWith("interface")) {
				result = ReportingParseRunner.run(parser.Interface(), lineCfg);
				if (result.matched) {
					inInterface = true;
					String interfaceName = parser.getName();
					// strip any space in the name.
					interfaceName = interfaceName.replace(" ", "");
					curCsIface = new CiscoIface();
					curCsIface.setName(interfaceName);
					curCsIface.setShutdown(parser.getShutdown() != null);
					_ciscoIfaces.put(curCsIface.getName(), curCsIface);

					dumpConfiguration(line);
				}
			}
		}
	}

	protected void addInterface(CiscoIface csIface) {
		String description = "";
		if (csIface.getDescription() != null)
			description = csIface.getDescription();

		Iface iface = addIface(csIface.getName(), description);

		for (IPNet ip: csIface.getIpAddresses()) {
			try {
				IPNet hostIP = ip.hostAddress();
				IPNet networkIP = ip.networkAddress();
				iface.addLink(hostIP, networkIP);
			} catch (UnknownHostException ex) {
				throw new JtaclConfigurationException("Iface: " + csIface.getName() +
						" Invalid IP address: " + ex.getMessage());
			}
		}
	}

	/*
	 * ip route and ipv6 route
	 */
	private void ruleRoute(String rule, PixParser parser) {

		String routeIPAddress = nameLookup(parser.getIpAddress());
		String routeIPNetmask = nameLookup(parser.getIpNetmask());
		String routeNexthop = nameLookup(parser.getNexthop());
		/*
		 * null route
		 */
		if (routeNexthop == null || routeNexthop.equalsIgnoreCase("null0"))
			return;

		try {
			IPNet prefix= null;
			// IPv4
			if (rule.equals("ip route") || rule.equals("route"))
				prefix = parseIp(routeIPAddress + "/" + routeIPNetmask);
			// IPv6
			if (rule.equals("ipv6 route"))
				prefix = parseIp(routeIPAddress);

			/*
			 * Retrieve the link associated to the nexthop (may be null)
			 */
			IPNet nexthop = parseIp(routeNexthop);
			Iface iface = getIfaceConnectedTo(nexthop);
			IfaceLink link = null;
			if (iface != null)
				link = iface.getLinkConnectedTo(nexthop);
			/*
			 * Add the route.
			 */
			Route<IfaceLink> route = new Route<IfaceLink>(prefix, nexthop, 1, link);
			Log.debug().info(_name + " add route: " + route.toString());
			_routingEngine.addRoute(route);

		} catch (UnknownHostException ex) {
				throwCfgException("Invalid route: " + ex.getMessage());
		}

	}

	// used while parsing
	private ObjectGroup _lastGroup;

	private HashMap<String, ObjectGroup> getObjectGroups(ObjectGroupType type) {
		switch (type) {
			case NETWORK:
				return _networkGroups;
			case PROTOCOL:
				return _protocolGroups;
			case SERVICE:
				return _serviceGroups;
			case ENHANCED:
				return _enhancedGroups;
			case ICMP:
				return _icmpGroups;
		}
		return null;
	}

	/*
	 * object-group
	 */
	private void ruleObjectGroup(PixParser parser) {

		ObjectGroupType type = null;
		String rule = parser.getRuleName();
		if (rule.equals("object-group network"))
			type = ObjectGroupType.NETWORK;

		if (rule.equals("object-group protocol"))
			type = ObjectGroupType.PROTOCOL;

		if (rule.equals("object-group service")) {
			 if (parser.getProtocol() == null)
				 type = ObjectGroupType.ENHANCED;
			 else
				 type = ObjectGroupType.SERVICE;
		}

		if (rule.equals("object-group icmp-type"))
			type = ObjectGroupType.ICMP;

		HashMap<String, ObjectGroup> groups = getObjectGroups(type);
		/*
		* check dupplicates
		*/
		String groupId = parser.getGroupId();
		if (groups.containsKey(groupId))
			throwCfgException("dupplicate object-group");

		ObjectGroup newGroup = null;
		switch (type) {
		 case NETWORK:
			 newGroup = new NetworkObjectGroup(groupId);
			 break;
		 case PROTOCOL:
			 newGroup = new ProtocolObjectGroup(groupId);
			 break;
		 case SERVICE:
			 String protocol = parser.getProtocol();
			 newGroup = new ServiceObjectGroup(groupId, protocol);
			 break;
		 case ENHANCED:
			 newGroup = new EnhancedServiceObjectGroup(groupId);
			 break;
		case ICMP:
			newGroup = new IcmpObjectGroup((groupId));
			break;
		}
		groups.put(groupId, newGroup);
		_lastGroup = newGroup;
	 }

	/*
	 * description in object-group
	 */
	private void ruleDescription(PixParser parser) {
		if (_lastGroup == null)
			return;
		if (_lastGroup.getDescription() == null)
			_lastGroup.setDescription(parser.getName());
	}

	/*
	 * group-object groupid
	 */
	private void ruleGroupObjectGroupID(PixParser parser) {

		if (_lastGroup == null)
			throwCfgException("group-object in non object-group context");

		ObjectGroupType type = _lastGroup.getType();
		HashMap <String, ObjectGroup> groups = getObjectGroups(type);

		String groupId = parser.getGroupId();
		ObjectGroup group = groups.get(groupId);
		if (group == null)
			throwCfgException("unknown group-id");

		/*
		 * check self reference
		 */
		if (_lastGroup.getGroupId().equals(groupId))
			throwCfgException("group-id pointing to itself");

		group.incRefCount();

		String line = _parseContext.getLine();
		ObjectGroupItem item = null;
		switch (type) {
			case NETWORK:
				item = new NetworkObjectGroupItem(_lastGroup, line, group);
				break;
			case PROTOCOL:
				item = new ProtocolObjectGroupItem(_lastGroup, line, group);
				break;
			case SERVICE:
				item = new ServiceObjectGroupItem(_lastGroup, line, group);
				break;
			case ENHANCED:
				item = new EnhancedServiceObjectGroupItem(_lastGroup, line, group);
				break;
			case ICMP:
				item = new IcmpObjectGroupItem(_lastGroup, line, group);
				break;
		}
		_lastGroup.add(item);
	 }

	/*
	 * "other"-object (except group-object)
	 */
	private void ruleOtherObject(PixParser parser) {

		if (_lastGroup == null)
			throwCfgException("group-object in non object-group context");
		ObjectGroupType type = _lastGroup.getType();

		String line = _parseContext.getLine();
		switch (type) {
			/*
			 * network object group
			 */
			case NETWORK:
				String sipAddress = nameLookup(parser.getIpAddress());
				String sipNetmask = nameLookup(parser.getIpNetmask());
				IPNet ip = null;
				if (sipNetmask == null)
					ip = parseIp(sipAddress);
				else
					ip = parseIp(sipAddress + "/" + sipNetmask);
				NetworkObjectGroupItem nobject =
						new NetworkObjectGroupItem(_lastGroup, line, ip);
				_lastGroup.add(nobject);
				/*
				 * cross reference
				 */
				IPNetCrossRef ixref = getIPNetCrossRef(ip);
				CrossRefContext refctx =
						new CrossRefContext(_parseContext,
						"network-object-group", _lastGroup.getGroupId() + 
						"; " + _parseContext.getFileNameAndLine());
				ixref.addContext(refctx);
				break;

			/*
			 * protocol object group
			 */
			case PROTOCOL:
				int proto = parseProtocol(parser.getProtocol());
				ProtocolObjectGroupItem pobject =
						new ProtocolObjectGroupItem(_lastGroup, line, proto);
				_lastGroup.add(pobject);
				break;

			/*
			 * service object group
			 */
			case SERVICE:
				ServiceObjectGroup group = (ServiceObjectGroup) _lastGroup;
				String protocol = group.getProtocol();
				if (protocol.equals("tcp-udp"))
					protocol = "tcp";

				String operator = parser.getPortOperator();
				int firstPort = parseService(parser.getFirstPort(), protocol);
				PortObject portObject;
				if (!operator.equals("range")) {
					portObject = new PortObject(operator, firstPort);
				} else {
					int lastPort = parseService(parser.getLastPort(), protocol);
					portObject = new PortObject(operator, firstPort, lastPort);
				}

				ServiceObjectGroupItem sobject = 
					new ServiceObjectGroupItem(_lastGroup, line, portObject);
				_lastGroup.add(sobject);
				break;

			/*
			 * enhanced service object group
			 */
			case ENHANCED:
				protocol = parser.getProtocol();
				proto = parseProtocol(protocol);

				operator = parser.getPortOperator();
				portObject = null;
				if (operator != null) {
					 firstPort = parseService(parser.getFirstPort(), protocol);
					if (!operator.equals("range")) {
						portObject = new PortObject(operator, firstPort);
					} else {
						int lastPort = parseService(parser.getLastPort(), protocol);
						portObject = new PortObject(operator, firstPort, lastPort);
					}
				}

				ServiceObject servObject = new ServiceObject(proto, portObject);

				EnhancedServiceObjectGroupItem eobject =
					new EnhancedServiceObjectGroupItem(_lastGroup,
					 line, servObject);
				
				_lastGroup.add(eobject);
				break;
			case ICMP:
				String sicmp = parser.getProtocol();
				IPIcmpEnt icmp = parseIcmp4(sicmp);
				IcmpObjectGroupItem iobject =
						new IcmpObjectGroupItem(_lastGroup, line, icmp.getIcmp());
				_lastGroup.add(iobject);
				break;
		}
	}

	/*
	 * access-list
	 */
	private void ruleAcl(AclTemplate tpl) {

		/*
		 * access-list
		 */
		String id = tpl.getAccessListId();
		AccessList acl = new AccessList(id);

		String fileName = _parseContext.getFileName();
		int i = fileName.lastIndexOf(File.separator);
		fileName = (i > -1) ? fileName.substring(i + 1) : fileName;

		acl.setConfigurationLine(fileName + " #" +
				_parseContext.getLineNumber() + " " +
				_parseContext.getLine());

		acl.setParseContext(_parseContext);

		/*
		 * create a new ACL group if needed
		 */
		AccessListGroup acg = _accessListGroups.get(id);
		if (acg == null) {
			acg = new AccessListGroup(id);
			_accessListGroups.put(id, acg);
		}

		if (tpl.getInactive())
			return;

		/*
		 * remark
		 */
		if (tpl.getRemark() != null) {
			acl.setRemark(tpl.getRemark());
			acg.add(acl);
			return;
		}

		/*
		 * action (permit, deny)
		 */
		acl.setAction(tpl.getAction());

		/*
		 * protocol
		 */
		String sprotocol = tpl.getProtocol();
		if (sprotocol != null) {
			int protocol = parseProtocol(sprotocol);
			acl.setProtocol(protocol);
		}

		/*
		 * protocol group
		 */
		String sprotocolGroup = tpl.getProtocolGroupId();
		if (sprotocolGroup != null) {
			ProtocolObjectGroup protocolGroup =
					(ProtocolObjectGroup) _protocolGroups.get(sprotocolGroup);
			if (protocolGroup == null)
				throwCfgException("unknown protocol group: " + sprotocolGroup);
			acl.setProtocolGroup(protocolGroup.expand());
			protocolGroup.incRefCount();
		}

		/*
		 * source ip
		 */
		String sIP = tpl.getSrcIp();
		String sNetmask = tpl.getSrcIpMask();
		if (sIP != null) {
			/*
			 * if 'any', ip address and network group are null.
			 */
			if (!sIP.equals("any")) {
				sIP = nameLookup(sIP);
				if (sNetmask != null) {
					sNetmask = nameLookup(sNetmask);
					sIP = sIP + "/" + sNetmask;
				}
				IPNet ip = parseIp(sIP);
				acl.setSourceIp(ip);
			}
		}

		/*
		 * source network group
		 */
		String sNetworkGroup = tpl.getSrcNetworkGroup();
		if (sNetworkGroup != null) {
			NetworkObjectGroup group =
					(NetworkObjectGroup) _networkGroups.get(sNetworkGroup);
			if (group == null)
				throwCfgException("unknown source network group:" + sNetworkGroup);
			acl.setSourceNetworkGroup(group.expand());
			group.incRefCount();
		}

		/*
		 * source service
		 */
		String sOperator = tpl.getSrcPortOperator();
		if (sOperator != null) {
			int firstPort = parseService(tpl.getSrcFirstPort(), null);
			int lastPort;
			PortObject pobj;
			if (sOperator.equals("range")) {
				lastPort = parseService(tpl.getSrcLastPort(), null);
				pobj = new PortObject("range", firstPort, lastPort);
			} else
				pobj = new PortObject(sOperator, firstPort);
			acl.setSourcePortObject(pobj);
		}

		/*
		 * source service object
		 */
		String sServiceGroup = tpl.getSrcServiceGroup();
		if (sServiceGroup != null) {
			ServiceObjectGroup group =
					(ServiceObjectGroup) _serviceGroups.get(sServiceGroup);
			if (group == null)
				throwCfgException("unknown source service group: " + sServiceGroup);
			acl.setSourceServiceGroup(group.expand());
			group.incRefCount();
		}

		/*
		 * destination ip
		 */
		sIP = tpl.getDstIp();
		sNetmask = tpl.getDstIpMask();
		if (sIP != null) {
			/*
			 * if 'any', ip address and network group are null.
			 */
			if (!sIP.equals("any")) {
				sIP = nameLookup(sIP);
				if (sNetmask != null) {
					sNetmask = nameLookup(sNetmask);
					sIP = sIP + "/" + sNetmask;
				}
				IPNet ip = parseIp(sIP);
				acl.setDestIp(ip);
			}
		}

		/*
		 * destination network group
		 */
		sNetworkGroup = tpl.getDstNetworkGroup();
		if (sNetworkGroup != null) {
			NetworkObjectGroup group =
					(NetworkObjectGroup) _networkGroups.get(sNetworkGroup);
			if (group == null)
				throwCfgException("unknown destination network group: " +
						sNetworkGroup);
			acl.setDestNetworkGroup(group.expand());
			group.incRefCount();
		}

		/*
		 * destination service
		 */
		sOperator = tpl.getDstPortOperator();
		if (sOperator != null) {
			int firstPort = parseService(tpl.getDstFirstPort(), null);
			int lastPort;
			PortObject pobj;
			if (sOperator.equals("range")) {
				lastPort = parseService(tpl.getDstLastPort(), null);
				pobj = new PortObject("range", firstPort, lastPort);
			} else
				pobj = new PortObject(sOperator, firstPort);
			acl.setDestPortObject(pobj);
		}

		/*
		 * destination service group
		 */
		sServiceGroup = tpl.getDstServiceGroup();
		if (sServiceGroup != null) {
			ServiceObjectGroup group =
					(ServiceObjectGroup) _serviceGroups.get(sServiceGroup);
			if (group == null)
				throwCfgException("unknown destination service group: " +
						sServiceGroup);
			acl.setDestServiceGroup(group.expand());
			group.incRefCount();
		}

		/*
		 * destination enhanced service group
		 */
		sServiceGroup = tpl.getDstEnhancedServiceGroup();
		if (sServiceGroup != null) {
			EnhancedServiceObjectGroup group =
					(EnhancedServiceObjectGroup) _enhancedGroups.get(sServiceGroup);
			if (group == null)
				throwCfgException("unknown enhanced destination service group: " +
						sServiceGroup);
			acl.setEnhancedDestServiceGroup(group.expand());
			group.incRefCount();
		}

		/*
		 * Icmp
		 */
		 String sIcmp = tpl.getIcmp();
		 String sIcmpGroup = tpl.getIcmpGroup();
		 if (sIcmp != null) {
			 IPIcmpEnt icmpType = parseIcmp4(sIcmp);
			 acl.setIcmp(icmpType.getIcmp());
		 }
		 if (sIcmpGroup != null) {
			IcmpObjectGroup group =
					(IcmpObjectGroup) _icmpGroups.get(sIcmpGroup);
			if (group == null)
				throwCfgException("unknown icmp group: " + sIcmpGroup);
			acl.setIcmpGroup(group.expand());
			group.incRefCount();
		 }

		/*
		 * Sanity checks.
		 */
		if (acl.getProtocol() == null &&
				acl.getProtocolGroup() == null &&
				acl.getEnhancedDestServiceGroup() == null)
			throwCfgException("undefined protocol");

		if (acl.getEnhancedDestServiceGroup() != null &&
				( acl.getSourcePortObject() != null ||
				  acl.getSourceServiceGroup() != null))
			throwCfgException("conflict enhanced service group/source");

		if (acl.getEnhancedDestServiceGroup() != null &&
				( acl.getDestPortObject() != null ||
				  acl.getDestServiceGroup() != null))
			throwCfgException("conflict enhanced service group/destination");

		acg.add(acl);
		
	}

	/*
	 * name
	 */
	private void ruleName(PixParser parser) {
		String name = parser.getName();
		String IPvalue = parser.getIpAddress();
		PixName pixName = new PixName(name, IPvalue);
		_names.put(name, pixName);

		/*
		 * cross reference
		 */
		IPNet ip = parseIp(IPvalue);
		IPNetCrossRef ixref = getIPNetCrossRef(ip);
		CrossRefContext refctx =
				new CrossRefContext(_parseContext, "name", name + "; " +
					_parseContext.getFileNameAndLine());
		ixref.addContext(refctx);
	}

	/*
	 * we need to provide the group's type to the parser because it depends
	 * of the type of the object-group.
	 * For example:
	 * access-list ID extended permit tcp any GROUP1 GROUP2
	 * is ambiguous without the group type. Is group1 the source service group
	 * or the destination network group?
	 */
	public ObjectGroupType getGroupType(String groupId) {
		if (_networkGroups.containsKey(groupId))
			return ObjectGroupType.NETWORK;

		if (_serviceGroups.containsKey(groupId))
			return ObjectGroupType.SERVICE;

		if (_protocolGroups.containsKey(groupId))
			return ObjectGroupType.PROTOCOL;

		if (_enhancedGroups.containsKey(groupId))
			return ObjectGroupType.ENHANCED;

		if (_icmpGroups.containsKey(groupId))
			return ObjectGroupType.ICMP;

		Log.config().warning(_parseContext.toString() +
				"unknown object group: " + groupId);
		return null;
	}

	protected void parse(ConfigurationFile cfg) {

		ParsingResult<?> result;

		PixParser parser = Parboiled.createParser(PixParser.class);
		parser.setGroupTypeSearch(this);

		for (int i = 0; i < cfg.size(); i++) {
			String line = cfg.get(i);
			line = parser.stripWhiteSpaces(line);
			String lineCfg = parser.stripComment(line).trim();
			lineCfg = filter(lineCfg);
			_parseContext = new ParseContext();
			_parseContext.set(cfg.getFileName(), i + 1, line);

			/*
			 * parse the line
			 */
			result = ReportingParseRunner.run(parser.Parse(), lineCfg);
			if (result.matched) {
				String rule = parser.getRuleName();
				dumpConfiguration(line);

				if (hasOptParseOnly())
					continue;

				/*
				 * route command
				 */
				if (rule.equals("ip route") ||
						rule.equals("ipv6 route") ||
						rule.equals("route"))
					ruleRoute(rule, parser);

				/*
				 * name
				 */
				if (rule.equals("name")) {
					ruleName(parser);
				}

				/*
				 * object-group network / protocol / service / icmp
				 */
				if (rule.equals("object-group network") ||
						rule.equals("object-group protocol") ||
						rule.equals("object-group service") ||
						rule.equals("object-group icmp-type"))
					ruleObjectGroup(parser);

				/*
				 * network-object / protocol-object / port-object 
				 * / service- object / icmp-object
				 */
				if (rule.equals("network-object") ||
						rule.equals("protocol-object") ||
						rule.equals("port-object") ||
						rule.equals("service-object") ||
						rule.equals("icmp-object"))
					ruleOtherObject(parser);

				/*
				 * description for object-group
				 */
				if (rule.equals("description"))
					ruleDescription(parser);

				/*
				 * group-object
				 */
				if (rule.equals("group-object"))
					ruleGroupObjectGroupID(parser);

				/*
				 * access-group
				 */
				if (rule.equals("access-group")) {
					String name = parser.getName();
					String ifname = parser.getInterface();
					String sdirection = parser.getDirection();
					Direction direction;
					if (sdirection.equals("in"))
						direction = Direction.IN;
					else
						direction = Direction.OUT;
					if (getIface(ifname) == null)
						throwCfgException("unknown interface");
					AccessGroup group = new AccessGroup(name, ifname, direction);
					if (_accessGroups.containsKey(name))
						throwCfgException("dupplicate access-group");
					_accessGroups.put(name, group);
				}

				/*
				 * acl
				 */
				if (rule.equals("access-list acl") ||
						rule.equals("access-list remark"))
					ruleAcl(parser.getAcl());

			} else {
				/*
				 * check if we should match a rule
				 */
				if (parser.shouldMatchInMain(lineCfg)) {
					Log.config().warning(_parseContext.toString() +
						"does not match any rule (but should).");
				}
			}
		}
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
		loadConfiguration(doc);
		/*
		 * parse and add interfaces
		 */
		for (ConfigurationFile cfg: _configurations) {
			parseInterfaces(cfg);
		}
		for (String n: _ciscoIfaces.keySet()) {
			CiscoIface csIface = _ciscoIfaces.get(n);
			if (!csIface.isShutdown())
				addInterface(csIface);
		}

		/*
		 * parse ACL
		 */
		for (ConfigurationFile cfg: _configurations) {
			parse(cfg);
		}

		/*
		 * add an implicit deny ACL in each ACL group
		 */
		for (AccessListGroup acg: _accessListGroups.values()) {
			AccessList acl = new AccessList(acg.getId());
			acl.setAction("deny");
			acl.setConfigurationLine("access-list " + acg.getId() +
				" *** implicit deny ***");
			acl.setImplicit(true);
			acg.add(acl);
		}

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

	/*
	 * Cross reference for used network-group
	 */
	protected void crossRefNetworkGroup(NetworkObjectGroup networkGroup, CrossRefContext refContext) {

		for (ObjectGroupItem groupItem: networkGroup.expand()) {
			NetworkObjectGroupItem networkGroupItem =
				(NetworkObjectGroupItem) groupItem;
			IPNet ip = networkGroupItem.getIpAddress();
			IPNetCrossRef ipNetRef = getIPNetCrossRef(ip);
			ipNetRef.addContext(refContext);
		}
	}

	/*
	 * Cross reference for an access list
	 */
	protected void crossRefAccessList(AccessList acl) {
		ParseContext context = acl.getParseContext();
		CrossRefContext refContext = new CrossRefContext(context, "acl",
				"[" + acl.getAction() + "]; " + context.getFileNameAndLine());

		NetworkObjectGroup networkGroup = acl.getSourceNetworkGroup();
		if (networkGroup != null)
			crossRefNetworkGroup(networkGroup, refContext);

		networkGroup = acl.getDestNetworkGroup();
		if (networkGroup != null)
			crossRefNetworkGroup(networkGroup, refContext);

		IPNet ip = acl.getSourceIp();
		if (ip != null) {
			IPNetCrossRef ipNetRef = getIPNetCrossRef(ip);
			ipNetRef.addContext(refContext);
		}

		ip = acl.getDestIp();
		if (ip != null) {
			IPNetCrossRef ipNetRef = getIPNetCrossRef(ip);
			ipNetRef.addContext(refContext);
		}

	}

	/**
	 * Compute IPNet cross references
	 */
	protected void ipNetCrossReference() {
		/*
		 * acl
		 */
		for (AccessListGroup aclGroup: _accessListGroups.values()) {
			for (AccessList acl: aclGroup) {
				if (!acl.isImplicit())
					crossRefAccessList(acl);
			}
		}
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
		Routes routes = null;
		routes = _routingEngine.getRoutes(probe);
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

	/*
	 * Port Filter
	 */
	protected MatchResult portFilter(PortObject port, PortSpec portRequest) {
		return port.matches(portRequest);
	}

	/**
	 *
	 */
	protected MatchResult probeFilter(Probe probe, AccessList acl, Direction direction)
			throws UnknownHostException {

		ProbeRequest request = probe.getRequest();

		/*
		 * check ip source
		 */
		IPNet aclIpSource = acl.getSourceIp();
		IPNet probeIpSource = probe.getSourceAddress();

		NetworkObjectGroup aclSourceNetwork = acl.getSourceNetworkGroup();

		MatchResult mIpSource = MatchResult.ALL;
		if (aclIpSource != null) {
			if (!aclIpSource.networkContains(probeIpSource)) {
				if (!aclIpSource.overlaps(probeIpSource))
					return MatchResult.NOT;
				else
					// match partialy.
					mIpSource = MatchResult.MATCH;
			}
		}

		if (aclSourceNetwork != null) {
			mIpSource = aclSourceNetwork.matches(probeIpSource);
			if (mIpSource == MatchResult.NOT)
				return MatchResult.NOT;
		}

		/*
		 * check ip destination
		 */
		IPNet aclIpDest = acl.getDestIp();
		IPNet probeIpDest = probe.getDestinationAddress();

		NetworkObjectGroup aclDestNetwork = acl.getDestNetworkGroup();
		MatchResult mIpDest = MatchResult.ALL;
		if (aclIpDest != null) {
			if (!aclIpDest.networkContains(probeIpDest)) {
				if (!aclIpDest.overlaps(probeIpDest))
					return MatchResult.NOT;
				else
					// match partialy
					mIpDest = MatchResult.MATCH;
			}
		}

		if (aclDestNetwork != null) {
			mIpDest = aclDestNetwork.matches(probeIpDest);
			if (mIpDest == MatchResult.NOT)
				return MatchResult.NOT;
		}

		/*
		 * check protocol
		 */
		List<Integer> reqProto = request.getProtocols();
		Integer aclProto = acl.getProtocol();
		ProtocolObjectGroup aclProtoGroup = acl.getProtocolGroup();

		if (reqProto != null) {
			/*
			 * protocol
			 */
			if (aclProto != null) {
				if (!ProtocolComparator.matches(reqProto, aclProto))
					return MatchResult.NOT;
			}
			/*
			 * protocol group
			 */
			if (aclProtoGroup != null) {
				if (!aclProtoGroup.matches(reqProto))
					return MatchResult.NOT;
			}
		}
		/*
		 * check source service
		 */
		PortSpec reqSourcePort = request.getSourcePort();
		PortObject aclSrcPort = acl.getSourcePortObject();
		ServiceObjectGroup aclSrcServiceGroup = acl.getSourceServiceGroup();
		MatchResult mSourcePort = MatchResult.ALL;

		if (reqSourcePort != null) {
			/*
			 * port object
			 */
			if (aclSrcPort != null) {
				mSourcePort = portFilter(aclSrcPort, reqSourcePort);
				if (mSourcePort == MatchResult.NOT)
					return MatchResult.NOT;
			}

			/*
			 * service group
			 */
			if (aclSrcServiceGroup != null) {
				mSourcePort = aclSrcServiceGroup.matches(aclProto, reqSourcePort);
				if (mSourcePort == MatchResult.NOT)
					return MatchResult.NOT;
			}
		}

		/*
		 * check destination service
		 */
		PortSpec reqDestPort = request.getDestinationPort();
		PortObject aclDestPort = acl.getDestPortObject();
		ServiceObjectGroup aclDestServiceGroup = acl.getDestServiceGroup();
		MatchResult mDestPort = MatchResult.ALL;

		if (reqDestPort != null) {
			/*
			 * port object
			 */
			if (aclDestPort != null) {
				mDestPort = portFilter(aclDestPort, reqDestPort);
				if (mDestPort == MatchResult.NOT)
					return MatchResult.NOT;
			}

			/*
			 * service group
			 */
			if (aclDestServiceGroup != null) {
				mDestPort = aclDestServiceGroup.matches(aclProto, reqDestPort);
				if (mDestPort == MatchResult.NOT)
					return MatchResult.NOT;
			}
		}

		/*
		 * icmp type
		 */
		Integer reqSubType = request.getSubType();
		Integer aclImp = acl.getIcmp();
		IcmpObjectGroup aclIcmpGroup = acl.getIcmpGroup();

		if (reqProto != null &&
				reqProto.contains(_ipProtocols.ICMP())) {
			if (reqSubType != null) {
				/*
				 * icmp type
				 */
				if (aclImp != null && aclImp.intValue() != reqSubType)
					return MatchResult.NOT;
				/*
				 * icmp group
				 */
				if (aclIcmpGroup != null && !aclIcmpGroup.matches(reqSubType))
					return MatchResult.NOT;
			}

		}

		/*
		 * enhanced service group.
		 */
		EnhancedServiceObjectGroup egroup = acl.getEnhancedDestServiceGroup();
		MatchResult mEnhancedService = MatchResult.ALL;

		if (egroup != null) {
			if (reqProto != null && reqDestPort != null) {
				mEnhancedService = egroup.matches(reqProto, reqDestPort);
				if (mEnhancedService == MatchResult.NOT)
					return MatchResult.NOT;
			}
			if (reqProto != null && !egroup.matches(reqProto))
					return MatchResult.NOT;
		}

		if (mIpSource == MatchResult.MATCH ||
				mIpDest == MatchResult.MATCH ||
				mSourcePort == MatchResult.MATCH ||
				mDestPort == MatchResult.MATCH ||
				mEnhancedService == MatchResult.MATCH)
			return MatchResult.MATCH;

		return MatchResult.ALL;
	}
	
	/**
	 * Packet filter for the pix.
	 */
	protected void packetFilter (IfaceLink link, Direction direction, Probe probe) {

		String ifaceName = link.getIface().getName();
		ProbeResults results = probe.getResults();

		/*
		 * retrieve access groups associated in direction with the interface.
		 */
		List<AccessGroup> agroups = new ArrayList<AccessGroup>();
		for (AccessGroup agroup: _accessGroups.values()) {
			if (agroup.getDirection() == direction &&
					agroup.getIfName().equals(ifaceName)) {
				agroups.add(agroup);
			}
		}

		boolean first = true;
		/*
		 * for each access group.
		 */
		for (AccessGroup group: agroups) {
			AccessListGroup acg = _accessListGroups.get(group.getName());
			if (acg == null) {
				/*
				 * XXX: what we shoud do?
				 */
				continue;
			}

			/*
			 * each acl in the group.
			 */
			for (AccessList acl: acg) {
				if (!acl.isRemark()) {
					MatchResult match = MatchResult.NOT;
					try {
						match = probeFilter(probe, acl, direction);
						if (match != MatchResult.NOT) {
							/*
							 * store the result in the probe
							 */
							AclResult aclResult = new AclResult();
							aclResult.addResult(acl.getAction().equals("permit") ?
								AclResult.ACCEPT : AclResult.DENY);

							if (match != MatchResult.ALL)
								aclResult.addResult(AclResult.MAY);

							results.addMatchingAcl(direction,
								acl.getConfigurationLine(),
								aclResult);

							results.setInterface(direction,
								ifaceName);

							/*
							 * the active acl is the acl accepting or denying the packet.
							 * On pix this is the first acl that match the packet.
							 */
							if (first) {
								results.addActiveAcl(direction,
										acl.getConfigurationLine(),
										aclResult);
								results.setAclResult(direction,
										aclResult);
								first = false;
							}
						}
					} catch (UnknownHostException ex) {
						// should not happen
						throw new JtaclInternalException(("unexpected exception: " +
							ex.getMessage()));
					}
				}
			}
		}
	}
	
}
