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

/**
 * "flow" security policy
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PolicyFlow extends Policy {

	protected String _protocol;
	protected String _port;
	protected String _sourcePort;
	protected String _flags;

	public PolicyFlow(String name, String comment) {
		super(name, comment);
	}

	public String getProtocol() {
		return _protocol;
	}

	public void setProtocol(String protocol) {
		_protocol = protocol;
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

	@Override
	public String toString() {
		return "Flow{" + "_name=" + _name + ", _comment=" + _comment
			+ ", _from=" + _from + ", _to=" + _to
			+ ", _protocol=" + _protocol + ", _port=" + _port
			+ ", _sourcePort=" + _sourcePort + ", _flags=" + _flags + '}';
	}
}
