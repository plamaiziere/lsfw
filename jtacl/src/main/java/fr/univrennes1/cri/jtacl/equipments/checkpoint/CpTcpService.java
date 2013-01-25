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

/**
 * Checkpoint TCP service object
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class CpTcpService extends CpService {

	/* port */
	protected CpPortItem _port;

	/* source port */
	protected CpPortItem _sourcePort;

	/* included in "Any" service */
	protected boolean _inAny;

	/**
	 * Construct a new checkpoint TCP service
	 * @param name service name
	 * @param comment comment
	 * @param port port
	 * @param sourcePort source port
	 * @param inAny true if this service is included in any
	 */
	public CpTcpService(String name,
			String comment,
			CpPortItem port,
			CpPortItem sourcePort,
			boolean inAny) {

		super(name, "tcp_service", comment, CpServiceType.TCP);
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
				+ ", inAny=" + _inAny;
	}

}
