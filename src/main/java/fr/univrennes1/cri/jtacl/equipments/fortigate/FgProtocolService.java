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
import fr.univrennes1.cri.jtacl.lib.ip.IPRangeable;
import fr.univrennes1.cri.jtacl.lib.ip.ProtocolsSpec;

import java.util.List;

public class FgProtocolService extends FgAddressService {

    Integer _protocol;

    public FgProtocolService(String name, String originKey, String comment
            , List<IPRangeable> ipRanges
            , String fqdn
            , Integer protocol) {

        super(name, originKey, comment, ipRanges, fqdn, FgServiceType.PROTOCOL);
        _protocol = protocol;
    }

    @Override
    public FgServicesMatch matches(Probe probe) {
        ProbeRequest request = probe.getRequest();
        ProtocolsSpec proto = request.getProtocols();
        FgServicesMatch servicesMatch = new FgServicesMatch();
        if (proto == null) {
            servicesMatch.setMatchResult(MatchResult.ALL);
            servicesMatch.add(new FgServiceMatch(this, MatchResult.ALL));
            return servicesMatch;
        }
        MatchResult mres = proto.matches(_protocol) ;
        if (mres == MatchResult.ALL) {
            mres = matchAddress(probe.getDestinationAddress());
        }
		if (mres == MatchResult.NOT) {
		    servicesMatch.setMatchResult(MatchResult.NOT);
		    return servicesMatch;
        }
		servicesMatch.setMatchResult(mres);
		servicesMatch.add(new FgServiceMatch(this, mres));
		return servicesMatch;
    }

    @Override
    public String toString() {
        String s = super.toString();
        s += ", protocol=" + _protocol;
        return s;
    }
}
