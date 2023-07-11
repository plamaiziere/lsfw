/*
 * Copyright (c) 2023, Universite de Rennes
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */
package fr.univrennes1.cri.jtacl.equipments.openbsd;

import fr.univrennes1.cri.jtacl.analysis.IPCrossRef;
import fr.univrennes1.cri.jtacl.core.exceptions.JtaclParameterException;
import fr.univrennes1.cri.jtacl.core.exceptions.JtaclRuntimeException;
import fr.univrennes1.cri.jtacl.core.monitor.Monitor;
import fr.univrennes1.cri.jtacl.core.network.IfaceLink;
import fr.univrennes1.cri.jtacl.core.network.NetworkEquipment;
import fr.univrennes1.cri.jtacl.core.network.Route;
import fr.univrennes1.cri.jtacl.core.network.RoutingEngine;
import fr.univrennes1.cri.jtacl.equipments.fortigate.FgFw;
import fr.univrennes1.cri.jtacl.equipments.fortigate.FgFwRuleAction;
import fr.univrennes1.cri.jtacl.equipments.fortigate.FgNetworkExternalResource;
import fr.univrennes1.cri.jtacl.equipments.fortigate.FgNetworkGroup;
import fr.univrennes1.cri.jtacl.equipments.fortigate.FgNetworkIP;
import fr.univrennes1.cri.jtacl.equipments.fortigate.FgNetworkObject;
import fr.univrennes1.cri.jtacl.equipments.fortigate.FgNetworkType;
import fr.univrennes1.cri.jtacl.equipments.fortigate.FgPortsSpec;
import fr.univrennes1.cri.jtacl.equipments.fortigate.FgService;
import fr.univrennes1.cri.jtacl.equipments.fortigate.FgServiceType;
import fr.univrennes1.cri.jtacl.equipments.fortigate.FgServicesGroup;
import fr.univrennes1.cri.jtacl.equipments.fortigate.FgTcpUdpSctpService;
import fr.univrennes1.cri.jtacl.lib.ip.AddressFamily;
import fr.univrennes1.cri.jtacl.lib.ip.IP;
import fr.univrennes1.cri.jtacl.lib.ip.IPNet;
import fr.univrennes1.cri.jtacl.lib.ip.IPRange;
import fr.univrennes1.cri.jtacl.lib.ip.IPRangeable;
import fr.univrennes1.cri.jtacl.lib.ip.IPversion;
import fr.univrennes1.cri.jtacl.lib.ip.PortOperator;
import fr.univrennes1.cri.jtacl.lib.ip.PortRange;
import fr.univrennes1.cri.jtacl.lib.ip.PortSpec;
import fr.univrennes1.cri.jtacl.lib.ip.Protocols;
import fr.univrennes1.cri.jtacl.lib.misc.Direction;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * "Ad hoc" Converter from PacketFilter rules to Fortinet rules
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PacketFilterToFortiConverter {

	private static final String LSFW_COMMENT  = quote("#MigrationFucop");

	protected PacketFilter pf;
	protected FgFw fortigate;
	protected PrintStream output;
	protected HashMap<String, FgNetworkObject> fgNetworks;
	protected HashMap<String, FgNetworkObject> fgCreatedNetworks = new HashMap<>();
	protected HashMap<String, FgService> fgServices;
	protected HashMap<String, FgService> fgCreatedServices = new HashMap<>();
	protected BufferedWriter logFile;
	protected BufferedWriter logErrorFile;
	protected BufferedWriter cliFileAddresses;
	protected BufferedWriter cliFileAddressesGroup;
	protected BufferedWriter cliFileExternalResource;
	protected BufferedWriter cliFileServices;
	protected BufferedWriter cliFileRules;
	protected BufferedWriter probesFile;

	protected HashMap<String, List<IPNet>> networksByLink;
	protected List<IPNet> internalNetworks;
	protected String defaultRouteIface;
	protected IPNet IP_1234;
	protected IPRangeable ANY;

	// initialize the converter using a PacketFilter equipment and a Fortigate equipment (by name)
	// the specified Fortigate equipment is used as a database for services and network objects, because
	// the database is shared by several Fortigate and we don't want to override an existing object
	// that may be tuned (for session or TTL by example)
	public PacketFilterToFortiConverter(PacketFilter pf, String fortigateName, PrintStream output) {
		try {
			IP_1234 = new IPNet("1.2.3.4");
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
		try {
			ANY = new IPRange("0/0");
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}

		this.pf = pf;
		this.output = output;
		this.networksByLink = computeNetworksByLink();
		this.internalNetworks = new ArrayList<>();
		for (List<IPNet> ips: networksByLink.values()) {
			internalNetworks.addAll(ips);
		}
		try {
			IP_1234 = new IPNet("1.2.3.4");
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
		try {
			ANY = new IPRange("0/0");
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}

		NetworkEquipment fg = Monitor.getInstance().getEquipments().get(fortigateName);
		if (fg != null) {
			if (fg instanceof FgFw) {
				fortigate = (FgFw)fg;
				fgNetworks = new HashMap<>(fortigate.getFgNetworks());
				fgServices = new HashMap<>(fortigate.getFgServices());
			} else {
				throw new JtaclParameterException("equipment is not of type FgFw");
			}
		} else {
			throw new JtaclParameterException("no such equipment");
		}

		// files generated
		String d = "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss"));
		try {
			cliFileAddresses = new BufferedWriter(new FileWriter("lsfw_fortigate_addresses" + d));
			cliFileAddressesGroup = new BufferedWriter(new FileWriter("lsfw_fortigate_addresses_groups" + d));
			cliFileExternalResource = new BufferedWriter(new FileWriter("lsfw_fortigate_external_resource" + d));
			cliFileServices = new BufferedWriter(new FileWriter("lsfw_fortigate_services" + d));
			cliFileRules = new BufferedWriter(new FileWriter("lsfw_fortigate_policies" + d));
			logFile = new BufferedWriter(new FileWriter("lsfw_fortigate_logs" + d));
			logErrorFile = new BufferedWriter(new FileWriter("lsfw_fortigate_error_logs" + d));
			probesFile = new BufferedWriter(new FileWriter("lsfw_fortigate_test_probes" + d));
		} catch (IOException e) {
			throw new JtaclParameterException("cannot open output files: " + e.getMessage());
		}
	}

	// here we go
	public void convert() {
		printLnFile(cliFileAddresses, "config firewall address");
		printLnFile(cliFileAddressesGroup, "config firewall addrgrp");
		printLnFile(cliFileExternalResource, "config system external-resource");
		printLnFile(cliFileServices, "config firewall service custom");
		printLnFile(cliFileRules, "config firewall policy");

		var ruleNumber = Integer.valueOf(100);
		for (PfGenericRule rule: pf._rootAnchor.getRules()) {
			if (! (rule instanceof PfRule)) continue;
			var pfrule = (PfRule) rule;
			logOut("RULE " + pfRuleToText(pfrule));

			try {
				ruleIsHandled(pfrule);

				var inIfacesName = pfrule.getIfList()
						.stream()
						.map(pfIfSpec -> pfIfSpec.getIfName())
						.collect(Collectors.toList());

				var pfFromIpSpec = pfrule.getFromIpSpec();
				boolean hasFromIPNegation = pfFromIpSpec
						.stream()
						.anyMatch(pfNodeHost -> pfNodeHost.isNot());
				if (hasFromIPNegation) {
					throw new JtaclPfToFortiException("from: cannot negate ip specification");
				}

				var pfToIpSpec = pfrule.getToIpSpec();
				boolean hasToIPNegation = pfToIpSpec
						.stream()
						.anyMatch(pfNodeHost -> pfNodeHost.isNot());
				if (hasToIPNegation) {
					if (pfToIpSpec.size() > 1) {
						throw new JtaclPfToFortiException("to: cannot negate more than one ip specification");
					}
					if (inIfacesName.contains(defaultRouteIface)) {
						throw new JtaclPfToFortiException("to: cannot negate if input interface is the default route interface");
					}
				}

				var fgNetObjFrom = pfIpspecToFortigate(pfrule.getFromIpSpec());
				if (fgNetObjFrom.isEmpty())
					throw new JtaclPfToFortiException("from addresses is empty (self rule?)");

				var fgNetObTo = pfIpspecToFortigate(pfrule.getToIpSpec());
				if (fgNetObTo.isEmpty())
					throw new JtaclPfToFortiException("to addresses is empty (self rule?)");

				boolean udp = pfrule.getProtocols().contains(Protocols.UDP);
				boolean tcp = pfrule.getProtocols().contains(Protocols.TCP);
				var fgServices = pfPortSpecToFortigate(pfrule.getToPortSpec(), udp, tcp);
				var action = pfrule.getAction().equals("pass") ? FgFwRuleAction.ACCEPT : FgFwRuleAction.DROP;

				generateCLIFwPolicy(ruleNumber.toString()
						, action
						, inIfacesName
						, fgNetObjFrom
						, hasFromIPNegation
						, fgNetObTo
						, hasToIPNegation
						, fgServices
						, pfRuleToText(pfrule)
						, " #" + pfrule.getParseContext().getLineNumber() + ", " + pfrule.getText());

				generateProbe(inIfacesName, fgNetObjFrom, hasToIPNegation, fgNetObTo, fgServices, action, udp, tcp, pfrule);

				ruleNumber += 100;

			} catch (JtaclPfToFortiException e) {
				var s = "#!#!#! rule " + pfRuleToText(pfrule) +  " => Error: " + e.getMessage();
				logError(s);
				printLnFile(cliFileRules, s);
				printLnFile(cliFileRules);
			}
			logOut("");
		}

		printLnFile(cliFileAddresses, "end");
		printLnFile(cliFileAddressesGroup, "end");
		printLnFile(cliFileExternalResource, "end");
		printLnFile(cliFileServices, "end");
		printLnFile(cliFileRules, "end");
	}

	protected HashMap<String, List<IPNet>> computeNetworksByLink() {
		HashMap<String, List<IPNet>> nbl = new HashMap<>();

		RoutingEngine.RoutingTable routingTable = pf.getRoutingEngine().getRoutingTableIPv4();
		for (RoutingEngine.RoutingTableItem rti: routingTable.values()) {
			for (Route<IfaceLink> route: rti.getRoutes()) {
				if (route.isNullRoute()) {
					continue;
				}
				if (route.getLink().isLoopback()) {
					continue;
				}
				var listNetworks = nbl.get(route.getLink().getIfaceName());
				if (listNetworks == null) { listNetworks = new ArrayList<>(); }
				if (!route.getPrefix().isHost()) {
					if(rangeIsAny(route.getPrefix())) {
						defaultRouteIface = route.getLink().getIfaceName();
						continue;
					}
					listNetworks.add(route.getPrefix());
				}

				nbl.put(route.getLink().getIfaceName(), listNetworks);
			}
		}
		return nbl;
	}

	protected boolean rangeIsAny(IPRangeable range) {
		return range.contains(ANY);
	}

	protected List<IPNet> reduceAny(List<IPNet> ipNetList) {
		var ips = ipNetList
			.stream()
			.map(ipNet -> {
				try {
					if (ipNet.isHost()) return ipNet;
					// on networks do not use '.0' addresses and '.255' and '.254' addresses
					// (because they are filtered in our ruleset)
					IPNet ip = new IPNet(ipNet.getIP().add(BigInteger.ONE), IPversion.IPV4);
					// search for an unreferenced IP address to avoid to match a rule using this address
					for(;;) {
						IPCrossRef xref = pf.getNetCrossRef().get(ip);
						if (xref == null) {
							return ip;
						}
						ip = new IPNet(ip.getIP().add(BigInteger.ONE), IPversion.IPV4);
					}
				} catch (UnknownHostException e) {
					throw new RuntimeException(e);
				}
			})
			.filter(ipNet -> {
				// avoid directly connected network
				return pf.getIfaceConnectedTo(ipNet) == null;
			})
			.collect(Collectors.toList());
		return ips;
	}

	protected List<IPRangeable> collectIpAndReduce(List<IPNet> reduces, List<FgNetworkObject> fgNetworkObjects, boolean negate) {

		List<IPRangeable> ips = collectAddresses(fgNetworkObjects)
					.stream()
					.map(ipNet -> rangeIsAny(ipNet) ? reduceAny(reduces) : List.of(ipNet))
					.flatMap(l -> l.stream())
					.collect(Collectors.toList());

     	if (negate) {
			 // try to find a random ip address that is not in the 'ips' collected addresses and not in the
			// internal networks of the equipment
			// works only on egress rule (to internet)
			var r = new Random();
			IPNet ip;
			for (;;) {
				int rand = r.nextInt();
				rand = rand >= 0 ? rand : -rand;
				BigInteger iip = BigInteger.valueOf(rand);
				try {
					ip = new IPNet(iip, IPversion.IPV4);
					final var lip = ip;
					boolean ok = ips
									.stream()
									.allMatch(ipNet -> !ipNet.contains(lip) && ! this.internalNetworks.contains(lip)
									);
					if (ok) {
						ip = lip;
						break;
					}
				} catch (UnknownHostException e) {
					throw new RuntimeException(e);
				}
			}
			ips = List.of(ip);
		}
		return ips;
	}

	// generates test cases for this rule
	// the cases will be replayed on the new fortinet equipment as regression tests using lsfw
	protected void generateProbe(List<String> sourceIfaces, List<FgNetworkObject> from
			, boolean negateTo, List<FgNetworkObject> to
			, List<FgService> services
			, FgFwRuleAction action
			, boolean udp, boolean tcp
			, PfRule rule) {

		printLnFile(probesFile, "### " + pfRuleToText(rule));

		// the meaning of 'any' depends on the interface used in input
		// => reduces 'any' to a set of networks addresses
		List<IPNet> reduceAnyFrom;
		if (!sourceIfaces.contains(defaultRouteIface)) {
			reduceAnyFrom = sourceIfaces
				.stream()
				.map(s -> networksByLink.get(s))
				.flatMap(n -> n.stream())
				.collect(Collectors.toList());
		} else reduceAnyFrom = List.of(IP_1234);

		List<IPRangeable> fromIps = collectIpAndReduce(reduceAnyFrom, from, false);

		// the meaning of 'any' depends on the interface used in input
		// => reduces 'any' to a set of networks addresses
		List<IPNet> reduceAnyTo;
		if (sourceIfaces.contains(defaultRouteIface)) {
			reduceAnyTo = networksByLink.values()
					.stream()
					.flatMap(ipNetList -> ipNetList.stream())
					.collect(Collectors.toList());
		} else reduceAnyTo = new ArrayList<>();

		List<IPRangeable> toIps = collectIpAndReduce(reduceAnyTo, to, negateTo);

		var udpServices = (udp) ? collectServices(services, Protocols.UDP) : null;
		var tcpServices = (tcp) ? collectServices(services, Protocols.TCP) : null;

		if (sourceIfaces.isEmpty())
			return;

		var expect = action == FgFwRuleAction.ACCEPT ? "accept" : "deny";
		var probe = "probe expect " + expect + " on " + pf.getName() + "|" + sourceIfaces.get(0) + " ";

		for (IPNet fip: collectRanges(fromIps)) {
			for (IPNet tip: collectRanges(toIps) ) {
				if (udp) {
					for (Integer ns: udpServices) {
						var s = probe + fip.toString("s") + " " + tip.toString("s") + " udp "  + "dyn:" + ns;
						printLnFile(probesFile, s);
					}
				}
				if (tcp) {
					for (Integer ns: tcpServices) {
						var s = probe + fip.toString("s") + " " + tip.toString("s") + " tcp "  + "dyn:" + ns + " flags Sa" ;
						printLnFile(probesFile, s);
					}
				}
				if (!udp && !tcp) {
					var s = probe + fip.toString("s") + " " + tip.toString("s") + " udp dyn:dyn";
					printLnFile(probesFile, s);

					s = probe + fip.toString("s") + " " + tip.toString("s") + " tcp dyn:dyn" + " flags Sa";
					printLnFile(probesFile, s);
				}
			}
		}
	}

	protected List<Integer> collectServices(Collection<FgService> serviceObjects, Integer protocol) {
		List<Integer> ports = new ArrayList<>();

		for (FgService fg: serviceObjects) {
			if (fg instanceof FgTcpUdpSctpService) {
				var s = (FgTcpUdpSctpService) fg;
				if (protocol == Protocols.UDP && s.isUdp()) {
					var pr = s.getUdpPortsSpec().get(0).getDestPorts().getRanges().get(0);
					ports.add(pr.getFirstPort());
					if (pr.getLastPort() != pr.getFirstPort()) ports.add(pr.getLastPort());
				}
				if (protocol == Protocols.TCP && s.isTcp()) {
					var pr = s.getTcpPortsSpec().get(0).getDestPorts().getRanges().get(0);
					ports.add(pr.getFirstPort());
					if (pr.getLastPort() != pr.getFirstPort()) ports.add(pr.getLastPort());
				}
			}
			if (fg instanceof FgServicesGroup) {
				var g = (FgServicesGroup) fg;
				ports.addAll(collectServices(g.getServices().values(), protocol));
			}
		}
		return ports;
	}

	protected List<IPNet> collectRanges(List<IPRangeable> ranges) {
		List<IPNet> ips = new ArrayList<>();
		for (IPRangeable range: ranges) {
			if (range.isHost()) {
				ips.add(range.getIpFirst());
				continue;
			}

			boolean internal = internalNetworks.stream().anyMatch(ipNet -> ipNet.contains(range));
			if (internal) {
				try {
					var biIp1 = range.getIpFirst().getIP().add(BigInteger.ONE);
					ips.add(new IPNet(biIp1, IPversion.IPV4));

					var biIp2 = range.getIpLast().getIP().subtract(BigInteger.TWO);
					int c = biIp2.compareTo(biIp1);
					if (c == 1) {
						ips.add(new IPNet(biIp2, IPversion.IPV4));
					}
				} catch (UnknownHostException e) {
					throw new RuntimeException(e);
				}
			} else {
				ips.add(range.getIpFirst());
				ips.add(range.getIpLast());
			}
		}
		return ips;
	}

	protected List<IPRangeable> collectAddresses(Collection<FgNetworkObject> networkObjects) {
		List<IPRangeable> ips = new ArrayList<>();

		for (FgNetworkObject fg: networkObjects) {
			if (fg instanceof FgNetworkIP) {
				IPRangeable range = ((FgNetworkIP) fg).getIpRange();
				ips.add(range);
			}

			// for groups, takes only the first item to reduce the tests
			if (fg instanceof FgNetworkGroup) {
				var group = (FgNetworkGroup) fg;
				var baseIPs = collectAddresses(group.getBaseObjects().values());
				ips.addAll(List.of(baseIPs.get(0)));
			}
			if (fg instanceof FgNetworkExternalResource) {
				var ext = (FgNetworkExternalResource) fg;
				var extIps = ext.getIpRanges();
				ips.addAll(List.of(extIps.get(0)));
			}
		}
		return ips;
	}

	protected void ruleIsHandled(PfRule pfrule) {
		// checks
		if (pfrule.getAf() == AddressFamily.INET6) {
			throw new JtaclPfToFortiException("INET6 unhandled");
		}

		if ((pfrule.getProtocols().size() > 0) && (pfrule.getProtocols().contains(Protocols.IPV6)
				|| (!pfrule.getProtocols().contains(Protocols.IP)
					&& !pfrule.getProtocols().contains(Protocols.UDP)
					&& !pfrule.getProtocols().contains(Protocols.TCP)))) {
			throw new JtaclPfToFortiException("protocol unhandled");

		}

		if (!pfrule.getIcmpspec().isEmpty()) {
			throw new JtaclPfToFortiException("icmp unhandled");
		}

		if (pfrule.getDirection() == Direction.OUT) {
			throw new JtaclPfToFortiException("out rule unhandled");
		}

		if (pfrule.getRouteOpts() != null) {
			throw new JtaclPfToFortiException("route option unhandled");
		}

		if (pfrule.getFromPortSpec().size() > 0) {
			throw new JtaclPfToFortiException("source port unhandled");
		}

		if (pfrule.isAll()) {
			throw new JtaclPfToFortiException("'all' rule unhandled");
		}

		if (!pfrule.isQuick())  {
			throw new JtaclPfToFortiException("'no quick' rule unhandled");
		}

		if (pfrule.getIfList()
				.stream()
				.filter(pfIfSpec -> pfIfSpec.isIfNot())
				.collect(Collectors.toList())
				.size() > 0) {
					throw new JtaclPfToFortiException("'not (!)' interface unhandled");
		}
	}

	protected static String pfRuleToText(PfRule pfRule) {
		 return pfRule.getParseContext().getFileName() + " #" + pfRule.getParseContext().getLineNumber()
						+ "," + pfRule.getText();
	}

	protected void logOut(String message) {
		output.println(message);
		printLnFile(logFile, message);
	}

	protected void logError(String message) {
		logOut(message);
		printLnFile(logErrorFile, message);
	}

	protected List<FgService> pfPortSpecToFortigate(PfPortSpec pfPortSpec, boolean udp, boolean tcp) {
		List<FgService> rObj = new ArrayList<>();

		for (PfPortItem pi: pfPortSpec) {
			if (pi.getPortSpec().getRanges().size() > 1) {
				throw new JtaclPfToFortiException("multiple port ranges not allowed");
			}

			var range = pi.getPortSpec().getRanges().get(0);
			var fs = findFortigateService(range, udp, tcp);
			if (fs != null) {
				var s = fgCreatedServices.get(fs.getName()) == null ? " (" + fortigate.getName() + ")" : " (created)";
				var p = "";
				if (udp && tcp) {
					p = "UDP/TCP";
				} else if (udp) { p = "UDP"; } else p = "TCP";

				logOut("SERVICE " + p + " " + range.toString() + " => " + fs + s);
				rObj.add(fs);
			} else {
				var name = tcp ? "TCP" : "";
				if (udp) name += "UDP";
				name +=  "_" + range.getFirstPort();
				name += range.getLastPort() != range.getFirstPort() ? "-" + range.getLastPort() : "";
				var fgPortSpec = new FgPortsSpec(null
						, new PortSpec(PortOperator.RANGE, range.getFirstPort(), range.getLastPort()));
				List<FgPortsSpec> portsSpecs = new ArrayList<>();
				portsSpecs.add(fgPortSpec);

				var fgService = new FgTcpUdpSctpService(name, name, name, null, null,
						(udp) ? portsSpecs : null, (tcp) ? portsSpecs : null, null);
				storeFgService(fgService);
				rObj.add(fgService);
			}
		}
		return rObj;
	}

	protected  FgTcpUdpSctpService findFortigateService(PortRange portRange, boolean udp, boolean tcp) {
		for (FgService fs: fgServices.values()) {
			if (fs.getType() != FgServiceType.TCPUDPSCTP) continue;
			var fgService = (FgTcpUdpSctpService) fs;
			if (fgService.hasFqdn() || fgService.hasRanges()) continue;
			if (udp && !fgService.isUdp()) continue;
			if (tcp && !fgService.isTcp()) continue;
			var udpOk = false;
			var tcpOk = false;
			if (udp) {
				var fgPortSpec = fgService.getUdpPortsSpec().get(0);
				if (fgPortSpec.hasSourcePortSpec()) continue;
				var ranges = fgPortSpec.getDestPorts().getRanges();
				if (ranges.size() > 1) continue;
				var range = ranges.get(0);
				if (range.getFirstPort() == portRange.getFirstPort()
						&& range.getLastPort() == portRange.getLastPort()) {
					udpOk = true;
				}
			}
			if (tcp) {
				var fgPortSpec = fgService.getTcpPortsSpec().get(0);
				if (fgPortSpec.hasSourcePortSpec()) continue;
				var ranges = fgPortSpec.getDestPorts().getRanges();
				if (ranges.size() > 1) continue;
				var range = ranges.get(0);
				if (range.getFirstPort() == portRange.getFirstPort()
						&& range.getLastPort() == portRange.getLastPort()) {
					tcpOk = true;
				}
			}
			if ( (!udp || udpOk) && (!tcp || tcpOk) ) {
				return fgService;
			}
		}
		return null;
	}

	// convert a PacketFilter ipspec to Fortigate network objects
	protected List<FgNetworkObject> pfIpspecToFortigate (PfIpSpec ipSpec) {
		List<FgNetworkObject> rObj = new ArrayList<>();

		for (PfNodeHost pfNodeHost: ipSpec) {

			// 'any' => 'all'
			if (pfNodeHost.isAddrAny()) {
				var o = fgNetworks.get("all");
				if (o == null) {
					throw new JtaclPfToFortiException("address object 'all' must exist");
				}
				var s = " (" + fortigate.getName() + ")";
				logOut("NETWORK 'any'  => " + o.getName() + s);
				rObj.add(o);
				continue;
			}

			// host or network to address object
			if (pfNodeHost.isAddrMask()) {
				var addr = new IPRange(pfNodeHost.getAddr().get(0));
				if (addr.isIPv4()) {
					var o = findFortigateNetworkIP(addr, fgNetworks);
					rObj.add(o != null ? o : createFortigateAddressRange(addr));
				}
				continue;
			}

			// range address to address object
			if (pfNodeHost.isAddrRange()) {
				var addr = new IPRange(pfNodeHost.getAddr().get(0), pfNodeHost.getRangeAddr().get(1));
				if (addr.isIPv4()) {
					var o = findFortigateNetworkIP(addr, fgNetworks);
					rObj.add(o != null ? o : createFortigateAddressRange(addr));
				}
				continue;
			}

			// table to addresses group or external ressource (if persist table)
			if (pfNodeHost.isAddrTable()) {
				String name = pfNodeHost.getTblName();
				PfTable table = pf._rootAnchor.findTable(name);
				// not persist
				if (table.getFileNames().isEmpty()) {
					var tableSpec = pfIpspecToFortigate(table.getIpspec()).stream().map(o -> (FgNetworkIP) o).collect(Collectors.toList());
					List<FgNetworkIP> tableObj = new ArrayList<>();
					for (FgNetworkIP ip: tableSpec) {
						boolean finded = false;
						for (FgNetworkIP tob: tableObj) {
							if (ip.getIpRange().same(tob.getIpRange())) {
								finded = true;
								break;
							}
						}
						if (!finded) tableObj.add(ip);
					}

					if (!tableObj.isEmpty()) {
						var group = findFortigateNetworkGroup(tableObj, fgNetworks);
						if (group != null) {
							var s = fgCreatedNetworks.get(group.getName()) == null ? " (" + fortigate.getName() + ")" : " (created)";
							rObj.add(group);
							logOut("NETWORK table " + table.getName() + " => " + group.getName() + s);
						} else {
							var gname = "G_" + name;
							var fgGroup = new FgNetworkGroup(gname, gname, null, null);
							for (FgNetworkObject o : tableObj) {
								fgGroup.addBaseReference(o.getName(), o);
							}
							rObj.add(fgGroup);
							storeFgNetwork(fgGroup);
							generateCLIAddressesGroups(fgGroup);
						}
					} else {
						throw new JtaclPfToFortiException("NETWORK " + table.getName() + " is empty");
					}
				} else {
					// persist
					List<IPRangeable> ips = table.getIpspec()
							.stream()
							.map(pfNH -> new IPRange(pfNH.getAddr().get(0)))
							.collect(Collectors.toList());

					var fg = findFortigateNetworkExternalResource(ips, fgNetworks);
					if (fg != null) {
							var s = fgCreatedNetworks.get(fg.getName()) == null ? " (" + fortigate.getName() + ")" : " (created)";
							rObj.add(fg);
							logOut("NETWORK table " + table.getName() + " => " + fg.getName() + s);
					} else {
						var gname = "G_" + name;
						var fgExternal = new FgNetworkExternalResource(gname, gname, null, null, "https://TABLE_" + table.getName(), true);
						fgExternal.getIpRanges().addAll(ips);
						rObj.add(fgExternal);
						storeFgNetwork(fgExternal);
						generateCLIExternalResource(fgExternal);
					}
				}
				continue;
			}
			throw new JtaclPfToFortiException("address object not handled " + pfNodeHost.toString());
		}
		return rObj;
	}

	protected FgNetworkExternalResource findFortigateNetworkExternalResource(
			List<IPRangeable> ips, HashMap<String, FgNetworkObject> fgNetworksObjs) {

		for (FgNetworkObject f :fgNetworksObjs.values()) {
			if (f instanceof FgNetworkExternalResource) {
				var er = (FgNetworkExternalResource) f;
				if (ips.size() != er.getIpRanges().size()) continue;
				boolean match = true;
				for (IPRangeable ip: ips) {
					boolean finded = false;
					for (IPRangeable eip: er.getIpRanges()) {
						if (ip.same(eip)) {
							finded = true;
							break;
						}
					}
					if (!finded) {
						match = false;
						continue;
					}
				}
				if (match) {
					return er;
				}
			}
		}
		return null;
	}

	protected List<FgNetworkIP> flatFgNetworkGroup(FgNetworkGroup group) {
		List<FgNetworkIP> ips = new ArrayList<>();
		for (FgNetworkObject obj: group.getBaseObjects().values()) {
			if (obj.getType() == FgNetworkType.IPRANGE) {
				var o = (FgNetworkIP) obj;
				ips.add(o);
			}
			if (obj.getType() == FgNetworkType.GROUP) {
				var g = (FgNetworkGroup) obj;
				ips.addAll(flatFgNetworkGroup(g));
			}

		}
		return ips;
	}

	protected FgNetworkGroup findFortigateNetworkGroup(List<FgNetworkIP> members, HashMap<String, FgNetworkObject> fgNetworksObjs) {
		for (FgNetworkObject fg: fgNetworksObjs.values()) {
			if (fg.getType() != FgNetworkType.GROUP) continue;
			var fgGroup = (FgNetworkGroup) fg;
			if (fgGroup.getExcludedObjects().size() > 0) continue;
			var groupValues = flatFgNetworkGroup(fgGroup);
			if (groupValues.size() != members.size()) continue;

			boolean matchAll = true;
			for (FgNetworkIP member: members) {
				boolean match = false;
				for (FgNetworkIP groupMember: groupValues) {
					if (member.getIpRange().same(groupMember.getIpRange())) {
						match = true;
						break;
					}
				}
				if (!match) {
					matchAll = false;
					break;
				}
			}
			if (matchAll) {
				return fgGroup;
			}
		}
		return null;
	}

	protected  FgNetworkIP findFortigateNetworkIP(IPRange range, HashMap<String, FgNetworkObject> fgNetworksObjs) {
		for (FgNetworkObject fg: fgNetworksObjs.values()) {
			if (fg.getType() == FgNetworkType.IPRANGE) {
				var nfg = (FgNetworkIP)fg;
				var fgAddress = nfg.getIpRange();
				if (range.same(fgAddress)) {
					var s = fgCreatedNetworks.get(nfg.getName()) == null ? " (" + fortigate.getName() + ")" : " (created)";
					logOut("NETWORK " + range.toNetString("::i") + " => " + nfg.getName() + s);
					return (FgNetworkIP) fg;
				}
			}
		}
		return null;
	}

	// create a Fortigate network object with our name convention
	protected  FgNetworkIP createFortigateAddressRange(IPRangeable ipRangeable) {
		String name = null;

		// host
		if (ipRangeable.isHost()) {
			name = "M_" + ipRangeable.toNetString("s");
		}

		// network and mask
		if (!ipRangeable.isHost() && ipRangeable.isNetwork()) {
			var ss = ipRangeable.getIpFirst().toString("s").split("\\.");
			var net = "";
			var c = 3;
			for (int i = 3; i >= 0; i--) {
				if (ss[c].equals("0")) c--; else break;
			}
			if (c < 0) net = "0";
			if (c == 0) net = ss[0];
			if (c == 1) net = ss[0] + "." + ss[1];
			if (c == 2) net = ss[0] + "." + ss[1] + "." + ss[2];
			if (c == 3) net = ss[0] + "." + ss[1] + "." + ss[2] + "." + ss[3];
			var prefixLength = ipRangeable.getIpFirst().getPrefixLen();
			if (prefixLength != 8 && prefixLength != 16 && prefixLength != 24) {
				net = net + "_" + prefixLength;
			}

			name = "R_" + net;
		}

		// other => range of addresses
		if (name == null) {
			var sb = ipRangeable.getIpFirst().toString("s").split(":");
			var ssb = ipRangeable.getIpLast().toString("s").split(":");
			name = "M_" + ipRangeable.getIpFirst().toString("s") + "-";
			String subname = "";
			if (!ssb[0].equals(sb[0]))
				subname = ssb[0] + "." + ssb[1] + "." + ssb[2] + "." + ssb[3];
			else if (!ssb[1].equals(sb[1]))
				subname = ssb[1] + "." + ssb[2] + "." + ssb[3];
			else if (!ssb[2].equals(sb[2]))
				subname = ssb[2] + "." + ssb[3];
			else if (!ssb[3].equals(sb[3]))
				subname = ssb[3] ;
			name = name + subname;
		}

		var fgIP = new FgNetworkIP(name, name, name, null, ipRangeable, null);
		storeFgNetwork(fgIP);
		generateCLIAddress(fgIP);
		return fgIP;
	}

	private void storeFgNetwork(FgNetworkObject fgNetworkObject) {
		var name = fgNetworkObject.getName();
		var check = fgNetworks.get(name);
		if (check != null) {
			throw new JtaclPfToFortiException("conflict! PF: " + fgNetworkObject.toString() + " --!!-- " + fortigate.getName() + ": " + check.toString());
		}
		fgNetworks.put(fgNetworkObject.getName(), fgNetworkObject);
		fgCreatedNetworks.put(fgNetworkObject.getName(), fgNetworkObject);
		logOut("NETWORK create: " + fgNetworkObject.getName() + " " + fgNetworkObject.toString());
	}

	private void storeFgService(FgTcpUdpSctpService fgService) {
		var name = fgService.getName();
		var check = fgServices.get(name);
		if (check != null) {
			throw new JtaclPfToFortiException("conflict! PF: " + fgService.toString() + " --!!-- " + fortigate.getName() + ": " + check.toString());
		}
		fgServices.put(fgService.getName(), fgService);
		fgCreatedServices.put(fgService.getName(), fgService);
		generateCLIService(fgService);
		logOut("SERVICE create: " + fgService.getName() + " " + fgService.toString());
	}

	private void generateCLIExternalResource(FgNetworkExternalResource resource) {

		printLnFile(cliFileExternalResource, "    edit " + quote(resource.getName()));
		printLnFile(cliFileExternalResource, "        set type address ");
		printLnFile(cliFileExternalResource, "        set comments " + LSFW_COMMENT);
		printLnFile(cliFileExternalResource, "        set resource " + quote(resource.getResource()));
		printLnFile(cliFileExternalResource, "        set user-agent " + quote("curl/7.58.0"));
		printLnFile(cliFileExternalResource, "    next");
	}

	private void generateCLIFwPolicy(String name, FgFwRuleAction action
			, List<String> srcIntf
			, List<FgNetworkObject> sourceAddresses
		 	, boolean negateSourceAddresses
			, List<FgNetworkObject> dstAddresses
			, boolean negateDstAddresses
			, List<FgService> services
			, String context
			, String ruleText) {

		//
		printLnFile(cliFileRules);
		printLnFile(cliFileRules, "    #### " + context);
		// rule number
		//printLnFile(cliFileRules, "    edit " + name);
		printLnFile(cliFileRules, "    edit 0");


		// source interfaces
		var ssrcint = quote("any");
		if (!srcIntf.isEmpty()) {
			ssrcint = "";
			for (String s: srcIntf) {
				ssrcint += quote("PFINTERFACE_" + s) + " ";
			}
		}
		printLnFile(cliFileRules, "        set scrintf " + ssrcint);

		// destination interfaces
		var sdestint = quote("any");
		printLnFile(cliFileRules, "        set dstintf " + sdestint);

		// action
		if (action == FgFwRuleAction.ACCEPT) {
			printLnFile(cliFileRules, "        set action accept");
		}

		// source addresses
		var ssrcaddr = "";
		for (FgNetworkObject fo: sourceAddresses) {
			ssrcaddr += quote(fo.getName()) + " ";
		}
		printLnFile(cliFileRules, "        set srcaddr " + ssrcaddr);
		if (negateSourceAddresses) {
			printLnFile(cliFileRules, "        set srcaddr-negate " + "enable");
		}

		// destination addresses
		var sdstaddr = "";
		for (FgNetworkObject fo: dstAddresses) {
			sdstaddr += quote(fo.getName()) + " ";
		}
		printLnFile(cliFileRules, "        set dstaddr " + sdstaddr);
		if (negateDstAddresses) {
			printLnFile(cliFileRules, "        set dstaddr-negate " + "enable");
		}

		printLnFile(cliFileRules, "        set schedule \"always\"");

		// services
		var sservices = "";
		for (FgService fs: services) {
			sservices += quote(fs.getName()) + " ";
		}
		if (!services.isEmpty()) printLnFile(cliFileRules, "        set service " + sservices);

		printLnFile(cliFileRules, "        set comments " + quote(LSFW_COMMENT + " " + ruleText));
		printLnFile(cliFileRules, "    next");
	}

	private void generateCLIService(FgTcpUdpSctpService service) {
		printLnFile(cliFileServices, "    edit " + quote(service.getName()));
		if (service.isTcp()) {
			var firstPort = service.getTcpPortsSpec().get(0).getDestPorts().getRanges().get(0).getFirstPort();
			var lastPort = service.getTcpPortsSpec().get(0).getDestPorts().getRanges().get(0).getLastPort();
			var s = firstPort == lastPort ? firstPort : firstPort + "-" + lastPort;
			printLnFile(cliFileServices, "        set tcp-portrange " + s);
		}
		if (service.isUdp()) {
			var firstPort = service.getUdpPortsSpec().get(0).getDestPorts().getRanges().get(0).getFirstPort();
			var lastPort = service.getUdpPortsSpec().get(0).getDestPorts().getRanges().get(0).getLastPort();
			var s = firstPort == lastPort ? firstPort : firstPort + "-" + lastPort;
			printLnFile(cliFileServices, "        set udp-portrange " + s);
		}
		printLnFile(cliFileServices, "        set session-ttl 8101");
		printLnFile(cliFileServices, "        set comment " + LSFW_COMMENT);
		printLnFile(cliFileServices, "    next");
	}

	private void generateCLIAddress(FgNetworkIP ip) {
		var range = ip.getIpRange();

		printLnFile(cliFileAddresses, "    edit " + quote(ip.getName()));
		if (range.isNetwork()) {
			var netmask = IP.prefixLenToNetmask(range.getIpFirst().getPrefixLen(), IPversion.IPV4);
			var smask = IP.ipv4ToStrings(netmask, 32)[0];
			printLnFile(cliFileAddresses, "        set subnet " + range.getIpFirst().getIpFirst().toString("s") + " " + smask);
		} else {
			// range
			printLnFile(cliFileAddresses, "        set start-ip " + range.getIpFirst().toString("s"));
			printLnFile(cliFileAddresses, "        set end-ip " + range.getIpLast().toString("s"));
		}
		printLnFile(cliFileAddresses, "        set comment " + LSFW_COMMENT);
		printLnFile(cliFileAddresses, "    next");
	}

	private void generateCLIAddressesGroups(FgNetworkGroup group) {

		printLnFile(cliFileAddressesGroup, "    edit " + quote(group.getName()));
		String members = "";
		for (FgNetworkObject o: group.getBaseObjects().values()) members += quote(o.getName()) + " ";
		printLnFile(cliFileAddressesGroup, "        set member " + members);
		printLnFile(cliFileAddressesGroup, "        set comment " + LSFW_COMMENT);
		printLnFile(cliFileAddressesGroup, "    next");
	}

	private static void printFile(BufferedWriter writer, String txt) {
		try {
			writer.write(txt);
			writer.flush();
		} catch (IOException e) {
			throw new JtaclRuntimeException("cannot write to file");
		}
	}

	private static void printLnFile(BufferedWriter writer, String txt) {
		printFile(writer, txt);
		try {
			writer.newLine();
			writer.flush();
		} catch (IOException e) {
			throw new JtaclRuntimeException("cannot write to file");
		}
	}

	private static void printLnFile(BufferedWriter writer) {
		printLnFile(writer, "");
	}

	private static String quote(String s) {
		return "\"" + s.replaceAll("\"", "") + "\"";
	}

}
