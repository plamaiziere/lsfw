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

package fr.univrennes1.cri.jtacl.lib.misc;

/**
 * The result of a match.
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public enum MatchResult {
	/**
	 * Match totaly.
	 */
	ALL,
	/**
	 * Don't match at all.
	 */
	NOT,
	/**
	 * Match partialy.
	 */
	MATCH,
	/**
	 * Unknown result.
	 */
	UNKNOWN

}
