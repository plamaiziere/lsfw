/*
 * Copyright (c) 2013 - 2018, Universite de Rennes 1
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

package fr.univrennes1.cri.jtacl.equipments.checkpointR80;

import fr.univrennes1.cri.jtacl.core.probing.MatchResult;
import fr.univrennes1.cri.jtacl.lib.ip.IPRange;
import fr.univrennes1.cri.jtacl.lib.ip.IPRangeable;

/**
 * Checkpoint IP network range
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */

public class CpNetworkRange extends CpNetworkObject {

	/* ip range */
	protected IPRange _ipRange;

    /* ipv6 range */
    protected IPRange _ipRange6;

	/**
	 * Construct a new checkpoint IP network range object
	 * @param name object name name
	 * @param className checkpoint class name
	 * @param comment comment
     * @param uid Objet's uid
	 * @param ipRange ip range address
     * @param ipRange6 ipv6 range address
	 */
	public CpNetworkRange(String name,	String className, String comment, String uid,
			IPRange ipRange, IPRange ipRange6)  {

		super(name, className, comment, uid, CpNetworkType.RANGE);
		_ipRange = ipRange;
		_ipRange6 = ipRange6;
	}

	public IPRange getIpRange() {
		return _ipRange;
	}

    public IPRange getIpRange6() {
        return _ipRange6;
    }

    @Override
	public String toString() {
		String s = _name + ", " + _className + ", " + _comment + ", " +  _type;
		if (_ipRange != null) {
            s += ", IPRange=" + _ipRange;
        }
        if (_ipRange6 != null) {
            s += ", IPRange6=" + _ipRange6;
        }
        return s;
	}

	@Override
	public MatchResult matches(IPRangeable ip) {
        if (ip.isIPv4()) {
            if (_ipRange == null)
                return MatchResult.NOT;
            if (_ipRange.contains(ip))
                return MatchResult.ALL;
            if (_ipRange.overlaps(ip))
                return MatchResult.MATCH;
        }
        if (ip.isIPv6()) {
            if (_ipRange6 == null)
                return MatchResult.NOT;
            if (_ipRange6.contains(ip))
                return MatchResult.ALL;
            if (_ipRange6.overlaps(ip))
                return MatchResult.MATCH;
        }
        return MatchResult.NOT;
	}
}
