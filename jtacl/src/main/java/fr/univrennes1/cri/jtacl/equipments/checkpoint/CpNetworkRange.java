/*
 * Copyright (c) 2013, Universite de Rennes 1
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
import fr.univrennes1.cri.jtacl.lib.ip.IPNet;
import fr.univrennes1.cri.jtacl.lib.ip.IPRange;

/**
 * Checkpoint IP network range
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */

public class CpNetworkRange extends CpNetworkObject {

	/* ip range */
	protected IPRange _ipRange;

	/**
	 * Construct a new checkpoint IP network range object
	 * @param name object name name
	 * @param className checkpoint class name
	 * @param comment comment
	 * @param ipRange ip range address
	 */
	public CpNetworkRange(String name,	String className, String comment,
			IPRange ipRange)  {

		super(name, className, comment, CpNetworkType.RANGE);
		_ipRange = ipRange;
	}

	public IPRange getIpRange() {
		return _ipRange;
	}

	@Override
	public String toString() {
		return _name + ", " + _className + ", " + _comment + ", " +  _type
				+ ", IPRange=" + _ipRange;
	}

	@Override
	public MatchResult matches(IPNet ip) {
		if (_ipRange.contains(ip))
			return MatchResult.ALL;
		if (_ipRange.overlaps(ip))
			return MatchResult.MATCH;
		return MatchResult.NOT;
	}
}
