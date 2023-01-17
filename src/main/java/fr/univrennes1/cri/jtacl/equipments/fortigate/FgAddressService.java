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

import java.util.List;

public abstract class FgAddressService extends FgService {
	protected String _comment;
    private List<IPRangeable> _ipRanges;
    private String _fqdn;

	protected  MatchResult matchAddress(IPRangeable range) {
            if (_ipRanges == null) return MatchResult.ALL;
            int all = 0;
            int may = 0;

            for (IPRangeable r: _ipRanges) {
                if (r.contains(range)) all++;
                if (r.overlaps(range)) may++;
            }
            if (all > 0) return MatchResult.ALL;
            if (may > 0) return MatchResult.MATCH;
            return MatchResult.NOT;
    }


	public FgAddressService(String name, String originKey, String comment, List<IPRangeable> ipRanges, String fqdn, FgServiceType type) {

		super(name, originKey, type);
		_comment = comment;
		_ipRanges = ipRanges;
		_fqdn = fqdn;
	}

	public String getComment() {
		return _comment;
	}

	public List<IPRangeable> getipRanges() { return _ipRanges; }

	public boolean hasRanges() { return _ipRanges != null; }

	public String getFqdn() { return _fqdn; }
	public boolean hasFqdn() { return _fqdn != null; }

    @Override
    public String toString() {
        String s = _name + ", " + _originKey + ", " + _comment + ", " + _type;
        if (hasRanges()) s += ", ipRanges=" + _ipRanges;
        if (hasFqdn()) s += ", fqdn = " + _fqdn;
        return s;
    }
}
