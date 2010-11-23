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

import fr.univrennes1.cri.jtacl.lib.ip.IPNet;

/**
 * Describes an access list.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class AccessList {

	/**
	 * the line of configuration corresponding to this acl (may be null)
	 */
	protected String _configurationLine;

	/**
	 * access list id
	 */
	protected String _accessListId;

	/**
	 * access group associated
	 */
	protected AccessGroup _accessGroup;

	/**
	 * action (permit, deny)
	 */
	protected String _action;

	/**
	 * protocol
	 */
	protected Integer _protocol;

	/**
	 * protocol object group
	 */
	protected ProtocolObjectGroup _protocolGroup;

	/**
	 * source IP address
	 */
	protected IPNet _sourceIp;

	/**
	 * source network object group
	 */
	protected NetworkObjectGroup _sourceNetworkGroup;

	/**
	 * source port.
	 */
	protected PortObject _sourcePortObject;

	/**
	 * source service group.
	 */
	protected ServiceObjectGroup _sourceServiceGroup;

	/**
	 * destination IP address
	 */
	protected IPNet _destIp;

	/**
	 * destination network group.
	 */
	protected NetworkObjectGroup _destNetworkGroup;

	/**
	 * destination port
	 */
	protected PortObject _destPortObject;

	/**
	 * destination service group.
	 */
	protected ServiceObjectGroup _destServiceGroup;

	/**
	 * enhanced service group. Also defines the protocols of the acl.
	 */
	protected EnhancedServiceObjectGroup _enhancedDestServiceGroup;

	/**
	 * icmp-type
	 */
	protected Integer _icmp;

	/**
	 * Icmp group (icmp only)
	 */
	protected IcmpObjectGroup _icmpGroup;

	/**
	 * true if the acl is inactive
	 */
	protected boolean _inactive;

	/**
	 * remark
	 */
	protected String _remark;

	/**
	 * Constructs a new access list with the access-list id in argument.
	 * @param accessListId access-list id of the acl.
	 */
	public AccessList(String accessListId) {
		_accessListId = accessListId;
	}

	/**
	 * Returns the access-list id of this acl.
	 * @return the access-list id of this acl.
	 */
	public String getAccessListId() {
		return _accessListId;
	}

	/**
	 * Returns the access-group associated to this acl.
	 * @return the access-group associated to this acl.
	 */
	public AccessGroup getAccessGroup() {
		return _accessGroup;
	}

	/**
	 * Sets the access-group associated to this acl.
	 * @param group the access-group to associate.
	 */
	public void setAccessGroup(AccessGroup group) {
		_accessGroup = group;
	}

	/**
	 * Returns the action of this acl.
	 * @return the action of this acl.
	 */
	public String getAction() {
		return _action;
	}

	/**
	 * Sets the action of this acl.
	 * @param action the action to set.
	 */
	public void setAction(String action) {
		_action = action;
	}

	/**
	 * Returns the line of configuration corresponding to this acl.
	 * @return the line of configuration corresponding to this acl.
	 */
	public String getConfigurationLine() {
		return _configurationLine;
	}

	/**
	 * Sets the line of configuration corresponding to this acl.
	 * @param configurationLine line to set.
	 */
	public void setConfigurationLine(String configurationLine) {
		_configurationLine = configurationLine;
	}

	/**
	 * Returns the destination network-group of this acl.
	 * @return the destination network-group of this acl.
	 */
	public NetworkObjectGroup getDestNetworkGroup() {
		return _destNetworkGroup;
	}

	/**
	 * Sets the destination network-group of this acl.
	 * @param destNetworkGroup destination network-group to set.
	 */
	public void setDestNetworkGroup(NetworkObjectGroup destNetworkGroup) {
		_destNetworkGroup = destNetworkGroup;
	}

	/**
	 * Returns the destination port object of this acl.
	 * @return the destination port object of this acl.
	 */
	public PortObject getDestPortObject() {
		return _destPortObject;
	}

	/**
	 * Sets the destination port object of this acl.
	 * @param destPortObject destination port object to set.
	 */
	public void setDestPortObject(PortObject destPortObject) {
		_destPortObject = destPortObject;
	}

	/**
	 * Returns the destination service-group of this acl.
	 * @return the destination service-group of this acl.
	 */
	public ServiceObjectGroup getDestServiceGroup() {
		return _destServiceGroup;
	}

	/**
	 * Sets the destination service-group of this acl.
	 * @param destServiceGroup destination service-group to set.
	 */
	public void setDestServiceGroup(ServiceObjectGroup destServiceGroup) {
		_destServiceGroup = destServiceGroup;
	}

	/**
	 * Returns the enhanced destination service-group of this acl.
	 * @return the enhanced destination service-group of this acl.
	 */
	public EnhancedServiceObjectGroup getEnhancedDestServiceGroup() {
		return _enhancedDestServiceGroup;
	}

	/**
	 * Sets the enhanced destination service-group of this acl.
	 * @param enhancedDestServiceGroup enhanced service-group to set.
	 */
	public void setEnhancedDestServiceGroup(
			EnhancedServiceObjectGroup enhancedDestServiceGroup) {

		_enhancedDestServiceGroup = enhancedDestServiceGroup;
	}

	/**
	 * Returns the protocol of this acl.
	 * @return the protocol of this acl.
	 */
	public Integer getProtocol() {
		return _protocol;
	}

	/**
	 * Sets the protocol of this acl.
	 * @param protocol protocol to set
	 */
	public void setProtocol(Integer protocol) {
		_protocol = protocol;
	}

	/**
	 * Returns the protocol-group of this acl.
	 * @return the protocol-group of this acl.
	 */
	public ProtocolObjectGroup getProtocolGroup() {
		return _protocolGroup;
	}

	/**
	 * Sets the protocol-group of this acl.
	 * @param protocolGroup protocol-group to set.
	 */
	public void setProtocolGroup(ProtocolObjectGroup protocolGroup) {
		_protocolGroup = protocolGroup;
	}

	/**
	 * Returns the source network-group of this acl.
	 * @return the source network-group of this acl.
	 */
	public NetworkObjectGroup getSourceNetworkGroup() {
		return _sourceNetworkGroup;
	}

	/**
	 * Sets the source network-group of this acl.
	 * @param sourceNetworkGroup source network-group to set.
	 */
	public void setSourceNetworkGroup(NetworkObjectGroup sourceNetworkGroup) {
		_sourceNetworkGroup = sourceNetworkGroup;
	}

	/**
	 * Returns the source port object of this acl.
	 * @return the source port object of this acl.
	 */
	public PortObject getSourcePortObject() {
		return _sourcePortObject;
	}

	/**
	 * Sets the source port port object of this acl.
	 * @param sourcePortObject source port object to set.
	 */
	public void setSourcePortObject(PortObject sourcePortObject) {
		_sourcePortObject = sourcePortObject;
	}

	/**
	 * Returns the source service-group of this acl.
	 * @return the source service-group of this acl.
	 */
	public ServiceObjectGroup getSourceServiceGroup() {
		return _sourceServiceGroup;
	}

	/**
	 * Sets the source service-group of this acl.
	 * @param sourceServiceGroup source service-group to set.
	 */
	public void setSourceServiceGroup(ServiceObjectGroup sourceServiceGroup) {
		_sourceServiceGroup = sourceServiceGroup;
	}

	/**
	 * Returns the destination IP address of this acl.
	 * @return the destination IP address of this acl.
	 */
	public IPNet getDestIp() {
		return _destIp;
	}

	/**
	 * Sets the destination IP address of this acl.
	 * @param destIp destination IP adress to set.
	 */
	public void setDestIp(IPNet destIp) {
		_destIp = destIp;
	}

	/**
	 * Checks if this acl is inactive.
	 * @return true if this acl is inactive.
	 */
	public boolean isInactive() {
		return _inactive;
	}

	/**
	 * Sets the inactive flag  of this acl.
	 * @param inactive inactive flag to set.
	 */
	public void setInactive(boolean inactive) {
		_inactive = inactive;
	}

	/**
	 * Returns the source IP address of this acl.
	 * @return the source IP address of this acl.
	 */
	public IPNet getSourceIp() {
		return _sourceIp;
	}

	/**
	 * Sets the source IP address of this acl.
	 * @param sourceIp source IP adress to set.
	 */
	public void setSourceIp(IPNet sourceIp) {
		_sourceIp = sourceIp;
	}

	/**
	 * Returns the icmp-type of this acl.
	 * @return the icmp-type of this acl.
	 */
	public Integer getIcmp() {
		return _icmp;
	}

	/**
	 * Sets the icmp-type of this acl.
	 * @param icmp the icmp-type to set.
	 */
	public void setIcmp(Integer icmp) {
		_icmp = icmp;
	}

	/**
	 * Returns the icmp-group of this acl.
	 * @return the icmp-group of this acl.
	 */
	public IcmpObjectGroup getIcmpGroup() {
		return _icmpGroup;
	}

	/**
	 * Sets the icmp-group of this acl.
	 * @param icmpGroup icmp-group to set.
	 */
	public void setIcmpGroup(IcmpObjectGroup icmpGroup) {
		_icmpGroup = icmpGroup;
	}

	/**
	 * Get the remark of this acl.
	 * @return the remark of this acl
	 */
	public String getRemark() {
		return _remark;
	}

	/**
	 * Returns true if this acl is a remark
	 * @return true if this acl is a remark
	 */
	public boolean isRemark() {
		return _remark != null;
	}

	/**
	 * Sets the remark of this acl.
	 * @param remark remark to set
	 */
	public void setRemark(String remark) {
		_remark = remark;
	}

}
