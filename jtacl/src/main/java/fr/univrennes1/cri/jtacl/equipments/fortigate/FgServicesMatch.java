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

