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

/**
 * "service" security policy
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class ServicePolicy extends Policy {

	protected PoliciesMap _policies = new PoliciesMap();

	public ServicePolicy(String name, String comment) {
		super(name, comment);
	}

	public PoliciesMap getPolicies() {
		return _policies;
	}

	@Override
	public String toString() {
		return "ServicePolicy{" + "_name=" + _name + ", _comment=" + _comment
			+ ", _from=" + _from + ", _to=" + _to
			+ ", _policies=" + _policies + '}';
	}

}