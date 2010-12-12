/*
 * Copyright (c) 2010, Universite de Rennes 1
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

package fr.univrennes1.cri.jtacl.equipments.openbsd;

import fr.univrennes1.cri.jtacl.lib.ip.IPIcmpEnt;
import java.util.ArrayList;

/**
 * Icmp specification: a list of IPIcmpEnt
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PfIcmpSpec extends ArrayList<IPIcmpEnt> {

	/**
	 * address family
	 */
	protected AddressFamily _af = AddressFamily.NONE;

	/**
	 * Checks if this {@link PfIcmpSpec} matches the icmp entry.
	 * @param icmpEnt icmp entry to check.
	 * @return true if this {@link PfIcmpSpec} matches the icmp entry in argument.
	 */
	public boolean matches(IPIcmpEnt icmpEnt) {
		for (IPIcmpEnt ent: this) {
			if (ent.getIcmp() == icmpEnt.getIcmp()) {
				if (ent.getCode() == -1 ||
					ent.getCode() == icmpEnt.getCode())
					return true;
			}
		}
		return false;
	}

	/**
	 * Returns the address family of this icmp specification.
	 * @return the address family of this icmp specification.
	 */
	public AddressFamily getAf() {
		return _af;
	}

	/**
	 * Sets the address family of this icmp specification.
	 * @param af Address family to set.
	 */
	public void setAf(AddressFamily af) {
		_af = af;
	}

}
