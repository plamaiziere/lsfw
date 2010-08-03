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

import java.util.ArrayList;

/**
 * Ancestor of object group.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class ObjectGroup extends ArrayList<ObjectGroupItem> {

	protected String _groupId;
	protected ObjectGroupType _type;

	/**
	 * Constructs a new {@link ObjectGroup} group with the group Id
	 * in argument.
	 * @param groupId the group Id of this group.
	 */
	public ObjectGroup(ObjectGroupType type, String groupId) {
		super();
		_type = type;
		_groupId = groupId;
	}

	/**
	 * Returns the group id of this group.
	 * @return the group id of this group.
	 */
	public String getGroupId() {
		return _groupId;
	}

	/**
	 * Returns the type of this group.
	 * @return the type of this group.
	 */
	public ObjectGroupType getType() {
		return _type;
	}

}
