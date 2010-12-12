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

package fr.univrennes1.cri.jtacl.core.monitor;

/**
 * The routing result of a Probe.
 * <ul>
 *  <li>UNKNOWN: unknown state.</li>
 *  <li>NOTROUTED: the probe was not routed to its destination (no route).</li>
 *  <li>KILLED: the probe was killed because of TTL expiration or error.
 *  <li>LOOP: the probe was dropped because it was looping.</li>
 *	<li>ROUTED: the probe was routed to its destination.</li>
 * </ul>
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public enum RoutingResult {

	UNKNOWN,
	NOTROUTED,
	KILLED,
	LOOP,
	ROUTED,

}
