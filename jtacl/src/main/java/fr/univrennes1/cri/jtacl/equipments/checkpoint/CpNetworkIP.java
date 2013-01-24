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

import fr.univrennes1.cri.jtacl.lib.ip.IPNet;

/**
 * Checkpoint IP network object
 * Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */

public class CpNetworkIP extends CpNetworkObject {

	/* ip/network address */
	protected IPNet _ip;

	/**
	 * Construct a new checkpoint IP network object
	 * @param name service name
	 * @param className checkpoint class name
	 * @param comment comment
	 * @param ip ip/network address
	 */
	public CpNetworkIP(String name,	String className, String comment, IPNet ip) {

		super(name, className, comment, CpNetworkType.IP);
		_ip = ip;
	}

	public IPNet getIp() {
		return _ip;
	}

	@Override
	public String toString() {
		return _name + ", " + _className + ", " + _comment + ", " +  _type
				+ ", IPNet=" + _ip.toString("i:");
	}
}
