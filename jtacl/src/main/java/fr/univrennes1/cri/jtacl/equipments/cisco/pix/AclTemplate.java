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

package fr.univrennes1.cri.jtacl.equipments.cisco.pix;

/**
 * Template to build Access List. This class is used at parsing time as an
 * intermediate storage.
 * @see AccessList
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class AclTemplate {
	private String _accessListId;
	private String _action;
	private String _protocol;
	private String _protocolGroupId;
	private String _srcIp;
	private String _srcIpMask;
	private String _srcIfName;
	private String _srcNetworkGroup;
	private String _srcPortOperator;
	private String _srcFirstPort;
	private String _srcLastPort;
	private String _srcServiceGroup;
	private String _dstIp;
	private String _dstIpMask;
	private String _dstIfName;
	private String _dstNetworkGroup;
	private String _dstPortOperator;
	private String _dstFirstPort;
	private String _dstLastPort;
	private String _dstServiceGroup;
	private String _dstEnhancedServiceGroup;
	private String _icmp;
	private String _icmpGroup;
	private boolean _inactive;
	private String _remark;

	public String getAccessListId() {
		return _accessListId;
	}

	public void setAccessListId(String accessListId) {
		_accessListId = accessListId;
	}

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

	public String getDstIfName() {
		return _dstIfName;
	}

	public void setDstIfName(String dstIfName) {
		_dstIfName = dstIfName;
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

	public String getDstNetworkGroup() {
		return _dstNetworkGroup;
	}

	public void setDstNetworkGroup(String dstNetworkGroup) {
		_dstNetworkGroup = dstNetworkGroup;
	}

	public String getDstPortOperator() {
		return _dstPortOperator;
	}

	public void setDstPortOperator(String dstPortOperator) {
		_dstPortOperator = dstPortOperator;
	}

	public String getDstServiceGroup() {
		return _dstServiceGroup;
	}

	public void setDstServiceGroup(String dstServiceGroup) {
		_dstServiceGroup = dstServiceGroup;
	}

	public String getDstEnhancedServiceGroup() {
		return _dstEnhancedServiceGroup;
	}

	public void setDstEnhancedServiceGroup(String dstEnhancedServiceGroup) {
		_dstEnhancedServiceGroup = dstEnhancedServiceGroup;
	}

	public String getIcmp() {
		return _icmp;
	}

	public void setIcmp(String _icmp) {
		this._icmp = _icmp;
	}

	public String getIcmpGroup() {
		return _icmpGroup;
	}

	public void setIcmpGroup(String icmpGroup) {
		_icmpGroup = icmpGroup;
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

	public String getProtocolGroupId() {
		return _protocolGroupId;
	}

	public void setProtocolGroupId(String protocolGroupId) {
		_protocolGroupId = protocolGroupId;
	}

	public String getSrcFirstPort() {
		return _srcFirstPort;
	}

	public void setSrcFirstPort(String srcFirstPort) {
		_srcFirstPort = srcFirstPort;
	}

	public String getSrcIfName() {
		return _srcIfName;
	}

	public void setSrcIfName(String srcIfName) {
		_srcIfName = srcIfName;
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

	public String getSrcNetworkGroup() {
		return _srcNetworkGroup;
	}

	public void setSrcNetworkGroup(String srcNetworkGroup) {
		_srcNetworkGroup = srcNetworkGroup;
	}

	public String getSrcPortOperator() {
		return _srcPortOperator;
	}

	public void setSrcPortOperator(String srcPortOperator) {
		_srcPortOperator = srcPortOperator;
	}

	public String getSrcServiceGroup() {
		return _srcServiceGroup;
	}

	public void setSrcServiceGroup(String srcServiceGroup) {
		_srcServiceGroup = srcServiceGroup;
	}

	public String getRemark() {
		return _remark;
	}

	public void setRemark(String remark) {
		_remark = remark;
	}

}
