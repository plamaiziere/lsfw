/*
 * Copyright (c) 2013, Universite de Rennes 1
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

package fr.univrennes1.cri.jtacl.equipments.checkpoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Checkpoint group service object
 * Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class CpGroupService extends CpService {

	/* hash of services */
	protected HashMap<String, CpService> _services =
		new HashMap<String, CpService>();

	/**
	 * Construct a new checkpoint service group
	 * @param name service name
	 * @param comment comment
	 */
	public CpGroupService(String name, String comment) {

		super(name, "service_group", comment, CpServiceType.GROUP);
	}

	public void addReference(String name, CpService service) {
		_services.put(name, service);
	}
	/**
	 * returns a list of the references name included in this group.
	 * @return a list of the references name included in this group.
	 */
	public List<String> getReferencesName() {
		return new LinkedList<String>(_services.keySet());
	}

	@Override
	public String toString() {
		return _name + ", " + _className + ", " + _comment + ", " +  _type
				+ ", services=" + getReferencesName();
	}

}