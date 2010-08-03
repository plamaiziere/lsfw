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
 * Describes an enhanced service object group.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class EnhancedServiceObjectGroup extends ObjectGroup {

	/**
	 * Constructs a new {@link ServiceObjectGroup} group with the group Id
	 * in argument.
	 * @param groupId the group Id of this group.
	 */
	public EnhancedServiceObjectGroup(String groupId) {
		super(ObjectGroupType.ENHANCED, groupId);
	}

	/**
	 * Checks if this at least one item of this group matches the protocols
	 * in argument.
	 * @param protocols protocols value to check.
	 * @return true if at least one item matches the protocols value in argument.
	 */
	public boolean matches(List<Integer> protocols) {
		for (ObjectGroupItem item: this) {
			EnhancedServiceObjectGroupItem sitem =
					(EnhancedServiceObjectGroupItem) item;
			if (sitem.isGroup())
				continue;
			if (sitem.matches(protocols))
				return true;
		}
		return false;
	}

	/**
	 * Checks if at least one item of this group matches the protocols and the
	 * service value in argument.
	 * @param protocols protocols value to check.
	 * @param service service value to check.
	 * @return true if at least one item matches the protocols
	 * and the service value in argument.
	 */
	public boolean matches(List<Integer> protocols, int service) {
		for (ObjectGroupItem item: this) {
			EnhancedServiceObjectGroupItem sitem =
					(EnhancedServiceObjectGroupItem) item;
			if (sitem.isGroup())
				continue;
			if (sitem.matches(protocols, service))
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
	public EnhancedServiceObjectGroup expand() {
		EnhancedServiceObjectGroup group = new EnhancedServiceObjectGroup(_groupId);

		for (ObjectGroupItem obj: this) {
			if (obj.isGroup())
				group.addAll(((EnhancedServiceObjectGroup)obj.getGroup()).expand());
			else
				group.add(obj);
		}
		return group;
	}

}
