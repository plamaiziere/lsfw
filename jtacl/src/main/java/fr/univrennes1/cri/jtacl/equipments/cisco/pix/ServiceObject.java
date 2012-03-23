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

import java.util.ArrayList;
import java.util.List;

/**
 * Describes a service object.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class ServiceObject {

	/**
	 * protocols of this object
	 */
	protected List<Integer> _protocols = new ArrayList<Integer>();

	/**
	 * port object associated to this item (may be null)
	 */
	protected PortObject _portObject;

	public ServiceObject(List<Integer> protocols, PortObject portObject) {
		_protocols.addAll(protocols);
		_portObject = portObject;
	}

	public PortObject getPortObject() {
		return _portObject;
	}

	public List<Integer> getProtocols() {
		return _protocols;
	}

}
