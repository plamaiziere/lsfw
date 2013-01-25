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

import fr.univrennes1.cri.jtacl.lib.ip.AddressFamily;
import fr.univrennes1.cri.jtacl.lib.ip.IPIcmpEnt;

/**
 * Checkpoint ICMP service object
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class CpIcmpService extends CpService {

	/**
	 * address family
	 */
	protected AddressFamily _af;

	/* icmp */
	protected IPIcmpEnt _icmp;

	/**
	 * Construct a new checkpoint ICMP service
	 * @param name service name
	 * @param comment comment
	 * @param af address family
	 * @param icmpType icmp type
	 * @param icmpCode icmp code (-1 = no code)
	 */
	public CpIcmpService(String name,
			String comment,
			AddressFamily af,
			int icmpType,
			int icmpCode) {

		super(name, af == AddressFamily.INET6 ? "icmpv6_service" : "icmp_service",
			comment, CpServiceType.ICMP);

		_icmp = new IPIcmpEnt(name, icmpType, icmpCode);
		_af = af;
	}

	public AddressFamily getAf() {
		return _af;
	}

	public IPIcmpEnt getIcmp() {
		return _icmp;
	}

	@Override
	public String toString() {
		return _name + ", " + _className + ", " + _comment + ", " +  _type
				+ ", " + _af + ", " + _icmp;
	}

}
