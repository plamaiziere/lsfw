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

package fr.univrennes1.cri.jtacl.policies;

import fr.univrennes1.cri.jtacl.lib.ip.ProtocolsSpec;

/**
 * "flow" security policy
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PolicyFlow extends Policy {

	protected ProtocolsSpec _protocol = new ProtocolsSpec();
	protected String _port;
	protected String _sourcePort;
	protected String _flags;
	protected boolean _connected;

	public PolicyFlow(String name, String comment) {
		super(name, comment);
		_protocol.add(0, null);
	}

	public Integer getProtocol() {
		return _protocol.get(0);
	}

	public void setProtocol(Integer protocol) {
		_protocol.add(0, protocol) ;
	}

	public String getPort() {
		return _port;
	}

	public void setPort(String port) {
		_port = port;
	}

	public String getSourcePort() {
		return _sourcePort;
	}

	public void setSourcePort(String sourcePort) {
		_sourcePort = sourcePort;
	}

	public String getFlags() {
		return _flags;
	}

	public void setFlags(String flags) {
		_flags = flags;
	}

	public boolean isConnected() {
		return _connected;
	}

	public void setConnected(boolean connected) {
		_connected = connected;
	}

	@Override
	public String toString() {
		return "Flow{" + "_name=" + _name + ", _comment=" + _comment
			+ ", _from=" + _from + ", _to=" + _to
			+ ", _protocol=" + _protocol.get(0) + ", _port=" + _port
			+ ", _sourcePort=" + _sourcePort + ", _flags=" + _flags
			+ ", _connected=" + _connected
			+ '}';
	}
}
