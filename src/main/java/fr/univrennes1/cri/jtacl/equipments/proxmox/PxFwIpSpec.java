/*
 * Copyright (c) 2013 - 2021, Universite de Rennes 1
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

package fr.univrennes1.cri.jtacl.equipments.proxmox;

/**
 * Proxmox IP specification firewall rule
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PxFwIpSpec {
  	protected PxNetworkIpSet _networks = new PxNetworkIpSet("", null);

	public PxNetworkIpSet getNetworks() {
		return _networks;
	}

	public void addReference(String name, PxNetworkObject networkObject) {
		_networks.addReference(name, networkObject);
	}

	public String toString() {
		String s = "";
		return s + _networks.getReferencesName();
	}
}
