/*
 * Copyright (c) 2010, Universite de Rennes 1
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

import java.util.HashMap;

/**
 * A HashMap of {@link Iface} items. Keyed by the name of the {@link Iface} item.
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class IfacesByName extends HashMap<String, Iface> {

	/**
	 * Return the name of the {@link Iface} as a key.
	 * @param iface the {@link Iface} interface to use as a key.
	 * @return the {@link String} key.
	 */
	public static String key(Iface iface) {
		return iface.getName();
	}

    /**
     * Associates the specified {@link Iface} interface in this map, using the
	 * {@link String} name of the interface as a key.
     *
     * @param iface value to be associated.
     */
	public void put(Iface iface) {
		put(key(iface), iface);
	}

}
