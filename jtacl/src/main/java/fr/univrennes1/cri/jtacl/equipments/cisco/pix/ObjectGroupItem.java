/*
 * Copyright (c) 2010, Université de Rennes 1
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
 * Ancestor of group item.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public abstract class ObjectGroupItem {

	/**
	 * the owner of this item (may be null)
	 */
	protected ObjectGroup _owner;

	/**
	 * the line of configuration corresponding to this item (may be null)
	 */
	protected String _configurationLine;


	/**
	 * link to an object group if this item is a group-object, null otherwise.
	 */
	protected ObjectGroup _group;


	/**
	 * Returns the object group owning this item.
	 * @return the object group owning this item.
	 */
	public ObjectGroup getOwner() {
		return _owner;
	}

	/**
	 * Returns the line of configuration corresponding to this item
	 */
	public String getConfigurationLine() {
		return _configurationLine;
	}

	/**
	 * Checks if this item is a group item.
	 * @return true if this item is a group item.
	 */
	public boolean isGroup() {
		return _group != null;
	}

	/**
	 * Returns the group linked to by this item. Valid only if isGroup() returns
	 * true.
	 * @return the group linked to by this item.
	 */
	public ObjectGroup getGroup() {
		return _group;
	}


}
