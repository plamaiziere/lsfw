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

import fr.univrennes1.cri.jtacl.lib.ip.IPRange;

/**
 * Checkpoint IP network range
 * Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */

public class CpNetworkRange extends CpNetworkObject {

	/* ip range */
	protected IPRange _ipRange;

	/**
	 * Construct a new checkpoint IP network range object
	 * @param name object name name
	 * @param className checkpoint class name
	 * @param comment comment
	 * @param ipFirst ip first address
	 * @param ipLast ip last address
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
}
