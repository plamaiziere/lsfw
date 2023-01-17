/*
 * Copyright (c) 2013 - 2020, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.equipments.fortigate;

import fr.univrennes1.cri.jtacl.core.probing.MatchResult;

/**
 * Match results for Fortigate service
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class FgServiceMatch {
	protected MatchResult _matchResult;
	protected FgService _service;

	public FgServiceMatch(FgService service, MatchResult result) {
		_service = service;
		_matchResult = result;
	}

	public MatchResult getMatchResult() {
		return _matchResult;
	}

	public FgService getService() {
		return _service;
	}
}
