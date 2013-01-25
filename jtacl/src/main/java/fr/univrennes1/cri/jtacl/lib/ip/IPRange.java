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

package fr.univrennes1.cri.jtacl.lib.ip;

import java.net.UnknownHostException;

/**
 * IP range. A range of ip addresses
 * Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class IPRange {

	protected IPNet _ipFirst;
	protected IPNet _ipLast;

	public IPRange(IPNet ipFirst, IPNet ipLast) throws UnknownHostException {
		_ipFirst = ipFirst.hostAddress();
		_ipLast = ipLast.hostAddress();
	}

	public IPNet getIpFirst() {
		return _ipFirst;
	}

	public IPNet getIpLast() {
		return _ipLast;
	}

	@Override
	public String toString() {
		return _ipFirst.toString("i::") + "-" + _ipLast.toString("i::");
	}

}
