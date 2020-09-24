package fr.univrennes1.cri.jtacl.equipments.fortigate;

import fr.univrennes1.cri.jtacl.core.probing.MatchResult;
import fr.univrennes1.cri.jtacl.equipments.checkpointR80.CpNetworkType;
import fr.univrennes1.cri.jtacl.lib.ip.IPRangeable;

/**
 * Checkpoint network object left unhandled by lsfw
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class FgUnhandledNetwork extends FgNetworkObject {

	/**
	 * Construct a new unhandled network
	 * @param name network name
	 * @param originKey Fortigate origin key
	 * @param comment comment
     * @param uid uid of the object
	 */
	public FgUnhandledNetwork(String name, String originKey, String comment, String uid) {

		super(name, originKey, comment, uid, FgNetworkType.UNHANDLED);
	}

	@Override
	public String toString() {
		return _name + ", " + _originKey + ", " + _comment + ", " +  _type;
	}

	@Override
	public MatchResult matches(IPRangeable ip) {
		return MatchResult.UNKNOWN;
	}

}
