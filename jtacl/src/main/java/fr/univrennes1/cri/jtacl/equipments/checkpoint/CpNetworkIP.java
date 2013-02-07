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
import fr.univrennes1.cri.jtacl.lib.ip.IPRangeable;

/**
 * Checkpoint IP network object
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */

public class CpNetworkIP extends CpNetworkObject {

	/* ip/network address */
	protected IPRange _ipRange;

	protected boolean _allowBroadcast;

	/**
	 * Construct a new checkpoint IP network object
	 * @param name object name name
	 * @param className checkpoint class name
	 * @param comment comment
	 * @param ip ip/network address
	 * @param allowBroadcast true if the broadcast address is allowed.
	 */
	public CpNetworkIP(String name,	String className, String comment, IPNet ip,
			boolean allowBroadcast) {

		super(name, className, comment, CpNetworkType.IP);
		_allowBroadcast = allowBroadcast;
		_ipRange = new IPRange(ip, _allowBroadcast);
	}

	public boolean broadcastAllowed() {
		return _allowBroadcast;
	}

	@Override
	public String toString() {
		return _name + ", " + _className + ", " + _comment + ", " +  _type
				+ ", range=" + _ipRange.toNetString("i::") + ", allow_broadcast="
				+ _allowBroadcast;
	}

	public IPRange getIpRange() {
		return _ipRange;
	}

	@Override
	public MatchResult matches(IPRangeable ip) {
		if (_ipRange.contains(ip))
			return MatchResult.ALL;
		if (_ipRange.overlaps(ip))
			return MatchResult.MATCH;
		return MatchResult.NOT;
	}
}
