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
import fr.univrennes1.cri.jtacl.lib.ip.*;

import java.util.List;

public class FgTcpUdpSctpService extends FgAddressService {

    private boolean _isUDP;
    private boolean _isTCP;
    private boolean _isSCTP;
    private PortSpec _tcpSourcePortSpec;
    private PortSpec _tcpPortSpec;
    private PortSpec _udpSourcePortSpec;
    private PortSpec _udpPortSpec;
    private PortSpec _sctpSourcePortSpec;
    private PortSpec _sctpPortSpec;

    /*
	 * TCP flags / TCP flagsSet
	 */
	private TcpFlags _tcpFlags = new TcpFlags("S");
	private TcpFlags _tcpFlagSet = new TcpFlags("SA");


    public FgTcpUdpSctpService(String name, String originKey, String comment
            , List<IPRangeable> ipRanges
            , String fqdn
            , PortSpec udpSourcePortSpec
            , PortSpec udpPortSpec
            , PortSpec tcpSourcePortSpec
            , PortSpec tcpPortSpec
            , PortSpec sctpSourcePortSpec
            , PortSpec sctpPortSpec) {

        super(name, originKey, comment, ipRanges, fqdn, FgServiceType.TCPUDPSCTP);

        _isTCP = tcpPortSpec != null || tcpSourcePortSpec != null;
        _isUDP = udpPortSpec != null || udpSourcePortSpec != null;
        _isSCTP = sctpPortSpec != null || sctpSourcePortSpec != null;
        _tcpPortSpec = tcpPortSpec;
        _tcpSourcePortSpec = tcpSourcePortSpec;
        _udpPortSpec = udpPortSpec;
        _udpSourcePortSpec = udpSourcePortSpec;
        _sctpPortSpec = sctpPortSpec;
        _sctpSourcePortSpec = sctpSourcePortSpec;
    }

    public PortSpec getUdpSourcePortSpec() {return _udpSourcePortSpec;}
    public PortSpec getUdpPortSpec() {return _udpPortSpec;}
    public PortSpec getTcpSourcePortSpec() {return _tcpSourcePortSpec;}
    public PortSpec getTcpPortSpec() {return _tcpPortSpec;}
    public PortSpec getSctpSourcePortSpec() {return _sctpSourcePortSpec;}
    public PortSpec getSctpPortSpec() {return _sctpPortSpec;}
    public boolean isUdp() {return _isUDP;}
    public boolean isTcp() {return _isTCP;}
    public boolean isSctp() {return _isSCTP;}

    public boolean hasUdpSourcePortSpec() {return _udpSourcePortSpec != null;}
    public boolean hasUdpPortSpec() {return _udpPortSpec != null;}
    public boolean hasTcpSourcePortSpec() {return _tcpSourcePortSpec != null;}
    public boolean hasTcpPortSpec() {return _tcpPortSpec != null;}
    public boolean hasSctpSourcePortSpec() {return _sctpSourcePortSpec != null;}
    public boolean hasSctpPortSpec() {return _sctpPortSpec != null;}


    @Override
    public String toString() {
        String s = "";
        String p = "";
        if (_isUDP) {
            if (hasUdpSourcePortSpec()) s += ", UDP src. ports=" + _udpSourcePortSpec;
            if (hasUdpPortSpec()) s += ", UDP dest. ports=" + _udpPortSpec;
            p = (_isTCP || _isSCTP) ? "UDP/" : "UDP";
        }
        if (_isTCP) {
            if (hasTcpSourcePortSpec()) s += ", TCP src. ports=" + _tcpSourcePortSpec;
            if (hasTcpPortSpec()) s += ", TCP dest. ports=" + _tcpPortSpec;
            p += (_isSCTP) ? "TCP/": "TCP";
        }
        if (_isSCTP) {
            if (hasSctpSourcePortSpec()) s += ", SCTP src. ports=" + _sctpSourcePortSpec;
            if (hasSctpPortSpec()) s += ", SCTP dest. ports=" + _sctpPortSpec;
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
            mres = matchPorts(request, _tcpSourcePortSpec, _tcpPortSpec);
            switch (mres) {
                case ALL: all++; break;
                case NOT: break;
                case MATCH: may++; break;
            }
        }
        if (testUDP && _isUDP) {
            mres = matchPorts(request, _udpSourcePortSpec, _udpPortSpec);
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

	protected MatchResult matchPorts(ProbeRequest request, PortSpec sourcePort, PortSpec destPort) {

		/*
		 * source port
		 */
		PortSpec port = request.getSourcePort();
		int sourceMay = 0;
		MatchResult mres = MatchResult.ALL;
		if (port != null && sourcePort != null) {
			mres = sourcePort.matches(port);
			/*
			 * does not match at all
			 */
			if (mres == MatchResult.NOT) {
				return MatchResult.NOT;
			}
		}
		if (mres != MatchResult.ALL)
			sourceMay++;

		/*
		 * destination port
		 */
		port = request.getDestinationPort();
		int destMay = 0;
		mres = MatchResult.ALL;
		if (port != null && destPort != null) {
			mres = destPort.matches(port);
			/*
			 * does not match at all
			 */
			if (mres == MatchResult.NOT) {
				return MatchResult.NOT;
			}
		}
		if (mres != MatchResult.ALL)
			destMay++;
		if (sourceMay == 0 && destMay == 0) {
			return MatchResult.ALL;
		}

		return MatchResult.MATCH;
    }
}