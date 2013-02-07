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

package fr.univrennes1.cri.jtacl.analysis;

import fr.univrennes1.cri.jtacl.lib.ip.IPRangeable;
import java.util.HashMap;
import java.util.Map;

/**
 * Map of IP cross references
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class IPCrossRefMap {

	protected Map<IPRangeable, IPNetCrossRef> _refs =
		new HashMap<IPRangeable, IPNetCrossRef>();

	/**
	 * put a reference into the map
	 * @param ref reference to put
	 */
	public void put(IPNetCrossRef ref) {
		_refs.put(ref.getIP(), ref);
	}

	/**
	 * Returns a reference from the map
	 * @param key key to use
	 * @return a reference from the map
	 */
	public IPNetCrossRef get(IPRangeable key) {
		return _refs.get(key);
	}

}
