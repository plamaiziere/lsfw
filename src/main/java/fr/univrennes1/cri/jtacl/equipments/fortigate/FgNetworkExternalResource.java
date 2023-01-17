/*
 * Copyright (c) 2013 - 2022, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.equipments.fortigate;

import fr.univrennes1.cri.jtacl.core.probing.MatchResult;
import fr.univrennes1.cri.jtacl.lib.ip.IPRangeable;

import java.util.LinkedList;
import java.util.List;

/**
 * Fortigate external resource network object
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class FgNetworkExternalResource extends FgNetworkObject {

	/* List of IP */
	protected List<IPRangeable> _ipRanges = new LinkedList<>();

	protected boolean _status;
	protected String _resource;

	/**
	 * Construct a new Fortigate network external resource object
	 * @param name object name name
	 * @param originKey Fortigate orgin key
	 * @param comment comment
	 */
	public FgNetworkExternalResource(String name, String originKey, String comment, String uid, String resource, boolean status) {

		super(name, originKey, comment, uid, FgNetworkType.EXTERNAL_RESOURCE);
		_resource = resource;
		_status = status;
	}

	public boolean getStatus() { return _status; }
	public List<IPRangeable> getIpRanges() { return _ipRanges; }
	public String getResource() { return _resource; }

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		String s = _name + ", " + _originKey + ", " + _comment + ", " + _type  + ", " + _resource + ", [";
		sb.append(s);
		boolean start = true;
		for (IPRangeable ipr: _ipRanges) {
			if (!start) sb.append(", "); else start = false;
			sb.append(ipr.toNetString("::i"));
		}
		sb.append("]");
		return sb.toString();
	}

    @Override
	public MatchResult matches(IPRangeable ip) {
		if (!_status) return MatchResult.NOT;
		int mAll = 0;
		int mMay = 0;
		for (IPRangeable i: _ipRanges) {
			if (ip.sameIPVersion(i)) {
				if (i.contains(ip))
					mAll++;
				if (i.overlaps(ip))
					mMay++;
			}
		}
		if (mAll > 0) return MatchResult.ALL;
		if (mMay > 0) return MatchResult.MATCH;
		return MatchResult.NOT;
	}
}
