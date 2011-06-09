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

	public boolean setAccessListId(String accessListId) {
		_accessListId = accessListId;
		return true;
	}

	public String getAction() {
		return _action;
	}

	public boolean setAction(String action) {
		_action = action;
		return true;
	}

	public String getDstFirstPort() {
		return _dstFirstPort;
	}

	public boolean setDstFirstPort(String dstFirstPort) {
		_dstFirstPort = dstFirstPort;
		return true;
	}

	public String getDstIfName() {
		return _dstIfName;
	}

	public boolean setDstIfName(String dstIfName) {
		_dstIfName = dstIfName;
		return true;
	}

	public String getDstIp() {
		return _dstIp;
	}

	public boolean setDstIp(String dstIp) {
		_dstIp = dstIp;
		return true;
	}

	public String getDstIpMask() {
		return _dstIpMask;
	}

	public boolean setDstIpMask(String dstIpMask) {
		_dstIpMask = dstIpMask;
		return true;
	}

	public String getDstLastPort() {
		return _dstLastPort;
	}

	public boolean setDstLastPort(String dstLastPort) {
		_dstLastPort = dstLastPort;
		return true;
	}

	public String getDstNetworkGroup() {
		return _dstNetworkGroup;
	}

	public boolean setDstNetworkGroup(String dstNetworkGroup) {
		_dstNetworkGroup = dstNetworkGroup;
		return true;
	}

	public String getDstPortOperator() {
		return _dstPortOperator;
	}

	public boolean setDstPortOperator(String dstPortOperator) {
		_dstPortOperator = dstPortOperator;
		return true;
	}

	public String getDstServiceGroup() {
		return _dstServiceGroup;
	}

	public boolean setDstServiceGroup(String dstServiceGroup) {
		_dstServiceGroup = dstServiceGroup;
		return true;
	}

	public String getDstEnhancedServiceGroup() {
		return _dstEnhancedServiceGroup;
	}

	public boolean setDstEnhancedServiceGroup(String dstEnhancedServiceGroup) {
		_dstEnhancedServiceGroup = dstEnhancedServiceGroup;
		return true;
	}

	public String getIcmp() {
		return _icmp;
	}

	public boolean setIcmp(String icmp) {
		_icmp = icmp;
		return true;
	}

	public String getIcmpGroup() {
		return _icmpGroup;
	}

	public boolean setIcmpGroup(String icmpGroup) {
		_icmpGroup = icmpGroup;
		return true;
	}

	public boolean getInactive() {
		return _inactive;
	}
	
	public boolean setInactive(boolean inactive) {
		_inactive = inactive;
		return true;
	}

	public String getProtocol() {
		return _protocol;
	}

	public boolean setProtocol(String protocol) {
		_protocol = protocol;
		return true;
	}

	public String getProtocolGroupId() {
		return _protocolGroupId;
	}

	public boolean setProtocolGroupId(String protocolGroupId) {
		_protocolGroupId = protocolGroupId;
		return true;
	}

	public String getSrcFirstPort() {
		return _srcFirstPort;
	}

	public boolean setSrcFirstPort(String srcFirstPort) {
		_srcFirstPort = srcFirstPort;
		return true;
	}

	public String getSrcIfName() {
		return _srcIfName;
	}

	public boolean setSrcIfName(String srcIfName) {
		_srcIfName = srcIfName;
		return true;
	}

	public String getSrcIp() {
		return _srcIp;
	}

	public boolean setSrcIp(String srcIp) {
		_srcIp = srcIp;
		return true;
	}

	public String getSrcIpMask() {
		return _srcIpMask;
	}

	public boolean setSrcIpMask(String srcIpMask) {
		_srcIpMask = srcIpMask;
		return true;
	}

	public String getSrcLastPort() {
		return _srcLastPort;
	}

	public boolean setSrcLastPort(String srcLastPort) {
		_srcLastPort = srcLastPort;
		return true;
	}

	public String getSrcNetworkGroup() {
		return _srcNetworkGroup;
	}

	public boolean setSrcNetworkGroup(String srcNetworkGroup) {
		_srcNetworkGroup = srcNetworkGroup;
		return true;
	}

	public String getSrcPortOperator() {
		return _srcPortOperator;
	}

	public boolean setSrcPortOperator(String srcPortOperator) {
		_srcPortOperator = srcPortOperator;
		return true;
	}

	public String getSrcServiceGroup() {
		return _srcServiceGroup;
	}

	public boolean setSrcServiceGroup(String srcServiceGroup) {
		_srcServiceGroup = srcServiceGroup;
		return true;
	}

	public String getRemark() {
		return _remark;
	}

	public boolean setRemark(String remark) {
		_remark = remark;
		return true;
	}

}
