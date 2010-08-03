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

import java.util.List;

/**
 * Describes a protocol object group.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class ProtocolObjectGroup extends ObjectGroup {

	/**
	 * Constructs a new {@link ProtocolObjectGroup} group with the group Id
	 * in argument.
	 * @param groupId the group Id of this group.
	 */
	public ProtocolObjectGroup(String groupId) {
		super(ObjectGroupType.PROTOCOL, groupId);
	}

	/**
	 * Checks if at least one item of this group matches the protocols
	 *  in argument.
	 * @param protocols protocols value to check.
	 * @return true if an item of this group matches the protocol in argument.
	 */
	public boolean matches(List<Integer> protocols) {
		for (ObjectGroupItem item: this) {
			ProtocolObjectGroupItem pitem = (ProtocolObjectGroupItem) item;
			if (pitem.isGroup())
				continue;
			if (pitem.matches(protocols))
				return true;
		}
		return false;
	}

	/**
	 * Returns an expanded group. Nested object groups
	 * are expended recursively.
	 * @return a group with nested object groups expanded
	 * recursively.
	 */
	public ProtocolObjectGroup expand() {
		ProtocolObjectGroup group = new ProtocolObjectGroup(_groupId);

		for (ObjectGroupItem obj: this) {
			if (obj.isGroup())
				group.addAll(((ProtocolObjectGroup)obj.getGroup()).expand());
			else
				group.add(obj);
		}
		return group;
	}

}
