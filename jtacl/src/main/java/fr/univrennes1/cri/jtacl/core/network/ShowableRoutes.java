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

/**
 * Interface on the ShowRoutes() operation.
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public interface ShowableRoutes {
	/**
	 * Returns a {@link String} representation of the routing tables.
	 * @return a {@link String} representation of the routing tables.
	 */
	String showRoutes();
}
