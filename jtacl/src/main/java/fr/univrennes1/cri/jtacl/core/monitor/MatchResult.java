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
 * The result of a match.
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public enum MatchResult {
	/**
	 * Match totally.
	 */
	ALL,
	/**
	 * Don't match at all.
	 */
	NOT,
	/**
	 * Match partially.
	 */
	MATCH,
	/**
	 * Unknown result.
	 */
	UNKNOWN;
	
	/**
	 * Negates this result.
	 * @return the negated result of this enum instance.
	 */
	public MatchResult not() {
		
		switch (this) {
			case ALL:
				return MatchResult.NOT;
			case NOT:
				return MatchResult.ALL;
			case MATCH:
				return MatchResult.MATCH;
			case UNKNOWN:
				return MatchResult.UNKNOWN;
			default:
				return MatchResult.UNKNOWN;
		}
	}

}
