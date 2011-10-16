/*
 * Copyright (c) 2010, Universite de Rennes 1
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

package fr.univrennes1.cri.jtacl.core.probing;

import fr.univrennes1.cri.jtacl.lib.ip.PortSpec;
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
	 * The source port specification.
	 * 
	 */
	protected PortSpec _sourcePort;

	/**
	 * The destination port specification
	 * 
	 */
	protected PortSpec _destPort;

	/**
	 * TCP flags we should match (null: any)
	 */
	protected ProbeTcpFlags _tcpFlags;
	
	/**
	 * Probe options
	 */
	protected ProbeOptions _probeOptions = new ProbeOptions();

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
	 * Returns the source port specification
	 * 
	 * @return the source port specification.
	 * 
	 */
	public PortSpec getSourcePort() {
		return _sourcePort;
	}

	/**
	 * Sets the source port specification.
	 * @param port port value to set.
	 */
	public void setSourcePort(PortSpec port) {
		_sourcePort = port;
	}

	/**
	 * Returns the destination port specification.
	 * 
	 * @return the destination port specification.
	 * 
	 */
	public PortSpec getDestinationPort() {
		return _destPort;
	}

	/**
	 * Sets the destination port specification.
	 * @param port port value to set..
	 */
	public void setDestinationPort(PortSpec port) {
		_destPort = port;
	}

	public ProbeTcpFlags getTcpFlags() {
		return _tcpFlags;
	}

	public void setTcpFlags(ProbeTcpFlags tcpFlags) {
		_tcpFlags = tcpFlags;
	}
	
	public ProbeOptions getProbeOptions() {
		return _probeOptions;
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
		req._tcpFlags = _tcpFlags;
		req._probeOptions.setFlags(_probeOptions.getFlags());
		return req;
	}


}
