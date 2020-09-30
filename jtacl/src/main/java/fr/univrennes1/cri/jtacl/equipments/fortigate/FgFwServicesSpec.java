/*
 * Copyright (c) 2013 - 2020, Universite de Rennes 1
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

package fr.univrennes1.cri.jtacl.equipments.fortigate;

/**
 * Fortigate service specification firewall rule
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class FgFwServicesSpec {

	protected FgServicesGroup _services = new FgServicesGroup();
	protected boolean _notIn;

	public FgServicesGroup getServices() {
		return _services;
	}

	public void addReference(String name, FgService service) {
		_services.addReference(name, service);
	}

	public void linkTo(FgFwRule fwrule) {
		for (FgService sobj: _services.getServices().values()) {
			sobj.linkWith(fwrule);
		}
	}

	public void setNotIn(boolean notIn) {
		_notIn = notIn;
	}

	public boolean isNotIn() {
		return _notIn;
	}

	@Override
	public String toString() {
		String s = "";
		if (_notIn)
			s= "!";
		return s + _services.getReferencesName();
	}

}
