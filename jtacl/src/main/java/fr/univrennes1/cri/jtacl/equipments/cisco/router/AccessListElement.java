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

package fr.univrennes1.cri.jtacl.equipments.cisco.router;

import fr.univrennes1.cri.jtacl.lib.ip.IPNet;
import fr.univrennes1.cri.jtacl.lib.misc.StringsList;

/**
 * Describes an access list element (ACE).
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class AccessListElement {

	/**
	 * the line of configuration corresponding to this ace (may be null)
	 */
	protected String _configurationLine;

	/**
	 * action (permit, deny)
	 */
	protected String _action;

	/**
	 * protocol
	 */
	protected Integer _protocol;

	/**
	 * source IP
	 */
	protected IPNet _sourceIp;

	/**
	 * source IP netmask
	 */
	protected IPNet _sourceNetmask;

	/**
	 * source port
	 */
	protected PortObject _sourcePortObject;

	/**
	 * destination IP
	 */
	protected IPNet _destIp;

	/**
	 * destination IP netmask
	 */
	protected IPNet _destNetmask;

	/**
	 * destination port
	 */
	protected PortObject _destPortObject;

	/**
	 * sub-type of the protocol (ex icmp-type echo)
	 */
	protected Integer _subType;

	/**
	 * Code of the protocol, the value depends of the protocol.
	 * For example, for icmp the code is the icmp code message.
	 */
	protected Integer _code;

	/**
	 * true if the acl is inactive
	 */
	protected boolean _inactive;

	/**
	 * TCP flags keyword (null | match-any | match-all)
	 */
	protected String _tcpKeyword;

	/**
	 * TCP flags (+syn, -ack, ...)
	 */
	protected StringsList _tcpFlags = new StringsList();

	/**
	 * Constructs a new ace.
	 */
	public AccessListElement() {
		super();
	}

	/**
	 * Returns the action of this ace.
	 * @return the action of this ace.
	 */
	public String getAction() {
		return _action;
	}

	/**
	 * Sets the action of this ace.
	 * @param action the action to set.
	 */
	public void setAction(String action) {
		_action = action;
	}

	/**
	 * Returns the line of configuration corresponding to this ace.
	 * @return the line of configuration corresponding to this ace.
	 */
	public String getConfigurationLine() {
		return _configurationLine;
	}

	/**
	 * Sets the line of configuration corresponding to this ace.
	 * @param configurationLine line to set.
	 */
	public void setConfigurationLine(String configurationLine) {
		_configurationLine = configurationLine;
	}

	/**
	 * Returns the destination port object of this ace.
	 * @return the destination port object of this ace.
	 */
	public PortObject getDestPortObject() {
		return _destPortObject;
	}

	/**
	 * Sets the destination port object of this ace.
	 * @param destPortObject destination port object to set.
	 */
	public void setDestPortObject(PortObject destPortObject) {
		_destPortObject = destPortObject;
	}

	/**
	 * Returns the protocol of this ace.
	 * @return the protocol of this ace.
	 */
	public Integer getProtocol() {
		return _protocol;
	}

	/**
	 * Sets the protocol of this ace.
	 * @param protocol protocol to set
	 */
	public void setProtocol(Integer protocol) {
		_protocol = protocol;
	}

	/**
	 * Returns the source port object of this ace.
	 * @return the source port object of this ace.
	 */
	public PortObject getSourcePortObject() {
		return _sourcePortObject;
	}

	/**
	 * Sets the source port port object of this ace.
	 * @param sourcePortObject source port object to set.
	 */
	public void setSourcePortObject(PortObject sourcePortObject) {
		_sourcePortObject = sourcePortObject;
	}

	/**
	 * Returns the destination IP address of this ace.
	 * @return the destination IP address of this ace.
	 */
	public IPNet getDestIp() {
		return _destIp;
	}

	/**
	 * Sets the destination IP address of this ace.
	 * @param destIp destination IP adress to set.
	 */
	public void setDestIp(IPNet destIp) {
		_destIp = destIp;
	}

	/**
	 * Returns the destination IP netmask of this ace.
	 * @return the destination IP netmask of this ace.
	 */
	public IPNet getDestNetmask() {
		return _destNetmask;
	}

	/**
	 * Sets the destination IP netmask of this ace.
	 * @param destNetmask destination IP netmask to set.
	 */
	public void setDestNetmask(IPNet destNetmask) {
		_destNetmask = destNetmask;
	}

	/**
	 * Checks if this ace is inactive.
	 * @return true if this ace is inactive.
	 */
	public boolean isInactive() {
		return _inactive;
	}

	/**
	 * Sets the inactive flag  of this ace.
	 * @param inactive inactive flag to set.
	 */
	public void setInactive(boolean inactive) {
		_inactive = inactive;
	}

	/**
	 * Returns the source IP address of this ace.
	 * @return the source IP address of this ace.
	 */
	public IPNet getSourceIp() {
		return _sourceIp;
	}

	/**
	 * Sets the source IP address of this ace.
	 * @param sourceIp source IP adress to set.
	 */
	public void setSourceIp(IPNet sourceIp) {
		this._sourceIp = sourceIp;
	}

	/**
	 * Returns the source IP netmask of this ace.
	 * @return the source IP netmask of this ace.
	 */
	public IPNet getSourceNetmask() {
		return _sourceNetmask;
	}

	/**
	 * Sets the source IP netmask of this ace.
	 * @param sourceNetmask source IP netmask to set.
	 */
	public void setSourceNetmask(IPNet sourceNetmask) {
		_sourceNetmask = sourceNetmask;
	}

	/**
	 * Returns the sub-type of the protocol. For example, with icmp it could be
	 * echo, echo-reply...
	 * @return the sub-type of the protocol.
	 */
	public Integer getSubType() {
		return _subType;
	}

	/**
	 * Sets the sub-type of the protocol. For example, with icmp it could be
	 * echo, echo-reply...
	 * @param subType sub-type to set.
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
	 * Returns the list tcp flags needed for this ACE.
	 * (+ack, -ack, ...)
	 * @return a non null List containing the flags.
	 */
	public StringsList getTcpFlags() {
		return _tcpFlags;
	}

	/**
	 * Returns the keyword associated to the tcp flags:
	 * match-any or match-all
	 * @return the keyword associated with the tcp flags.
	 */
	public String getTcpKeyword() {
		return _tcpKeyword;
	}

	/**
	 * Sets the keyword associated to the tcp flags.
	 * @param tcpKeyword keyword to set.
	 */
	public void setTcpKeyword(String tcpKeyword) {
		_tcpKeyword = tcpKeyword;
	}

}
