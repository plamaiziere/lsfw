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

package fr.univrennes1.cri.jtacl.equipments.cisco.pix;

/**
 * An item of an icmp-typen object group.
 *
 * An item can describe a group or an icmp-type.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class IcmpObjectGroupItem extends ObjectGroupItem {

	/**
	 * icmp-type if this item is not a group, null otherwise
	 */
	protected int _icmp;

	/**
	 * Returns the icmp-type. Valid only isGroup() returns false.
	 * @return the icmp-type. Valid only isGroup() returns false.
	 */
	public int getIcmp() {
		return _icmp;
	}

	/**
	 * Constructs a new Icmp object item of type "icmp-type.
	 */
	public IcmpObjectGroupItem(ObjectGroup owner, String configurationLine,
			int icmp)  {
		_owner = owner;
		_configurationLine = configurationLine;
		_icmp = icmp;
	}

	/**
	 * Constructs a new Icmp object item of type "group".
	 */
	public IcmpObjectGroupItem(ObjectGroup owner, String configurationLine,
			ObjectGroup group) {
		_owner = owner;
		_configurationLine = configurationLine;
		_group = group;
	}

	/**
	 * Checks if this item matches the icmp-type in argument.
	 * @param icmp icmp-type value to check.
	 * @return true if this item matches the icmp-type in argument.
	 */
	public boolean matches(int icmp) {
		return _icmp == icmp;
	}
}
