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

import fr.univrennes1.cri.jtacl.lib.misc.ParseContext;

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
	 * parse context of this item.
	 */
	protected ParseContext _parseContext;

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

	/**
	 * Returns the parse context of this item.
	 * @return the parse context of this rule. May be null.
	 */
	public ParseContext getParseContext() {
		return _parseContext;
	}

	/**
	 * Sets the parse context of this item.
	 * @param parseContext parse context to set.
	 */
	public void setParseContext(ParseContext parseContext) {
		_parseContext = parseContext;
	}

}
