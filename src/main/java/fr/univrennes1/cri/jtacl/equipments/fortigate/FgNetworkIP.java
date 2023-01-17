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
import fr.univrennes1.cri.jtacl.lib.ip.IPRangeable;

/**
 * Fortigate IP network object
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class FgNetworkIP extends FgNetworkObject {

   	/* ip/network address */
	protected IPRangeable _ipRange;

    /* ipv6/network address */
    protected IPRangeable _ipRange6;

	/**
	 * Construct a new Fortigate IP network object
	 * @param name object name name
	 * @param originKey Fortigate origin key
	 * @param comment comment
     * @param uid uid of the object
	 * @param ip ipv4/network address
     * @param ip6 ipv6/network address
	 */
	public FgNetworkIP(String name, String originKey, String comment, String uid, IPRangeable ip, IPRangeable ip6) {

		super(name, originKey, comment, uid, FgNetworkType.IPRANGE);
	    _ipRange = ip;
        _ipRange6 = ip6;
	}

	@Override
	public String toString() {
		String s = _name + ", " + _originKey + ", " + _comment + ", " +  _type;
		if (_ipRange != null) {
            s += ", range=" + _ipRange.toNetString("i::");
        }
		if (_ipRange6 != null) {
		    s+= ", range6=" + _ipRange6.toNetString("i::");
        }
		return s;
	}

	public IPRangeable getIpRange() {
		return _ipRange;
	}

    public IPRangeable getIpRange6() {
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

