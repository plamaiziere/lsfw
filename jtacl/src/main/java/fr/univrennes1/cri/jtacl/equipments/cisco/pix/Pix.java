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
import fr.univrennes1.cri.jtacl.analysis.IPCrossRef;
import fr.univrennes1.cri.jtacl.analysis.IPCrossRefMap;
import fr.univrennes1.cri.jtacl.analysis.ServiceCrossRef;
import fr.univrennes1.cri.jtacl.analysis.ServiceCrossRefContext;
import fr.univrennes1.cri.jtacl.analysis.ServiceCrossRefMap;
import fr.univrennes1.cri.jtacl.analysis.ServiceCrossRefType;
import fr.univrennes1.cri.jtacl.core.exceptions.JtaclConfigurationException;
import fr.univrennes1.cri.jtacl.core.exceptions.JtaclInternalException;
import fr.univrennes1.cri.jtacl.core.monitor.Log;
import fr.univrennes1.cri.jtacl.core.monitor.Monitor;
import fr.univrennes1.cri.jtacl.core.network.Iface;
import fr.univrennes1.cri.jtacl.core.network.IfaceLink;
import fr.univrennes1.cri.jtacl.core.network.Route;
import fr.univrennes1.cri.jtacl.core.network.Routes;
import fr.univrennes1.cri.jtacl.core.probing.FwResult;
import fr.univrennes1.cri.jtacl.core.probing.MatchResult;
import fr.univrennes1.cri.jtacl.core.probing.Probe;
import fr.univrennes1.cri.jtacl.core.probing.ProbeRequest;
import fr.univrennes1.cri.jtacl.core.probing.ProbeResults;
import fr.univrennes1.cri.jtacl.core.probing.ProbeTcpFlags;
import fr.univrennes1.cri.jtacl.equipments.generic.GenericEquipment;
import fr.univrennes1.cri.jtacl.lib.ip.IPIcmpEnt;
import fr.univrennes1.cri.jtacl.lib.ip.IPNet;
import fr.univrennes1.cri.jtacl.lib.ip.IPRangeable;
import fr.univrennes1.cri.jtacl.lib.ip.PortRange;
import fr.univrennes1.cri.jtacl.lib.ip.PortSpec;
import fr.univrennes1.cri.jtacl.lib.ip.Protocols;
import fr.univrennes1.cri.jtacl.lib.ip.ProtocolsSpec;
import fr.univrennes1.cri.jtacl.lib.ip.TcpFlags;
import fr.univrennes1.cri.jtacl.lib.misc.Direction;
import fr.univrennes1.cri.jtacl.lib.misc.ParseContext;
import fr.univrennes1.cri.jtacl.lib.misc.StringTools;
import fr.univrennes1.cri.jtacl.lib.misc.StringsList;
import fr.univrennes1.cri.jtacl.lib.xml.XMLUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import org.parboiled.Parboiled;
import org.parboiled.parserunners.BasicParseRunner;
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
	 * Parser
	 */
	protected PixParser _parser = Parboiled.createParser(PixParser.class);

	/**
	 * ParseRunner for Parse()
	 */
	protected BasicParseRunner _parseRunParse =
			new BasicParseRunner(_parser.Parse());

	/**
	 * ParseRunner for ExitInterface()
	 */
	protected BasicParseRunner _parseRunExitInterface =
			new BasicParseRunner(_parser.ExitInterface());

	/**
	 * ParseRunner for InInterface()
	 */
	protected BasicParseRunner _parseRunInInterface =
			new BasicParseRunner(_parser.InInterface());

	/**
	 * ParseRunner for Interface()
	 */
	protected BasicParseRunner _parseRunInterface =
			new BasicParseRunner(_parser.Interface());

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
	 * IP cross references map
	 */
	protected IPCrossRefMap _netCrossRef = new IPCrossRefMap();

	/**
	 * Services cross references map
	 */
	protected ServiceCrossRefMap _serviceCrossRef = new ServiceCrossRefMap();

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
	 * IP cross references map
	 */
	IPCrossRefMap getNetCrossRef() {
		return _netCrossRef;
	}

	/*
	 * Service cross reference map
	 */
	public ServiceCrossRefMap getServiceCrossRef() {
		return _serviceCrossRef;
	}

	protected PixShell _shell = new PixShell(this);

	/**
	 * Create a new {@link Pix} with this name and this comment.<br/>
	 * @param monitor the {@link Monitor} monitor associated with this equipment.
	 * @param name the name of the equipment.
	 * @param comment a free comment for this equipment.
	 * @param configurationFileName name of the configuration file to use (may be null).
	 */
	public Pix(Monitor monitor, String name, String comment, String configurationFileName) {
		super(monitor, name, comment, configurationFileName);
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

		for (int i = 0; i < cfg.size(); i++) {
			String line = cfg.get(i);
			line = StringTools.stripWhiteSpaces(line);
			_parseContext = new ParseContext();
			_parseContext.set(cfg.getFileName(), i + 1, line);

			if (Log.debug().isLoggable(Level.INFO))
				Log.debug().info("#" + _parseContext.getLineNumber() +
					": " + line);

			String lineCfg = _parser.stripComment(line).trim();
			lineCfg = filter(lineCfg);
			if (inInterface) {
				result = _parseRunExitInterface.run(lineCfg);
				if (result.matched) {
					inInterface = false;
					curCsIface = null;
				} else {
					result = _parseRunInInterface.run(lineCfg);
					if (result.matched) {
						String rule = _parser.getRuleName();

						/*
						 * nameif
						 */
						if (rule.equals("nameif"))
							curCsIface.setName(_parser.getName());

						/*
						 * ip address
						 */
						if (rule.equals("ip address")) {
							String ip = nameLookup(_parser.getIpAddress());
							String ipNetmask = nameLookup(_parser.getIpNetmask());
							IPNet ipnet = parseIp(ip + "/" + ipNetmask);
							curCsIface.getIpAddresses().add(ipnet);
						}

						/*
						 * ipv6 address
						 */
						if (rule.equals("ipv6 address")) {
							String ipv6Address = nameLookup(_parser.getIpAddress());
							IPNet ipnet = parseIp(ipv6Address);
							curCsIface.getIpAddresses().add(ipnet);
						}
						dumpConfiguration(line);
					} else {
						/*
						 * check if we should match a rule
						 */
						if (_parser.shouldMatchInInterface(lineCfg)) {
							Log.config().warning(_parseContext.toString() +
								"does not match any rule (but should).");
						}
					}
				}
			}
			if (lineCfg.startsWith("interface")) {
				result = _parseRunInterface.run(lineCfg);
				if (result.matched) {
					inInterface = true;
					String interfaceName = _parser.getName();
					// strip any space in the name.
					interfaceName = interfaceName.replace(" ", "");
					curCsIface = new CiscoIface();
					curCsIface.setName(interfaceName);
					curCsIface.setShutdown(_parser.getShutdown() != null);
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
			IPNet hostIP = ip.hostAddress();
			IPNet networkIP = ip.networkAddress();
			iface.addLink(hostIP, networkIP);
		}
	}

	/*
	 * ip route and ipv6 route
	 */
	private void ruleRoute(String rule, PixParser parser) {

		String routeIPAddress = nameLookup(parser.getIpAddress());
		String routeIPNetmask = nameLookup(parser.getIpNetmask());
		String routeNexthop = nameLookup(parser.getNexthop());

		IPNet prefix= null;
		// IPv4
		if (rule.equals("ip route") || rule.equals("route"))
			prefix = parseIp(routeIPAddress + "/" + routeIPNetmask);
		// IPv6
		if (rule.equals("ipv6 route"))
			prefix = parseIp(routeIPAddress);

		Route<IfaceLink> route;

		/*
		 * null route
		 */
		if (routeNexthop == null ||
				routeNexthop.equalsIgnoreCase("null0")) {
			route = new Route<IfaceLink>(prefix);
		} else {
			/*
			 * Retrieve the link associated to the nexthop (may be null)
			 */
			IPNet nexthop = parseIp(routeNexthop);
			Iface iface = getIfaceConnectedTo(nexthop);
			IfaceLink link = null;
			if (iface != null)
				link = iface.getLinkConnectedTo(nexthop);
			route = new Route<IfaceLink>(prefix, nexthop, 1, link);
		}
		/*
		 * Add the route.
		 */

		Log.debug().info(_name + " add route: " + route.toString());
		_routingEngine.addRoute(route);
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
			 ProtocolsSpec protoSpec = new ProtocolsSpec();
			 if (protocol.contains("udp"))
				 protoSpec.add(Protocols.UDP);
			 if (protocol.contains("tcp"))
				 protoSpec.add(Protocols.TCP);
			 newGroup = new ServiceObjectGroup(groupId, protoSpec);
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

		ObjectGroupItem item = null;
		switch (type) {
			case NETWORK:
				item = new NetworkObjectGroupItem(_lastGroup, _parseContext, group);
				break;
			case PROTOCOL:
				item = new ProtocolObjectGroupItem(_lastGroup, _parseContext, group);
				break;
			case SERVICE:
				item = new ServiceObjectGroupItem(_lastGroup, _parseContext, group);
				break;
			case ENHANCED:
				item = new EnhancedServiceObjectGroupItem(_lastGroup, _parseContext, group);
				break;
			case ICMP:
				item = new IcmpObjectGroupItem(_lastGroup, _parseContext, group);
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
				IPNet ip;
				if (sipNetmask == null)
					ip = parseIp(sipAddress);
				else
					ip = parseIp(sipAddress + "/" + sipNetmask);
				NetworkObjectGroupItem nobject =
						new NetworkObjectGroupItem(_lastGroup, _parseContext, ip);
				_lastGroup.add(nobject);
				break;

			/*
			 * protocol object group
			 */
			case PROTOCOL:
				int proto = parseProtocol(parser.getProtocol());
				ProtocolObjectGroupItem pobject =
						new ProtocolObjectGroupItem(_lastGroup, _parseContext, proto);
				_lastGroup.add(pobject);
				break;

			/*
			 * service object group
			 */
			case SERVICE:
				ServiceObjectGroup group = (ServiceObjectGroup) _lastGroup;
				ProtocolsSpec protoSpec = group.getProtocols();
				String protocol;
				if (protoSpec.contains(Protocols.TCP))
					protocol = "tcp";
				else
					protocol = "udp";

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
					new ServiceObjectGroupItem(_lastGroup, _parseContext, portObject);
				_lastGroup.add(sobject);
				break;

			/*
			 * enhanced service object group
			 */
			case ENHANCED:
				protocol = parser.getProtocol();
				ProtocolsSpec protos = new ProtocolsSpec();
				if (protocol.equalsIgnoreCase("tcp-udp")) {
					protos.add(Protocols.UDP);
					protos.add(Protocols.TCP);
					protocol = "tcp";
				} else {
					protos.add(parseProtocol(protocol));
				}
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

				ServiceObject servObject = new ServiceObject(protos, portObject);

				EnhancedServiceObjectGroupItem eobject =
					new EnhancedServiceObjectGroupItem(_lastGroup,
					 _parseContext, servObject);

				_lastGroup.add(eobject);
				break;
			case ICMP:
				String sicmp = parser.getProtocol();
				IPIcmpEnt icmp = parseIcmp4(sicmp);
				IcmpObjectGroupItem iobject =
						new IcmpObjectGroupItem(_lastGroup, _parseContext, icmp.getIcmp());
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
		if (_monitorOptions.getXref()) {
			IPNet ip = parseIp(IPvalue);
			IPCrossRef ixref = getIPNetCrossRef(ip);
			CrossRefContext refctx =
					new CrossRefContext(_parseContext.getLine(), "name", name,
						_parseContext.getFileName(),
						_parseContext.getLineNumber());
			ixref.addContext(refctx);
		}
	}

	/*
	 * we need to provide the group's type to the parser because it depends
	 * of the type of the object-group.
	 * For example:
	 * access-list ID extended permit tcp any GROUP1 GROUP2
	 * is ambiguous without the group type. Is group1 the source service group
	 * or the destination network group?
	 */
	@Override
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

		_parser.setGroupTypeSearch(this);

		for (int i = 0; i < cfg.size(); i++) {
			String line = cfg.get(i);
			line = StringTools.stripWhiteSpaces(line);
			String lineCfg = _parser.stripComment(line).trim();
			lineCfg = filter(lineCfg);
			_parseContext = new ParseContext();
			_parseContext.set(cfg.getFileName(), i + 1, line);

			if (Log.debug().isLoggable(Level.INFO))
				Log.debug().info("#" + _parseContext.getLineNumber() +
					": " + lineCfg);

			/*
			 * parse the line
			 */
			result = _parseRunParse.run(lineCfg);
			if (result.matched) {
				String rule = _parser.getRuleName();
				dumpConfiguration(line);

				if (hasOptParseOnly())
					continue;

				/*
				 * route command
				 */
				if (rule.equals("ip route") ||
						rule.equals("ipv6 route") ||
						rule.equals("route"))
					ruleRoute(rule, _parser);

				/*
				 * name
				 */
				if (rule.equals("name")) {
					ruleName(_parser);
				}

				/*
				 * object-group network / protocol / service / icmp
				 */
				if (rule.equals("object-group network") ||
						rule.equals("object-group protocol") ||
						rule.equals("object-group service") ||
						rule.equals("object-group icmp-type"))
					ruleObjectGroup(_parser);

				/*
				 * network-object / protocol-object / port-object
				 * / service- object / icmp-object
				 */
				if (rule.equals("network-object") ||
						rule.equals("protocol-object") ||
						rule.equals("port-object") ||
						rule.equals("service-object") ||
						rule.equals("icmp-object"))
					ruleOtherObject(_parser);

				/*
				 * description for object-group
				 */
				if (rule.equals("description"))
					ruleDescription(_parser);

				/*
				 * group-object
				 */
				if (rule.equals("group-object"))
					ruleGroupObjectGroupID(_parser);

				/*
				 * access-group
				 */
				if (rule.equals("access-group")) {
					String name = _parser.getName();
					String ifname = _parser.getInterface();
					String sdirection = _parser.getDirection();
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
					ruleAcl(_parser.getAcl());

			} else {
				/*
				 * check if we should match a rule
				 */
				if (_parser.shouldMatchInMain(lineCfg)) {
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
		if (_monitorOptions.getXref())
			CrossReferences();
	}


	protected IPCrossRef getIPNetCrossRef(IPNet ipnet) {
		if (!_monitorOptions.getXref())
			throw new JtaclInternalException(
					"Cross reference computing without crossreference option set");
		IPCrossRef ref = _netCrossRef.get(ipnet);
		if (ref == null) {
			ref = new IPCrossRef(ipnet);
			_netCrossRef.put(ref);
		}
		return ref;
	}

	protected ServiceCrossRef getServiceCrossRef(PortRange portrange) {
		if (!_monitorOptions.getXref())
			throw new JtaclInternalException(
					"Cross reference computing without crossreference option set");
		ServiceCrossRef ref = _serviceCrossRef.get(portrange);
		if (ref == null) {
			ref = new ServiceCrossRef(portrange);
			_serviceCrossRef.put(ref);
		}
		return ref;
	}

	/*
	 * Cross reference for used network-group in rule
	 */
	protected void crossRefNetworkGroupSpec(NetworkObjectGroup networkGroup,
			CrossRefContext refContext) {

		if (Log.debug().isLoggable(Level.INFO)) {
			Log.debug().info("xref NetworkGroupSpec: " + networkGroup.getGroupId());
		}

		for (ObjectGroupItem groupItem: networkGroup.expand()) {
			NetworkObjectGroupItem networkGroupItem =
				(NetworkObjectGroupItem) groupItem;
			IPNet ip = networkGroupItem.getIpAddress();
			IPCrossRef ipNetRef = getIPNetCrossRef(ip);
			ipNetRef.addContext(refContext);
		}
	}

	/*
	 * Cross reference for network-group
	 */
	protected void crossRefNetworkGroup(NetworkObjectGroup networkGroup) {

		if (Log.debug().isLoggable(Level.INFO)) {
			Log.debug().info("xref NetworkGroup: " + networkGroup.getGroupId());
		}

		for (ObjectGroupItem groupItem: networkGroup.expand()) {
			NetworkObjectGroupItem networkGroupItem =
				(NetworkObjectGroupItem) groupItem;
			IPNet ip = networkGroupItem.getIpAddress();
			IPCrossRef ipNetRef = getIPNetCrossRef(ip);
			ParseContext pctx = groupItem.getParseContext();
			if (pctx == null)
				continue;

			CrossRefContext refctx =
				new CrossRefContext(pctx.getLine(),
				"network-object-group",
				groupItem.getOwner().getGroupId(),
				pctx.getFileName(),
				pctx.getLineNumber());
			ipNetRef.addContext(refctx);
		}
	}

	protected void crossRefServiceGroupSpec(ObjectGroup objectGroup,
			ServiceCrossRefContext refContext) {

		if (Log.debug().isLoggable(Level.INFO)) {
			Log.debug().info("xref ServiceGroupSpec: " + objectGroup.getGroupId());
		}

		if (objectGroup.getType() == ObjectGroupType.SERVICE) {
			ServiceObjectGroup sObject = (ServiceObjectGroup) objectGroup;
			for (ObjectGroupItem item: sObject.expand()) {
				ServiceObjectGroupItem sitem = (ServiceObjectGroupItem) item;
				crossRefPortObject(sitem.getPortObject(), refContext);
			}
		}

		if (objectGroup.getType() == ObjectGroupType.ENHANCED) {
			EnhancedServiceObjectGroup sObject =
				(EnhancedServiceObjectGroup) objectGroup;
			for (ObjectGroupItem item: sObject.expand()) {
				EnhancedServiceObjectGroupItem sitem
					= (EnhancedServiceObjectGroupItem) item;
				PortObject portObj = sitem.getServiceObject().getPortObject();
				if (portObj == null)
					continue;
				ServiceCrossRefContext nref = new ServiceCrossRefContext(
					sitem.getServiceObject().getProtocols(),
					refContext.getType(),
					refContext.getContextString(),
					refContext.getContextName(),
					refContext.getComment(),
					refContext.getFilename(),
					refContext.getLinenumber());
				crossRefPortObject(portObj,	nref);
			}
		}
	}

	protected void crossRefServiceGroup(ObjectGroup objectGroup) {

		if (Log.debug().isLoggable(Level.INFO)) {
			Log.debug().info("xref ServiceGroup: " + objectGroup.getGroupId());
		}

		if (objectGroup.getType() == ObjectGroupType.SERVICE) {
			ServiceObjectGroup sObject = (ServiceObjectGroup) objectGroup;
			ProtocolsSpec protoSpec = sObject.getProtocols();
			for (ObjectGroupItem item: sObject.expand()) {
				ServiceObjectGroupItem sitem = (ServiceObjectGroupItem) item;
				ParseContext pctx = item.getParseContext();
				if (pctx == null)
					continue;
				for (PortRange range: sitem.getPortObject().getPortSpec().getRanges()) {
					ServiceCrossRefContext refctx = new ServiceCrossRefContext(
						protoSpec,
						ServiceCrossRefType.OTHER,
						pctx.getLine(),
						"service-object-group",
						item.getOwner().getGroupId(),
						pctx.getFileName(),
						pctx.getLineNumber());
					ServiceCrossRef serv = getServiceCrossRef(range);
					serv.addContext(refctx);
				}
			}
		}

		if (objectGroup.getType() == ObjectGroupType.ENHANCED) {
			EnhancedServiceObjectGroup sObject =
				(EnhancedServiceObjectGroup) objectGroup;
			for (ObjectGroupItem item: sObject.expand()) {
				EnhancedServiceObjectGroupItem sitem
					= (EnhancedServiceObjectGroupItem) item;
				ParseContext pctx = item.getParseContext();
				if (pctx == null)
					continue;
				PortObject portObj = sitem.getServiceObject().getPortObject();
				if (portObj == null)
					continue;
				for (PortRange range: portObj.getPortSpec().getRanges()) {
					ProtocolsSpec protoSpec = sitem.getServiceObject().getProtocols();

					ServiceCrossRefContext refctx = new ServiceCrossRefContext(
						protoSpec,
						ServiceCrossRefType.OTHER,
						pctx.getLine(),
						"enhanced-service-object-group",
						item.getOwner().getGroupId(),
						pctx.getFileName(),
						pctx.getLineNumber());
					ServiceCrossRef serv = getServiceCrossRef(range);
					serv.addContext(refctx);
				}
			}
		}
	}

	protected void crossRefPortObject(PortObject portObject,
			ServiceCrossRefContext refContext) {

		PortRange range = portObject.getPortSpec().getRanges().get(0);
		ServiceCrossRef refService = getServiceCrossRef(range);
		refService.addContext(refContext);
	}


	/*
	 * Cross reference for an access list
	 */
	protected void crossRefAccessList(AccessList acl) {

		ParseContext context = acl.getParseContext();
		if (Log.debug().isLoggable(Level.INFO)) {
			Log.debug().info("xref acl: " + context.getLine());
		}

		CrossRefContext refContext = new CrossRefContext(context.getLine(),
				"acl",
				acl.getAction(),
				context.getFileName(),
				context.getLineNumber());

		NetworkObjectGroup networkGroup = acl.getSourceNetworkGroup();
		if (networkGroup != null)
			crossRefNetworkGroupSpec(networkGroup, refContext);

		networkGroup = acl.getDestNetworkGroup();
		if (networkGroup != null)
			crossRefNetworkGroupSpec(networkGroup, refContext);

		IPNet ip = acl.getSourceIp();
		if (ip != null) {
			IPCrossRef ipNetRef = getIPNetCrossRef(ip);
			ipNetRef.addContext(refContext);
		}

		ip = acl.getDestIp();
		if (ip != null) {
			IPCrossRef ipNetRef = getIPNetCrossRef(ip);
			ipNetRef.addContext(refContext);
		}

		/*
		 * protocol
		 */
		ProtocolsSpec protoSpec = new ProtocolsSpec();
		if (acl.getProtocol() != null)
			protoSpec.add(acl.getProtocol());
		if (acl.getProtocolGroup() != null)
			protoSpec.addAll(acl.getProtocolGroup().getProtocols());

		/*
		 * services sources
		 */
		/*
		 * port object
		 */
		if (acl.getSourcePortObject() != null) {
			ServiceCrossRefContext serviceContext =
				new ServiceCrossRefContext(protoSpec,
						ServiceCrossRefType.FROM,
						context.getLine(), "acl", acl.getAction(),
						context.getFileName(), context.getLineNumber());
			crossRefPortObject(acl.getSourcePortObject(), serviceContext);
		}

		/*
		 * service group
		 */
		if (acl.getSourceServiceGroup() != null) {
			protoSpec = acl.getSourceServiceGroup().getProtocols();

			ServiceCrossRefContext serviceContext =
				new ServiceCrossRefContext(protoSpec,
						ServiceCrossRefType.FROM,
						context.getLine(), "acl", acl.getAction(),
						context.getFileName(), context.getLineNumber());
			crossRefServiceGroupSpec(acl.getSourceServiceGroup(), serviceContext);
		}

		/*
		 * services destination
		 */
		/*
		 * port object
		 */
		if (acl.getDestPortObject() != null) {
			ServiceCrossRefContext serviceContext =
				new ServiceCrossRefContext(protoSpec,
						ServiceCrossRefType.TO,
						context.getLine(), "acl", acl.getAction(),
						context.getFileName(), context.getLineNumber());
			crossRefPortObject(acl.getDestPortObject(), serviceContext);
		}

		/*
		 * service group
		 */
		if (acl.getDestServiceGroup() != null) {
			protoSpec = acl.getDestServiceGroup().getProtocols();

			ServiceCrossRefContext serviceContext =
				new ServiceCrossRefContext(protoSpec,
						ServiceCrossRefType.TO,
						context.getLine(), "acl", acl.getAction(),
						context.getFileName(), context.getLineNumber());
			crossRefServiceGroupSpec(acl.getDestServiceGroup(), serviceContext);
		}

		/*
		 * enhanced service group
		 */
		if (acl.getEnhancedDestServiceGroup() != null) {
			protoSpec = new ProtocolsSpec();

			ServiceCrossRefContext serviceContext =
				new ServiceCrossRefContext(protoSpec,
						ServiceCrossRefType.TO,
						context.getLine(), "acl", acl.getAction(),
						context.getFileName(), context.getLineNumber());
			crossRefServiceGroupSpec(acl.getEnhancedDestServiceGroup(),
				serviceContext);
		}
	}

	/**
	 * Compute cross references
	 */
	protected void CrossReferences() {

		/*
		 * cross references for network groups
		 */
		for (ObjectGroup ogroup: _networkGroups.values()) {
			NetworkObjectGroup ngroup = (NetworkObjectGroup) ogroup;
			crossRefNetworkGroup(ngroup);
		}

		/*
		 * cross references for services groups
		 */
		for (ObjectGroup ogroup: _serviceGroups.values()) {
			crossRefServiceGroup(ogroup);
		}

		/*
		 * cross references for enhanced-services groups
		 */
		for (ObjectGroup ogroup: _enhancedGroups.values()) {
			crossRefServiceGroup(ogroup);
		}

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
		IPNet ipdest = probe.getDestinationAddress().toIPNet();
		if (ipdest != null) {
			IfaceLink ilink = getIfaceLink(ipdest);
			if (ilink != null) {
				/*
				 * Set the probe's final position and notify the monitor
				 */
				probe.setOutgoingLink(ilink, ipdest);
				probe.destinationReached("destination reached");
				return;
			}
		}

		/*
		 * Route the probe.
		 */
		Routes routes;
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
	 * tcp flags filter
	 */
	protected boolean tcpFlagsFilter(ProbeTcpFlags reqFlags) {

		TcpFlags flags = new TcpFlags("S");
		TcpFlags flagset = new TcpFlags("SA");

		if (flagset == null) {
			return reqFlags.matchAll(flags);
		} else {
			return reqFlags.matchAllWithout(flags, flagset);
		}
	}

	/**
	 *
	 */
	protected MatchResult probeFilter(Probe probe, AccessList acl, Direction direction) {

		ProbeRequest request = probe.getRequest();

		/*
		 * check ip source
		 */
		IPNet aclIpSource = acl.getSourceIp();
		IPRangeable probeIpSource = probe.getSourceAddress();

		NetworkObjectGroup aclSourceNetwork = acl.getSourceNetworkGroup();

		MatchResult mIpSource = MatchResult.ALL;
		if (aclIpSource != null) {
			if (!aclIpSource.contains(probeIpSource)) {
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
		IPRangeable probeIpDest = probe.getDestinationAddress();

		NetworkObjectGroup aclDestNetwork = acl.getDestNetworkGroup();
		MatchResult mIpDest = MatchResult.ALL;
		if (aclIpDest != null) {
			if (!aclIpDest.contains(probeIpDest)) {
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
		ProtocolsSpec reqProto = request.getProtocols();
		Integer aclProto = acl.getProtocol();
		ProtocolObjectGroup aclProtoGroup = acl.getProtocolGroup();

		if (reqProto != null) {
			/*
			 * protocol
			 */
			if (aclProto != null) {
				if (reqProto.matches(aclProto) == MatchResult.NOT)
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
				mSourcePort = MatchResult.NOT;
				int all = 0;
				int may = 0;
				for (int proto: reqProto) {
					MatchResult mres =
						aclSrcServiceGroup.matches(proto, reqSourcePort);

					if (mres == MatchResult.ALL)
						all++;
					if (mres == MatchResult.MATCH)
						may++;
				}
				if (all > 0) {
					mSourcePort = MatchResult.ALL;
				} else {
					if (may > 0)
						mSourcePort = MatchResult.MATCH;
				}
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
				mDestPort = MatchResult.NOT;
				int all = 0;
				int may = 0;
				for (int proto: reqProto) {
					MatchResult mres =
						aclDestServiceGroup.matches(proto, reqDestPort);

					if (mres == MatchResult.ALL)
						all++;
					if (mres == MatchResult.MATCH)
						may++;
				}
				if (all > 0) {
					mDestPort = MatchResult.ALL;
				} else {
					if (may > 0)
						mDestPort = MatchResult.MATCH;
				}
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
				reqProto.contains(Protocols.ICMP)) {
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

		/*
		 * check tcp flags
		 */
		if (acl.getAction().equals("permit")) {
			ProbeTcpFlags pflags = request.getTcpFlags();
			if (pflags != null) {
				if (!tcpFlagsFilter(pflags))
					return MatchResult.NOT;
			}
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

		String ifaceName = link.getIfaceName();
		ProbeResults results = probe.getResults();

		/*
		 * Accept the probe if probe request "state option" is set.
		 */
		if (probe.getRequest().getProbeOptions().hasState()) {
			results.setAclResult(direction, new FwResult(FwResult.ACCEPT));
			results.setInterface(direction, ifaceName + " # STATE MATCH");
			return;
		}

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
				 * XXX: what we should do?
				 */
				continue;
			}

			/*
			 * each acl in the group.
			 */
			for (AccessList acl: acg) {
				if (!acl.isRemark()) {
					MatchResult match;
					match = probeFilter(probe, acl, direction);
					if (match != MatchResult.NOT) {
						/*
						 * store the result in the probe
						 */
						FwResult aclResult = new FwResult();
						aclResult.addResult(acl.getAction().equals("permit") ?
							FwResult.ACCEPT : FwResult.DENY);

						if (match != MatchResult.ALL)
							aclResult.addResult(FwResult.MAY);

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
				}
			}
		}
	}

	@Override
	public void runShell(String command, PrintStream output) {
		_shell.shellCommand(command, output);
	}

}
