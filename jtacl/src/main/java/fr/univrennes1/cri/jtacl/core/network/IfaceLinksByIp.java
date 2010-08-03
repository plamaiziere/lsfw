/*
 * Copyright (c) 2010, Université de Rennes 1
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

package fr.univrennes1.cri.jtacl.core.network;

import fr.univrennes1.cri.jtacl.lib.ip.IPNet;
import java.util.HashMap;

/**
 * A HashMap of {@link IfaceLink} items. Keyed by the IP {@link IPNet} of the
 * {@link IfaceLink} item.
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class IfaceLinksByIp extends HashMap<Integer, IfaceLink> {

	/**
	 * Return the {@link Integer} key of an {@link IfaceLink} link.
	 * @param link the {@link IfaceLink} link to use as a key.
	 * @return the {@link Integer} key.
	 */
	public static Integer key(IfaceLink link) {
		return link.getIp().hashCode();
	}

    /**
     * Associates the specified {@link IfaceLink} link in this map, using the
	 * {@link IPNet} IP address of the link as a key.
     *
     * @param link value to be associated.
     */
	public void put(IfaceLink link) {
		put(key(link), link);
	}

	/**
	 * Returns the {@link IfaceLink} link associated  with the {@link IPNet} IP
	 * address.
	 * @param ip {@link IPNet} IP address of the link.
	 * @return the {@link IfaceLink} link associated. Null if no link was found.
	 */
	public IfaceLink get(IPNet ip) {
		return get(ip.hashCode());
	}
	
}
