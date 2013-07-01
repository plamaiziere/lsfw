/*
 * Copyright (c) 2013, Universite de Rennes 1
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

package fr.univrennes1.cri.jtacl.equipments.checkpoint;

import fr.univrennes1.cri.jtacl.core.probing.MatchResult;
import fr.univrennes1.cri.jtacl.core.probing.ProbeRequest;
import fr.univrennes1.cri.jtacl.lib.ip.AddressFamily;
import fr.univrennes1.cri.jtacl.lib.ip.IPIcmpEnt;
import fr.univrennes1.cri.jtacl.lib.ip.Protocols;
import fr.univrennes1.cri.jtacl.lib.ip.ProtocolsSpec;

/**
 * Checkpoint ICMP service object
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class CpIcmpService extends CpService {

	/**
	 * address family
	 */
	protected AddressFamily _af;

	/* icmp */
	protected IPIcmpEnt _icmp;

	/**
	 * Construct a new checkpoint ICMP service
	 * @param name service name
	 * @param comment comment
	 * @param af address family
	 * @param icmpType icmp type
	 * @param icmpCode icmp code (-1 = no code)
	 */
	public CpIcmpService(String name,
			String comment,
			AddressFamily af,
			int icmpType,
			int icmpCode) {

		super(name, af == AddressFamily.INET6 ? "icmpv6_service" : "icmp_service",
			comment, CpServiceType.ICMP, null);

		_icmp = new IPIcmpEnt(name, icmpType, icmpCode);
		_af = af;
	}

	public AddressFamily getAf() {
		return _af;
	}

	public IPIcmpEnt getIcmp() {
		return _icmp;
	}

	@Override
	public String toString() {
		return _name + ", " + _className + ", " + _comment + ", " +  _type
				+ ", " + _af + ", " + _icmp;
	}

	@Override
	public CpServicesMatch matches(ProbeRequest request) {

		ProtocolsSpec reqProto = request.getProtocols();
		CpServicesMatch servicesMatch = new CpServicesMatch();

		/*
		 * address family
		 */
		if (_af == AddressFamily.INET && !reqProto.contains(Protocols.ICMP)) {
			servicesMatch.setMatchResult(MatchResult.NOT);
			return servicesMatch;
		}

		if (_af == AddressFamily.INET6 && !reqProto.contains(Protocols.ICMP6)) {
			servicesMatch.setMatchResult(MatchResult.NOT);
			return servicesMatch;
		}

		/*
		 * icmp type and code
		 */
		Integer icmpType = request.getSubType();
		if (icmpType == null) {
			servicesMatch.setMatchResult(MatchResult.ALL);
			servicesMatch.add(new CpServiceMatch(this, MatchResult.ALL));
			return servicesMatch;
		}

		if (icmpType != _icmp.getIcmp()) {
			servicesMatch.setMatchResult(MatchResult.NOT);
			return servicesMatch;
		}

		Integer icmpCode = request.getCode();
		if (icmpCode == null) {
			servicesMatch.setMatchResult(MatchResult.ALL);
			servicesMatch.add(new CpServiceMatch(this, MatchResult.ALL));
			return servicesMatch;
		}

		if (icmpCode == _icmp.getCode()) {
			servicesMatch.setMatchResult(MatchResult.ALL);
			servicesMatch.add(new CpServiceMatch(this, MatchResult.ALL));
			return servicesMatch;
		}

		servicesMatch.setMatchResult(MatchResult.NOT);
		return servicesMatch;
	}


}
