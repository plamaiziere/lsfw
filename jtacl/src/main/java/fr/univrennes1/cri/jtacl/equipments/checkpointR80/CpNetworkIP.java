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
import fr.univrennes1.cri.jtacl.lib.ip.IPNet;
import fr.univrennes1.cri.jtacl.lib.ip.IPRange;
import fr.univrennes1.cri.jtacl.lib.ip.IPRangeable;

/**
 * Checkpoint IP network object
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */

public class CpNetworkIP extends CpNetworkObject {

	/* ip/network address */
	protected IPRange _ipRange;

    /* ipv6/network address */
    protected IPRange _ipRange6;

	protected boolean _allowBroadcast;

	/**
	 * Construct a new checkpoint IP network object
	 * @param name object name name
	 * @param className checkpoint class name
	 * @param comment comment
	 * @param ip ipv4/network address
     * @param ip6 ipv6/network address
	 * @param allowBroadcast true if the broadcast address is allowed.
	 */
	public CpNetworkIP(String name,	String className, String comment, String uid, IPNet ip, IPNet ip6,
			boolean allowBroadcast) {

		super(name, className, comment, uid, CpNetworkType.IP);
		_allowBroadcast = allowBroadcast;
		if (ip != null) {
		    _ipRange = new IPRange(ip, _allowBroadcast);
		}
		if (ip6 != null) {
            _ipRange6 = new IPRange(ip6, _allowBroadcast);
        }
	}

	public boolean broadcastAllowed() {
		return _allowBroadcast;
	}

	@Override
	public String toString() {
		String s = _name + ", " + _className + ", " + _comment + ", " +  _type;
		if (_ipRange != null) {
            s += ", range=" + _ipRange.toNetString("i::");
        }
		if (_ipRange6 != null) {
		    s+= ", range6=" + _ipRange6.toNetString("i::");
        }
        s += ", allow_broadcast=" + _allowBroadcast;
		return s;
	}

	public IPRange getIpRange() {
		return _ipRange;
	}

    public IPRange getIpRange6() {
        return _ipRange6;
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
