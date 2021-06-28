package fr.univrennes1.cri.jtacl.equipments.proxmox;

import fr.univrennes1.cri.jtacl.core.probing.MatchResult;
import fr.univrennes1.cri.jtacl.lib.ip.IPRangeable;
import fr.univrennes1.cri.jtacl.lib.misc.ParseContext;

public class PxNetworkAlias extends PxNetworkObject {

	protected PxNetworkIp _ipRange;

	public PxNetworkAlias(String name, PxNetworkIp ipRange, ParseContext context) {
		super(name, PxNetworkType.IPALIAS, context);
		_ipRange = ipRange;
	}

	public PxNetworkIp getIpRange() { return _ipRange; }

	@Override
	public MatchResult matches(IPRangeable ip) {
		return _ipRange.matches(ip);
	}

	@Override
	public String toString() {
		return _name + ", " + _type.name() + ", " + _ipRange.getIpRange().toNetString("i::");
	}
}
