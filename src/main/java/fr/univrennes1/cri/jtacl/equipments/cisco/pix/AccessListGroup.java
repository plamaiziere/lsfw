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

/**
 * An Access List Group is a collection of access list.
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class AccessListGroup extends ArrayList<AccessList> {

	/**
	 * Id of this acl group
	 */
	private String _id;

	/**
	 * Creates a new AccessListGroup with id set to id.
	 * @param id the id to set;
	 */
	public AccessListGroup(String id) {
		_id = id;
	}

	/**
	 * Returns the id of this acl group.
	 * @return the id of this acl group.
	 */
	public String getId() {
		return _id;
	}

	/**
	 * Sets the id of this acl group.
	 * @param id the id to set.
	 */
	public void setId(String id) {
		_id = id;
	}


}
