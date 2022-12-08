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
 * A HashMap of {@link NetworkEquipment} items. Keyed by the name of the equipment.
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class NetworkEquipmentsByName extends HashMap<String, NetworkEquipment> {

	/**
	 * Return the name of the {@link NetworkEquipment} as a key.
	 * @param equipment the {@link NetworkEquipment} equipment to use as a key.
	 * @return the {@link String} key.
	 */
	public static String key(NetworkEquipment equipment) {
		return equipment.getName();
	}

    /**
     * Associates the specified {@link NetworkEquipment} equipment in this map,
	 * using the {@link String} name of the equipment as a key.
     *
     * @param equipment value to be associated.
     */
	public void put(NetworkEquipment equipment) {
		put(key(equipment), equipment);
	}

}
