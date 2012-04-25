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

package fr.univrennes1.cri.jtacl.shell;

import fr.univrennes1.cri.jtacl.App;
import fr.univrennes1.cri.jtacl.core.exceptions.JtaclConfigurationException;
import fr.univrennes1.cri.jtacl.core.exceptions.JtaclInternalException;
import fr.univrennes1.cri.jtacl.core.exceptions.JtaclRuntimeException;
import fr.univrennes1.cri.jtacl.core.probing.AclResult;
import fr.univrennes1.cri.jtacl.core.monitor.Log;
import fr.univrennes1.cri.jtacl.core.monitor.Monitor;
import fr.univrennes1.cri.jtacl.core.probing.ProbeRequest;
import fr.univrennes1.cri.jtacl.core.probing.ProbeTcpFlags;
import fr.univrennes1.cri.jtacl.core.probing.ProbesTracker;
import fr.univrennes1.cri.jtacl.core.probing.Probing;
import fr.univrennes1.cri.jtacl.core.probing.RoutingResult;
import fr.univrennes1.cri.jtacl.core.network.Iface;
import fr.univrennes1.cri.jtacl.core.network.IfaceLink;
import fr.univrennes1.cri.jtacl.core.network.IfaceLinks;
import fr.univrennes1.cri.jtacl.core.network.NetworkEquipment;
import fr.univrennes1.cri.jtacl.core.network.NetworkEquipmentsByName;
import fr.univrennes1.cri.jtacl.core.probing.ProbeOptions;
import fr.univrennes1.cri.jtacl.core.topology.NetworkLink;
import fr.univrennes1.cri.jtacl.core.topology.NetworkLinks;
import fr.univrennes1.cri.jtacl.core.topology.Topology;
import fr.univrennes1.cri.jtacl.lib.ip.IPIcmp;
import fr.univrennes1.cri.jtacl.lib.ip.IPIcmp4;
import fr.univrennes1.cri.jtacl.lib.ip.IPIcmp6;
import fr.univrennes1.cri.jtacl.lib.ip.IPIcmpEnt;
import fr.univrennes1.cri.jtacl.lib.ip.IPNet;
import fr.univrennes1.cri.jtacl.lib.ip.IPProtocols;
import fr.univrennes1.cri.jtacl.lib.ip.IPServices;
import fr.univrennes1.cri.jtacl.lib.ip.IPversion;
import fr.univrennes1.cri.jtacl.lib.ip.PortOperator;
import fr.univrennes1.cri.jtacl.lib.ip.PortSpec;
import fr.univrennes1.cri.jtacl.lib.ip.TcpFlags;
import fr.univrennes1.cri.jtacl.lib.misc.StringsList;
import groovy.lang.Binding;
import groovy.ui.Console;
import groovy.util.GroovyScriptEngine;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import org.parboiled.Parboiled;
import org.parboiled.buffers.InputBuffer;
import org.parboiled.errors.ParseError;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

/**
 * lsfw shell
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class Shell {

	protected String _prompt = "lsfw> ";
	protected ShellParser _parser = Parboiled.createParser(ShellParser.class);
	protected ReportingParseRunner _parseRunner =
		new ReportingParseRunner(_parser.CommandLine());
	protected Monitor _monitor = Monitor.getInstance();;
	protected boolean _interactive;
	protected Probing _lastProbing;
	protected boolean _testResult;
	protected PrintStream _outStream = System.out;

	static protected final List<String> _specialPorts = Arrays.asList(
		"none", "any", "known", "reg", "dyn");

	static protected final List<String> _expectStrings = Arrays.asList(
		"ROUTED", "NONE-ROUTED", "UNKNOWN", "ACCEPT", "DENY", "MAY",
		"UNACCEPTED");

	/**
	 * Returns all the {@link IfaceLink} links matching an 'equipment specification'
	 * string.
	 *
	 * The format of the 'equipment specification' string is:
	 * equipment-name'|'[iface-name|IPaddress]
	 *
	 * @param EquipmentSpecification Equipment specification used to filter.
	 * @return a {@link IfaceLinks} list containing the links.
	 */
	protected IfaceLinks getIfaceLinksByEquipmentSpec(IPNet sourceIP,
			String EquipmentSpecification) {

		IfaceLinks resLinks = new IfaceLinks();

		String [] specSplit = EquipmentSpecification.split("\\|");
		String equipmentName = specSplit[0];
		NetworkEquipment equipment = _monitor.getEquipments().get(equipmentName);
		if (equipment == null) {
			_outStream.println("No such equipment: " + equipmentName);
			return null;
		}
		IPNet ipaddress = null;
		Iface iface = null;
		String ifaceName = null;

		if (specSplit.length ==2) {
			try {
				/*
				 * we can use either an IP address or the name of an interface
				 * try with IP address first.
				 */
				ipaddress = new IPNet(specSplit[1]);
			} catch (UnknownHostException ex) {
				//do nothing (not an IP address)
			}
			if (ipaddress == null) {
				/*
				 * try with an interface
				 */
				ifaceName = specSplit[1];
				iface = equipment.getIface(ifaceName);
				if (iface == null) {
					_outStream.println("No such interface: " + ifaceName);
					return null;
				}
			}
		}
		/*
		 * filter the iface links
		 */
		IfaceLinks links = equipment.getIfaceLinks();
		for (IfaceLink link: links) {
			/*
			 * by interface name
			 */
			if (iface != null) {
				if (link.getIfaceName().equals(ifaceName)) {
					/*
					 * pick up the first link
					 */
					resLinks.add(link);
					break;
				}
				continue;
			}
			/*
			 * by IP address
			 */
			if (ipaddress  != null) {
				if (link.getIp().equals(ipaddress)) {
					resLinks.add(link);
				}
				continue;
			}
			if (sourceIP != null) {
				/*
				 * by source IP address
				 */
				try {
					if (link.getIp().equals(sourceIP) ||
							link.getNetwork().networkContains(sourceIP))
						resLinks.add(link);

					} catch (UnknownHostException ex) {
						// should not happen, just in case.
						throw new JtaclInternalException("Invalid network in " +
							link.getNetwork().toString());
					}
				continue;
			}
		}
		return resLinks;
	}

	protected boolean checkTcpFlags(String flags) {

		for (int i = 0; i < flags.length(); i++) {
			if (!TcpFlags.isFlag(flags.charAt(i)))
				return false;
		}
		return true;
	}

	public Shell() {
		_interactive = false;
	}

	public Shell(boolean interactive) {
		_interactive = interactive;
	}

	protected String substitute(String line) {

		String r = line;
		for (String s: _monitor.getDefines().keySet()) {
			r = r.replace("$" + s, _monitor.getDefines().get(s));
		}
		return r;
	}

	protected Integer parseService(String service, String protocol) {
		IPServices ipServices = IPServices.getInstance();

		Integer	port = ipServices.serviceLookup(service, protocol);
		if (port.intValue() == -1) {
			_outStream.println("unknown service: " + service);
			return null;
		}
		return port;
	}

	protected PortSpec parsePortSpec(String sportSpec, String sprotocol) {

		/*
		 * predefined intervals
		 */
		sportSpec = sportSpec.toLowerCase();
		if (sportSpec.equals("none"))
			return PortSpec.NONE;

		if (sportSpec.equals("any"))
			return PortSpec.ANY;

		if (sportSpec.equals("reg"))
			return PortSpec.REGISTERED;

		if (sportSpec.equals("dyn"))
			return PortSpec.DYNAMIC;

		if (sportSpec.equals("known"))
			return PortSpec.WELLKNOWN;

		/*
		 * interval
		 */
		if (sportSpec.startsWith("(") && sportSpec.endsWith(")")) {
			String sports = sportSpec.substring(1, sportSpec.length() - 1);
			String [] ports = sports.split(",");
			if (ports.length != 2) {
				_outStream.println("invalid services range: " + sportSpec);
				return null;
			}
			Integer portFirst = parseService(ports[0], sprotocol);
			if (portFirst == null)
				return null;
			Integer portLast = parseService(ports[1], sprotocol);
			if (portLast == null)
				return null;
			PortSpec spec = new PortSpec(PortOperator.RANGE, portFirst, portLast);
			return spec;
		}

		/*
		 * service
		 */
		Integer port = parseService(sportSpec, sprotocol);
		if (port == null)
			return null;

		PortSpec spec = new PortSpec(PortOperator.EQ, port);
		return spec;
	}

	protected void autoReload() {

		for (String eqName: _monitor.getEquipments().keySet()) {

			NetworkEquipment equipment =
					_monitor.getEquipments().get(eqName);
			if (equipment.hasChanged()) {
				Log.debug().info("reloading " + eqName);
				try {
					_monitor.reloadEquipment(equipment);
				} catch (JtaclRuntimeException ex) {
					System.err.println("Error: " + ex.getMessage());
				}
			}
		}
	}

	protected LsfwBinding newBinding(String args) {
		return new LsfwBinding(args);
	}

	public void helpCommand(ShellParser command) {
		try {
			String topic = command.getHelpTopic();
			if (topic == null) {
				topic = "help";
			}
			InputStream stream = null;
			do {
				stream = this.getClass().getResourceAsStream("/help/" + topic);
				if (stream == null) {
					topic = "help";
				}
			} while (stream == null);
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			for (;;) {
				String line = reader.readLine();
				if (line == null) {
					break;
				}
				_outStream.println(line);
			}
			reader.close();
		} catch (IOException ex) {
			_outStream.println("cannot print help");
		}

	}

	public void optionCommand(ShellParser command) {
	String name = command.getSetValueName();
	String value = command.getSetValueValue();

		if (name != null && value != null)
			try {
				_monitor.getOptions().setOption(name, value);
			} catch (JtaclConfigurationException e) {
				_outStream.println("Error: " + e.getMessage());
			}
		else
			_outStream.println(_monitor.getOptions().getOptionsList());
	}

	public void defineCommand(ShellParser command) {
		String name = command.getSetValueName();
		String value = command.getSetValueValue();

		if (name != null) {
			String define = _monitor.getDefines().get(name);
			if (define != null)
				_monitor.getDefines().remove(name);
			if (value != null) {
				_monitor.getDefines().put(name, value);
			}
		} else {
			for (String d: _monitor.getDefines().keySet()) {
				_outStream.println(d + "=" + _monitor.getDefines().get(d));
			}
		}

	}

	public void topologyCommand(ShellParser command) {

		String equipmentName = command.getEquipments();
		String option = command.getTopologyOption();

		/*
		 * Try to parse the value as an IP. If ok, display network links
		 * containing this IP. Overwise this is an equipment.
		 */
		IPNet ip = null;
		NetworkEquipment equipment = null;

		if (equipmentName != null) {
			try {
				ip = new IPNet(equipmentName);
			} catch (UnknownHostException ex) {
				// do nothing
			}
			equipment = null;
			if (ip == null) {
				equipment = _monitor.getEquipments().get(equipmentName);
				if (equipment == null) {
					_outStream.println("No such equipment: " + equipmentName);
					return;
				}
			}
		}
		/*
		 * filter out each link by IP or equipment.
		 */
		NetworkLinks links = _monitor.getTopology().getNetworkLinks();
		Collections.sort(links);

		for (NetworkLink link: links) {
			NetworkLink candidate = null;
			/*
			 * IP
			 */
			if (ip != null) {
				try {
					if (link.getNetwork().overlaps(ip)) {
						candidate = link;
					}
				} catch (UnknownHostException ex) {
					_outStream.println("Error " + ex.getMessage());
					return;
				}
			} else {
				/*
				 * Equipment
				 */
				if (equipmentName != null) {
					if (link.isConnectedTo(equipment))
						candidate = link;
				}
				else {
					candidate = link;
				}
			}
			/*
			 * filter out by connected or !connected.
			 * a NetworkLink is 'connected' if it is associated to a least two
			 * IfaceLink.
			 */
			if (candidate != null && option != null) {
				if (option.equalsIgnoreCase("connected")) {
					if (!candidate.isInterconnection())
						candidate = null;
				}
				if (option.equalsIgnoreCase("!connected")) {
					if (candidate.isInterconnection())
						candidate = null;
				}
			}
			/*
			 * output
			 */
			if (candidate != null)
				_outStream.println(candidate.toString());

		}
	}

	public void routeCommand(ShellParser command) {

		String equipmentName = command.getEquipments();
		if (equipmentName != null) {
			if (!_monitor.getEquipments().containsKey(equipmentName)) {
				_outStream.println("No such equipment: " + equipmentName);
				return;
			}
		}
		NetworkEquipmentsByName equipments = _monitor.getEquipments();
		for (NetworkEquipment equipment: equipments.values()) {
			if (equipmentName== null || equipment.getName().equals(equipmentName)) {
				_outStream.println("Routes on " + equipment.getName());
				_outStream.println("-------------------");
				_outStream.println(equipment.getShowableRoutes().showRoutes());
				_outStream.println("-------------------");
				_outStream.println();
			}
		}
	}

	public void equipmentCommand(ShellParser command) {

		String equipmentName = command.getEquipments();
		NetworkEquipment equipment = _monitor.getEquipments().get(equipmentName);
		if (equipment == null) {
			_outStream.println("No such equipment: " + equipmentName);
			return;
		}

		equipment.runShell(command.getSubCommand(), _outStream);
	}

	public void reloadCommand(ShellParser command) {

		String equipmentName = command.getEquipments();

		if (equipmentName == null) {
			_monitor.reload();
			return;
		}

		NetworkEquipment equipment = _monitor.getEquipments().get(equipmentName);
		if (equipment == null) {
			_outStream.println("No such equipment: " + equipmentName);
			return;
		}

		try {
			_monitor.reloadEquipment(equipment);
		} catch (JtaclRuntimeException ex) {
			System.err.println("Error: " + ex.getMessage());
		}
	}

	public boolean probeCommand(String commandLine, ShellParser command) {

		boolean testMode = command.getProbeExpect() != null;
		boolean learnMode = command.getProbeOptLearn();
		boolean silent = testMode || learnMode;

		IPversion ipVersion;
		if (command.getCommand().equals("probe6"))
			ipVersion = IPversion.IPV6;
		else
			ipVersion = IPversion.IPV4;

		String sSourceAddress = command.getSrcAddress();
		/*
		 * =host or =addresse in source addresse
		 */
		boolean equalSourceAddress;
		if (sSourceAddress.startsWith("=")) {
			equalSourceAddress = true;
			sSourceAddress = sSourceAddress.substring(1);
		} else {
			equalSourceAddress = false;
		}

		IPNet sourceAddress;
		try {
			sourceAddress = new IPNet(sSourceAddress);
		} catch (UnknownHostException ex) {
			try {
				// not an IP try to resolve as a host.
				sourceAddress = IPNet.getByName(sSourceAddress, ipVersion);
				if (!silent)
					_outStream.println(sSourceAddress + " => " +
						sourceAddress.toString("::i"));
			} catch (UnknownHostException ex1) {
				_outStream.println("Error: " + ex1.getMessage());
				return false;
			}
		}
		IPNet destinationAdress;
		try {
			destinationAdress = new IPNet(command.getDestAddress());
		} catch (UnknownHostException ex) {
			try {
				// not an IP try to resolve as a host.
				destinationAdress = IPNet.getByName(command.getDestAddress(), ipVersion);
				if (!silent)
					_outStream.println(command.getDestAddress() + " => " +
						destinationAdress.toString("::i"));
			} catch (UnknownHostException ex1) {
				_outStream.println("Error: " + ex1.getMessage());
				return false;
			}
		}

		/*
		 * Check address family
		 */
		if (!sourceAddress.sameIPVersion(destinationAdress)) {
			_outStream.println("Error: source address and destination address" +
					" must have the same address family");
			return false;
		}

		/*
		 * We can specify where we want to inject the probes.
		 */
		IfaceLinks ilinks = null;
		if (command.getEquipments() != null) {
			ilinks = getIfaceLinksByEquipmentSpec(sourceAddress, command.getEquipments());
			// error
			if (ilinks == null)
				return false;
		} else {
			/*
			 * try to find a network link that matches the source IP address.
			 */
			NetworkLinks nlinks;
			Topology topology = _monitor.getTopology();
			if (!equalSourceAddress) {
				nlinks = topology.getNetworkLinksByIP(sourceAddress);
			} else {
				try {
					nlinks = topology.getNetworkLinksByIP(sourceAddress.hostAddress());
				}  catch (UnknownHostException ex1) {
					_outStream.println("Error: " + ex1.getMessage());
					return false;
				}
			}
			if (nlinks.isEmpty()) {
				/*
				 * use the DFLTEQUIPMENT variable if defined.
				 */
				String defaultEquipment;
				if (sourceAddress.isIPv4())
					 defaultEquipment = _monitor.getDefines().get("DFLTEQUIPMENT");
				else
					defaultEquipment = _monitor.getDefines().get("DFLTEQUIPMENT6");
				if (defaultEquipment != null) {
					ilinks = getIfaceLinksByEquipmentSpec(sourceAddress, defaultEquipment);
					// error
					if (ilinks == null)
						return false;
				} else {
					_outStream.println("No network matches");
					return false;
				}
			} else {
				if (nlinks.size() > 1) {
					_outStream.println("Too many networks match this source IP address");
					return false;
				}
				ilinks = nlinks.get(0).getIfaceLinks();
			}
		}

		if (ilinks.isEmpty()) {
			_outStream.println("No link found");
			return false;
		}

		if (ilinks.size() > 1) {
			_outStream.println("Too many links");
			return false;
		}

		String expect = command.getProbeExpect();
		if (expect == null)
			expect = "";

		IfaceLink ilink = ilinks.get(0);

		/*
		 * build the probe request
		 */
		String sprotocol = command.getProtoSpecification();
		String sportSource = command.getPortSource();
		String sportDest = command.getPortDest();

		IPProtocols ipProtocols = IPProtocols.getInstance();
		Integer protocol;

		ProbeRequest request = new ProbeRequest();
		if (sprotocol != null) {
			protocol = ipProtocols.protocolLookup(sprotocol);
			if (protocol.intValue() == -1)  {
				_outStream.println("unknown protocol: " + sprotocol);
				return false;
			}
			List<Integer> protocols = new ArrayList<Integer>();
			request.setProtocols(protocols);
			protocols.add(protocol);

			/*
			 * tcp or udp with port source/port destination
			 */
			if (sprotocol.equalsIgnoreCase("tcp") ||
					sprotocol.equalsIgnoreCase("udp")) {
				/*
				 * if tcp or udp we want to match ip too.
				 */
				if (sourceAddress.isIPv4())
					protocols.add(ipProtocols.IP());
				else
					protocols.add(ipProtocols.IPV6());

				/*
				 * services lookup, by default "any"
				 */
				if (sportSource == null)
					sportSource = "any";
				PortSpec sourceSpec = parsePortSpec(sportSource, sprotocol);
				if (sourceSpec == null)
					return false;
				request.setSourcePort(sourceSpec);

				if (sportDest == null)
					sportDest = "any";
				PortSpec destSpec = parsePortSpec(sportDest, sprotocol);
				if (destSpec == null)
					return false;
				request.setDestinationPort(destSpec);

				/*
				 * tcp flags
				 */
				StringsList tcpFlags = command.getTcpFlags();
				if (tcpFlags != null) {
					if (sprotocol.equalsIgnoreCase("udp")) {
						_outStream.println("TCP flags not allowed for UDP!");
						return false;
					}

					ProbeTcpFlags probeTcpFlags = new ProbeTcpFlags();

					/*
					 * check and add each flags spec
					 */
					for (String flag: tcpFlags) {
						if (flag.equalsIgnoreCase("any")) {
							probeTcpFlags = null;
							break;
						}
						if (flag.equalsIgnoreCase("none")) {
							probeTcpFlags.add(new TcpFlags());
							continue;
						}
						if (!checkTcpFlags(flag)) {
							_outStream.println("invalid TCP flags: " + flag);
							return false;
						}
						TcpFlags tf = new TcpFlags(flag);
						probeTcpFlags.add(tf);
					}
					request.setTcpFlags(probeTcpFlags);
				}
			}

			/*
			 * if ip we want to match tcp, udp, icmp too
			 */
			if (sprotocol.equalsIgnoreCase("ip") ||
					sprotocol.equalsIgnoreCase("ipv6")) {
				protocols.add(ipProtocols.TCP());
				protocols.add(ipProtocols.UDP());
				protocols.add(ipProtocols.ICMP());
				protocols.add(ipProtocols.ICMP6());
			}

			/*
			 * icmp with icmp-type
			 */
			if (sprotocol.equalsIgnoreCase("icmp") ||
					sprotocol.equalsIgnoreCase("icmp6")) {
				IPIcmp ipIcmp;
				if (sprotocol.equalsIgnoreCase("icmp"))
					ipIcmp = IPIcmp4.getInstance();
				else
					ipIcmp = IPIcmp6.getInstance();

				if (sportSource != null) {
					IPIcmpEnt icmpEnt = ipIcmp.icmpLookup(sportSource);
					if (icmpEnt == null) {
						_outStream.println("unknown icmp-type or message: "
							+ sportSource);
						return false;
					}
					request.setSubType(icmpEnt.getIcmp());
					request.setCode(icmpEnt.getCode());
				}
			}
		}

		/*
		 * probe options
		 */
		ProbeOptions options = request.getProbeOptions();
		options.setNoAction(command.getProbeOptNoAction());
		options.setQuickDeny(command.getProbeOptQuickDeny());

		/*
		 * probe
		 */
		_monitor.resetProbing();
		_monitor.newProbing(ilink, sourceAddress, destinationAdress, request);
		_lastProbing = _monitor.startProbing();

		/*
		 * results
		 */
		/*
		 * acl counters
		 */
		int accepted = 0;
		int denied = 0;
		int may = 0;
		int match = 0;

		/*
		 * routing counters
		 */
		int routed = 0;
		int notrouted = 0;
		int routeunknown =0;

		boolean verbose = command.getProbeOptVerbose();
		boolean active = command.getProbeOptActive();
		boolean matching = command.getProbeOptMatching();

		if (verbose) {
			active = true;
			matching = true;
		}

		if (!active)
			matching = true;
		/*
		 * each tracker
		 */
		for (ProbesTracker tracker: _lastProbing) {
			if (!silent) {
				ShellReport report = new ShellReport(tracker, verbose, active,
					matching);
				_outStream.print(report.showResults());
			}
			AclResult aclResult = tracker.getAclResult();
			if (aclResult.hasAccept())
				accepted++;
			if (aclResult.hasDeny())
				denied++;
			if (aclResult.hasMay())
				may++;
			if (aclResult.hasMatch())
				match++;

			switch (tracker.getRoutingResult()) {
				case ROUTED:	routed++;
								break;
				case NOTROUTED:	notrouted++;
								break;
				default:
								routeunknown++;
			}
		}

		/*
		 * Global ACL result
		 */
		AclResult aclResult = new AclResult();
		/*
		 * one result was MAY
		 */
		if (may > 0 || match > 0)
			aclResult.addResult(AclResult.MAY);

		/*
		 * some probes were accepted and some were denied => MAY
		 */
		if (match == 0 && accepted > 0 && denied > 0)
			aclResult.addResult(AclResult.MAY);

		/*
		 * all probes were accepted => ACCEPT
		 */
		if (match == 0 && accepted > 0 && denied == 0)
			aclResult.addResult(AclResult.ACCEPT);

		/*
		 * all probes were denied => DENY
		 */
		if (match == 0 && denied > 0 && accepted == 0) {
			aclResult.addResult(AclResult.DENY);
		}

		/*
		 * some probes were matching => MATCH
		 */
		if (match > 0) {
			aclResult.setResult(AclResult.MATCH);
		}

		/*
		 * Global routing result
		 */
		RoutingResult routingResult = RoutingResult.UNKNOWN;
		/*
		 * one result was UNKNOWN.
		 */
		if (routeunknown > 0) {
			routingResult = RoutingResult.UNKNOWN;
		} else {
			/*
			 * some probes were routed, and some not => UNKNOWN
			 */
			if (routed > 0 && notrouted > 0) {
				routingResult = RoutingResult.UNKNOWN;
			} else {
				/*
				 * all probes were routed => ROUTED
				 */
				if (routed > 0) {
					routingResult = RoutingResult.ROUTED;
				}
				/*
				 * all probes were not routed => NOTROUTED
				 */
				if (notrouted > 0) {
					routingResult = RoutingResult.NOTROUTED;
				}
			}
		}

		if (!silent) {
			_outStream.println("Global ACL result is: " + aclResult);
			_outStream.println("Global routing result is: " + routingResult);
			_outStream.println();
		}

		/*
		 * XXX we need a better logic here.
		 */
		boolean testExpect = false;
		boolean notExpect = expect.startsWith("!");
		if (notExpect && expect.length() > 1)
			expect = expect.substring(1);

		expect = expect.toUpperCase();
		
		if (!expect.isEmpty() && !_expectStrings.contains(expect)) {
			_outStream.println("invalid expect: " + expect);
			return false;
		}

		if (expect.equals("ROUTED") &&
				routingResult == RoutingResult.ROUTED)
			testExpect = true;
		if (expect.equals("NONE-ROUTED") &&
				routingResult == RoutingResult.NOTROUTED)
			testExpect = true;
		if (expect.equals("UNACCEPTED") && 
				(routingResult == RoutingResult.NOTROUTED || 
				(aclResult.hasDeny() && !aclResult.hasMay())))	
			testExpect = true;		
		if (expect.equals("UNKNOWN") &&
				routingResult == RoutingResult.UNKNOWN)
			testExpect = true;

		if (expect.equals("ACCEPT") &&
				aclResult.hasAccept() && !aclResult.hasMay())
			testExpect = true;
		if (expect.equals("DENY") &&
				aclResult.hasDeny() && !aclResult.hasMay())
			testExpect = true;

		if (expect.equals("MAY") &&
				aclResult.hasMay())
				testExpect = true;

		if (notExpect)
			testExpect = !testExpect;

		if (testMode) {
			if (!testExpect) {
				_outStream.println(commandLine + " [FAILED]");
			} else {
				_outStream.println(commandLine + " [OK]");
			}
		}

		if (learnMode) {
			_outStream.print(commandLine);
			_outStream.print(" [ACL: " + aclResult);
			_outStream.print("; Routing: " + routingResult);
			_outStream.println("]");
		}

		return testExpect;
	}

	public void groovyCommand(ShellParser parser) {

		Binding binding = new Binding();
		LsfwBinding lsfw = newBinding(parser.getGroovyArgs());
		binding.setVariable("lsfw", lsfw);

		GroovyScriptEngine scriptEngine;
		try {
			scriptEngine = new GroovyScriptEngine(parser.getGroovyDirectory());
		} catch (IOException ex) {
			_outStream.println(ex.getMessage());
			return;
		}
		try {
			scriptEngine.run(parser.getGroovyScript(), binding);
		} catch (Exception ex) {
			_outStream.println();
			_outStream.println("Error: " + ex.getMessage());
		}
	}

	public void groovyConsoleCommand(ShellParser parser) {
		Binding binding = new Binding();
		LsfwBinding lsfw = newBinding(parser.getGroovyArgs());
		binding.setVariable("lsfw", lsfw);
		Console console = new Console(binding);
		console.run();
	}

	public void parseShellCommand(String commandLine) {

		if (Log.debug().isLoggable(Level.INFO))
			Log.debug().info(commandLine);

		// comment
		if (commandLine.startsWith("#"))
			return;
		// macro substitution
		String subs = substitute(commandLine);

		// tee redirection
		boolean teeAppend = false;
		String teeFile = null;
		int p = subs.lastIndexOf("|>>");
		if (p != -1) {
			teeAppend = true;
			teeFile = subs.substring(p + 3);
			teeFile = teeFile.trim();
			subs = subs.substring(0, p ).trim();

		} else {
			p = subs.lastIndexOf("|>");
			if (p != -1) {
				teeAppend = false;
				teeFile = subs.substring(p + 2);
				teeFile = teeFile.trim();
				subs = subs.substring(0, p ).trim();
			}
		}

		// empty command
		if (subs.isEmpty())
			return;

		_parseRunner.getParseErrors().clear();
		ParsingResult<?> result = _parseRunner.run(subs);

		if (!result.matched) {
			if (result.hasErrors()) {
				ParseError error = result.parseErrors.get(0);
				InputBuffer buf = error.getInputBuffer();
				_outStream.println("Syntax error: " +
					buf.extract(0, error.getStartIndex()));
			}
		}

		/*
		 * tee stdout
		 */
		if (teeFile != null) {
			try {
				ShellConsole.out().tee(teeFile, teeAppend);
			} catch (FileNotFoundException ex) {
				_outStream.println("Cannot open file: " + teeFile);
				return;
			}
		}

		if (_parser.getCommand().equals("quit")) {
			if (_interactive)
				_outStream.println("Goodbye!");
			System.exit(0);
		}

		if (_interactive && _monitor.getOptions().getAutoReload())
			autoReload();

		if (_parser.getCommand().equals("probe") ||
			  _parser.getCommand().equals("probe6")) {
			boolean test = probeCommand(commandLine, _parser);
			if (_parser.getProbeExpect() != null) {
				if (!test)
					_testResult = false;
			}
		}
		if (_parser.getCommand().equals("option"))
			optionCommand(_parser);
		if (_parser.getCommand().equals("topology"))
			topologyCommand(_parser);
		if (_parser.getCommand().equals("route"))
			routeCommand(_parser);
		if (_parser.getCommand().equals("help"))
			helpCommand(_parser);
		if (_parser.getCommand().equals("define"))
			defineCommand(_parser);
		if (_parser.getCommand().equals("equipment"))
			equipmentCommand(_parser);
		if (_parser.getCommand().equals("reload"))
			reloadCommand(_parser);
		if (_parser.getCommand().equals("groovy"))
			groovyCommand(_parser);
		if (_parser.getCommand().equals("groovyconsole"))
			groovyConsoleCommand(_parser);

		/*
		 * 'untee' stdout
		 */
		if (teeFile != null) {
			try {
				ShellConsole.out().unTee();
			} catch (IOException ex) {
				_outStream.println("Cannot unTee file: " + teeFile);
			}
		}
	}

	public int runCommand(String commandLine) {
		_testResult = true;
		parseShellCommand(commandLine.trim());
		if (!_testResult)
			return App.EXIT_FAILURE;
		return App.EXIT_SUCCESS;
	}

	public int runFromFile(String fileName) {
		_testResult = true;
		BufferedReader dataIn = null;
		if (fileName == null) {
			 dataIn = new BufferedReader(new InputStreamReader(System.in));
		} else {
			try {
				dataIn = new BufferedReader(new FileReader(fileName));
			} catch (FileNotFoundException ex) {
				throw new JtaclRuntimeException(ex.getMessage());
			}
		}

		for (;;) {
			if (_interactive)
				_outStream.print(_prompt);
			String commandLine;
			try {
				commandLine = dataIn.readLine();
			} catch (IOException ex) {
				throw new JtaclRuntimeException(ex.getMessage());
			}
			if (commandLine == null) {
				break;
			}
			commandLine = commandLine.trim();
			parseShellCommand(commandLine);
		}
		try {
			dataIn.close();
		} catch (IOException ex) {
				throw new JtaclRuntimeException(ex.getMessage());
		}
		if (!_testResult)
			return App.EXIT_FAILURE;
		return App.EXIT_SUCCESS;
	}

	public PrintStream getOutputStream() {
		return _outStream;
	}

	public void setOutputStream(PrintStream outStream) {
		_outStream = outStream;
	}

}