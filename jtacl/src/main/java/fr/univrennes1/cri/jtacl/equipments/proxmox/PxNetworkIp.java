package fr.univrennes1.cri.jtacl.equipments.proxmox;

import fr.univrennes1.cri.jtacl.core.probing.MatchResult;
import fr.univrennes1.cri.jtacl.lib.ip.IPRangeable;

public class PxNetworkIp extends PxNetworkObject {

	protected IPRangeable _ipRange;

	public PxNetworkIp(IPRangeable ipRange) {
		super(ipRange.toNetString("i::"), PxNetworkType.IPRANGE, null);
		_ipRange = ipRange;
	}

	@Override
	public MatchResult matches(IPRangeable ip) {
		if (_ipRange.contains(ip))
			return MatchResult.ALL;
		if (_ipRange.overlaps(ip))
			return MatchResult.MATCH;
        return MatchResult.NOT;
	}

	public IPRangeable getIpRange() {
		return _ipRange;
	}
}
