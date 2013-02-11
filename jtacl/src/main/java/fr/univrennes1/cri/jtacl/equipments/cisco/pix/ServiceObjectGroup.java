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

import fr.univrennes1.cri.jtacl.core.probing.MatchResult;
import fr.univrennes1.cri.jtacl.lib.ip.PortSpec;
import fr.univrennes1.cri.jtacl.lib.ip.ProtocolsSpec;

/**
 * Describes a service object group.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class ServiceObjectGroup extends ObjectGroup {

	/**
	 * the protocols of this group.
	 */
	protected ProtocolsSpec _protocols;

	/**
	 * Constructs a new {@link ServiceObjectGroup} group with the group Id
	 * in argument.
	 * @param groupId the group Id of this group.
	 * @param protocols protocols of this group.
	 */
	public ServiceObjectGroup(String groupId, ProtocolsSpec protocols) {
		super(ObjectGroupType.SERVICE, groupId);
		_protocols = protocols;
	}

	/**
	 * Returns the protocols of this group.
	 * @return the protocols of this group.
	 */
	public ProtocolsSpec getProtocols() {
		return _protocols;
	}

	/**
	 * Checks if at least one item of this group matches the service
	 *  in argument and if this group matches the protocol in argument.
	 * @param protocol protocol value to check.
	 * @param port {@link PortSpec} port spec value to check.
	 * @return a {@link MatchResult} between this group and the port spec in
	 * argument.
	 */
	 public MatchResult matches(int protocol, PortSpec port) {
		/*
		 * protocols
		 */
		if (!_protocols.contains(protocol))
			return MatchResult.NOT;

		int match = 0;
		int all = 0;
		for (ObjectGroupItem item: this) {
			ServiceObjectGroupItem sitem = (ServiceObjectGroupItem) item;
			if (sitem.isGroup())
				continue;
			MatchResult res = sitem.matches(port);
			if (res == MatchResult.ALL)
				all++;
			if (res == MatchResult.MATCH)
				match++;
		}
		if (all > 0)
			return MatchResult.ALL;
		if (match > 0)
			return MatchResult.MATCH;

		return MatchResult.NOT;
	}

	/**
	 * Returns an expanded group. Nested object groups
	 * are expended recursively.
	 * @return a group with nested object groups expanded
	 * recursively.
	 */
	public ServiceObjectGroup expand() {
		ServiceObjectGroup group = new ServiceObjectGroup(_groupId, _protocols);
		group.setDescription(_description);

		for (ObjectGroupItem obj: this) {
			if (obj.isGroup())
				group.addAll(((ServiceObjectGroup)obj.getGroup()).expand());
			else
				group.add(obj);
		}
		return group;
	}
}
