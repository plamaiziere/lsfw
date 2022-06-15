/*
 * Copyright (c) 2013 - 2020, Universite de Rennes 1
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

package fr.univrennes1.cri.jtacl.equipments.fortigate;

import fr.univrennes1.cri.jtacl.core.probing.MatchResult;
import fr.univrennes1.cri.jtacl.core.probing.Probe;
import fr.univrennes1.cri.jtacl.core.probing.ProbeRequest;
import fr.univrennes1.cri.jtacl.core.probing.ProbeTcpFlags;
import fr.univrennes1.cri.jtacl.lib.ip.IPRangeable;
import fr.univrennes1.cri.jtacl.lib.ip.Protocols;
import fr.univrennes1.cri.jtacl.lib.ip.ProtocolsSpec;
import fr.univrennes1.cri.jtacl.lib.ip.TcpFlags;

import java.util.List;

public class FgTcpUdpSctpService extends FgAddressService {

    protected  boolean _isUDP;
    protected boolean _isTCP;
    protected boolean _isSCTP;

    List<FgPortsSpec> _tcpPortsSpecs;
    List<FgPortsSpec> _udpPortsSpecs;
    List<FgPortsSpec> _sctpPortsSpecs;

    /*
	 * TCP flags / TCP flagsSet
	 */
	private TcpFlags _tcpFlags = new TcpFlags("S");
	private TcpFlags _tcpFlagSet = new TcpFlags("SA");

    public FgTcpUdpSctpService(String name, String originKey, String comment
            , List<IPRangeable> ipRanges
            , String fqdn
            , List<FgPortsSpec> udpPortsSpecs
            , List<FgPortsSpec> tcpPortsSpecs
            , List<FgPortsSpec> sctpPortsSpecs) {

        super(name, originKey, comment, ipRanges, fqdn, FgServiceType.TCPUDPSCTP);

        _isTCP = tcpPortsSpecs != null;
        _isUDP = udpPortsSpecs != null;
        _isSCTP = sctpPortsSpecs != null;
        _udpPortsSpecs = udpPortsSpecs;
        _tcpPortsSpecs = tcpPortsSpecs;
        _sctpPortsSpecs = sctpPortsSpecs;
    }

    public List<FgPortsSpec> getUdpPortsSpec() {return _udpPortsSpecs;}
    public List<FgPortsSpec> getTcpPortsSpec() {return _tcpPortsSpecs;}
    public List<FgPortsSpec> getSctpPortsSpec() {return _sctpPortsSpecs;}
    public boolean isUdp() {return _isUDP;}
    public boolean isTcp() {return _isTCP;}
    public boolean isSctp() {return _isSCTP;}

    @Override
    public String toString() {
        String s = "";
        String p = "";
        if (_isUDP) {
            s += ", UDP: " + _udpPortsSpecs;
            p = (_isTCP || _isSCTP) ? "UDP/" : "UDP";
        }
        if (_isTCP) {
            s += ", TCP: " + _tcpPortsSpecs;
            p += (_isSCTP) ? "TCP/": "TCP";
        }
        if (_isSCTP) {
            s += ", SCTP: " + _sctpPortsSpecs;
            p += "SCTP";
        }
        return super.toString() + ", " + p + s;
    }

    @Override
    public FgServicesMatch matches(Probe probe) {

        ProbeRequest request = probe.getRequest();
		ProtocolsSpec reqProto = request.getProtocols();
		FgServicesMatch servicesMatch = new FgServicesMatch();
		boolean testTCP = reqProto.contains(Protocols.TCP) && _isTCP;
		boolean testUDP = reqProto.contains(Protocols.UDP) && _isUDP;

		/*
		 * protocol
		 */
		if (!testUDP && !testTCP) {
			servicesMatch.setMatchResult(MatchResult.NOT);
			return servicesMatch;
		}

		/*
		 * TCP flags TODO: à verifier
		 */
		ProbeTcpFlags reqFlags = request.getTcpFlags();

		if (_isTCP && reqFlags != null && !reqFlags.matchAllWithout(_tcpFlags, _tcpFlagSet)) {
			servicesMatch.setMatchResult(MatchResult.NOT);
			return servicesMatch;
		}

		MatchResult mres = MatchResult.NOT;
		int all = 0;
		int may = 0;
        if (testTCP && _isTCP) {
            mres = matchPorts(request, _tcpPortsSpecs);
            switch (mres) {
                case ALL: all++; break;
                case NOT: break;
                case MATCH: may++; break;
            }
        }
        if (testUDP && _isUDP) {
            mres = matchPorts(request, _udpPortsSpecs);
            switch (mres) {
                case ALL: all++; break;
                case NOT: break;
                case MATCH: may++; break;
            }
        }
        if (all > 0) {
            switch (matchAddress(probe.getDestinationAddress())) {
                case ALL: mres = MatchResult.ALL; break;
                case MATCH: mres = MatchResult.MATCH; break;
                case NOT: mres = MatchResult.NOT; break;
            }
            servicesMatch.setMatchResult(mres);
		    servicesMatch.add(new FgServiceMatch(this, mres));
		    return servicesMatch;
        }
        if (may > 0) {
            switch (matchAddress(probe.getDestinationAddress())) {
                case ALL: mres = MatchResult.MATCH; break;
                case MATCH: mres = MatchResult.MATCH; break;
                case NOT: mres = MatchResult.NOT; break;
            }
            servicesMatch.setMatchResult(mres);
		    servicesMatch.add(new FgServiceMatch(this, mres));
		    return servicesMatch;
        }
        servicesMatch.setMatchResult(MatchResult.NOT);
        servicesMatch.add(new FgServiceMatch(this, MatchResult.NOT));
	    return servicesMatch;
	}

	protected MatchResult matchPorts(ProbeRequest request, List<FgPortsSpec> portsSpecs) {
        int all = 0;
        int may = 0;

        for (FgPortsSpec portsSpec: portsSpecs) {
            MatchResult mres = portsSpec.matches(request);
            switch (mres) {
                case ALL: all++; break;
                case MATCH: may++; break;
            }
        }
        if (all > 0) return MatchResult.ALL;
        if (may > 0) return MatchResult.MATCH;
        return MatchResult.NOT;
    }
}
