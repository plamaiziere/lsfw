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
 * Checkpoint service specification firewall rule
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class CpFwServicesSpec {
	protected Map <String, CpService> _services
			= new HashMap<String, CpService>();

	public Map<String, CpService> getServices() {
		return _services;
	}

	public void addReference(String name, CpService service) {
		_services.put(name, service);
	}

	@Override
	public String toString() {
		return "" + _services.keySet();
	}

}
