/*
 * Copyright (c) 2013 - 2018, Universite de Rennes 1
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

package fr.univrennes1.cri.jtacl.equipments.checkpointR80;

/**
 * Checkpoint service specification firewall rule
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class CpFwServicesSpec {

	protected CpGroupService _services = new CpGroupService("", "", null);
	protected boolean _notIn;

	public CpGroupService getServices() {
		return _services;
	}

	public void addReference(String name, CpService service) {
		_services.addReference(name, service);
	}

	public void linkTo(CpFwRule fwrule) {
		for (CpService sobj: _services.getServices().values()) {
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
