/*
 * Copyright (c) 2012, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
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
import fr.univrennes1.cri.jtacl.lib.ip.IPIcmp;
import fr.univrennes1.cri.jtacl.lib.ip.IPIcmp4;
import fr.univrennes1.cri.jtacl.lib.ip.IPIcmp6;
import fr.univrennes1.cri.jtacl.lib.ip.IPIcmpEnt;
import fr.univrennes1.cri.jtacl.lib.ip.IPNet;
import fr.univrennes1.cri.jtacl.lib.ip.IPProtocols;
import fr.univrennes1.cri.jtacl.lib.ip.IPRange;
import fr.univrennes1.cri.jtacl.lib.ip.IPRangeable;
import fr.univrennes1.cri.jtacl.lib.ip.IPversion;
import fr.univrennes1.cri.jtacl.lib.ip.PortSpec;
import fr.univrennes1.cri.jtacl.lib.ip.Protocols;
import fr.univrennes1.cri.jtacl.lib.ip.ProtocolsSpec;
import fr.univrennes1.cri.jtacl.lib.ip.TcpFlags;
import fr.univrennes1.cri.jtacl.lib.misc.StringsList;

import java.net.UnknownHostException;

/**
 * Shell probe command.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class ProbeCommand {

    protected Monitor _monitor = Monitor.getInstance();
    protected ProbeRequest _request;
    protected IfaceLink _ilink;
    protected Probing _probing;
    protected IPRangeable _sourceAddress;
    protected IPRangeable _destinationAddress;

    protected class ProbeAddresses {
        public IPRangeable sourceAddress; // source
        public IPRangeable destAddress;   // destination
        public IPNet linkSourceAddress;   // where to inject
    }

    protected enum SearchLinkBy {
        NONE,
        EQMT,
        AUTOLINK,
        AUTO
    }

    public void buildRequest(ProbeCommandTemplate probeCmd) {

        // addresses
        ProbeAddresses paddr = parseAddresses(probeCmd);

        /*
         * We can specify where we want to inject the probes.
         */
        NetworkLinks nlinks = _monitor.getTopology().getNetworkLinksByIP(paddr.linkSourceAddress);
        IfaceLinks ilinks = null;

        String onEquipment = probeCmd.getEquipments();
        SearchLinkBy sby = SearchLinkBy.NONE;
        if (onEquipment != null) {
            switch (onEquipment.toLowerCase()) {
                case "autolink":
                    sby = SearchLinkBy.AUTOLINK;
                    break;
                case "auto":
                    sby = SearchLinkBy.AUTO;
                    break;
                default:
                    sby = SearchLinkBy.EQMT;
            }
        }

        switch (sby) {
            case NONE:
                ilinks = ShellUtils.searchIfacelinksByAddress(nlinks, paddr.linkSourceAddress);
                break;

            case EQMT:
                ilinks = ShellUtils.getIfaceLinksByEquipmentSpec(paddr.linkSourceAddress.nearestNetwork(),
                        onEquipment);
                break;

            case AUTO:
                ilinks = ShellUtils.getLoopBackIfaceLinksByIP(paddr.linkSourceAddress);
                if (ilinks != null) break; // if no links continue as AUTOLINK

            case AUTOLINK:
                ilinks = ShellUtils.searchIfacelinksByAddress(nlinks, paddr.linkSourceAddress);
                if (ilinks.size() > 1) {
                    /*
                     * try to find a suitable link to reach the destination
                     */
                    if (nlinks.size() > 1)
                        throw new JtaclParameterException("Too many networks match this source IP address");

                    IfaceLink ilink = ShellUtils.findOnRouteIfaceLink(nlinks.get(0), paddr.destAddress);
                    if (ilink != null) {
                        ilinks = new IfaceLinks();
                        ilinks.add(ilink);
                    }
                }
                break;
        }

        if (ilinks == null || ilinks.isEmpty()) {
            throw new JtaclParameterException("No link found");
        }

        if (ilinks.size() > 1) {
            throw new JtaclParameterException("Too many links found");
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
            if (protocol == -1) {
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
                if (paddr.sourceAddress.isIPv4())
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
                    for (String flag : tcpFlags) {
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
        _sourceAddress = paddr.sourceAddress;
        _destinationAddress = paddr.destAddress;
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

    public IPRangeable getSourceAddress() {
        return _sourceAddress;
    }

    public IPRangeable getDestinationAddress() {
        return _destinationAddress;
    }

    protected ProbeAddresses parseAddresses(ProbeCommandTemplate probeCmd) {

        IPversion ipVersion = probeCmd.getProbe6flag() ? IPversion.IPV6 : IPversion.IPV4;

        String sSourceAddress = probeCmd.getSrcAddress();
        ProbeAddresses paddr = new ProbeAddresses();

        /*
         * =host or =addresse in source addresse
         */
        boolean equalSourceAddress = sSourceAddress.startsWith("=");
        if (equalSourceAddress) {
            sSourceAddress = sSourceAddress.substring(1);
            paddr.sourceAddress = parseRangeAddress(sSourceAddress, ipVersion);
            paddr.linkSourceAddress = parseIpAddress(sSourceAddress, ipVersion).hostAddress();
        } else {
            paddr.sourceAddress = parseRangeAddress(sSourceAddress, ipVersion);
            paddr.linkSourceAddress = parseRangeAddress(sSourceAddress, ipVersion).nearestNetwork();
        }

        String sDestAddress = probeCmd.getDestAddress();
        paddr.destAddress = parseRangeAddress(sDestAddress, ipVersion);

        /*
         * Check address family
         */
        if (!paddr.sourceAddress.sameIPVersion(paddr.destAddress)) {
            throw new JtaclParameterException(
                    "Error: source address and destination address must have the same address family");
        }
        return paddr;
    }

    protected IPRangeable parseRangeAddress(String address, IPversion ipVersion) {
        IPRangeable addr;
        try {
            addr = new IPRange(address);
        } catch (UnknownHostException ex) {
            try {
                // not an IP try to resolve as a host.
                addr = IPNet.getByName(address, ipVersion);
            } catch (UnknownHostException ex1) {
                throw new JtaclParameterException("Error in probe address: " +
                        address + " " + ex1.getMessage());
            }
        }
        return addr;
    }

    protected IPNet parseIpAddress(String address, IPversion ipVersion) {
        IPNet addr;
        try {
            addr = new IPNet(address);
        } catch (UnknownHostException ex) {
            try {
                // not an IP try to resolve as a host.
                addr = IPNet.getByName(address, ipVersion);
            } catch (UnknownHostException ex1) {
                throw new JtaclParameterException("Error in probe address: " +
                        address + " " + ex1.getMessage());
            }
        }
        return addr;
    }
}
