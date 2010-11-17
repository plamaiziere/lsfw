/*
 * Copyright (c) 2010, Université de Rennes 1
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

package fr.univrennes1.cri.jtacl.core.monitor;

import java.util.List;

/**
 * Describes what we want to check in a probe
 * (protocol, icmp type, source port...).
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class ProbeRequest {

	/**
	 * protocols we should match (null: any)
	 */
	protected List<Integer> _protocols;

	/**
	 * Sub type of the protocol, the value depends of the protocol.
	 * For example, for icmp the subType is the icmp-type (echo, echo-reply...)
	 */
	protected Integer _subType;

	/**
	 * Code of the protocol, the value depends of the protocol.
	 * For example, for icmp the code is the icmp code message.
	 */
	protected Integer _code;

	/**
	 * The source port.
	 * (null = any)
	 */
	protected Integer _sourcePort;

	/**
	 * The destination port..
	 * (null = any)
	 */
	protected Integer _destPort;

	/**
	 * Returns the internet protocols we should match.
	 * (null = any)
	 * @return the internet protocol,
	 * null if "any".
	 */
	public List<Integer> getProtocols() {
		return _protocols;
	}

	/**
	 * Sets the internet protocols we should match.
	 * @param protocols protocols value to set. Null designates any protocol.
	 */
	public void setProtocols(List<Integer> protocols) {
		_protocols = protocols;
	}

	/**
	 * Returns the sub-type of the protocol. The value depends on the protocol,
	 * For example, for icmp the subType is the icmp-type (echo, echo-reply...)
	 * (null = any)
	 * @return the sub-type of the protocol.
	 * null if "any".
	 */
	public Integer getSubType() {
		return _subType;
	}

	/**
	 * Sets the sub-type of the protocol. The value depends on the protocol,
	 * For example, for icmp the subType is the icmp-type (echo, echo-reply...)
	 * @param subType value to set. Null designates any type.
	 */
	public void setSubType(Integer subType) {
		_subType = subType;
	}

	/**
	 * Returns the code of the protocol. The value depends of the protocol.
	 * For example, for icmp the code is the icmp code message.
	 * @return the code of the protocol.
	 * null if "any".
	 */
	public Integer getCode() {
		return _code;
	}

	/**
	 * Sets the code of the protocol. The value depends of the protocol.
	 * For example, for icmp the code is the icmp code message.
	 * @param code value to set. Null designates any code.
	 */
	public void setCode(Integer code) {
		_code = code;
	}



	/**
	 * Returns the source port.
	 * (null = any)
	 * @return the source port.
	 * (null = any)
	 */
	public Integer getSourcePort() {
		return _sourcePort;
	}

	/**
	 * Sets the source port.
	 * @param port port value to set. Null designates any port.
	 */
	public void setSourcePort(Integer port) {
		_sourcePort = port;
	}

	/**
	 * Returns the destination port.
	 * (null = any)
	 * @return the destination port.
	 * (null = any)
	 */
	public Integer getDestinationPort() {
		return _destPort;
	}

	/**
	 * Sets the destination port.
	 * @param port port value to set. Null designates any port.
	 */
	public void setDestinationPort(Integer port) {
		_destPort = port;
	}

	/**
	 * Creates and returns a new copy of this instance.
	 * @return a new copy of this instance.
	 */
	public ProbeRequest newInstance() {
		ProbeRequest req = new ProbeRequest();
		req._protocols = _protocols;
		req._subType = _subType;
		req._code = _code;
		req._sourcePort = _sourcePort;
		req._destPort = _destPort;
		return req;
	}


}
