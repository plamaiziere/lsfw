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
 * Describes a service object.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class ServiceObject {

	/**
	 * protocol of this object
	 */
	protected int _protocol;

	/**
	 * port object associated to this item (may be null)
	 */
	protected PortObject _portObject;
	
	public ServiceObject(int protocol, PortObject portObject) {
		_protocol = protocol;
		_portObject = portObject;
	}

	public PortObject getPortObject() {
		return _portObject;
	}

	public int getProtocol() {
		return _protocol;
	}

	public MatchResult matches(int protocol, PortSpec port) {
		if (protocol == _protocol) {
			if (_portObject != null)
				return _portObject.matches(port);
		}
		return MatchResult.NOT;
	}

}
