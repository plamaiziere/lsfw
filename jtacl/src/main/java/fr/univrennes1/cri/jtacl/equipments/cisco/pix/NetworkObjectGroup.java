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

import fr.univrennes1.cri.jtacl.lib.ip.IPNet;
import fr.univrennes1.cri.jtacl.lib.misc.MatchResult;

/**
 * Describes a network object group.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class NetworkObjectGroup extends ObjectGroup {

	/**
	 * Constructs a new {@link NetworkObjectGroup} group with the group Id
	 * in argument.
	 * @param groupId the group Id of this group.
	 */
	public NetworkObjectGroup(String groupId) {
		super(ObjectGroupType.NETWORK, groupId);
	}

	/**
	 * Checks if at least one item of this group matches the IP address
	 * in argument.
	 * @param ip IP address to check.
	 * @return a {@link MatchResult} according to the test.
	 */
	public MatchResult matches(IPNet ip) {
		for (ObjectGroupItem item: this) {
			NetworkObjectGroupItem nitem = (NetworkObjectGroupItem) item;
			if (nitem.isGroup())
				continue;
			MatchResult result = nitem.matches(ip);
			if (result != MatchResult.NOT)
				return result;
		}
		return MatchResult.NOT;
	}

	/**
	 * Returns an expanded group. Nested object groups
	 * are expended recursively.
	 * @return a group with nested object groups expanded
	 * recursively.
	 */
	public NetworkObjectGroup expand() {
		NetworkObjectGroup group = new NetworkObjectGroup(_groupId);

		for (ObjectGroupItem obj: this) {
			if (obj.isGroup())
				group.addAll(((NetworkObjectGroup)obj.getGroup()).expand());
			else
				group.add(obj);
		}
		return group;
	}
}
