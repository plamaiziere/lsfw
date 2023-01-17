/*
 * Copyright (c) 2013 - 2020, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.equipments.fortigate;

import fr.univrennes1.cri.jtacl.core.probing.MatchResult;
import fr.univrennes1.cri.jtacl.core.probing.Probe;
import fr.univrennes1.cri.jtacl.core.probing.ProbeRequest;
import fr.univrennes1.cri.jtacl.lib.ip.*;

import java.util.List;

public class FgIcmpService extends FgAddressService {
    private IPIcmpEnt _icmp;
    private AddressFamily _af;
    public boolean hasIcmp() { return _icmp != null; };

    public FgIcmpService(String name, String originKey, String comment
            , List<IPRangeable> ipRanges
            , String fqdn
            , AddressFamily af
            , Integer icmpType
            , Integer icmpCode) {

        super(name, originKey, comment, ipRanges, fqdn, FgServiceType.ICMP);
        if (icmpType != null)
            _icmp = new IPIcmpEnt(name, icmpType, icmpCode);
        else _icmp = null;
        _af = af;
    }

    @Override
    public FgServicesMatch matches(Probe probe) {
        ProbeRequest request = probe.getRequest();
		ProtocolsSpec reqProto = request.getProtocols();
		FgServicesMatch servicesMatch = new FgServicesMatch();

		/*
		 * address family
		 */
		if (_af == AddressFamily.INET && !reqProto.contains(Protocols.ICMP)) {
			servicesMatch.setMatchResult(MatchResult.NOT);
			return servicesMatch;
		}

		if (_af == AddressFamily.INET6 && !reqProto.contains(Protocols.ICMP6)) {
			servicesMatch.setMatchResult(MatchResult.NOT);
			return servicesMatch;
		}

		/* icmp and address */
		MatchResult mres = matchIcmp(request);
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

    protected MatchResult matchIcmp(ProbeRequest request) {

        /*
         * all icmp
         */
        if (!hasIcmp()) return MatchResult.ALL;

		/*
		 * icmp type and code
		 */
		Integer icmpType = request.getSubType();
		if (icmpType == null) {
		    return MatchResult.ALL;
		}

		if (icmpType != _icmp.getIcmp()) {
			return MatchResult.NOT;
		}

		Integer icmpCode = request.getCode();
		if (icmpCode == null) {
			return MatchResult.ALL;
		}

		if (icmpCode == _icmp.getCode()) {
		    return MatchResult.ALL;
		}

		return MatchResult.NOT;
	}

    @Override
    public String toString() {
        return super.toString() + ", " + _af + ", " + _icmp;
    }
}
