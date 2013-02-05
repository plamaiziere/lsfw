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

package fr.univrennes1.cri.jtacl.equipments.openbsd;

import fr.univrennes1.cri.jtacl.analysis.CrossRefContext;
import fr.univrennes1.cri.jtacl.analysis.IPNetCrossRef;
import fr.univrennes1.cri.jtacl.core.exceptions.JtaclConfigurationException;
import fr.univrennes1.cri.jtacl.core.exceptions.JtaclInternalException;
import fr.univrennes1.cri.jtacl.core.monitor.Log;
import fr.univrennes1.cri.jtacl.core.monitor.Monitor;
import fr.univrennes1.cri.jtacl.core.network.Iface;
import fr.univrennes1.cri.jtacl.core.network.IfaceLink;
import fr.univrennes1.cri.jtacl.core.network.IfaceLinksByIp;
import fr.univrennes1.cri.jtacl.core.network.Route;
import fr.univrennes1.cri.jtacl.core.network.Routes;
import fr.univrennes1.cri.jtacl.core.network.RoutingEngine;
import fr.univrennes1.cri.jtacl.core.probing.AclResult;
import fr.univrennes1.cri.jtacl.core.probing.MatchResult;
import fr.univrennes1.cri.jtacl.core.probing.Probe;
import fr.univrennes1.cri.jtacl.core.probing.ProbeRequest;
import fr.univrennes1.cri.jtacl.core.probing.ProbeResults;
import fr.univrennes1.cri.jtacl.core.probing.ProbeTcpFlags;
import fr.univrennes1.cri.jtacl.equipments.generic.GenericEquipment;
import fr.univrennes1.cri.jtacl.lib.ip.AddressFamily;
import fr.univrennes1.cri.jtacl.lib.ip.IPIcmpEnt;
import fr.univrennes1.cri.jtacl.lib.ip.IPNet;
import fr.univrennes1.cri.jtacl.lib.ip.PortSpec;
import fr.univrennes1.cri.jtacl.lib.ip.Protocols;
import fr.univrennes1.cri.jtacl.lib.ip.TcpFlags;
import fr.univrennes1.cri.jtacl.lib.misc.Direction;
import fr.univrennes1.cri.jtacl.lib.misc.ParseContext;
import fr.univrennes1.cri.jtacl.lib.misc.StringTools;
import fr.univrennes1.cri.jtacl.lib.misc.StringsList;
import fr.univrennes1.cri.jtacl.lib.xml.XMLUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.InvalidParameterException;
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
import org.w3c.dom.NodeList;

/**
 * Packet Filter based equipment.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PacketFilter extends GenericEquipment {

	/**
	 * Configuration file, mapped into string.
	 */
	protected class ConfigurationFile {

		protected String _fileName;
		protected StringBuilder _buffer = new StringBuilder();

		public StringBuilder getBuffer() {
			return _buffer;
		}

		public String getFileName() {
			return _fileName;
		}

		public void readFromFile(String fileName)
				throws FileNotFoundException, IOException {

			BufferedReader reader;
			reader = new BufferedReader(new FileReader(fileName));
			try {
				for (;;) {
					String s = reader.readLine();
					if (s != null) {
						_buffer.append(s.trim());
						_buffer.append('\n');
					} else
						break;
				}
			} finally {
				reader.close();
			}
			_fileName = fileName;
		}
	}

	/**
	 * Filter context
	 */
	protected class FilterContext {
		protected PfAnchor _anchor;
		protected Direction _direction;
		protected IfaceLink _link;

		public Direction getDirection() {
			return _direction;
		}

		public void setDirection(Direction _direction) {
			this._direction = _direction;
		}

		public IfaceLink getLink() {
			return _link;
		}

		public void setLink(IfaceLink link) {
			_link = link;
		}

		public PfAnchor getAnchor() {
			return _anchor;
		}

		public void setAnchor(PfAnchor anchor) {
			_anchor = anchor;
		}

		public FilterContext newInstance() {
			FilterContext instance = new FilterContext();
			instance._anchor = _anchor;
			instance._direction = _direction;
			instance._link = _link;
			return instance;
		}

	}

	protected class TableToLoad {
		protected String _name;
		protected String _anchorName;
		protected StringsList _files = new StringsList();

		protected TableToLoad(String name, String anchorName) {
			_name = name;
			_anchorName = anchorName;
		}

		public String getAnchorName() {
			return _anchorName;
		}

		public StringsList getFiles() {
			return _files;
		}

		public String getName() {
			return _name;
		}

	}

	/**
	 * Parser
	 */
	protected PacketFilterParser _parser =
			Parboiled.createParser(PacketFilterParser.class);

	/**
	 * ParseRunner for parse()
	 */
	protected BasicParseRunner _parseRunParse =
			new BasicParseRunner(_parser.Parse());

	/**
	 * ParseRunner for PfGenericRule
	 */
	protected BasicParseRunner _parseRunePfGenericRule =
			new BasicParseRunner(_parser.PfGenericRule());

	/*
	 * interfaces
	 */
	protected HashMap<String, PfIface> _pfIfaces = new HashMap<String, PfIface>();

	/*
	 * skipped interfaces
	 */
	protected Map<String, String> _skippedIfaces = new HashMap<String, String>();

	/*
	 * pf.conf mapped into a string
	 */
	protected ConfigurationFile _pfConf;

	/*
	 * List of directories to search for file.
	 */
	protected StringsList _dirMap = new StringsList();

	/*
	 * List of files mapped.
	 */
	protected Map<String, String> _fileMap = new HashMap<String, String>();

	/**
	 * parse context
	 */
	protected ParseContext _parseContext = new ParseContext();

	/**
	 * Tables to load from file.
	 */
	protected Map<String, TableToLoad> _tablesToLoad =
			new HashMap<String, TableToLoad>();

	/**
	 * Anchors to load from file.
	 */
	protected Map<String, String> _anchorsToLoad =
			new HashMap<String, String>();

	/**
	 * routes filename
	 */
	 String _routesFile;

	/**
	 * The root anchor
	 */
	protected PfAnchor _rootAnchor;

	/**
	 * Current anchor at parsing time
	 */
	protected PfAnchor _curAnchor;

	/**
	 * refence of all tables
	 */
	protected List<PfTable> _refTables = new ArrayList<PfTable>();

	/**
	 * reference of all rules
	 */
	protected List<PfRule> _refRules = new ArrayList<PfRule>();

	/**
	 * IPNet cross references
	 */
	protected Map<IPNet, IPNetCrossRef> _netCrossRef =
			new HashMap<IPNet, IPNetCrossRef>();

	/**
	 * the next anchor uid that will be generated.
	 */
	protected static int _anchorNextUid = 0;

	/**
	 * Generates and returns a new anchor uid.
	 */
	synchronized protected int newAnchorUid() {
		int uid = _anchorNextUid;
		_anchorNextUid++;
		return uid;
	}

	/**
	 * route to engine.
	 */
	protected RoutingEngine _routeToEngine = null;

	/**
	 * filtering flag done, used at filtering time.
	 */
	protected boolean _filteringDone = false;

	protected void throwCfgException(String msg) {
		if (_parseContext != null)
			throw new JtaclConfigurationException(_parseContext.toString() + msg);
		else
			throw new JtaclConfigurationException(msg);
	}

	protected void warnConfig(String msg) {

		String context;
		if (_parseContext != null)
			context = _parseContext.toString();
		else
			context = "";

		Log.config().warning(context + msg);
	}

	protected void severeConfig(String msg) {

		String context;
		if (_parseContext != null)
			context = _parseContext.toString();
		else
			context = "";

		Log.config().severe(context + msg);
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

	/*
	 * IPNet cross reference
	 */
	public Map<IPNet, IPNetCrossRef> getNetCrossRef() {
		return _netCrossRef;
	}

	protected PacketFilterShell _shell = new PacketFilterShell(this);

	/**
	 * Create a new {@link PacketFilter} with this name and this comment.<br/>
	 * @param monitor the {@link Monitor} monitor associated with this equipment.
	 * @param name the name of the equipment.
	 * @param comment a free comment for this equipment.
	 * @param configurationFileName name of the configuration file to use (may be null).
	 */
	public PacketFilter(Monitor monitor, String name, String comment,
			String configurationFileName) {

		super(monitor, name, comment, configurationFileName);
		_rootAnchor = PfAnchor.newRootAnchor();
	}

	protected void loadIfaces(Document doc) {

		NodeList list = doc.getElementsByTagName("iface");
		for (int i = 0; i < list.getLength(); i++) {
			Element e = (Element) list.item(i);
			String name = e.getAttribute("name");
			String comment = e.getAttribute("comment");
			String ifIp = e.getAttribute("ip");
			String ifNetwork = e.getAttribute("network");
			String ifGroups = e.getAttribute("groups");

			String s = "name: " + name + " comment: " + comment +
					" IP: " + ifIp + " network: " + ifNetwork + " groups: " +
					ifGroups;

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
			 * interface groups
			 */
			String[] groups = ifGroups.split("\\s+");

			/*
			 * Append the link to an existing Iface or create a new one.
			 */
			PfIface pfIface = _pfIfaces.get(name);
			Iface iface = getIface(name);
			if (iface == null) {
				if (comment.isEmpty())
					throw new JtaclConfigurationException("Missing interface comment: " + s);
				iface = addIface(name, comment);
				pfIface = new PfIface(iface);
				for (String g: groups) {
					if (!g.isEmpty())
						pfIface.getGroups().add(g);
				}
				_pfIfaces.put(name, pfIface);
			}
			IfaceLink link = iface.addLink(ip, network);
			/*
			 * we assume that the first address ip for each familly is
			 * not an alias.
			 */
			if (link.getIp().isIPv4() && pfIface.getFirstIpV4() == null)
				pfIface.setFirstIpV4(link);
			if (link.getIp().isIPv6() && pfIface.getFirstIpV6() == null)
				pfIface.setFirstIpV6(link);

		}
	}

	/**
	 * Loads routes from a file.
	 * The format of the file should be "route add network nexthop"
	 * @param fileName filename to use.
	 * @throws JtaclConfigurationException if error occurs.
	 */
	protected void loadRoutesFromFile(String fileName) {

		StringsList sroutes = new StringsList();
		try {
			sroutes.readFromFile(fileName);
		} catch (FileNotFoundException ex) {
			throw new JtaclConfigurationException("No such routes file: " +
					fileName);
		} catch (IOException ex) {
			throw new JtaclConfigurationException("Cannot read routes file: " +
					fileName);
		}
		for (String sroute: sroutes) {
			sroute = sroute.trim();
			if (sroute.isEmpty() || sroute.startsWith("#"))
				continue;
			String[] sparams = sroute.split("\\s+");
			int nbargs = sparams.length;
			if (nbargs < 4 || nbargs > 5)
				continue;
			if (!sparams[0].equals("route") || !sparams[1].equals("add"))
				continue;
			String sprefix = sparams[2];
			String snexthop = sparams[3];
			IPNet prefix;
			try {
				prefix = new IPNet(sprefix);
			} catch (UnknownHostException ex) {
				throw new JtaclConfigurationException("Invalid route prefix: "
					+ sroute);
			}

			IPNet nexthop;
			try {
				nexthop = new IPNet(snexthop);
			} catch (UnknownHostException ex) {
				throw new JtaclConfigurationException("Invalid route nexthop: "
					+ sroute);
			}

			Route<IfaceLink> route;

			/*
			 * null route
			 */
			if (nbargs == 5 && sparams[4].equals("-blackhole")) {
				route = new Route<IfaceLink>(prefix);
			} else {
				Iface iface = null;
				IfaceLink link = null;

				/*
				 * use the directly connected network containing nextHop
				 */
				try {
					iface = getIfaceConnectedTo(nexthop);
				} catch (UnknownHostException ex) {
					throw new JtaclConfigurationException("Invalid route " +
							sroute + " " + ex.getMessage());
				}
				if (iface == null) {
					Log.config().severe("Invalid route, nexthop" +
							" is not on a subnet of this equipment: " + _name +
							": " + sroute);
					continue;
				}
				try {
					link = iface.getLinkConnectedTo(nexthop);
				} catch (UnknownHostException ex) {
					throw new JtaclConfigurationException("Invalid route " +
							sroute + " " + ex.getMessage());
				}

				/*
				 * add the route.
				 */
				if (link == null)
					throw new JtaclConfigurationException(
							"Invalid route: cannot find link " + sroute);

				route = new Route<IfaceLink>(prefix, nexthop, 1, link);
			}
			Log.debug().info(_name + " add route: " + route.toString());
			_routingEngine.addRoute(route);

		} // for
	}


	protected void loadConfiguration(Document doc) {

		/*
		 * pf.conf filename
		 */
		NodeList list = doc.getElementsByTagName("pfconf");
		if (list.getLength() == 0)
			throw new JtaclConfigurationException("Missing 'pfconf' entity");
		if (list.getLength() > 1)
			throw new JtaclConfigurationException("Only one pfconf entity is allowed");

		Element e = (Element) list.item(0);
		String filename = e.getAttribute("filename");
		if (filename.isEmpty())
			throw new JtaclConfigurationException("Missing pfconf file name");

		_pfConf = new ConfigurationFile();
		try {
			_pfConf.readFromFile(filename);
		} catch (FileNotFoundException ex) {
				throw new JtaclConfigurationException("pfconf file not found: " +
					filename);
		} catch (IOException ex) {
				throw new JtaclConfigurationException("Cannot read pfconf file: " +
					filename);
		}

		/*
		 * routes filename
		 */
		list = doc.getElementsByTagName("routes-file");
		if (list.getLength() > 1)
			throw new JtaclConfigurationException("Only one routes-file entity is allowed");
		if (list.getLength() == 1) {
			e = (Element) list.item(0);
			filename = e.getAttribute("filename");
			if (filename.isEmpty())
				throw new JtaclConfigurationException("Missing routes-file file name");

			File file = new File(filename);
			if (!file.exists() || !file.isFile())
				throw new JtaclConfigurationException("No such routes-file: " + filename);
			_routesFile = filename;
		}

		/*
		 * search directories for files
		 */
		list = doc.getElementsByTagName("dirmap");
		for (int i = 0; i < list.getLength(); i++) {
			e = (Element) list.item(i);
			String directory = e.getAttribute("directory");
			if (directory.isEmpty())
				throw new JtaclConfigurationException("Missing dirmap directory");
			File file = new File(directory);
			if (!file.exists() || !file.isDirectory())
				throw new JtaclConfigurationException("No such dirmap directory: " +
					directory);

			_dirMap.add(directory);
		}

		/*
		 * direct map filename => filename
		 */
		list = doc.getElementsByTagName("filemap");
		for (int i = 0; i < list.getLength(); i++) {
			e = (Element) list.item(i);
			String from = e.getAttribute("from");
			if (from.isEmpty())
				throw new JtaclConfigurationException("Missing 'from' file");
			String to = e.getAttribute("to");
			if (to.isEmpty())
				throw new JtaclConfigurationException("Missing 'to' file");
			File file = new File(to);
			if (!file.exists() || !file.isFile())
				throw new JtaclConfigurationException("No such 'to' file: " + to);

			_fileMap.put(from, to);
		}

		/*
		 * files associated to tables
		 */
		list = doc.getElementsByTagName("table");
		for (int i = 0; i < list.getLength(); i++) {
			e = (Element) list.item(i);
			String name = e.getAttribute("name");
			String anchorName = e.getAttribute("anchor");
			filename = e.getAttribute("filename");

			String s = "name: " + name + " anchor: " + anchorName +
					" filename: " + filename;

			if (name.isEmpty())
				throw new JtaclConfigurationException("Missing table name: " + s);

			if (filename.isEmpty())
				throw new JtaclConfigurationException("Missing table filename: " + s);

			File file = new File(filename);
			if (!file.exists() || !file.isFile())
				throw new JtaclConfigurationException("No such file: " + s);


			String tkey = anchorName + "/" + name;
			TableToLoad toLoad = _tablesToLoad.get(anchorName + "/" + name);
			if (toLoad == null) {
				toLoad = new TableToLoad(name, anchorName);
				_tablesToLoad.put(tkey, toLoad);
			}
			toLoad.getFiles().add(filename);
		}

		/*
		 * files associated to anchors
		 */
		list = doc.getElementsByTagName("anchor");
		for (int i = 0; i < list.getLength(); i++) {
			e = (Element) list.item(i);
			String name = e.getAttribute("name");
			filename = e.getAttribute("filename");

			String s = "name: " + name + " filename: " + filename;

			if (name.isEmpty())
				throw new JtaclConfigurationException("Missing anchor name: " + s);

			if (filename.isEmpty())
				throw new JtaclConfigurationException("Missing anchor filename: " + s);

			File file = new File(filename);
			if (!file.exists() || !file.isFile())
				throw new JtaclConfigurationException("No such anchor file: " + s);

			if (!_anchorsToLoad.containsKey(name))
				_anchorsToLoad.put(name, filename);
		}

	}

	protected void loadTables() {
		for (String n:  _tablesToLoad.keySet()) {
			TableToLoad toLoad = _tablesToLoad.get(n);
			String anchorName = toLoad.getAnchorName();
			String tableName = toLoad.getName();
			/*
			 * anchor to use
			 */
			PfAnchor anchor;
			if (anchorName.isEmpty())
				anchor = _rootAnchor;
			else {
 				anchor = _rootAnchor.findOrCreateAnchor(anchorName);
				if (anchor == null)
					throwCfgException("Table: " + tableName +
							"  access anchor: " + anchorName);
			}
			PfTable table = new PfTable();
			table.setName(tableName);
			table.setConfigurationLine("");
			table.setText("");
			anchor.addTable(table);
			/*
			 * load each file
			 */
			for (String fileName: toLoad.getFiles()) {
				PfIpSpec ipspec = loadTable(fileName);
				table.getIpspec().addAll(ipspec);
			}

			if (Log.debug().isLoggable(Level.INFO)) {
				String s = "table: " + table.getName() + " " + table.getFileNames() +
					"\nip=" + table.getIpspec();
				Log.debug().info(s);
			}
		}
	}

	protected void loadAnchors() {

		_curAnchor = _rootAnchor;
		for (String anchorName: _anchorsToLoad.keySet()) {
			String fileName = _anchorsToLoad.get(anchorName);
			ruleLoadAnchor(anchorName, fileName);
		}
	}

	protected String translateFileName(String fileName) {
		String name = _fileMap.get(fileName);
		if (name != null)
			return name;
		for (String d: _dirMap) {
			name = d + File.separator + fileName;
			File file = new File(name);
			if (file.exists() && file.isFile())
				return name;
		}
		return fileName;
	}

	/*
	 * look for ifaces member of groupName.
	 */
	protected StringsList ifaGroupLookup(String groupName) {
		StringsList listIf = new StringsList();

		for (PfIface iface : _pfIfaces.values()) {
			if (iface.getGroups().contains(groupName))
				listIf.add(iface.getIface().getName());
		}
		return listIf;
	}

	protected List<IPNet> ifaLookup(String ifname, int flags) {

		List<IPNet> addr = new ArrayList<IPNet>();

		/*
		 * if ifname is a group, retrieve the name of the ifaces associated
		 * to this group
		 */
		StringsList listIf = ifaGroupLookup(ifname);
		if (listIf.isEmpty())
			listIf.add(ifname);

		/*
		 * each interface
		 */
		for (PfIface pfIface : _pfIfaces.values()) {
			String curIfName = pfIface.getIface().getName();
			if (!ifname.equals("self") &&  !listIf.contains(curIfName))
				continue;

			/*
			 * each link
			 */
			IfaceLinksByIp links = pfIface.getIface().getLinks();
			for (IfaceLink link: links.values()) {
				/*
				 * XXX: we don't handle PFI_AFLAG_PEER
				 */
				if ((flags & PfConst.PFI_AFLAG_BROADCAST) > 0) {
					try {
						IPNet ip = link.getNetwork().lastNetworkAddress().hostAddress();
						addr.add(ip);
					} catch (UnknownHostException ex) {
						throwCfgException("invalid IP address: " + ex.getMessage());
					}
				}
				if ((flags & PfConst.PFI_AFLAG_NETWORK) > 0) {
					IPNet ip = link.getNetwork();
					addr.add(ip);
				}
				if ((flags & PfConst.PFI_AFLAG_NOALIAS) > 0) {
					if ((link.getIp().isIPv4() && pfIface.getFirstIpV4() == link) ||
						(link.getIp().isIPv6() && pfIface.getFirstIpV6() == link)) {
						IPNet ip = link.getIp();
						addr.add(ip);
					}
				}
				if ((flags & PfConst.PFI_AFLAG_PEER) > 0) {
					throwCfgException(":peer not supported");
				}
				if (flags == 0) {
					IPNet ip = link.getIp();
					addr.add(ip);
				}
			}
		}
		return addr;
	}

	protected List<IPNet> parseHostIf(String hostName, int mask)
			throws UnknownHostException {

		List<IPNet> addr = new ArrayList<IPNet>();
		int flags = 0;

		for (;;) {
			int p = hostName.lastIndexOf(':');
			if (p < 0)
				break;
			String spec = hostName.substring(p + 1);
			hostName = hostName.substring(0, p);
			if (spec.equals("network"))
				flags |= PfConst.PFI_AFLAG_NETWORK;
			if (spec.equals("broadcast"))
				flags |= PfConst.PFI_AFLAG_BROADCAST;
			if (spec.equals("peer")) {
				flags |= PfConst.PFI_AFLAG_PEER;
				warnConfig(" :peer in address not implemented");
				return addr;
			}
			if (spec.equals("0"))
				flags |= PfConst.PFI_AFLAG_NOALIAS;
		}
		if ((mask >= 0 && (flags & (PfConst.PFI_AFLAG_BROADCAST |
				PfConst.PFI_AFLAG_NETWORK)) > 0))
			throwCfgException("network or broadcast lookup with extra netmask");

		List<IPNet> ifaddr = ifaLookup(hostName, flags);

		for (IPNet ip : ifaddr) {
			if (mask >= 0)
				addr.add(ip.setMask(mask));
			else
				addr.add(ip);
		}

		return addr;
	}

	protected List<IPNet> pfHostDns(String host, int mask)
			throws UnknownHostException {

		List<IPNet> addr = new ArrayList<IPNet>();

		boolean noalias = false;
		boolean got4 = false;
		boolean got6 = false;

		if (host.endsWith(":0")) {
			noalias = true;
			host = host.substring(0, host.length() - 2);
		}

		InetAddress [] inets = InetAddress.getAllByName(host);
		for (InetAddress inet: inets) {
			if (noalias) {
				if (inet instanceof Inet4Address) {
					if (got4)
						continue;
					got4 = true;

				}
				if (inet instanceof Inet6Address) {
					if (got6)
						continue;
					got6 = true;
				}
			}
			IPNet ip = new IPNet(inet.getHostAddress());
			if (mask >= 0)
				ip = ip.setMask(mask);
			addr.add(ip);
		}
		return addr;
	}

	protected List<IPNet> parseHost(String host) throws UnknownHostException {

		List<IPNet> hosts = null;
		int imask = -1;
		boolean cont = true;

		/*
		 * ifname
		 * XXX ?
		 */
		String ifname = null;
		int p = host.lastIndexOf('@');
		if (p > 0) {
			ifname = host.substring(p + 1);
			host = host.substring(0, p - 1);
		}

		/*
		 * mask
		 */
		String mask;
		String dup = host;
		p = host.lastIndexOf('/');
		if (p > 0) {
			mask = host.substring(p + 1);
			host = host.substring(0, p);
			try {
				imask = Integer.parseInt(mask);
			} catch (NumberFormatException ex) {
				throwCfgException("invalid netmask: " + ex.getMessage());
			}
		}

		/*
		 * interface with that name exists?
		 */
		try {
			hosts = parseHostIf(host, imask);
			if (!hosts.isEmpty())
				cont = false;
		} catch (UnknownHostException ex) {
			throwCfgException("invalid IP address: " + ex.getMessage());
		}

		/*
		 * ip address?
		 */
		if (cont) {
			try {
				IPNet ip = new IPNet(dup);
				hosts = new ArrayList<IPNet>();
				hosts.add(ip);
				cont = false;
			} catch (UnknownHostException ex) {
				//
			}
		}

		/*
		 * dns lookup
		 */
		if (cont) {
			hosts = pfHostDns(host, imask);
		}
		return hosts;

	}

	protected PfNodeHost xhostToHost(Xhost xhost)
			throws UnknownHostException {

		PfNodeHost host;
		boolean not = xhost.isNot();
		List<IPNet> addr = null;
		List<IPNet> addrRange = null;

		if (xhost.isAny()) {
			host = PfNodeHost.newAddrAny();
			host.setNot(not);
			return host;
		}

		if (xhost.isNoroute()) {
			host = PfNodeHost.newAddrNoRoute();
			host.setNot(not);
			return host;
		}

		if (xhost.isUrpffailed()) {
			host = PfNodeHost.newAddrUrpfFailed();
			host.setNot(not);
			return host;
		}

		if (xhost.getRoute() != null) {
			host = PfNodeHost.newAddrRtLabel(xhost.getRoute());
			host.setNot(not);
			return host;
		}

		if (xhost.getTable() != null) {
			host = PfNodeHost.newAddrTable(xhost.getTable());
			host.setNot(not);
			return host;
		}

		/*
		 * if dynaddr, use a normal host specification
		 */
		String hostName;
		if (xhost.getDynaddr() != null) {
			hostName = xhost.getDynaddr();
			if (xhost.getDynaddrMask() != null)
				hostName = hostName + "/" + xhost.getDynaddrMask();
		} else
			hostName = xhost.getFirstAddress();

		/*
		 * parse host
		 */
		if (hostName != null) {
			addr = parseHost(hostName);
			if (addr.isEmpty()) {
				warnConfig("cannot resolve host: " + hostName);
				return null;
			}
		}

		hostName = xhost.getLastAddress();
		if (hostName != null) {
			addrRange = parseHost(hostName);
			if (addrRange.isEmpty()) {
				warnConfig("cannot resolve host: " + hostName);
				return null;
			}
		}

		boolean range = addrRange != null && addr != null;
		if (range)
			host = PfNodeHost.newAddrRange();
		else
			host = PfNodeHost.newAddrMask();

		if (range && (addrRange.size() != addr.size() ||
				addrRange.size() != 1)) {
			warnConfig("invalid range");
			return null;
		}

		host.addAddr(addr);
		if (range)
			host.addAddrRange(addrRange);

		host.setNot(not);
		return host;
	}

	protected PfIfListSpec parseIfList(StringsList list) {
		PfIfListSpec ifList = new PfIfListSpec();
		for (String ifname: list) {
			/*
			 * not interface
			 */
			boolean ifnot = false;
			if (ifname.startsWith("!")) {
				ifnot = true;
				ifname = ifname.substring(1);
			}
			/*
			 * lookup for interface group
			 */
			StringsList group = ifaGroupLookup(ifname);
			if (group.isEmpty())
				group.add(ifname);
			for (String ifgroup: group) {
				PfIfSpec ifspec = new PfIfSpec(ifnot, ifgroup);
				ifList.add(ifspec);
			}
		 }
		/*
		 * check associated iface
		 */
		for (PfIfSpec ifspec: ifList) {
			if (!_pfIfaces.containsKey(ifspec.getIfName())) {
				warnConfig("unknown interface: " + ifspec.getIfName());
			}
		}
		return ifList;
	 }

	protected List<Integer> parseProtoList(StringsList protocols) {
		List<Integer> proto = new ArrayList<Integer>();

		for (String protoName: protocols) {
			int protocol = parseProtocol(protoName);
			proto.add(protocol);
		}
		return proto;
	}

	protected PfIpSpec parseIpSpec(List<Xhost> xhostList) {
		PfIpSpec ipspec = new PfIpSpec();

		for (Xhost xhost: xhostList) {
			PfNodeHost node = null;
			try {
				node = xhostToHost(xhost);
			} catch (UnknownHostException ex) {
				throwCfgException("invalid host specification " +
					ex.getMessage());
			}
			ipspec.add(node);
		}
		return ipspec;
	}

	protected PfPortSpec parsePortSpec(List<PortItemTemplate> portsList) {
		PfPortSpec portspec = new PfPortSpec();

		for (PortItemTemplate port: portsList) {
			String sfirst = port.getFirstPort();
			String slast = port.getLastPort();
			int first = -1;
			int last = -1;
			if (sfirst != null) {
				first = parseService(sfirst, null);
			}
			if (slast != null ) {
				last = parseService(slast, null);
			}
			String operator = port.getOperator();
			if (operator == null)
				operator = "=";

			/*
			 * XXX: PF allows range with first port > last port but
			 * this does not mean anything. Reject this.
			 */
			if (last != -1 && (last < first))
				severeConfig("invalid port range: " + first +
					" " + last);

			if (last != -1) {
				PfPortItem portItem = new PfPortItem(operator, first, last);
				portspec.add(portItem);
			} else {
				PfPortItem portItem = new PfPortItem(operator, first);
				portspec.add(portItem);
			}
		}
		return portspec;
	}

	protected IPIcmpEnt parseIcmpItem(IcmpItem item, AddressFamily af) {

		int code = -1;
		String scode = item.getIcmpCode();
		if (scode != null) {
			IPIcmpEnt ent = af == AddressFamily.INET ? parseIcmp4(scode) :
				parseIcmp6(scode);
			code = ent.getCode();
			/*
			 * not a code
			 */
			if (code == -1)
				throwCfgException("invalid icmp code: " + scode);
		}
		String sicmp = item.getIcmpType();
		IPIcmpEnt ent = af == AddressFamily.INET ? parseIcmp4(sicmp) :
			parseIcmp6(sicmp);
		if (code != -1) {
			ent = new IPIcmpEnt(ent.getName(), ent.getIcmp(), code);
		}
		return ent;
	}

	protected PfRouteOpts parseRouteOpts(RouteOptsTemplate template) {

		/*
		 * XXX: we just handle route-to (interface nexthop)
		 */
		if (template.getRt() != PfConst.PF_ROUTETO)
			return null;
		if (template.getHosts().size() != 1)
			return null;
		Xhost xhost = template.getHosts().get(0);
		String sifname = xhost.getIfName();
		if (sifname == null)
			return null;
		if (xhost == null)
			return null;

		/*
		 * check ifname
		 */
		PfIface pfiface = _pfIfaces.get(sifname);
		if (pfiface == null) {
				warnConfig("unknown interface: " + sifname);
				return null;
		}

		/*
		 * nexthop
		 */
		PfNodeHost nhost;
		try {
			nhost = xhostToHost(xhost);
		} catch (UnknownHostException ex) {
			warnConfig("invalid nexthop: " + xhost.getFirstAddress());
			return null;
		}
		if (nhost == null || !nhost.isAddrMask() ||
				nhost.getAddr().size() != 1) {
			warnConfig("invalid nexthop: " + xhost.getFirstAddress());
			return null;
		}

		/*
		 * link
		 */
		IfaceLink link;
		try {
			link = pfiface.getIface().getLinkConnectedTo(nhost.getAddr().get(0));
		} catch (UnknownHostException ex) {
			link = null;
		}
		if (link == null) {
			warnConfig("invalid nexthop (no link): " + xhost.getFirstAddress());
		}

		PfRouteOpts ropts = PfRouteOpts.newRouteOptsRouteTo();
		ropts.setRoute(sifname, nhost.getAddr().get(0), link);
		return ropts;
	}

	protected PfIpSpec loadTable(String fileName) {

		StringBuilder buf = new StringBuilder("");
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(fileName));
		} catch (FileNotFoundException ex) {
			throwCfgException("cannot load table file: " + fileName +
					" : " + ex.getMessage());
		}

		try {
			/*
			 * Build a tabledef rule
			 */
			for (;;) {
				String s = null;
				try {
					s = reader.readLine();
				} catch (IOException ex) {
					throwCfgException("cannot read table file: " + fileName +
						" : " + ex.getMessage());
				}
				if (s != null) {
					s = s.trim();
					if (s.isEmpty() || s.startsWith("#"))
						continue;
					if (buf.length() > 0)
						buf.append(", ");
					buf.append(s.trim());
				}
				else
					break;
			}
		} finally {
			try {
				reader.close();
			} catch (IOException ex) {
				throwCfgException("cannot close file: " + fileName +
						" : " + ex.getMessage());
			}
		}
		buf.append(" }");
		buf.insert(0, "table <XXXX> { ");
		/*
		 * use the parser to retrieve the values
		 */
		ParsingResult<?> result;
		result = _parseRunParse.run(buf.toString());

		if (!result.matched)
			throwCfgException("cannot parse table's file:  " + fileName);

		famAdd(fileName);
		return parseIpSpec(_parser.getPfTable().getHosts());
	}

	/*
	 * PF filtering rule
	 */
	protected PfRule rulePfRule(RuleTemplate ruleTpl, boolean anchorRule) {

		PfRule rule = new PfRule();

		/*
		 * context
		 */
		String text = _parseContext.getLine().trim();
		rule.setConfigurationLine(_parseContext.getFileName() + " #" +
			_parseContext.getLineNumber() + ": ");
		rule.setText(text);
		rule.setParseContext(_parseContext);

		/*
		 * action
		 */
		rule.setAction(ruleTpl.getAction());
		if (!anchorRule && !rule.isBlock() && !rule.isMatch() && !rule.isPass())
			throwCfgException("invalid action: " + ruleTpl.getAction());

		/*
		 * direction
		 */
		String dir = ruleTpl.getDir();
		if (dir == null) {
			rule.setDirection(Direction.INOUT);
		} else {
			if (dir.equals("in")) {
				rule.setDirection(Direction.IN);
			} else {
				if (dir.equals("out")) {
					rule.setDirection(Direction.OUT);
				} else {
					throwCfgException("invalid direction: " + dir);
				}
			}
		}

		/*
		 * quick
		 */
		rule.setQuick(ruleTpl.isQuick());

		/*
		 * interfaces
		 */
		if (ruleTpl.getIfList() != null) {
			PfIfListSpec ifList = parseIfList(ruleTpl.getIfList());
			rule.getIfList().addAll(ifList);
		 }

		/*
		 * protocols
		 */
		if (ruleTpl.getProtoList() != null) {
			List<Integer> protoList = parseProtoList(ruleTpl.getProtoList());
			rule.getProtocols().addAll(protoList);
		}

		/*
		 * af
		 */
		String af = ruleTpl.getAf();
		rule.setAf(AddressFamily.NONE);
		if (af != null) {
			if (af.equals("inet")) {
				rule.setAf(AddressFamily.INET);
			} else {
				if (af.equals("inet6")) {
					rule.setAf(AddressFamily.INET6);
				} else
					throwCfgException("invalid address family: " + af);
			}
		}

		/*
		 * ALL
		 */
		rule.setAll(ruleTpl.isAll());

		/*
		 * from ip spec
		 */
		if (ruleTpl.getSourceHostList() != null) {
			PfIpSpec ipspec = parseIpSpec(ruleTpl.getSourceHostList());
			rule.getFromIpSpec().addAll(ipspec);
		}

		/*
		 * from port spec
		 */
		if (ruleTpl.getSourcePortList() != null) {
			PfPortSpec portspec = parsePortSpec(ruleTpl.getSourcePortList());
			rule.getFromPortSpec().addAll(portspec);
		}

		/*
		 * to ip spec
		 */
		if (ruleTpl.getDestHostList() != null) {
			PfIpSpec ipspec = parseIpSpec(ruleTpl.getDestHostList());
			rule.getToIpSpec().addAll(ipspec);
		}

		/*
		 * to port spec
		 */
		if (ruleTpl.getDestPortList() != null) {
			PfPortSpec portspec = parsePortSpec(ruleTpl.getDestPortList());
			rule.getToPortSpec().addAll(portspec);
		}

		/*
		 * options
		 */
		FilterOptsTemplate opts = ruleTpl.getFilterOpts();

		if (opts != null) {
			/*
			 * route options
			 */
			RouteOptsTemplate rot = opts.getRouteOpts();
			if (rot != null) {
				PfRouteOpts routeOpts = parseRouteOpts(rot);
				rule.SetRouteOpts(routeOpts);
			}

			/*
			 * tag option
			 */
			String tag = opts.getTag();
			if (tag != null) {
				PfTagOpt tagopt = new PfTagOpt(tag);
				rule.setTagOpt(tagopt);
			}

			/*
			 * tagged option
			 */
			tag = opts.getMatchTag();
			if (tag != null) {
				PfTaggedOpt taggedOpt =
						new PfTaggedOpt(opts.isMatchTagNot(), tag);
				rule.setTaggedOpt(taggedOpt);
			}
		}

		/*
		 * icmp specification
		 */
		AddressFamily icmpAf = AddressFamily.NONE;
		List<IcmpItem> icmpItems = null;
		if (!opts.getIcmpspec().isEmpty()) {
			icmpAf = AddressFamily.INET;
			icmpItems = opts.getIcmpspec();
		}
		if (!opts.getIcmp6spec().isEmpty()) {
			icmpAf = AddressFamily.INET6;
			icmpItems = opts.getIcmp6spec();
		}
		if (icmpItems != null) {
			for (IcmpItem item: icmpItems) {
				IPIcmpEnt icmpEnt = parseIcmpItem(item, icmpAf);
				rule.getIcmpspec().add(icmpEnt);
			}
		}
		rule.getIcmpspec().setAf(icmpAf);

		/*
		 * filtering action
		 */
		String filterAction = opts.getAction();
		boolean nostate = false;
		if (filterAction == null && rule.isPass()) {
			/* default keep state for pass rule*/
			filterAction = "keep-state";
		}
		if (filterAction != null && filterAction.equals("no-state")) {
			filterAction = null;
			nostate = true;
		}

		if (filterAction != null && !rule.isPass())
			throwCfgException("state only allowed in pass rule: " + filterAction);

		rule.setFilterAction(filterAction);

		/*
		 * tcp flags
		 */

		/*
		 * flags are allowed for rule with empty protocol or containing TCP.
		 */
		boolean acceptflags = rule.getProtocols().isEmpty() ||
				rule.getProtocols().contains(Protocols.TCP);

		String flags = opts.getFlags();
		String flagset = opts.getFlagset();
		if (flags == null && flagset == null && rule.isPass() &&
				filterAction != null && acceptflags && !nostate) {
			/* default to S/SA for pass rules unless "no state" is specified */
			flags = "S";
			flagset = "SA";
		}

		if ((flags != null || flagset != null) && !acceptflags)
			throwCfgException("tcp flags not allowed here");

		/*
		 * if "any" do not assign any flags, else convert the flags and
		 * store them in the rule.
		 */
		if (flags != null && flags.equals("any")) {
			// nothing
		} else {
			TcpFlags tcpFlags = null;
			TcpFlags tcpFlagsSet = null;
			if (flags != null) {
				try {
					tcpFlags = new TcpFlags(flags);
				} catch (InvalidParameterException ex) {
					throwCfgException("invalid tcp flags: " + flags);
				}
			} else {
				tcpFlags = new TcpFlags();
			}
			if (flagset != null) {
				try {
					tcpFlagsSet = new TcpFlags(flagset);
				} catch (InvalidParameterException ex) {
					throwCfgException("invalid tcp flagset: " + flagset);
				}
				if (tcpFlagsSet.equals(tcpFlags))
					tcpFlagsSet = null;
			}
			rule.setFlags(tcpFlags);
			rule.setFlagset(tcpFlagsSet);
		}

		if (Log.debug().isLoggable(Level.INFO)) {
			String s = "pfrule: " +
					rule.getAction() + " " + rule.getDirection() + " af=" +
					rule.getAf() + " quick=" + rule.isQuick() + " ALL= " +
					rule.isAll() +
					"\nif=" + rule.getIfList() +
					"\nproto=" + rule.getProtocols() +
					"\nfrom=" + rule.getFromIpSpec() +
					"\nfromport=" + rule.getFromPortSpec() +
					"\nto=" + rule.getToIpSpec() +
					"\ntoport " + rule.getToPortSpec() +
					"\nicmp " + rule.getIcmpspec() +
					"\nfAction " + rule.getFilterAction() +
					"\nflags " + rule.getFlags() +
					"\nflagset " + rule.getFlagset() +
					"\nrouteOpts " + rule.getRouteOpts() +
					"\nTagOpts " + rule.getTagOpt() +
					"\nTaggedOpts " + rule.getTaggedOpt();
			Log.debug().info(s);
		}

		return rule;
	}

	/*
	 * tabledef rule
	 */
	protected PfTable ruleTableDef(TableTemplate tableTpl) {
		PfTable table = new PfTable();

		/*
		 * context
		 */
		String text = _parseContext.getLine().trim();
		table.setConfigurationLine(_parseContext.getFileName() + " #" +
			_parseContext.getLineNumber() + ": ");
		table.setText(text);
		table.setParseContext(_parseContext);

		/*
		 * name
		 */
		String name = tableTpl.getName();
		table.setName(name);

		/*
		 * Translate filenames
		 */
		StringsList fileNames = new StringsList();
		for (String n: tableTpl.getFileNames()) {
			fileNames.add(translateFileName(n));
		}
		/*
		 * load each file
		 */
		PfIpSpec ipspec = new PfIpSpec();
		for (String n: fileNames) {
			ipspec.addAll(loadTable(n));
			table.getFileNames().add(n);
			famAdd(n);
		}
		/*
		 * use rule's ip
		 */
		if (ipspec.isEmpty()) {
			PfIpSpec ip = parseIpSpec(tableTpl.getHosts());
			ipspec.addAll(ip);
		}

		table.getIpspec().addAll(ipspec);

		if (Log.debug().isLoggable(Level.INFO)) {
			String s = "table: " + table.getName() + " " + table.getFileNames() +
				"\nip=" + table.getIpspec();
			Log.debug().info(s);
		}

		return table;
	}

	protected PfAnchorRule ruleAnchor(AnchorTemplate anchorTpl) {
		PfAnchorRule rule = new PfAnchorRule();

		/*
		 * context
		 */
		String text = _parseContext.getLine().trim();
		rule.setConfigurationLine(_parseContext.getFileName() + " #" +
			_parseContext.getLineNumber() + ": ");
		rule.setText(text);

		/*
		 * filtering rule associated
		 */
		PfRule pfRule = rulePfRule(anchorTpl.getRule(), true);
		rule.setPfRule(pfRule);
		_refRules.add(pfRule);

		/*
		 * inlined
		 */
		rule.setInlined(anchorTpl.isInlined());

		/*
		 * anchor name
		 */
		String name = anchorTpl.getName();

		/*
		 * use a fake name for anonymous anchor
		 */
		if (name == null) {
			if (rule.isInlined()) {
				name = "_inline_" + newAnchorUid();
			} else {
				throwCfgException("missing anchor name");
			}
		}
		/*
		 * check the name. PF allows inlined anchors named '*' or '..'
		 * but we don't.
		 */
		if (rule.isInlined()) {
			if (name.endsWith("*") || name.endsWith(".."))
				throwCfgException("invalid anchor name: " + name);
		}
		rule.setAnchorName(name);

		if (Log.debug().isLoggable(Level.INFO)) {
			String s = "anchor: " + rule.getAnchorName() +
				" inlined:" + rule.isInlined();
			Log.debug().info(s);
		}
		return rule;
	}

	protected void ruleLoadAnchor(String anchorName, String fileName) {

		PfAnchor anchor = _curAnchor.findOrCreateAnchor(anchorName);
		if (anchor == null)
			throwCfgException("cannot access anchor: " + anchorName);

		ConfigurationFile anchorConf = new ConfigurationFile();
		try {
			anchorConf.readFromFile(fileName);
		} catch (FileNotFoundException ex) {
			throwCfgException("anchor file not found: " + fileName);
		} catch (IOException ex) {
			throwCfgException("cannot read anchor file: " + fileName);
		}
		/*
		 * save the current anchor
		 */
		PfAnchor saveContext = _curAnchor;
		/*
		 * parse the anchor
		 */
		_curAnchor = anchor;
		parse(anchorConf);
		/*
		 * restore the context
		 */
		_curAnchor = saveContext;
	}

	protected void ruleSetSkip(RuleTemplate ruleTpl) {
		/*
		 * put the interfaces into the skipped interface map
		 */
		if (ruleTpl.getIfList() != null) {
			PfIfListSpec ifList = parseIfList(ruleTpl.getIfList());
			for (PfIfSpec ifspec: ifList) {
				if (!_skippedIfaces.containsKey(ifspec.getIfName()))
					_skippedIfaces.put(ifspec.getIfName(), ifspec.getIfName());
			}
		 }

		if (Log.debug().isLoggable(Level.INFO)) {
			Log.debug().info("skipped:" + _skippedIfaces);
		}
	}

	protected void ruleMacro(Map<String, String> macros, String name,
			String value) {

		macros.put(name, value);
		/*
		 * Check for IP addresses in the macro and add them in cross ref.
		 */
		String SPECIALS = "{},!=<>() \t\n";
		while (!value.isEmpty()) {
			int n = StringTools.IndexOfChars(value, SPECIALS);
			if (n == -1)
				n = value.length();
			String s = value.substring(0, n);
			if (n < value.length())
				value = value.substring(n + 1);
			else
				value = "";

			if (s.isEmpty())
				continue;

			if (s.contains(":")) {
				/*
				 * XXX: avoid port:port to be interpreted as IPV6
				 */
				String[] dot = s.split(":");
				if (dot.length <= 2) {
					if (! s.contains("::"))
						continue;
				}
			} else {
				if (!s.contains("."))
					continue;
			}
			// ip
			IPNet ipnet = null;
			try {
				ipnet = new IPNet(s);
			} catch (UnknownHostException ex) {
				// not an IP
			}
			if (ipnet != null) {
				CrossRefContext refContext = new CrossRefContext(
					_parseContext.getLine(),
					"macro", name, _parseContext.getFileName(),
					_parseContext.getLineNumber());
				IPNetCrossRef ipNetRef = getIPNetCrossRef(ipnet);
				ipNetRef.addContext(refContext);
			}
		}
	}

	protected void parse(ConfigurationFile cfg) {
		ParsingResult<?> result;

		Map<String, String> macros = new HashMap<String, String>();

		famAdd(cfg.getFileName());

		StringBuilder buffer = cfg.getBuffer();
		int lineNumber = 1;
		int nbLine;
		int inlinedAnchor = 0;

		while (buffer.length() > 0) {
			/*
			 * get the rule.
			 */
			_parseContext = new ParseContext();
			ExpandedRule exRule = null;
			try {
				exRule = _parser.getRule(buffer, macros);
			} catch (JtaclConfigurationException ex) {
				StringBuilder ctx = _parser.getCurExpandedContext();
				_parseContext.set(cfg.getFileName(), lineNumber, ctx.toString());
				throwCfgException(ex.getMessage());
			}
			String ctx = exRule.lineToString();
			_parseContext.set(cfg.getFileName(), lineNumber, ctx);

			/*
			 * parse the line
			 */
			String lineCfg = exRule.expandedToString();
			if (lineCfg.isEmpty())
				continue;

			if (Log.debug().isLoggable(Level.INFO))
				Log.debug().info("#" + _parseContext.getLineNumber() +
					": "+ lineCfg);

			result = _parseRunParse.run(lineCfg);
			if (result.matched) {
				String rule = _parser.getRuleName();

				/*
				 * some text may not have been parsed, keep it.
				 */
				String matchedText = _parser.getMatchedText();
				int n = 0;
				for (int i = 0; i < matchedText.length(); i++) {
					if (matchedText.charAt(i) == '\n')
						n++;
				}
				if (!matchedText.endsWith("\n"))
					n++;
				nbLine = exRule.expandedCountToLineCount(n);
				if (nbLine > 0) {
					while (exRule.size() > nbLine) {
						exRule.remove(exRule.size() - 1);
					}
				}
				lineCfg = exRule.lineToString();
				_parseContext.setLine(lineCfg);
				dumpConfiguration(lineCfg);

				if (Log.debug().isLoggable(Level.INFO)) {
					Log.debug().info("rule = " + rule);
					Log.debug().info("match = " + _parser.getMatchedText());
				}
				/*
				 * macros
				 */
				if (rule.equals("macro")) {
					ruleMacro(macros, _parser.getName(), _parser.getValue());
				}

				/*
				 * include
				 */
				if (rule.equals("include")) {
					//TODO:
				}

				/*
				 * anchor
				 */
				if (rule.equals("anchorrule")) {
					PfAnchorRule anchorRule = ruleAnchor(_parser.getPfAnchor());
					anchorRule.setOwnerAnchor(_curAnchor);
					_curAnchor.addRule(anchorRule);
					/*
					 * find or create the anchor
					 */
					String anchorName = anchorRule.getAnchorName();
					PfAnchor anchor = _curAnchor.findOrCreateAnchor(anchorName);
					if (anchor == null)
						throwCfgException("cannot access anchor: " +
							anchorName);
					/*
					 * if the anchor is an inlined one, change the context
					 * to the new anchor and store the reference into the rule.
					 */
					if (anchorRule.isInlined()) {
						anchorRule.setInlinedAnchor(anchor);
						_curAnchor = anchor;
						inlinedAnchor++;
					}
				}

				/*
				 * end of an anchor
				 */
				if (rule.equals("closing brace")) {
					if (inlinedAnchor <= 0) {
						warnConfig("invalid '}', not in anchor");
					} else {
						inlinedAnchor--;
						_curAnchor = _curAnchor.getParent();
						if (_curAnchor == null)
							throwCfgException("invalid anchor context");
					}
				}

				/*
				 * load anchor rule
				 */
				if (rule.equals("loadrule")) {
					String fileName = translateFileName(_parser.getValue());
					ruleLoadAnchor(_parser.getName(), fileName);
				}

				if (!hasOptParseOnly()) {
					/*
					 * set skip
					 */
					if (rule.equals("option set skip")) {
						ruleSetSkip(_parser.getPfRule());
					}
					/*
					 * pfrule
					 */
					if (rule.equals("pfrule")) {
						PfRule pfrule = rulePfRule(_parser.getPfRule(), false);
						_curAnchor.addRule(pfrule);
						pfrule.setOwnerAnchor(_curAnchor);
						_refRules.add(pfrule);
					}

					/*
					 * tabledef
					 */
					if (rule.equals("tabledef")) {
						PfTable pfTable = ruleTableDef(_parser.getPfTable());
						/*
						 * Check for dupplicate
						 */
						String name = pfTable.getName();
						PfTable table = _curAnchor.getTable(name);
						if (table != null)
							warnConfig("table <" + name + "> redefined");

						/*
						 * namespace conflict
						 */
						table = _rootAnchor.getTable(name);
						if (table != null)
							warnConfig("table name <" + name
								+ "> conflicts with root table");
						_curAnchor.addTable(pfTable);
						_refTables.add(pfTable);
					}
				}
			} else {
				/*
				 * use a generic rule to known the number of lines taken by
				 * the rule.
				 */
				result = _parseRunePfGenericRule.run(lineCfg);
				if (result.matched) {
					nbLine = 0;
					String matchedText = _parser.getMatchedText();
					for (int i = 0; i < matchedText.length(); i++) {
						if (matchedText.charAt(i) == '\n')
							nbLine++;
					}
					if (!matchedText.endsWith("\n"))
						nbLine++;
				} else {
					nbLine = 1;
				}
				/*
				 * check if we should match a rule.
				 */
				if (_parser.shouldMatch(lineCfg)) {
					Log.config().warning(_parseContext.toString() +
						"does not match any rule (but should).\n" + lineCfg);
				}
			}

			/*
			 * adjust the rule buffer.
			 */
			lineNumber += nbLine;
			int j = 0;
			for (int n = nbLine; n > 0 && j < buffer.length(); j++) {
				if (buffer.charAt(j) == '\n') {
					n --;
				}
			}
			if (j > 0)
				buffer.delete(0, j);
		} // while

		/*
		 * sanity check
		 */
		if (inlinedAnchor > 0) {
			warnConfig("inlined anchor may not be closed");
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
		loadIfaces(doc);
		loadConfiguration(doc);
		_curAnchor = _rootAnchor;
		parse(_pfConf);
		loadAnchors();
		loadTables();
		routeDirectlyConnectedNetworks();
		if (_routesFile != null) {
			loadRoutesFromFile(_routesFile);
			famAdd(_routesFile);
		}
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
	 * Cross reference for ipspec
	 */
	protected void crossRefIpSpec(PfAnchor anchor, PfIpSpec ipspec, CrossRefContext refContext) {
		for (PfNodeHost nodeHost: ipspec) {
			if (nodeHost.isAddrMask()) {
				for (IPNet ip: nodeHost.getAddr()) {
					IPNetCrossRef ipNetRef = getIPNetCrossRef(ip);
					ipNetRef.addContext(refContext);
				}
			}

			if (nodeHost.isAddrRange()) {
				for (IPNet ip: nodeHost.getAddr()) {
					IPNetCrossRef ipNetRef = getIPNetCrossRef(ip);
					ipNetRef.addContext(refContext);
				}
				for (IPNet ip: nodeHost.geRangeAddr()) {
					IPNetCrossRef ipNetRef = getIPNetCrossRef(ip);
					ipNetRef.addContext(refContext);
				}
			}

			if (nodeHost.isAddrTable()) {
				PfTable table = anchor.findTable(nodeHost.getTblName());
				if (table != null)
					crossRefIpSpec(anchor, table.getIpspec(), refContext);
			}
		}
	}

	/*
	 * Cross reference for a rule
	 */
	protected void crossRefRule(PfRule rule) {
		ParseContext context = rule.getParseContext();
		CrossRefContext refContext = new CrossRefContext(context.getLine(),
				"rule",
				rule.getAction(), context.getFileName(), context.getLineNumber());

		if (rule.getFromIpSpec() != null)
			crossRefIpSpec(rule.getOwnerAnchor(), rule.getFromIpSpec(), refContext);

		if (rule.getToIpSpec() != null)
			crossRefIpSpec(rule.getOwnerAnchor(), rule.getToIpSpec(), refContext);

		PfRouteOpts ropts = rule.getRouteOpts();
		if (ropts != null && ropts.isRouteTo()) {
			IPNetCrossRef ipNetRef = getIPNetCrossRef(ropts.getNextHop());
			ipNetRef.addContext(refContext);
		}

	}

	/**
	 * Compute IPNet cross references
	 */
	protected void ipNetCrossReference() {
		/*
		 * tables
		 */
		for (PfTable table: _refTables) {
			CrossRefContext refContext = new CrossRefContext(
				table.getParseContext().getLine(),
				"table", "<" + table.getName() + ">",
				table.getParseContext().getFileName(),
				table.getParseContext().getLineNumber());
			crossRefIpSpec(table.getOwnerAnchor(), table.getIpspec(), refContext);
		}

		/*
		 * rules
		 */
		for (PfRule rule: _refRules) {
			crossRefRule(rule);
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
		 * PF probe extension
		 */
		PfProbeExtension pext = new PfProbeExtension();
		probe.setExtension(pext);

		/*
		 * reset route-to Engine
		 */
		_routeToEngine = null;

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
		Routes routes;
		if (_routeToEngine != null)
			routes = _routeToEngine.getRoutes(probe);
		else
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

	/**
	 * family filter
 	 */
	protected boolean familyFilter(IPNet ipAddress, AddressFamily af) {
		return af == AddressFamily.NONE ||
				(af == AddressFamily.INET && ipAddress.isIPv4()) ||
				(af == AddressFamily.INET6 && ipAddress.isIPv6());
	}

	/**
	 * host filter
	 */
	protected MatchResult hostFilter(FilterContext context, PfNodeHost host,
			IPNet ipAddress, AddressFamily af)
		throws UnknownHostException {

		/*
		 * any
		 */
		if (host.isAddrAny()) {
			/*
			 * check family
			 */
			if (familyFilter(ipAddress, af))
				return MatchResult.ALL;
			return MatchResult.NOT;
		}

		/*
		 * no-route
		 */
		if (host.isAddrNoRoute()) {
			MatchResult res = MatchResult.NOT;
			Routes routes = _routingEngine.getRoutes(ipAddress);
			if (routes.isEmpty())
				res = MatchResult.ALL;
			if (host.isNot())
				res = res.not();
			return res;
		}

		/*
		 * urpf-failed
		 */
		if (host.isAddrUrpfFailed()) {
			MatchResult res = MatchResult.ALL;
			Routes routes = _routingEngine.getRoutes(ipAddress);
			/*
			 * check the routes
			 */
			for (Route route: routes) {
				if (route.getLink() == context.getLink()) {
					res = MatchResult.NOT;
					break;
				}
			}
			if (host.isNot())
				res = res.not();
			return res;
		}

		/*
		 * route label
		 * XXX: not supported.
		 */
		if (host.isAddrRtLabel()) {
			return MatchResult.UNKNOWN;
		}

		/*
		 * table
		 */
		if (host.isAddrTable()) {
			String name = host.getTblName();
			PfTable table = context.getAnchor().findTable(name);
			/*
			 * table not found.
			 */
			if (table == null) {
				return host.isNot() ? MatchResult.ALL : MatchResult.NOT;
			}
			/*
			 * get the result for the table
			 */
			MatchResult res = ipspecFilter(context, table.getIpspec(), ipAddress, af);
			if (host.isNot())
				res = res.not();
			return res;
		}

		/*
		 * addr mask
		 */
 		if (host.isAddrMask()) {
			int mAll = 0;
			int mMatch = 0;

			for (IPNet ip: host.getAddr()) {
				if (!ip.sameIPVersion(ipAddress))
					continue;

				if (ip.networkContains(ipAddress)) {
					mAll++;
				} else {
					if (ip.overlaps(ipAddress))
						mMatch++;
				}
			}
			MatchResult res = MatchResult.NOT;
			if (mAll > 0)
				res = MatchResult.ALL;
			if (mMatch > 0)
				res = MatchResult.MATCH;
			if (host.isNot())
				res = res.not();
			return res;
		}

		/*
		 * addr range
		 */
		if (host.isAddrRange()) {
			if (host.getAddr().size() != 1 || host.geRangeAddr().size() != 1)
				return MatchResult.UNKNOWN;

			IPNet ipfirst = host.getAddr().get(0);
			IPNet ipLast = host.geRangeAddr().get(0);

			if (!ipfirst.sameIPVersion(ipAddress))
				return MatchResult.NOT;

			int mAll = 0;
			int mMatch = 0;

			if (ipAddress.isHost()) {
				if (ipAddress.isBetweenIP(ipfirst, ipLast))
					mAll++;
			} else {
				IPNet ipAddressFirst = ipAddress.networkAddress();
				IPNet ipAddressLast = ipAddress.lastNetworkAddress();
				if (ipAddressFirst.isBetweenIP(ipfirst, ipLast)) {
					if (ipAddressLast.isBetweenIP(ipfirst, ipLast))
						mAll++;
					else
						mMatch++;
				} else {
					if (ipAddressLast.isBetweenIP(ipfirst, ipLast))
						mMatch++;
				}
			}

			MatchResult res = MatchResult.NOT;
			if (mAll > 0)
				res = MatchResult.ALL;
			if (mMatch > 0)
				res = MatchResult.MATCH;
			if (host.isNot())
				res = res.not();
			return res;
		}

		return MatchResult.UNKNOWN;
	}

	/**
	 * port filter
	 */
	protected MatchResult portspecFilter(PfPortSpec portspec, PortSpec portRequest) {

		if (portspec.isEmpty())
			return MatchResult.ALL;
		return portspec.matches(portRequest);
	}

	/**
	 * ippsec filter
	 */
	protected MatchResult ipspecFilter(FilterContext context, PfIpSpec ipspec,
			IPNet ipAddress, AddressFamily af) {

		if (ipspec.isEmpty())
			return MatchResult.ALL;

		int mAll = 0;
		int mNot = 0;
		int mMatch = 0;
		int mUnknown = 0;

		for (PfNodeHost host: ipspec) {
			MatchResult match = MatchResult.NOT;
			try {
				match = hostFilter(context, host, ipAddress, af);
			} catch (UnknownHostException ex) {
				// should not happen
				throw new JtaclInternalException(("unexpected exception: " +
					ex.getMessage()));
			}
			switch (match) {
				case ALL:
						mAll++;
						break;
				case MATCH:
						mMatch++;
						break;
				case NOT:
						mNot++;
						break;
				case UNKNOWN:
						mUnknown++;
						break;
			}
		}
		if (mAll > 0)
			return MatchResult.ALL;
		if (mMatch > 0)
			return MatchResult.MATCH;
		if (mUnknown > 0)
			return MatchResult.UNKNOWN;
		return MatchResult.NOT;
	}

	/**
	 * icmp filter
	 */
	protected MatchResult icmpFilter(PfIcmpSpec icmpSpec, IPIcmpEnt ent,
			AddressFamily af) {

		if (icmpSpec.isEmpty())
			return MatchResult.ALL;
		if (icmpSpec.getAf() != af)
			return MatchResult.NOT;
		boolean match = icmpSpec.matches(ent);
		return match ? MatchResult.ALL : MatchResult.NOT;
	}

	/**
	 * tcp flags filter
	 */
	protected boolean tcpFlagsFilter(ProbeTcpFlags reqFlags, PfRule rule) {

		TcpFlags flags = rule.getFlags();
		TcpFlags flagset = rule.getFlagset();

		if (flagset == null) {
			return reqFlags.matchAll(flags);
		} else {
			return reqFlags.matchAllWithout(flags, flagset);
		}
	}

	/**
	 * pfrule fiter
	 */
	protected MatchResult ruleFilter(FilterContext context, PfRule rule,
			Probe probe) {

		ProbeRequest request = probe.getRequest();

		/*
		 * direction
		 */
		if (rule.getDirection() != Direction.INOUT) {
			if (context.getDirection() != rule.getDirection())
				return MatchResult.NOT;
		}

		/*
		 * interfaces
		 */
		String ifname = context.getLink().getIfaceName();
		if (!rule.getIfList().isEmpty() && !rule.getIfList().matches(ifname)) {
			return MatchResult.NOT;
		}

		/*
		 * protocols
		 */
		List<Integer> reqProto = request.getProtocols();
		List<Integer> ruleProto = rule.getProtocols();

		if (reqProto != null && !ruleProto.isEmpty()) {
			boolean match = false;
			for (Integer proto: reqProto) {
				if (ruleProto.contains(proto)) {
					match = true;
					break;
				}
			}
			if (!match)
				return MatchResult.NOT;
		}

		/*
		 * Address family
		 */
		AddressFamily af = rule.getAf();
		if (af == AddressFamily.INET && !probe.isIPv4())
			return MatchResult.NOT;
		if (af == AddressFamily.INET6 && !probe.isIPv6())
			return MatchResult.NOT;

		MatchResult mIpSource = MatchResult.ALL;
		MatchResult mIpDest = MatchResult.ALL;
		MatchResult mSourcePort = MatchResult.ALL;
		MatchResult mDestPort = MatchResult.ALL;

		if (!rule.isAll()) {
			/*
			 * from ipspec
			 */
			PfIpSpec ipspec = rule.getFromIpSpec();
			mIpSource = ipspecFilter(context, ipspec,
				probe.getSourceAddress(), af);
			if (mIpSource == MatchResult.NOT)
				return MatchResult.NOT;

			/*
			 * from port spec
			 */
			PortSpec port = request.getSourcePort();
			if (port != null) {
				mSourcePort = portspecFilter(rule.getFromPortSpec(), port);
				if (mSourcePort == MatchResult.NOT)
					return MatchResult.NOT;
			}

			/*
			 * to ipspec
			 */
			ipspec = rule.getToIpSpec();
			mIpDest = ipspecFilter(context, ipspec,
				probe.getDestinationAddress(), af);
			if (mIpDest == MatchResult.NOT)
				return MatchResult.NOT;

			/*
			 * to port spec
			 */
			port = request.getDestinationPort();
			if (port != null) {
				mDestPort = portspecFilter(rule.getToPortSpec(), port);
				if (mDestPort == MatchResult.NOT)
					return MatchResult.NOT;
			}
		}

		/*
		 * icmp spec
		 */
		Integer icmpType = request.getSubType();
		Integer icmpCode = request.getCode();
		if (reqProto != null && icmpType != null && icmpCode != null) {
			IPIcmpEnt ent = new IPIcmpEnt("", icmpType, icmpCode);
			AddressFamily icmpAf = AddressFamily.NONE;
			if (reqProto.contains(Protocols.ICMP))
				icmpAf = AddressFamily.INET;
			if (reqProto.contains(Protocols.ICMP6))
				icmpAf = AddressFamily.INET6;
			if (icmpAf != AddressFamily.NONE) {
				MatchResult match = icmpFilter(rule.getIcmpspec(), ent, icmpAf);
				if (match == MatchResult.NOT)
					return MatchResult.NOT;
			}
		}

		/*
		 * check tcp flags
		 */
		ProbeTcpFlags pflags = request.getTcpFlags();
		if (pflags != null && rule.getFlags() != null) {
			if (!tcpFlagsFilter(pflags, rule))
				return MatchResult.NOT;
		}

		/*
		 * check tagged
		 */
		PfTaggedOpt taggedOpt = rule.getTaggedOpt();
		if (taggedOpt != null) {
			String tagged = taggedOpt.getTag();
			PfProbeExtension pext = (PfProbeExtension) probe.getExtension();
			String currentTag = pext.getTag();
			if (currentTag == null && !taggedOpt.isNot())
				return MatchResult.NOT;
			if (!currentTag.equals(tagged) && !taggedOpt.isNot())
				return MatchResult.NOT;
			if (currentTag.equals(tagged) && taggedOpt.isNot())
				return MatchResult.NOT;
		}

		MatchResult mResult = MatchResult.MATCH;

		if (mIpSource == MatchResult.ALL && mIpDest == MatchResult.ALL &&
				mSourcePort == MatchResult.ALL && mDestPort == MatchResult.ALL)
			mResult = MatchResult.ALL;

		if (mResult == MatchResult.MATCH)
			return MatchResult.MATCH;

		if (_filteringDone)
			return mResult;

		/*
		 * actions
		 */

		/*
		 * tag
		 */
		PfTagOpt tagOpt = rule.getTagOpt();
		if (tagOpt != null && !request.getProbeOptions().hasNoAction()) {
			PfProbeExtension pext = (PfProbeExtension) probe.getExtension();
			pext.setTag(tagOpt.getTag());
		}

		/*
		 * route-to
		 */
		PfRouteOpts routeOpts = rule.getRouteOpts();
		if (routeOpts == null || !routeOpts.isRouteTo() ||
				request.getProbeOptions().hasNoAction())
			return mResult;

		Route<IfaceLink> route = new Route(probe.getDestinationAddress(),
				routeOpts.getNextHop(), 0, routeOpts.getLink());
		_routeToEngine = new RoutingEngine();
		_routeToEngine.addRoute(route);

		return mResult;
	}

	protected class RuleResult {
		protected PfGenericRule _rule;
		protected MatchResult _match;
		protected String _action;
		protected String _text;

		protected RuleResult(PfGenericRule rule, MatchResult match,
				String action, String text) {
			_rule = rule;
			_match = match;
			_action = action;
			_text = text;
		}

		protected MatchResult getMatch() {
			return _match;
		}

		protected void setMatch(MatchResult match) {
			_match = match;
		}

		protected PfGenericRule getRule() {
			return _rule;
		}

		protected void setRule(PfGenericRule rule) {
			_rule = rule;
		}

		public String getAction() {
			return _action;
		}

		public void setAction(String action) {
			_action = action;
		}

		public String getText() {
			return _text;
		}

		public void setText(String text) {
			_text = text;
		}

		protected RuleResult newInstance() {
			return new RuleResult(_rule, _match, _action, _text);
		}
	}

	protected class AnchorResult {
		protected List<RuleResult> _ruleResults = new ArrayList<RuleResult>();
		protected boolean _quickRule;
		protected RuleResult _lastResult;

		protected RuleResult getLastResult() {
			return _lastResult;
		}

		protected void setLastResult(RuleResult lastRuleResult) {
			if (_quickRule)
				return;
			_lastResult = lastRuleResult.newInstance();
		}

		protected boolean hasQuickRule() {
			return _quickRule;
		}

		protected void setQuickRule(boolean quickRule) {
			_quickRule = quickRule;
		}

		protected List<RuleResult> getRuleResults() {
			return _ruleResults;
		}

		protected RuleResult addRuleResult(PfGenericRule rule, MatchResult match,
				String action, String anchorPath) {
			String anchorText = "";
			if (!anchorPath.isEmpty())
				anchorText = "[" + anchorPath +"] ";
			String text = rule.getConfigurationLine() + anchorText + rule.getText();
			RuleResult result = new RuleResult(rule, match, action, text);
			_ruleResults.add(result);
			return result;
		}
	}

	protected AnchorResult anchorFilter(FilterContext context, Probe probe) {

		PfAnchor anchor = context.getAnchor();
		AnchorResult anchorResult = new AnchorResult();
		String anchorPath = anchor.getPath();

		/*
		 * each rule
		 */
		for (PfGenericRule rule: anchor.getRules()) {

			/*
			 * anchor rule
			 */
			if (rule instanceof PfAnchorRule) {
				PfAnchorRule anchorRule = (PfAnchorRule) rule;
				MatchResult match = ruleFilter(context, anchorRule.getPfRule(), probe);
				if (match == MatchResult.NOT)
					continue;
				RuleResult ruleResult = anchorResult.addRuleResult(rule, match,
						PfRule.PASS, anchorPath);
				/*
				 * inlined anchor: get the anchor from the rule.
				 * Else, search them by the anchor name.
				 */
				List<PfAnchor> anchorList;
				if (anchorRule.isInlined()) {
					anchorList = new ArrayList<PfAnchor>();
					anchorList.add(anchorRule.getInlinedAnchor());
				} else {
					anchorList = anchor.findAnchors(anchorRule.getAnchorName());
				}
				if (anchorList == null)
					continue;

				/*
				 * save the current context and evaluate each anchor.
				 */
				FilterContext save = context;
				context = context.newInstance();
				RuleResult lastResult;
				int matchCount = 0;
				for (PfAnchor anchorItem : anchorList) {
					context.setAnchor(anchorItem);
					AnchorResult anchorItemResult = anchorFilter(context, probe);
					/*
					 * copy the rules result from the anchor
					 */
					List<RuleResult> itemResults = new ArrayList<RuleResult>();
					itemResults.addAll(anchorItemResult.getRuleResults());
					lastResult = anchorItemResult.getLastResult();
					/*
					 * If the current anchor matching result is not "all",
					 * mark the results of the sub-anchor according to.
					 */
					if (match == MatchResult.MATCH || match == MatchResult.UNKNOWN) {
						for (RuleResult resultItem: itemResults) {
							if (resultItem.getMatch() != MatchResult.UNKNOWN)
								resultItem.setMatch(match);
						}
						if (lastResult != null &&
								lastResult.getMatch() != MatchResult.UNKNOWN)
							lastResult.setMatch(match);
					}
					/*
					 * update the current anchor results
					 */
					anchorResult.getRuleResults().addAll(itemResults);
					if (lastResult != null) {
						anchorResult.setLastResult(lastResult);
						ruleResult.setAction(lastResult.getAction());
						matchCount++;
					}
					if (anchorItemResult.hasQuickRule())
						anchorResult.setQuickRule(true);
				}
				/*
				 * quick anchor
				 */
				if (anchorRule.getPfRule().isQuick() && matchCount > 0)
					anchorResult.setQuickRule(true);
				/*
				 * restore the context
				 */
				context = save;
			}

			/*
			 * pf rule
			 */
			if (rule instanceof PfRule) {
				PfRule pfrule = (PfRule) rule;
				MatchResult match = ruleFilter(context, pfrule, probe);
				if (match != MatchResult.NOT) {
					RuleResult result = anchorResult.addRuleResult(rule, match,
						pfrule.getAction(), anchorPath);
					anchorResult.setLastResult(result);
					if (pfrule.isQuick()) {
						anchorResult.setQuickRule(true);
						_filteringDone = true;
					}
				}
			}
		}

		return anchorResult;
	}

	protected void packetFilter (IfaceLink link, Direction direction, Probe probe) {

		String interfaceDesc = link.getIfaceName() + " (" +
			link.getIface().getComment() + ")";
		ProbeResults probeResults = probe.getResults();

		_filteringDone = false;
		/*
		 * skipped interface
		 */
		if (_skippedIfaces.containsKey(link.getIfaceName())) {
			probeResults.setAclResult(direction, new AclResult(AclResult.ACCEPT));
			probeResults.setInterface(direction, interfaceDesc + " SKIPPED");
			return;
		}

		/*
		 * filter context
		 */
		FilterContext context = new FilterContext();
		context.setDirection(direction);
		context.setLink(link);
		context.setAnchor(_rootAnchor);
		AnchorResult anchorResult = anchorFilter(context, probe);

		/*
		 * rule results
		 */
		for (RuleResult ruleResult: anchorResult.getRuleResults()) {
			AclResult aclResult = new AclResult();
			if (ruleResult.getAction().equals(PfRule.BLOCK))
				aclResult.addResult(AclResult.DENY);
			else
				aclResult.addResult(AclResult.ACCEPT);
			if (ruleResult.getMatch() != MatchResult.ALL)
				aclResult.addResult(AclResult.MAY);
			probeResults.addMatchingAcl(direction,
				ruleResult.getText(),
				aclResult);
		}

		/*
		 * global result
		 */
		RuleResult lastResult = anchorResult.getLastResult();
		if (lastResult != null) {
			AclResult aclResult = new AclResult();
			if (lastResult.getAction().equals(PfRule.BLOCK))
				aclResult.addResult(AclResult.DENY);
			else
				aclResult.addResult(AclResult.ACCEPT);
			if (lastResult.getMatch() != MatchResult.ALL)
				aclResult.addResult(AclResult.MAY);
			probeResults.addActiveAcl(direction, lastResult.getText(), aclResult);
			probeResults.setAclResult(direction, aclResult);
		} else {
			probeResults.setAclResult(direction, new AclResult(AclResult.ACCEPT));
		}

		probeResults.setInterface(direction, interfaceDesc);

	}

	@Override
	public void runShell(String command, PrintStream output) {
		_shell.shellCommand(command, output);
	}

}
