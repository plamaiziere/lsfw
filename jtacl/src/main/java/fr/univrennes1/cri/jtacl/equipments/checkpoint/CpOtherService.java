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
import fr.univrennes1.cri.jtacl.lib.ip.ProtocolsSpec;

/**
 * Checkpoint other service object
 * Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class CpOtherService extends CpService {

	/* port */
	protected Integer _protocol;

	/* expression */
	protected String _exp;

	/* included in "Any" service */
	protected boolean _inAny;

	/**
	 * Construct a new checkpoint other service
	 * @param name service name
	 * @param comment comment
	 * @param protocol protocol
	 * @param exp expression
	 * @param inAny true if this service is included in any
	 */
	public CpOtherService(String name,
			String comment,
			Integer protocol,
			String exp,
			String protocolTypeName,
			boolean inAny) {

		super(name, "other_service", comment, CpServiceType.OTHER,
				protocolTypeName);
		_protocol = protocol;
		_exp = exp;
		_inAny = inAny;
	}

	public Integer getProtocol() {
		return _protocol;
	}

	public String getExp() {
		return _exp;
	}

	public boolean isInAny() {
		return _inAny;
	}

	@Override
	public String toString() {
		return _name + ", " + _className + ", " + _comment + ", " +  _type
				+ ", protocol=" + _protocol + ", exp=" + _exp
				+ ", protocolType= " + _protocolTypeName
				+ ", inAny=" + _inAny;
	}

	@Override
	public MatchResult matches(ProbeRequest request) {

		ProtocolsSpec reqProto = request.getProtocols();

		/*
		 * protocol
		 */
		if (!reqProto.contains(_protocol))
			return MatchResult.NOT;

		/*
		 * we don't handle the "exp" expression
		 */
		if (_exp == null)
			return MatchResult.ALL;

		return MatchResult.UNKNOWN;
	}



}
