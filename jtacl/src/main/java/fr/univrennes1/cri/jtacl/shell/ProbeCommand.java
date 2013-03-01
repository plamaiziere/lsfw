/*
 * Copyright (c) 2012, Universite de Rennes 1
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

import fr.univrennes1.cri.jtacl.core.exceptions.JtaclParameterException;
import fr.univrennes1.cri.jtacl.core.monitor.Monitor;
import fr.univrennes1.cri.jtacl.core.network.IfaceLink;
import fr.univrennes1.cri.jtacl.core.network.IfaceLinks;
import fr.univrennes1.cri.jtacl.core.probing.ProbeOptions;
import fr.univrennes1.cri.jtacl.core.probing.ProbeRequest;
import fr.univrennes1.cri.jtacl.core.probing.ProbeTcpFlags;
import fr.univrennes1.cri.jtacl.core.probing.Probing;
import fr.univrennes1.cri.jtacl.core.topology.NetworkLinks;
import fr.univrennes1.cri.jtacl.core.topology.Topology;
import fr.univrennes1.cri.jtacl.lib.ip.IPIcmp;
import fr.univrennes1.cri.jtacl.lib.ip.IPIcmp4;
import fr.univrennes1.cri.jtacl.lib.ip.IPIcmp6;
import fr.univrennes1.cri.jtacl.lib.ip.IPIcmpEnt;
import fr.univrennes1.cri.jtacl.lib.ip.IPNet;
import fr.univrennes1.cri.jtacl.lib.ip.IPProtocols;
import fr.univrennes1.cri.jtacl.lib.ip.IPversion;
import fr.univrennes1.cri.jtacl.lib.ip.PortSpec;
import fr.univrennes1.cri.jtacl.lib.ip.Protocols;
import fr.univrennes1.cri.jtacl.lib.ip.ProtocolsSpec;
import fr.univrennes1.cri.jtacl.lib.ip.TcpFlags;
import fr.univrennes1.cri.jtacl.lib.misc.StringsList;
import java.net.UnknownHostException;

/**
 * Shell probe command.
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class ProbeCommand {

	protected Monitor _monitor = Monitor.getInstance();
	protected ProbeRequest _request;
	protected IfaceLink _ilink;
	protected Probing _probing;
	protected IPNet _sourceAddress;
	protected IPNet _destinationAddress;

	public void buildRequest(ProbeCommandTemplate probeCmd) {

		boolean testMode = probeCmd.getProbeExpect() != null;
		boolean learnMode = probeCmd.getProbeOptLearn();
		boolean silent = testMode || learnMode;

		IPversion ipVersion;
		if (probeCmd.getProbe6flag())
			ipVersion = IPversion.IPV6;
		else
			ipVersion = IPversion.IPV4;

		String sSourceAddress = probeCmd.getSrcAddress();
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
			} catch (UnknownHostException ex1) {
				throw new JtaclParameterException("Error in source address: " +
						sSourceAddress + " " +  ex1.getMessage());
			}
		}

		String sDestAddress = probeCmd.getDestAddress();
		IPNet destAddress;
		try {
			destAddress = new IPNet(sDestAddress);
		} catch (UnknownHostException ex) {
			try {
				// not an IP try to resolve as a host.
				destAddress = IPNet.getByName(sDestAddress, ipVersion);
			} catch (UnknownHostException ex1) {
				throw new JtaclParameterException("Error in destination address: " +
					 sDestAddress + ex1.getMessage());
			}
		}

		/*
		 * Check address family
		 */
		if (!sourceAddress.sameIPVersion(destAddress)) {
			throw new JtaclParameterException(
					"Error: source address and destination address" +
					" must have the same address family");
		}

		/*
		 * We can specify where we want to inject the probes.
		 */
		IfaceLinks ilinks;
		if (probeCmd.getEquipments() != null) {
			ilinks = ShellUtils.getIfaceLinksByEquipmentSpec(sourceAddress,
					probeCmd.getEquipments());
			// error
			if (ilinks == null)
				throw new JtaclParameterException("no links found");
		} else {
			/*
			 * try to find a network link that matches the source IP address.
			 */
			NetworkLinks nlinks;
			Topology topology = _monitor.getTopology();
			if (!equalSourceAddress) {
				nlinks = topology.getNetworkLinksByIP(sourceAddress);
			} else {
				nlinks = topology.getNetworkLinksByIP(sourceAddress.hostAddress());
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
					ilinks = ShellUtils.getIfaceLinksByEquipmentSpec(
							sourceAddress, defaultEquipment);
					// error
					if (ilinks == null)
						throw new JtaclParameterException("no links found");
				} else {
					throw new JtaclParameterException("No network matches");
				}
			} else {
				if (nlinks.size() > 1) {
					throw new JtaclParameterException(
						"Too many networks match this source IP address");
				}
				ilinks = nlinks.get(0).getIfaceLinks();
			}
		}

		if (ilinks.isEmpty()) {
			throw new JtaclParameterException("No link found");
		}

		if (ilinks.size() > 1) {
			throw new JtaclParameterException("Too many links");
		}

		/*
		 * build the probe request
		 */
		String sprotocol = probeCmd.getProtoSpecification();
		String sportSource = probeCmd.getPortSource();
		String sportDest = probeCmd.getPortDest();

		IPProtocols ipProtocols = IPProtocols.getInstance();
		Integer protocol;

		ProbeRequest request = new ProbeRequest();
		if (sprotocol != null) {
			protocol = ipProtocols.protocolLookup(sprotocol);
			if (protocol.intValue() == -1)  {
				throw new JtaclParameterException(
					"unknown protocol: " + sprotocol);
			}
			ProtocolsSpec protocols = new ProtocolsSpec();
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
					protocols.add(Protocols.IP);
				else
					protocols.add(Protocols.IPV6);

				/*
				 * services lookup, by default "any"
				 */
				if (sportSource == null)
					sportSource = "any";
				PortSpec sourceSpec = ShellUtils.parsePortSpec(
						sportSource, sprotocol);
				request.setSourcePort(sourceSpec);

				if (sportDest == null)
					sportDest = "any";
				PortSpec destSpec = ShellUtils.parsePortSpec(sportDest, sprotocol);
				request.setDestinationPort(destSpec);

				/*
				 * tcp flags
				 */
				StringsList tcpFlags = probeCmd.getTcpFlags();
				if (tcpFlags != null) {
					if (sprotocol.equalsIgnoreCase("udp")) {
						throw new JtaclParameterException(
							"TCP flags not allowed for UDP!");
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
						if (!ShellUtils.checkTcpFlags(flag)) {
							throw new JtaclParameterException(
								"invalid TCP flags: " + flag);
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
				protocols.add(Protocols.TCP);
				protocols.add(Protocols.UDP);
				if (!sprotocol.equalsIgnoreCase("ipv6"))
					protocols.add(Protocols.ICMP);
				else
					protocols.add(Protocols.ICMP6);
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
						throw new JtaclParameterException(
							"unknown icmp-type or message: " + sportSource);
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
		options.setNoAction(probeCmd.getProbeOptNoAction());
		options.setQuickDeny(probeCmd.getProbeOptQuickDeny());
		options.setState(probeCmd.getProbeOptState());

		_ilink = ilinks.get(0);
		_request = request;
		_sourceAddress = sourceAddress;
		_destinationAddress = destAddress;
	}

	public void runCommand() {
		/*
		 * probe
		 */
		_monitor.resetProbing();
		_monitor.newProbing(_ilink, _sourceAddress,
				_destinationAddress, _request);
		_probing = _monitor.startProbing();
	}

	public ProbeRequest getRequest() {
		return _request;
	}

	public IfaceLink getIlink() {
		return _ilink;
	}

	public Probing getProbing() {
		return _probing;
	}

	public IPNet getSourceAddress() {
		return _sourceAddress;
	}

	public IPNet getDestinationAddress() {
		return _destinationAddress;
	}

}
