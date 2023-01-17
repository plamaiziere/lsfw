/*
 * Copyright (c) 2013 - 2021, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */
package fr.univrennes1.cri.jtacl.equipments.proxmox;

import fr.univrennes1.cri.jtacl.core.probing.MatchResult;
import fr.univrennes1.cri.jtacl.lib.ip.IPRangeable;
import fr.univrennes1.cri.jtacl.lib.misc.ParseContext;

/**
 * Proxmox network alias
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PxNetworkAlias extends PxNetworkObject {

	protected PxNetworkIp _ipRange;

	public PxNetworkAlias(String name, PxNetworkIp ipRange, ParseContext context) {
		super(name, PxNetworkType.IPALIAS, context);
		_ipRange = ipRange;
	}

	public PxNetworkIp getIpRange() { return _ipRange; }

	@Override
	public MatchResult matches(IPRangeable ip) {
		return _ipRange.matches(ip);
	}

	@Override
	public String toString() {
		return _name + ", " + _type.name() + ", " + _ipRange.getIpRange().toNetString("i::");
	}
}
