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
import java.util.List;

/**
 * An item of an enhanced service object group.
 *
 * An item can be a group or a service-object.
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class EnhancedServiceObjectGroupItem extends ObjectGroupItem {

	/**
	 * ServiceObject if this item is a service object, null otherwise
	 */
	protected ServiceObject _serviceObject;

	/**
	 * Returns the Service Object. Valid only isGroup() returns false.
	 * @return the Service Object. Valid only isGroup() returns false.
	 */
	public ServiceObject getServiceObject() {
		return _serviceObject;
	}

	/**
	 * Constructs a new service object item of type service object.
	 */
	public EnhancedServiceObjectGroupItem(ObjectGroup owner, String configurationLine,
			ServiceObject serviceObject)  {
		_owner = owner;
		_configurationLine = configurationLine;
		_serviceObject = serviceObject;
	}

	/**
	 * Constructs a new service object item of type "group".
	 */
	public EnhancedServiceObjectGroupItem(ObjectGroup owner, String configurationLine,
			ObjectGroup group) {
		_owner = owner;
		_configurationLine = configurationLine;
		_group = group;
	}

	/**
	 * Checks if this item matches the protocols in argument.
	 * @param protocols protocols value to check.
	 * @return true if this item matches any of the protocols value in argument.
	 */
	public boolean matches(List<Integer> protocols) {
		for (Integer proto: _serviceObject.getProtocols()) {
			if (ProtocolComparator.matches(protocols, proto)) {
				return true;
			}
		}
		return false;
	}


	/**
	 * Checks if this item matches the protocols and service in argument.
	 * @param protocols protocols value to check.
	 * @param port {@link PortSpec} port spec value to check.
	 * @return a {@link MatchResult} between this group and the port spec in
	 * argument.
	 */
	public MatchResult matches(List<Integer> protocols, PortSpec port) {
		PortObject pobject = _serviceObject.getPortObject();

		if (!matches(protocols))
			return MatchResult.NOT;
		if (pobject != null)
			return (pobject.matches(port));
		return MatchResult.NOT;
	}

}
