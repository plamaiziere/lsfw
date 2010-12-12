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

import fr.univrennes1.cri.jtacl.core.exceptions.JtaclInternalException;
import fr.univrennes1.cri.jtacl.lib.ip.IPNet;
import fr.univrennes1.cri.jtacl.lib.misc.MatchResult;
import java.net.UnknownHostException;

/**
 * An item of a network object group.
 *
 * An item can describe a group or an IP address.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class NetworkObjectGroupItem extends ObjectGroupItem {

	/**
	 * IP address if this item is not a group, null otherwise
	 */
	protected IPNet _ipAddress;

	/**
	 * Returns the IP address. Valid only isGroup() returns false.
	 * @return the IP address. Valid only isGroup() returns false.
	 */
	public IPNet getIpAddress() {
		return _ipAddress;
	}

	/**
	 * Constructs a new network object item of type "IP address".
	 */
	public NetworkObjectGroupItem(ObjectGroup owner, String configurationLine,
			IPNet ipAddress)  {
		_owner = owner;
		_configurationLine = configurationLine;
		_ipAddress = ipAddress;
	}

	/**
	 * Constructs a new network object item of type "group".
	 */
	public NetworkObjectGroupItem(ObjectGroup owner, String configurationLine,
			ObjectGroup group) {
		_owner = owner;
		_configurationLine = configurationLine;
		_group = group;
	}

	/**
	 * Checks if this item matches the IP address in argument.
	 * @param ip IP address to check.
	 * @return a {@link MatchResult} according to the test.
	 */
	public MatchResult matches(IPNet ip) {
		try {
			if (_ipAddress.networkContains(ip))
				return MatchResult.ALL;
			if (_ipAddress.overlaps(ip))
				return MatchResult.MATCH;
			return MatchResult.NOT;
			} catch (UnknownHostException ex) {
			// should not happen
			throw new JtaclInternalException("unexpected exception: "
				+ ex.getMessage());
		}
	}

}
