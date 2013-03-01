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

package fr.univrennes1.cri.jtacl.policies;

import java.util.List;

/**
 * "host" security policy
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class HostPolicy extends Policy {

	protected PoliciesMap _policies = new PoliciesMap();
	protected List<String> _address;

	public HostPolicy(String name, String comment) {
		super(name, comment);
	}

	public PoliciesMap getPolicies() {
		return _policies;
	}

	public void setAddress(List<String> address) {
		_address = address;
	}

	public List<String> getAddress() {
		return _address;
	}

	@Override
	public String toString() {
		return "HostPolicy{" + "_policies=" + _policies
				+ ", _address=" + _address + '}';
	}

}
