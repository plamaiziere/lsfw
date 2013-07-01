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
import fr.univrennes1.cri.jtacl.lib.ip.PortSpec;
import fr.univrennes1.cri.jtacl.lib.ip.Protocols;
import fr.univrennes1.cri.jtacl.lib.ip.ProtocolsSpec;

/**
 * Checkpoint UDP service object
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class CpUdpService extends CpService {

	/* port */
	protected CpPortItem _port;

	/* source port */
	protected CpPortItem _sourcePort;

	/* included in "Any" service */
	protected boolean _inAny;

	/**
	 * Construct a new checkpoint UDP service
	 * @param name service name
	 * @param comment comment
	 * @param port port
	 * @param sourcePort source port
	 * @param inAny true if this service is included in any
	 */
	public CpUdpService(String name,
			String comment,
			CpPortItem port,
			CpPortItem sourcePort,
			String protocolTypeName,
			boolean inAny) {

		super(name, "udp_service", comment, CpServiceType.UDP, protocolTypeName);
		_port = port;
		_sourcePort = sourcePort;
		_inAny = inAny;
	}

	public CpPortItem getPort() {
		return _port;
	}

	public CpPortItem getSourcePort() {
		return _sourcePort;
	}

	public boolean isInAny() {
		return _inAny;
	}

	@Override
	public String toString() {
		return _name + ", " + _className + ", " + _comment + ", " +  _type
				+ ", port=" + _port + ", sourcePort=" + _sourcePort
				+ ", protocolType= " + _protocolTypeName
				+ ", inAny=" + _inAny;
	}

	@Override
	public MatchResult matches(ProbeRequest request) {

		ProtocolsSpec reqProto = request.getProtocols();

		/*
		 * protocol
		 */
		if (!reqProto.contains(Protocols.UDP))
			return MatchResult.NOT;

		/*
		 * source port
		 */
		PortSpec port = request.getSourcePort();
		int sourceMay = 0;
		MatchResult mres = MatchResult.ALL;
		if (port != null && _sourcePort != null) {
			mres = _sourcePort.matches(port);
			/*
			 * does not match at all
			 */
			if (mres == MatchResult.NOT)
				return MatchResult.NOT;
		}
		if (mres != MatchResult.ALL)
			sourceMay++;

		/*
		 * destination port
		 */
		port = request.getDestinationPort();
		int destMay = 0;
		mres = MatchResult.ALL;
		if (port != null && _port != null) {
			mres = _port.matches(port);
			/*
			 * does not match at all
			 */
			if (mres == MatchResult.NOT)
				return MatchResult.NOT;
		}
		if (mres != MatchResult.ALL)
			destMay++;
		if (sourceMay == 0 && destMay == 0)
			return MatchResult.ALL;

		return MatchResult.MATCH;
	}

}
