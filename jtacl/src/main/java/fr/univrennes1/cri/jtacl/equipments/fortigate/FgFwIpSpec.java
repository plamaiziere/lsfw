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
 * Fortigate IP specification firewall rule
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class FgFwIpSpec {
    	protected FgNetworkGroup _networks = new FgNetworkGroup("", "", "", "");
	protected boolean _notIn;

	public FgNetworkGroup getNetworks() {
		return _networks;
	}

	public void addReference(String name, FgNetworkObject networkObject) {
		_networks.addBaseReference(name, networkObject);
	}

	public void linkTo(FgObject fwrule) {
		for (FgNetworkObject nobj: _networks.getBaseObjects().values()) {
			nobj.linkWith(fwrule);
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
		return s + _networks.getBaseReferencesName();
	}
}
