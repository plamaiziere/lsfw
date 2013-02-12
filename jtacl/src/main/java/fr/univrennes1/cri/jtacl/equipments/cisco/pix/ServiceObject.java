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

import fr.univrennes1.cri.jtacl.lib.ip.ProtocolsSpec;

/**
 * Describes a service object.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class ServiceObject {

	/**
	 * protocols of this object
	 */
	protected ProtocolsSpec _protocols = new ProtocolsSpec();

	/**
	 * port object associated to this item (may be null)
	 */
	protected PortObject _portObject;

	public ServiceObject(ProtocolsSpec protocols, PortObject portObject) {
		_protocols.addAll(protocols);
		_portObject = portObject;
	}

	/**
	 * Returns the portObject of this service (may be null if protocols !=
	 * udp/tcp)
	 * @return the portObject of this service
	 */
	public PortObject getPortObject() {
		return _portObject;
	}

	/**
	 * Returns the list of protocols of this service
	 * @return
	 */
	public ProtocolsSpec getProtocols() {
		return _protocols;
	}

}
