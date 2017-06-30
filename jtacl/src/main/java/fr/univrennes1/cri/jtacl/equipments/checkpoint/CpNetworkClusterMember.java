/*
 * Copyright (c) 2017, Universite de Rennes 1
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

package fr.univrennes1.cri.jtacl.equipments.checkpoint;

import fr.univrennes1.cri.jtacl.core.probing.MatchResult;
import fr.univrennes1.cri.jtacl.lib.ip.IPRange;
import fr.univrennes1.cri.jtacl.lib.ip.IPRangeable;
import java.util.ArrayList;
import java.util.List;

/**
 * Checkpoint network cluster member
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */

public class CpNetworkClusterMember extends CpNetworkObject {

	/* list of ip addresses */
	protected ArrayList<IPRange> _ipRanges;

	/**
	 * Construct a new checkpoint cluster member object
	 * @param name object name name
	 * @param className checkpoint class name
	 * @param comment comment
	 */
	public CpNetworkClusterMember(String name,	String className, String comment)
	{
		super(name, className, comment, CpNetworkType.IPS);
		_ipRanges = new ArrayList();
	}

	public List<IPRange> getIpRanges() {
		return _ipRanges;
	}

	@Override
	public String toString() {
		String sips = "";
		for (IPRange ip: _ipRanges) {
			sips = sips + ip.getIpFirst().toString("i::") + ", ";
		}
		return _name + ", " + _className + ", " + _comment + ", " +  _type
				+ ", " + sips;
	}

	@Override
	public MatchResult matches(IPRangeable ip) {
		MatchResult res = MatchResult.NOT;

		for (IPRange ipRange: _ipRanges) {
			if (ipRange.contains(ip))
				return MatchResult.ALL;
			if (ipRange.overlaps(ip))
				res = MatchResult.MATCH;
		}
		return res;
	}
}
