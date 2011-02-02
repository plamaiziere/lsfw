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

import fr.univrennes1.cri.jtacl.core.monitor.MatchResult;
import fr.univrennes1.cri.jtacl.lib.ip.PortSpec;

/**
 * An item of a service object group.
 *
 * An item can be a group or a port object.
 * 
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class ServiceObjectGroupItem extends ObjectGroupItem {

	/**
	 * PortObject if this item is a port object, null otherwise
	 */
	protected PortObject _portObject;

	/**
	 * Returns the Port Object. Valid only isGroup() returns false.
	 * @return the Port Object. Valid only isGroup() returns false.
	 */
	public PortObject getPortObject() {
		return _portObject;
	}

	/**
	 * Constructs a new service object item of type port object.
	 */
	public ServiceObjectGroupItem(ObjectGroup owner, String configurationLine,
			PortObject portObject)  {
		_owner = owner;
		_configurationLine = configurationLine;
		_portObject = portObject;
	}

	/**
	 * Constructs a new service object item of type "group".
	 */
	public ServiceObjectGroupItem(ObjectGroup owner, String configurationLine,
			ObjectGroup group) {
		_owner = owner;
		_configurationLine = configurationLine;
		_group = group;
	}

	/**
	 * Checks if this item matches the port spec in argument.
	 * @param port {@link PortSpec} value to check.
	 * @return the {@link MatchResult} between this instance and the port spec.
	 */
	public MatchResult matches(PortSpec port) {
		return _portObject.matches(port);
	}	


}
