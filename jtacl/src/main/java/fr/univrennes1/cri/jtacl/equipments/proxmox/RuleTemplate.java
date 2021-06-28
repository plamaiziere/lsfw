/*
 * Copyright (c) 2021, Universite de Rennes 1
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
package fr.univrennes1.cri.jtacl.equipments.proxmox;

import java.util.List;

/**
 * Template to build rule. This class is used at parsing time
 * as an intermediate storage.
 * @see
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class RuleTemplate {

	protected boolean _disabled = false;
	protected String _direction;
	protected String _action;
	protected String _macro;
	protected List<String> _sourceIpSpec;
	protected List<String> _destIpSpec;
	protected List<String> _sourcePortSpec;
	protected List<String> _destPortSpec;
	protected String _icmpType;
	protected String _proto;
	protected String _iface;
	protected String _groupName;

	public boolean isDisabled() {
		return _disabled;
	}

	public boolean setDisabled(boolean disabled) {
		this._disabled = disabled;
		return true;
	}

	public String getDirection() {
		return _direction;
	}

	public boolean setDirection(String direction) {
		this._direction = direction;
		return true;
	}

	public String getAction() {
		return _action;
	}

	public boolean setAction(String action) {
		this._action = action;
		return true;
	}

	public String getMacro() {
		return _macro;
	}

	public boolean setMacro(String macro) {
		this._macro = macro;
		return true;
	}

	protected boolean setIcmpType(String s) {
		_icmpType = s;
		return true;
	}

	protected boolean setProto(String s) {
		_proto = s;
		return true;
	}

	protected boolean setIface(String s) {
		_iface = s;
		return true;
	}

	public String getIcmpType() {
		return _icmpType;
	}

	public String getProto() {
		return _proto;
	}

	public String getIface() {
		return _iface;
	}

	public List<String> getSourcePortSpec() {
		return _sourcePortSpec;
	}

	public boolean setSourcePortSpec(List<String> sourcePortSpec) {
		this._sourcePortSpec = sourcePortSpec;
		return true;
	}

	public List<String> getDestPortSpec() {
		return _destPortSpec;
	}

	public boolean setDestPortSpec(List<String> destPortSpec) {
		this._destPortSpec = destPortSpec;
		return true;
	}

	public List<String> getSourceIpSpec() {
		return _sourceIpSpec;
	}

	public boolean setSourceIpSpec(List<String> sourceIpSpec) {
		this._sourceIpSpec = sourceIpSpec;
		return true;
	}

	public List<String> getDestIpSpec() {
		return _destIpSpec;
	}

	public boolean setDestIpSpec(List<String> destIpSpec) {
		this._destIpSpec = destIpSpec;
		return true;
	}

	public String getGroupName() {
		return _groupName;
	}

	public boolean setGroupName(String groupName) {
		this._groupName = groupName;
		return true;
	}

	public boolean isGroup() {
		return _groupName != null;
	}
}
