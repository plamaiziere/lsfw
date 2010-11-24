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

import fr.univrennes1.cri.jtacl.lib.misc.StringsList;

/**
 * Template to build ACE (access-list element). This class is used at parsing time
 * as an intermediate storage.
 * @see AccessListElement
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class AceTemplate {
	private String _action;
	private String _protocol;
	private String _srcIp;
	private String _srcIpMask;
	private String _srcPortOperator;
	private String _srcFirstPort;
	private String _srcLastPort;
	private String _dstIp;
	private String _dstIpMask;
	private String _dstPortOperator;
	private String _dstFirstPort;
	private String _dstLastPort;
	private String _subType;
	private Integer _code;
	private boolean _inactive;
	private StringsList _tcpFlags = new StringsList();
	private String _tcpKeyword;

	public String getAction() {
		return _action;
	}

	public void setAction(String action) {
		_action = action;
	}

	public String getDstFirstPort() {
		return _dstFirstPort;
	}

	public void setDstFirstPort(String dstFirstPort) {
		_dstFirstPort = dstFirstPort;
	}

	public String getDstIp() {
		return _dstIp;
	}

	public void setDstIp(String dstIp) {
		_dstIp = dstIp;
	}

	public String getDstIpMask() {
		return _dstIpMask;
	}

	public void setDstIpMask(String dstIpMask) {
		_dstIpMask = dstIpMask;
	}

	public String getDstLastPort() {
		return _dstLastPort;
	}

	public void setDstLastPort(String dstLastPort) {
		_dstLastPort = dstLastPort;
	}

	public String getDstPortOperator() {
		return _dstPortOperator;
	}

	public void setDstPortOperator(String dstPortOperator) {
		_dstPortOperator = dstPortOperator;
	}

	public String getSubType() {
		return _subType;
	}

	public void setSubType(String subType) {
		this._subType = subType;
	}

	public Integer getCode() {
		return _code;
	}

	public void setCode(Integer code) {
		_code = code;
	}

	public boolean getInactive() {
		return _inactive;
	}
	
	public void setInactive(boolean inactive) {
		_inactive = inactive;
	}

	public String getProtocol() {
		return _protocol;
	}

	public void setProtocol(String protocol) {
		_protocol = protocol;
	}

	public String getSrcFirstPort() {
		return _srcFirstPort;
	}

	public void setSrcFirstPort(String srcFirstPort) {
		_srcFirstPort = srcFirstPort;
	}

	public String getSrcIp() {
		return _srcIp;
	}

	public void setSrcIp(String srcIp) {
		_srcIp = srcIp;
	}

	public String getSrcIpMask() {
		return _srcIpMask;
	}

	public void setSrcIpMask(String srcIpMask) {
		_srcIpMask = srcIpMask;
	}

	public String getSrcLastPort() {
		return _srcLastPort;
	}

	public void setSrcLastPort(String srcLastPort) {
		_srcLastPort = srcLastPort;
	}

	public String getSrcPortOperator() {
		return _srcPortOperator;
	}

	public void setSrcPortOperator(String srcPortOperator) {
		_srcPortOperator = srcPortOperator;
	}

	public StringsList getTcpFlags() {
		return _tcpFlags;
	}

	public String getTcpKeyword() {
		return _tcpKeyword;
	}

	public void setTcpKeyword(String _tcpKeyword) {
		this._tcpKeyword = _tcpKeyword;
	}

}
