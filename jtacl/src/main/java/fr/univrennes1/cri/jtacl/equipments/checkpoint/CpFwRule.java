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

package fr.univrennes1.cri.jtacl.equipments.checkpoint;

/**
 * Checkpoint firewall rule
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class CpFwRule {

	protected String _name;
	protected String _className;
	protected String _comment;
	protected Integer _number;
	protected boolean _disabled;

	protected String _action;

	protected CpFwIpSpec _sourceIp;
	protected CpFwIpSpec _destIp;
	protected CpFwServicesSpec _services;

	protected boolean _implicitDrop;

	public String getName() {
		return _name;
	}

	public String getClassName() {
		return _className;
	}

	public String getComment() {
		return _comment;
	}

	public Integer getNumber() {
		return _number;
	}

	public boolean isDisabled() {
		return _disabled;
	}

	public String getAction() {
		return _action;
	}

	public void setAction(String _action) {
		this._action = _action;
	}

	public CpFwIpSpec getSourceIp() {
		return _sourceIp;
	}

	public CpFwIpSpec getDestIp() {
		return _destIp;
	}

	public CpFwServicesSpec getServices() {
		return _services;
	}

	public boolean isImplicitDrop() {
		return _implicitDrop;
	}

	/**
	 * Construct a new fw rule
	 * @param name name
	 * @param className class name
	 * @param comment comment
	 * @param number rule number
	 * @param disabled rule disabled
	 * @param srcIpSpec source IP specification
	 * @param dstIpSpec destination IP specification
	 * @param servicesSpec services specification
	 * @param action action class name
	 */
	public CpFwRule(String name, String className, String comment,
			Integer number, boolean disabled,
			CpFwIpSpec srcIpSpec, CpFwIpSpec dstIpSpec,
			CpFwServicesSpec servicesSpec, String action) {
		_name = name;
		_className = className;
		_comment = comment;
		_number = number;
		_disabled = disabled;
		_sourceIp = srcIpSpec;
		_destIp = dstIpSpec;
		_services = servicesSpec;
		_action = action;
	}

	/**
	 * Construct a new fw rule (implicit drop)
	 * @param name name
	 * @param className class name
	 */
	public CpFwRule(String name, String className) {
		_name = name;
		_className = className;
		_comment = "implicit drop rule";
		_number = 999999;
		_action = "drop_action";
		_implicitDrop = true;
	}

	@Override
	public String toString() {
		return "rule name=" + _name + ", className=" + _className
				+ ", comment=" + _comment + ", number=" + _number +
				", disabled=" + _disabled + ", implicit= " + _implicitDrop
				+ ", action=" + _action + ", sourceIp=" + _sourceIp
				+ ", destIp=" + _destIp	+ " services=" + _services;
	}

	public String toText() {
		if (isImplicitDrop())
			return "*** implicit drop ***";

		String s = "# " + _number;
		if (_name != null)
			s+= ", name: " + _name;

		s += ", from: " +
			_sourceIp.getNetworks().getBaseReferencesName() +
			", to: " + _destIp.getNetworks().getBaseReferencesName() +
			", services: " + _services.getServices().getReferencesName() +
			", action: " + _action;
		if (_comment != null)
			s+= ", # "	+ _comment;
		return s;
	}

}
