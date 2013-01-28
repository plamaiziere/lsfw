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

import java.util.HashMap;
import java.util.Map;

/**
 * Checkpoint IP specification firewall rule
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class CpFwIpSpec {
	protected Map <String, CpNetworkObject> _networks
			= new HashMap<String, CpNetworkObject>();

	public Map<String, CpNetworkObject> getNetworks() {
		return _networks;
	}

	public void addReference(String name, CpNetworkObject networkObject) {
		_networks.put(name, networkObject);
	}

	@Override
	public String toString() {
		return "" + _networks.keySet();
	}
}
