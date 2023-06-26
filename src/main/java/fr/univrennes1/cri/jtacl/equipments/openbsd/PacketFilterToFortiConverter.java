/*
 * Copyright (c) 2023, Universite de Rennes
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */
package fr.univrennes1.cri.jtacl.equipments.openbsd;

import fr.univrennes1.cri.jtacl.core.exceptions.JtaclParameterException;
import fr.univrennes1.cri.jtacl.core.exceptions.JtaclRuntimeException;
import fr.univrennes1.cri.jtacl.core.monitor.Monitor;
import fr.univrennes1.cri.jtacl.core.network.NetworkEquipment;
import fr.univrennes1.cri.jtacl.equipments.fortigate.FgFw;
import fr.univrennes1.cri.jtacl.equipments.fortigate.FgNetworkGroup;
import fr.univrennes1.cri.jtacl.equipments.fortigate.FgNetworkIP;
import fr.univrennes1.cri.jtacl.equipments.fortigate.FgNetworkObject;
import fr.univrennes1.cri.jtacl.equipments.fortigate.FgNetworkType;
import fr.univrennes1.cri.jtacl.equipments.fortigate.FgPortsSpec;
import fr.univrennes1.cri.jtacl.equipments.fortigate.FgService;
import fr.univrennes1.cri.jtacl.equipments.fortigate.FgServiceType;
import fr.univrennes1.cri.jtacl.equipments.fortigate.FgTcpUdpSctpService;
import fr.univrennes1.cri.jtacl.lib.ip.IP;
import fr.univrennes1.cri.jtacl.lib.ip.IPRange;
import fr.univrennes1.cri.jtacl.lib.ip.IPRangeable;
import fr.univrennes1.cri.jtacl.lib.ip.IPversion;
import fr.univrennes1.cri.jtacl.lib.ip.PortOperator;
import fr.univrennes1.cri.jtacl.lib.ip.PortRange;
import fr.univrennes1.cri.jtacl.lib.ip.PortSpec;
import fr.univrennes1.cri.jtacl.lib.ip.Protocols;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Converter from PacketFilter rules to Fortinet rules
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PacketFilterToFortiConverter {
	protected PacketFilter pf;
	protected FgFw fortigate;
	protected PrintStream output;
	protected HashMap<String, FgNetworkObject> fgNetworks;
	protected HashMap<String, FgNetworkObject> fgCreatedNetworks = new HashMap<>();
	protected HashMap<String, FgService> fgServices;
	protected HashMap<String, FgService> fgCreatedServices = new HashMap<>();
	protected BufferedWriter logFile;
	protected BufferedWriter cliFileAddresses;
	protected BufferedWriter cliFileAddressesGroup;
	protected BufferedWriter cliFileRules;

	public PacketFilterToFortiConverter(PacketFilter pf, String fortigateName, PrintStream output) {
		this.pf = pf;
		this.output = output;
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
		String d = "-" + LocalDateTime.now().toString();

		try {
			cliFileAddresses = new BufferedWriter(new FileWriter("lsfw_fortigate_addresses" + d));
			cliFileAddressesGroup = new BufferedWriter(new FileWriter("lsfw_fortigate_addresses_groups" + d));
			logFile = new BufferedWriter(new FileWriter("lsfw_fortigate_logs" + d));

		} catch (IOException e) {
			throw new JtaclParameterException("cannot open output files: " + e.getMessage());
		}
	}

	public void convert() {
		printLnFile(cliFileAddresses, "config firewall address");
		printLnFile(cliFileAddressesGroup, "config firewall addrgrp");

		for (PfGenericRule rule: pf._rootAnchor.getRules()) {
			try {
				if (! (rule instanceof PfRule)) continue;
				var pfrule = (PfRule) rule;
				logOut("RULE " + pfrule.getText());
				var fgNetObjIn = pfIpspecToFortigate(pfrule.getFromIpSpec());
				var fgNetObjOut = pfIpspecToFortigate(pfrule.getToIpSpec());
				if (pfrule.getFromPortSpec().size() > 0) {
					logOut(rule._configurationLine);
				}
				boolean udp = pfrule.getProtocols().contains(Protocols.UDP);
				boolean tcp = pfrule.getProtocols().contains(Protocols.TCP);
				pfPortSpecToFortigate(pfrule.getToPortSpec(), udp, tcp);
			} catch (JtaclPfToFortiException e) {
				logOut("!!!!!! " + e.getMessage());
			}

		}

		printLnFile(cliFileAddresses, "end");
		printLnFile(cliFileAddressesGroup, "end");
	}

	public void logOut(String message) {
		output.println(message);
		printLnFile(logFile, message);
	}
	
	public void pfPortSpecToFortigate(PfPortSpec pfPortSpec, boolean udp, boolean tcp) {
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

			}
		}
	}


	public FgTcpUdpSctpService findFortigateService(PortRange portRange, boolean udp, boolean tcp) {
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
			if ( (!udp | udpOk) && (!tcp || tcpOk) ) {
				return fgService;
			}
		}
		return null;
	}

	public List<FgNetworkObject> pfIpspecToFortigate (PfIpSpec ipSpec) {
		List<FgNetworkObject> rObj = new ArrayList<>();

		for (PfNodeHost pfNodeHost: ipSpec) {
			if (pfNodeHost.isAddrMask()) {
				var addr = new IPRange(pfNodeHost.getAddr().get(0));
				if (addr.isIPv4()) {
					var o = findFortigateNetworkIP(addr, fgNetworks);
					rObj.add(o != null ? o : createFortigateAddressRange(addr));
				}
			}
			if (pfNodeHost.isAddrRange()) {
				var addr = new IPRange(pfNodeHost.getAddr().get(0), pfNodeHost.getRangeAddr().get(1));
				if (addr.isIPv4()) {
					var o = findFortigateNetworkIP(addr, fgNetworks);
					rObj.add(o != null ? o : createFortigateAddressRange(addr));
				}
			}
			if (pfNodeHost.isAddrTable()) {
				String name = pfNodeHost.getTblName();
				PfTable table = pf._rootAnchor.findTable(name);
				// not persist
				if (table.getFileNames().isEmpty()) {
					var tableObj = pfIpspecToFortigate(table.getIpspec());
					if (!tableObj.isEmpty()) {
						var group = findFortigateNetworkGroup(tableObj, fgNetworks);
						if (group != null) {
							var s = fgCreatedNetworks.get(group.getName()) == null ? " (" + fortigate.getName() + ")" : " (created)";
							logOut("NETWORK table " + table.getName() + " => " + group.getName() + s);
						} else {
							var gname = "G_" + name;
							var fgGroup = new FgNetworkGroup(gname, gname, null, null);
							for (FgNetworkObject o : tableObj) {
								fgGroup.addBaseReference(o.getName(), o);
							}
							rObj.addAll(tableObj);
							rObj.add(fgGroup);
							storeFgNetwork(fgGroup);
							generateCLIAddressesGroups(fgGroup);
						}
					} else {
						logOut("NETWORK " + table.getName() + " is empty");
					}
				} else {
					// persist
					// todo
				}
			}
		}
		return rObj;
	}

	public FgNetworkGroup findFortigateNetworkGroup(List<FgNetworkObject> members, HashMap<String, FgNetworkObject> fgNetworksObjs) {
		for (FgNetworkObject fg: fgNetworksObjs.values()) {
			if (fg.getType() != FgNetworkType.GROUP) continue;
			var fgGroup = (FgNetworkGroup) fg;
			if (fgGroup.getExcludedObjects().size() > 0) continue;
			if (fgGroup.getBaseObjects().size() != members.size()) continue;
			boolean match = true;
			for (FgNetworkObject member: members) {
				if (fgGroup.getBaseObjects().get(member.getName()) == null) {
					match = false;
					break;
				}
			}
			if (match) {
				return fgGroup;
			}
		}
		return null;
	}

	public FgNetworkIP findFortigateNetworkIP(IPRange range, HashMap<String, FgNetworkObject> fgNetworksObjs) {
		for (FgNetworkObject fg: fgNetworksObjs.values()) {
			if (fg.getType() == FgNetworkType.IPRANGE) {
				var nfg = (FgNetworkIP)fg;
				var fgAddress = nfg.getIpRange();
				if (range.equals(fgAddress)) {
					var s = fgCreatedNetworks.get(nfg.getName()) == null ? " (" + fortigate.getName() + ")" : " (created)";
					logOut("NETWORK " + range.toNetString("::i") + " => " + nfg.getName() + s);
					return (FgNetworkIP) fg;
				}
			}
		}
		return null;
	}

	public FgNetworkIP createFortigateAddressRange(IPRangeable ipRangeable) {
		String name = null;
		if (ipRangeable.isHost()) {
			name = "M_" + ipRangeable.toNetString("s");
		}

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
			throw new JtaclPfToFortiException("conflict! PF: " + check.toString() + " --!!-- " + fortigate.getName() + ": " + fgNetworkObject.toString());
		}
		fgNetworks.put(fgNetworkObject.getName(), fgNetworkObject);
		fgCreatedNetworks.put(fgNetworkObject.getName(), fgNetworkObject);
		logOut("NETWORK create: " + fgNetworkObject.getName() + " " + fgNetworkObject.toString());
	}

	private void storeFgService(FgService fgService) {
		var name = fgService.getName();
		var check = fgServices.get(name);
		if (check != null) {
			throw new JtaclPfToFortiException("conflict! PF: " + check.toString() + " --!!-- " + fortigate.getName() + ": " + fgService.toString());
		}
		fgServices.put(fgService.getName(), fgService);
		fgCreatedServices.put(fgService.getName(), fgService);
		logOut("SERVICE create: " + fgService.getName() + " " + fgService.toString());
	}


	private void storeFgNetworks(List<FgNetworkObject> fgNetworkObjects) {
		for (FgNetworkObject f: fgNetworkObjects) storeFgNetwork(f);
	}

	private void generateCLIAddress(FgNetworkIP ip) {
		var range = ip.getIpRange();

		printLnFile(cliFileAddresses, "	edit " + "\"" + ip.getName() + "\"");
		if (range.isNetwork()) {
			var netmask = IP.prefixLenToNetmask(range.getIpFirst().getPrefixLen(), IPversion.IPV4);
			var smask = IP.ipv4ToStrings(netmask, 32)[0];
			printLnFile(cliFileAddresses, "		set subnet " + range.getIpFirst().getIpFirst().toString("s") + " " + smask);
		} else {
			// range
			printLnFile(cliFileAddresses, "		set start-ip " + range.getIpFirst().toString("s"));
			printLnFile(cliFileAddresses, "		set end-ip " + range.getIpLast().toString("s"));
		}
		printLnFile(cliFileAddresses, "	next");
	}

	public void generateCLIAddressesGroups(FgNetworkGroup group) {

		printLnFile(cliFileAddressesGroup, "	edit " + "\"" + group.getName() + "\"");
		String members = "";
		for (FgNetworkObject o: group.getBaseObjects().values()) members += "\"" + o.getName() + "\" ";
		printLnFile(cliFileAddressesGroup, "		set member " + members);
		printLnFile(cliFileAddressesGroup, "	next");
	}

	private static void printFile(BufferedWriter writer, String txt) {
		try {
			writer.write(txt);
		} catch (IOException e) {
			throw new JtaclRuntimeException("cannot write to file");
		}
	}

	private static void printLnFile(BufferedWriter writer, String txt) {
		printFile(writer, txt);
		try {
			writer.newLine();
		} catch (IOException e) {
			throw new JtaclRuntimeException("cannot write to file");
		}
	}
}
