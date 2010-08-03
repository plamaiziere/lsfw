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
 * Describes an icmp-type object group.
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class IcmpObjectGroup extends ObjectGroup {

	/**
	 * Constructs a new {@link IcmpObjectGroup} group with the group Id
	 * in argument.
	 * @param groupId the group Id of this group.
	 */
	public IcmpObjectGroup(String groupId) {
		super(ObjectGroupType.ICMP, groupId);
	}

	/**
	 * Checks if at least one item of this group matches the icmp-type
	 * in argument.
	 * @param icmp icmp-type value to check.
	 * @return true if an item of this group matches the icmp-type in argument.
	 */
	public boolean matches(int icmp) {
		for (ObjectGroupItem item: this) {
			IcmpObjectGroupItem iitem = (IcmpObjectGroupItem) item;
			if (iitem.isGroup())
				continue;
			if (iitem.matches(icmp))
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
	public IcmpObjectGroup expand() {
		IcmpObjectGroup group = new IcmpObjectGroup(_groupId);

		for (ObjectGroupItem obj: this) {
			if (obj.isGroup())
				group.addAll(((IcmpObjectGroup)obj.getGroup()).expand());
			else
				group.add(obj);
		}
		return group;
	}

}
