/*
 * Copyright (c) 2013 - 2020, Universite de Rennes 1
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

package fr.univrennes1.cri.jtacl.equipments.fortigate;

import fr.univrennes1.cri.jtacl.core.probing.MatchResult;

import java.util.LinkedList;

/**
 * Match results for Fortigate service
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */

public class FgServicesMatch extends LinkedList<FgServiceMatch> {
	protected MatchResult _matchResult;

	public MatchResult getMatchResult() {
		return _matchResult;
	}

	public void setMatchResult(MatchResult matchResult) {
		_matchResult = matchResult;
	}
}

