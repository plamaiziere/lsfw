/*
 * Copyright (c) 2022, Universite de Rennes 1
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
 * Fortigate IP Pool network object
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class FgNetworkIpPool extends FgNetworkObject {

   	/* ip/network address */
	protected IPRangeable _ipRange;

	/**
	 * Construct a new Fortigate IP Pool network object
	 * @param name object name name
	 * @param originKey Fortigate origin key
	 * @param comment comment
	 * @param ip ip /network address
	 */
	public FgNetworkIpPool(String name, String originKey, String comment, IPRangeable ip) {

		super(name, originKey, comment, "", FgNetworkType.IPPOOL);
	    _ipRange = ip;
	}

	@Override
	public String toString() {
		String s = _name + ", " + _originKey + ", " + _comment + ", " +  _type;
		if (_ipRange != null) {
            s += ", range=" + _ipRange.toString("i::");
        }
		return s;
	}

	public IPRangeable getIpRange() {
		return _ipRange;
	}

    @Override
	public MatchResult matches(IPRangeable ip) {
		// XXX: not used
		return MatchResult.NOT;
	}
}

