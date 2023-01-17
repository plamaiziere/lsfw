/*
 * Copyright (c) 2013, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.equipments.checkpoint;

import fr.univrennes1.cri.jtacl.core.probing.MatchResult;
import java.util.LinkedList;
import java.util.List;

/**
 * Match results for Checkpoint services
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */

public class CpServicesMatch extends LinkedList<CpServiceMatch> {
	protected MatchResult _matchResult;

	public MatchResult getMatchResult() {
		return _matchResult;
	}

	public void setMatchResult(MatchResult matchResult) {
		_matchResult = matchResult;
	}

	public boolean isInspected() {
		for (CpServiceMatch cps: this) {
			if (cps.isInspected())
				return true;
		}
		return false;
	}
}
