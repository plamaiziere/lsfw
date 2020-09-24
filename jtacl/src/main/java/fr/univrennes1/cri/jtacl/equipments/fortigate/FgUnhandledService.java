package fr.univrennes1.cri.jtacl.equipments.fortigate;

import fr.univrennes1.cri.jtacl.core.probing.MatchResult;
import fr.univrennes1.cri.jtacl.core.probing.ProbeRequest;
import fr.univrennes1.cri.jtacl.equipments.checkpoint.CpServiceMatch;
import fr.univrennes1.cri.jtacl.equipments.checkpoint.CpServiceType;
import fr.univrennes1.cri.jtacl.equipments.checkpoint.CpServicesMatch;

/**
 * Checkpoint service left unhandled by lsfw
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class FgUnhandledService extends FgService {

	/**
	 * Construct a new unhandled service
	 * @param name service name
	 * @param originKey Fortigate origin key
	 * @param comment comment
	 */
	public FgUnhandledService(String name, String originKey, String comment, String uid) {
		super(name, originKey, comment, FgServiceType.UNHANDLED);
	}

	@Override
	public String toString() {
		return _name + ", " + _originKey + ", " + _comment + ", " +  _type;
	}

	@Override
	public FgServicesMatch matches(ProbeRequest request) {
		FgServicesMatch servicesMatch = new FgServicesMatch();
		servicesMatch.setMatchResult(MatchResult.UNKNOWN);
		servicesMatch.add(new FgServiceMatch(this, MatchResult.UNKNOWN));
		return servicesMatch;
	}

}
