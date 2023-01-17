/*
 * Copyright (c) 2010, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.equipments.cisco.pix;

import fr.univrennes1.cri.jtacl.lib.misc.Direction;

/**
 * Describes an access group.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class AccessGroup {

	/**
	 * name of this access group.
	 */
	String _name;

	/**
	 * name of the iface linked with this access group.
	 */
	String _ifName;

	/**
	 * the direction (IN/OUT) associated to this access group.
	 */
	Direction _direction;

	/**
	 * Contructs a new access group.
	 * @param name name of the access group.
	 * @param ifName the name of the iface linked to this access group.
	 * @param direction the direction associated to this access group.
	 */
	public AccessGroup(String name, String ifName, Direction direction) {
		_name = name;
		_ifName = ifName;
		_direction = direction;
	}

	/**
	 * Returns the name of the iface linked to this access group.
	 * @return the name of the iface linked to this access group.
	 */
	public String getIfName() {
		return _ifName;
	}

	/**
	 * Returns the name of this access group.
	 * @return the name of this access group.
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Returns the direction of this access group.
	 * @return the direction of this access group.
	 */
		public Direction getDirection() {
		return _direction;
	}

}
