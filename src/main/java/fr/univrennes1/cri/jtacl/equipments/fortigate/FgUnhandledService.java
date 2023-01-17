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
import fr.univrennes1.cri.jtacl.lib.ip.IPRangeable;

import java.util.List;

/**
 * Fortigate service left unhandled by lsfw
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class FgUnhandledService extends FgAddressService {

	public FgUnhandledService(String name, String originKey, String comment, List<IPRangeable> ipRanges, String fqdn) {
		super(name, originKey, comment, ipRanges, fqdn, FgServiceType.UNHANDLED);
	}

	@Override
	public FgServicesMatch matches(Probe probe) {
    	FgServicesMatch servicesMatch = new FgServicesMatch();
    	MatchResult mres = matchAddress(probe.getDestinationAddress());
	    if (mres == MatchResult.NOT) {
	        servicesMatch.setMatchResult(MatchResult.NOT);
	        return servicesMatch;
        }

	    servicesMatch.setMatchResult(MatchResult.UNKNOWN);
		servicesMatch.add(new FgServiceMatch(this, MatchResult.UNKNOWN));
		return servicesMatch;
	}

}
