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

package fr.univrennes1.cri.jtacl.analysis;

import fr.univrennes1.cri.jtacl.equipments.openbsd.AddressFamily;
import fr.univrennes1.cri.jtacl.lib.ip.IPIcmpEnt;
import fr.univrennes1.cri.jtacl.lib.ip.IPNet;
import fr.univrennes1.cri.jtacl.lib.ip.PortSpec;
import fr.univrennes1.cri.jtacl.lib.ip.TcpFlags;
import fr.univrennes1.cri.jtacl.lib.misc.Direction;

/**
 * Simplified rule
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class SimplifiedRule {

	/**
	 * action pass
	 */
	public static final String PASS = "pass";

	/**
	 * action match
	 */
	public static final String MATCH = "match";

	/**
	 * action block
	 */
	public static final String BLOCK = "block";

	/**
	 * action (pass, match, block)
	 */
	protected String _action;

	/**
	 * direction
	 */
	protected Direction _direction;

	/**
	 * quick rule
	 */
	protected boolean _quick;

	/**
	 * interface of the rule
	 */
	protected String _ifName;

	/**
	 * protocol
	 */
	protected Integer _protocol;

	/**
	 * Address family
	 */
	protected AddressFamily _af;

	/**
	 * ALL IP specification
	 */
	protected boolean _all;

	/**
	 * from IP specification
	 */
	protected IPNet _fromIpSpec;

	/**
	 * from port specification
	 */
	protected PortSpec _fromPortSpec;

	/**
	 * to IP specification
	 */
	protected IPNet _toIpSpec;

	/**
	 * to port specification
	 */
	protected PortSpec _toPortSpec;

	/**
	 * icmp specification
	 */
	protected IPIcmpEnt _icmpspec;

	/**
	 * tcp flags
	 */
	protected TcpFlags _flags;

	/**
	 * Out-of tcp flags set
	 */
	protected TcpFlags _flagset;

	/**
	 * Returns the action of this rule (pass, match, block).
	 * @return the action of this rule.
	 */
	public String getAction() {
		return _action;
	}

	/**
	 * Sets the action of this rule (pass, match, block).
	 * @param action action to set.
	 */
	public void setAction(String action) {
		_action = action;
	}

	/**
	 * Returns the address family of this rule.
	 * @return the address familly of this rule.
	 */
	public AddressFamily getAf() {
		return _af;
	}

	/**
	 * Sets the address family of this rule.
	 * @param af address family to set.
	 */
	public void setAf(AddressFamily af) {
		_af = af;
	}

	/**
	 * Returns true if the fromTo specification of this rule is "ALL".
	 * @return true if the fromTo specification of this rule is "ALL".
	 */
	public boolean isAll() {
		return _all;
	}

	/**
	 * Sets the fromTo specification of this rule to all if the argument is true.
	 * @param all true if the specification is ALL.
	 */
	public void setAll(boolean all) {
		_all = all;
	}

	/**
	 * Checks if the action is set to "block".
	 * @return true if the action is set to "block".
	 */
	public boolean isBlock() {
		return _action != null && _action.equals(BLOCK);
	}

	/**
	 * Checks if the action is set to "match".
	 * @return true if the action is set to "match".
	 */
	public boolean isMatch() {
		return _action != null && _action.equals(MATCH);
	}

	/**
	 * Checks if the action is set to "pass".
	 * @return true if the action is set to "pass".
	 */
	public boolean isPass() {
		return _action != null && _action.equals(PASS);
	}

	/**
	 * Returns the direction of this rule.
	 * @return the direction of this rule.
	 */
	public Direction getDirection() {
		return _direction;
	}

	/**
	 * Sets the direction of this rule.
	 * @param direction direction to set.
	 */
	public void setDirection(Direction direction) {
		_direction = direction;
	}

	/**
	 * Returns the from ip specification of this rule.
	 * @return the from ip specification of this rule.
	 */
	public IPNet getFromIpSpec() {
		return _fromIpSpec;
	}

	/**
	 * Sets  the from ip specification of this rule.
	 * @param fromIpSpec from ip specification to set.
	 */
	public void setFromIpSpec(IPNet fromIpSpec) {
		_fromIpSpec = fromIpSpec;
	}

	/**
	 * Returns the from port specification of this rule.
	 * @return the from port specification of this rule.
	 */
	public PortSpec getFromPortSpec() {
		return _fromPortSpec;
	}

	/**
	 * Sets the from port specification of this rule.
	 * @param fromPortSpec port specification to set.
	 */
	public void setFromPortSpec(PortSpec fromPortSpec) {
		_toPortSpec = fromPortSpec;
	}

	/**
	 * returns the interface of this rule.
	 * @return the interface of this rule.
	 */
	public String getIfName() {
		return _ifName;
	}

	/**
	 * Sets the interface name of this rule.
	 * @param ifName interface name to set.
	 */
	public void setIfName(String ifName) {
		_ifName = ifName;
	}

	/**
	 * returns the protocol of this rule.
	 * @return the protocol of this rule.
	 */
	public Integer getProtocol() {
		return _protocol;
	}

	/**
	 * Sets the protocol of this rule.
	 * @param protocol protocol to set.
	 */
	public void setProtocol(Integer protocol) {
		_protocol = protocol;
	}

	/**
	 * Retuns true if this rule is a quick rule.
	 * @return true if this rule is a quick rule.
	 */
	public boolean isQuick() {
		return _quick;
	}

	/**
	 * Sets the quick option of this rule.
	 * @param quick quick option to set.
	 */
	public void setQuick(boolean quick) {
		_quick = quick;
	}

	/**
	 * Returns the to ip specification of this rule.
	 * @return the to ip specification of this rule.
	 */
	public IPNet getToIpSpec() {
		return _toIpSpec;
	}

	/**
	 * Sets  the to ip specification of this rule.
	 * @param toIpSpec from ip specification to set.
	 */
	public void setToIpSpec(IPNet toIpSpec) {
		_toIpSpec = toIpSpec;
	}

	/**
	 * Returns the to port specification of this rule.
	 * @return the to port specification of this rule.
	 */
	public PortSpec getToPortSpec() {
		return _toPortSpec;
	}

	/**
	 * Sets the to port specification of this rule.
	 * @param toPortSpec port specification to set.
	 */
	public void setToPortSpec(PortSpec toPortSpec) {
		_toPortSpec = toPortSpec;
	}

	/**
	 * Returns the icmp specification of this rule.
	 * @return the icmp specification of this rule.
	 */
	public IPIcmpEnt getIcmpspec() {
		return _icmpspec;
	}

	/**
	 * Sets the icmp specification of this rule.
	 * @param icmpspec icmp specification to set.
	 */	
	public void setIcmpspec(IPIcmpEnt icmpspec) {
		_icmpspec = icmpspec;
	}

	/**
	 * Returns the tcp flags of this rule (null = any).
	 * @return the tcp flags of this rule.
	 */
	public TcpFlags getFlags() {
		return _flags;
	}

	/**
	 * Sets the tcp flags of this rule (null = any).
	 * @param flags flags to set.
	 */
	public void setFlags(TcpFlags flags) {
		 _flags = flags;
	}

	/**
	 * Returns the out-of tcp flags of this rule (null = none).
	 * @return the out-of tcp flags of this rule.
	 */
	public TcpFlags getFlagset() {
		return _flagset;
	}

	/**
	 * Sets the out-of tcp flags of this rule (null = none).
	 * @param flags flags to set.
	 */
	public void setFlagset(TcpFlags flags) {
		 _flagset = flags;
	}

}
