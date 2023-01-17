/*
 * Copyright (c) 2013, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.analysis;

import fr.univrennes1.cri.jtacl.lib.ip.IPRangeable;
import java.util.HashMap;

/**
 * Map of IP cross references
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class IPCrossRefMap extends HashMap<IPRangeable, IPCrossRef> {

	/**
	 * put a reference into the map
	 * @param ref reference to put
	 */
	public void put(IPCrossRef ref) {
		put(ref.getIP(), ref);
	}
}
