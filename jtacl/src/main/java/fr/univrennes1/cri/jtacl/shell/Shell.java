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

import fr.univrennes1.cri.jtacl.core.exceptions.JtaclConfigurationException;
import fr.univrennes1.cri.jtacl.core.exceptions.JtaclInternalException;
import fr.univrennes1.cri.jtacl.core.exceptions.JtaclRuntimeException;
import fr.univrennes1.cri.jtacl.core.monitor.AclResult;
import fr.univrennes1.cri.jtacl.core.monitor.Monitor;
import fr.univrennes1.cri.jtacl.core.monitor.ProbeRequest;
import fr.univrennes1.cri.jtacl.core.monitor.ProbeTcpFlags;
import fr.univrennes1.cri.jtacl.core.monitor.ProbesTracker;
import fr.univrennes1.cri.jtacl.core.monitor.Probing;
import fr.univrennes1.cri.jtacl.core.monitor.RoutingResult;
import fr.univrennes1.cri.jtacl.core.network.Iface;
import fr.univrennes1.cri.jtacl.core.network.IfaceLink;
import fr.univrennes1.cri.jtacl.core.network.IfaceLinks;
import fr.univrennes1.cri.jtacl.core.network.NetworkEquipment;
import fr.univrennes1.cri.jtacl.core.network.NetworkEquipmentsByName;
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
import fr.univrennes1.cri.jtacl.lib.ip.TcpFlags;
import fr.univrennes1.cri.jtacl.lib.misc.StringsList;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.ArrayList;
import static java.util.Arrays.* ;
import java.util.Collections;
import java.util.List;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.parboiled.Parboiled;
import org.parboiled.buffers.InputBuffer;
import org.parboiled.errors.ParseError;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

/**
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class Shell {

	protected String _prompt;
	protected ShellParser _parser;
	protected Monitor _monitor;
	protected OptionParser _optParser;
	protected boolean _interactiveMode;
	protected boolean _testMode;
	protected boolean _verbose;
	protected Probing _lastProbing;
	protected boolean _testResult;

	static public final int EXIT_SUCCESS = 0;
	static public final int EXIT_FAILURE = 1;
	static public final int EXIT_ERROR = 255;

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
			System.out.println("No such equipment: " + equipmentName);
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
					System.out.println("No such interface: " + ifaceName);
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
				if (link.getIface().getName().equals(ifaceName)) {
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
		_prompt = "jtacl> ";

		 _parser = Parboiled.createParser(ShellParser.class);
		 _monitor = Monitor.getInstance();
		 _lastProbing = null;

		_optParser = new OptionParser();
		_optParser.acceptsAll(asList("c", "command"),
				"Execute the command in argument and quit.")
				.withRequiredArg().describedAs("command to execute");

		_optParser.acceptsAll(asList("f", "file"), 
				"Use the configuration file in argument.")
				.withRequiredArg().describedAs("configuration file");

		_optParser.acceptsAll(asList("i", "input"),
				"Read and execute commands from the input file and quit.")
				.withRequiredArg().describedAs("input file");

		_optParser.acceptsAll(asList("h", "help"), 
				"This help.");

		_optParser.acceptsAll(asList("n", "no-interactive"), 
				"Non interactive mode.");

		_optParser.acceptsAll(asList("t", "test"), 
				"Test mode.");

		_optParser.acceptsAll(asList("v", "verbose"),
				"Use verbose reports.");
		
		_optParser.acceptsAll(asList("o", "option"), "Set option").
				withRequiredArg().describedAs("option to set (option=value)");

	}

	protected String substitute(String line) {

		String r = line;
		for (String s: _monitor.getDefines().keySet()) {
			r = r.replace("$" + s, _monitor.getDefines().get(s));
		}
		return r;
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
				System.out.println(line);
			}
			reader.close();
		} catch (IOException ex) {
			System.out.println("cannot print help");
		}

	}

	public void optionCommand(ShellParser command) {
	String name = command.getSetValueName();
	String value = command.getSetValueValue();

		if (name != null && value != null)
			try {
				_monitor.getOptions().setOption(name, value);
			} catch (JtaclConfigurationException e) {
				System.out.println("Error: " + e.getMessage());
			}
		else
			System.out.println(_monitor.getOptions().getOptionsList());
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
				System.out.println(d + "=" + _monitor.getDefines().get(d));
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
					System.out.println("No such equipment: " + equipmentName);
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
					System.out.println("Error " + ex.getMessage());
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
					if (candidate.getIfaceLinks().size() <= 1)
						candidate = null;
				}
				if (option.equalsIgnoreCase("!connected")) {
					if (candidate.getIfaceLinks().size() > 1)
						candidate = null;
				}
			}
			/*
			 * output
			 */
			if (candidate != null)
				System.out.println(candidate.toString());

		}
	}

	public void routeCommand(ShellParser command) {

		String equipmentName = command.getEquipments();
		if (equipmentName != null) {
			if (!_monitor.getEquipments().containsKey(equipmentName)) {
				System.out.println("No such equipment: " + equipmentName);
				return;
			}
		}
		NetworkEquipmentsByName equipments = _monitor.getEquipments();
		for (NetworkEquipment equipment: equipments.values()) {
			if (equipmentName== null || equipment.getName().equals(equipmentName)) {
				System.out.println("Routes on " + equipment.getName());
				System.out.println("-------------------");
				System.out.println(equipment.getShowableRoutes().showRoutes());
				System.out.println("-------------------");
				System.out.println();
			}
		}
	}

	public void equipmentCommand(ShellParser command) {

		String equipmentName = command.getEquipments();
		NetworkEquipment equipment = _monitor.getEquipments().get(equipmentName);
		if (equipment == null) {
			System.out.println("No such equipment: " + equipmentName);
			return;
		}

		equipment.runShell(command.getSubCommand());
	}

	public void reloadCommand(ShellParser command) {

		String equipmentName = command.getEquipments();

		if (equipmentName == null) {
			_monitor.reload();
			return;
		}

		NetworkEquipment equipment = _monitor.getEquipments().get(equipmentName);
		if (equipment == null) {
			System.out.println("No such equipment: " + equipmentName);
			return;
		}

		try {
			_monitor.reloadEquipment(equipment);
		} catch (JtaclRuntimeException ex) {
			System.err.println("Error: " + ex.getMessage());
		}
	}

	public boolean probeCommand(ShellParser command) {

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
				if (!_testMode)
					System.out.println(sSourceAddress + " => " +
						sourceAddress.toString("::i"));
			} catch (UnknownHostException ex1) {
				System.out.println("Error: " + ex1.getMessage());
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
				if (!_testMode)
					System.out.println(command.getDestAddress() + " => " +
						destinationAdress.toString("::i"));
			} catch (UnknownHostException ex1) {
				System.out.println("Error: " + ex1.getMessage());
				return false;
			}
		}

		/*
		 * Check address family
		 */
		if (!sourceAddress.sameIPVersion(destinationAdress)) {
			System.out.println("Error: source address and destination address" +
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
					System.out.println("Error: " + ex1.getMessage());
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
					System.out.println("No network matches");
					return false;
				}
			} else {
				if (nlinks.size() > 1) {
					System.out.println("Too many networks match this source IP address");
					return false;
				}
				ilinks = nlinks.get(0).getIfaceLinks();
			}
		}

		if (ilinks.isEmpty()) {
			System.out.println("No link found");
			return false;
		}

		if (ilinks.size() > 1) {
			System.out.println("Too many links");
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
		String sprotoSource = command.getProtoSource();
		String sprotoDest = command.getProtoDest();

		IPProtocols ipProtocols = IPProtocols.getInstance();
		IPServices ipServices = IPServices.getInstance();

		Integer protocol;
		Integer protoSource;
		Integer protoDest;
		
		ProbeRequest request = new ProbeRequest();
		if (sprotocol != null) {
			protocol = ipProtocols.protocolLookup(sprotocol);
			if (protocol.intValue() == -1)  {
				System.out.println("unknown protocol: " + sprotocol);
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
				 * services lookup
				 */
				if (sprotoSource != null) {
					protoSource = ipServices.serviceLookup(sprotoSource, sprotocol);
					if (protoSource.intValue() == -1) {
						System.out.println("unknown service: " + sprotoSource);
						return false;
					}
					request.setSourcePort(protoSource);
				}
				if (sprotoDest != null) {
					protoDest = ipServices.serviceLookup(sprotoDest, sprotocol);
					if (protoDest.intValue() == -1) {
						System.out.println("unknown service: " + sprotoDest);
						return false;
					}
					request.setDestinationPort(protoDest);
				}

				/*
				 * tcp flags
				 */
				StringsList tcpFlags = command.getTcpFlags();
				if (tcpFlags != null) {
					if (sprotocol.equalsIgnoreCase("udp")) {
						System.out.println("TCP flags not allowed for UDP!");
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
							System.out.println("invalid TCP flags: " + flag);
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

				if (sprotoSource != null) {
					IPIcmpEnt icmpEnt = ipIcmp.icmpLookup(sprotoSource);
					if (icmpEnt == null) {
						System.out.println("unknown icmp-type or message: "
							+ sprotoSource);
						return false;
					}
					request.setSubType(icmpEnt.getIcmp());
					request.setCode(icmpEnt.getCode());
				}
			}
		}

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

		/*
		 * each tracker
		 */
		for (ProbesTracker tracker: _lastProbing) {
			if (!_testMode) {
				ShellReport report = new ShellReport((tracker));
				System.out.print(report.showResults(_verbose));
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
			aclResult.setResult(AclResult.ACCEPT);

		/*
		 * all probes were denied => DENY
		 */
		if (match == 0 && denied > 0 && accepted == 0) {
			aclResult.setResult(AclResult.DENY);
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

		if (!_testMode) {
			System.out.println("Global ACL result is: " + aclResult);
			System.out.println("Global routing result is: " + routingResult);
			System.out.println();
		}

		/*
		 * XXX we need a better logic here.
		 */
		boolean testExpect = false;
		boolean notExpect = expect.startsWith("!");
		if (notExpect && expect.length() > 1)
			expect = expect.substring(1);

		if (expect.equalsIgnoreCase("ROUTED") &&
				routingResult == RoutingResult.ROUTED)
			testExpect = true;
		if (expect.equalsIgnoreCase("NONE-ROUTED") &&
				routingResult == RoutingResult.NOTROUTED)
			testExpect = true;
		if (expect.equalsIgnoreCase("UNKNOWN") &&
				routingResult == RoutingResult.UNKNOWN)
			testExpect = true;

		if (expect.equalsIgnoreCase("ACCEPT") &&
				aclResult.hasAccept() && !aclResult.hasMay())
			testExpect = true;
		if (expect.equalsIgnoreCase("DENY") &&
				aclResult.hasDeny() && !aclResult.hasMay())
			testExpect = true;

		if (expect.equalsIgnoreCase("MAY") &&
				aclResult.hasMay())
				testExpect = true;

		if (notExpect)
			testExpect = !testExpect;
		return testExpect;
	}

	public void parseShellCommand(String commandLine) {
		_parser.clear();
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

		ParsingResult<?> result = ReportingParseRunner.run(_parser.CommandLine(),
				subs);

		if (!result.matched) {
			if (result.hasErrors()) {
				ParseError error = result.parseErrors.get(0);
				InputBuffer buf = error.getInputBuffer();
				System.out.println("Syntax error: " +
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
				System.out.println("Cannot open file: " + teeFile);
				return;
			}
		}

		if (_parser.getCommand().equals("quit")) {
			System.out.println("Goodbye!");
			System.exit(0);
		}
		if (_parser.getCommand().equals("probe") ||
			  _parser.getCommand().equals("probe6")) {
			boolean test = probeCommand(_parser);
			if (_testMode) {
				if (!test) {
					_testResult = false;
					System.out.println(commandLine + " [FAILED]");
				} else {
					System.out.println(commandLine + " [OK]");
				}
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

		/*
		 * 'untee' stdout
		 */
		if (teeFile != null) {
			try {
				ShellConsole.out().unTee();
			} catch (IOException ex) {
				System.out.println("Cannot unTee file: " + teeFile);
			}
		}
	}

	protected void printUsage() {
		try {
			_optParser.printHelpOn(System.out);
		} catch (IOException ex) {
			// do nothing
		}
	}

	public void runCommand(String commandLine) {

		parseShellCommand(commandLine.trim());
	}

	public void runFromFile(String fileName) {
		
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
			if (_interactiveMode && !_testMode)
				System.out.print(_prompt);
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
	}

	public void run(String[] args) {
		try {
			ShellConsole.install();
			OptionSet option = _optParser.parse(args);
			String configFile = null;
			_testMode = option.has("test");
			if (_testMode)
				_testResult = true;
			if (option.has("file")) {
				configFile = (String) option.valueOf("file");
			} else {
				printUsage();
				System.exit(Shell.EXIT_ERROR);
			}

			_verbose = option.has("verbose");
			if (option.has("option")) {
				_interactiveMode = false;
				List<?> options = option.valuesOf("o");
				for (Object o: options) {
					parseShellCommand("option " + (String)o);
				}
			}

			_monitor.configure(configFile);
			_monitor.init();

			if (option.has("command")) {
				String line = (String) option.valueOf("command");
				_interactiveMode = false;
				runCommand(line);
			}
			if (option.has("input")) {
				String fileName = (String) option.valueOf("input");
				_interactiveMode = false;
				runFromFile(fileName);
			}
			
			_interactiveMode = !option.has("no-interactive");
			if (!option.has("command") && !option.has("input"))
				runFromFile(null);
			
			if (_testMode) {
				if (!_testResult)
					System.exit(Shell.EXIT_FAILURE);
				else
					System.exit(Shell.EXIT_SUCCESS);
			}
			System.exit(Shell.EXIT_SUCCESS);

		} catch (Exception ex) {
			System.err.println("Error: " + ex.getMessage());
			if (!(ex instanceof JtaclConfigurationException))
				ex.printStackTrace();
			System.exit(Shell.EXIT_ERROR);
		}
	}

}