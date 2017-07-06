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

import java.util.List;

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
	protected boolean _headerText;

	protected String _action;
	protected CpFwRuleAction _ruleAction;

	protected CpFwIpSpec _sourceIp;
	protected CpFwIpSpec _destIp;
	protected CpFwServicesSpec _services;

	protected boolean _implicitDrop;

	protected List<String> _installGateway;

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

	public boolean isHeaderText() {
		return _headerText;
	}

	public boolean isSecurityRule() {
		return ! _headerText;
	}

	public String getAction() {
		return _action;
	}

	public void setAction(String action) {
		_action = action;
	}

	public CpFwRuleAction getRuleAction() {
		return _ruleAction;
	}

	public void setRuleAction(CpFwRuleAction ruleAction) {
		_ruleAction = ruleAction;
	}

	public boolean ruleActionIsAccept() {
		return _ruleAction != null &&
			(_ruleAction == CpFwRuleAction.ACCEPT
				|| _ruleAction == CpFwRuleAction.AUTH);
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

	public List<String> getInstallGateway() {
		return _installGateway;
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
	 * @param ruleAction rule action (Accept, drop etc)
	 * @param installGateway rule gateway installation
	 */
	public CpFwRule(String name, String className, String comment,
			Integer number, boolean disabled,
			CpFwIpSpec srcIpSpec, CpFwIpSpec dstIpSpec,
			CpFwServicesSpec servicesSpec, String action,
			CpFwRuleAction ruleAction,
			List<String> installGateway) {
		_name = name;
		_className = className;
		_comment = comment;
		_number = number;
		_disabled = disabled;
		_sourceIp = srcIpSpec;
		_destIp = dstIpSpec;
		_services = servicesSpec;
		_action = action;
		_ruleAction = ruleAction;
		_installGateway = installGateway;
	}

	/**
	 * Construct a new fw rule (implicit drop)
	 * @param name name
	 * @param className class name
	 * @param installGateway rule gateway installation
	 */
	public CpFwRule(String name, String className, List<String> installGateway) {
		_name = name;
		_className = className;
		_comment = "implicit drop rule";
		_number = 999999;
		_action = "drop_action";
		_ruleAction = CpFwRuleAction.DROP;
		_implicitDrop = true;
		_installGateway = installGateway;
	}

	/**
	 * Construct a new fw rule (headerText)
	 * @param name name
	 * @param className class name
	 * @param comment header text
	 * @param installGateway rule gateway installation
	 */
	public CpFwRule(String name, String className, String comment,
			List<String> installGateway) {
		_name = name;
		_className = "header_text";
		_comment = comment;
		_number = 0;
		_action = "none";
		_headerText = true;
		_installGateway = installGateway;
	}

	@Override
	public String toString() {
		return "rule name=" + _name + ", className=" + _className
				+ ", comment=" + _comment + ", number=" + _number +
				", disabled=" + _disabled + ", implicit= " + _implicitDrop
				+ ", action=" + _action + ", ruleAction=" + _ruleAction
				+ ", sourceIp=" + _sourceIp
				+ ", destIp=" + _destIp	+ " services=" + _services
				+ ", install=" + _installGateway;
	}

	public String toText() {
		if (isImplicitDrop())
			return "*** implicit drop ***";

		if (isHeaderText())
			return "### " + _comment;

		String s = "#" + _number;
		if (_name != null)
			s+= ", name: " + _name;

		String senabled = _disabled ? "disabled" : "enabled";
		String srcNot = _sourceIp.isNotIn() ? "!" : "";
		String destNot = _destIp.isNotIn() ? "!" : "";
		String servicesNot = _services.isNotIn() ? "!" : "";

		s += ", " + senabled +", from: " + srcNot +
			_sourceIp.getNetworks().getBaseReferencesName() +
			", to: " + destNot +
			_destIp.getNetworks().getBaseReferencesName() +
			", services: " + servicesNot +
			_services.getServices().getReferencesName() +
			", ruleAction: " + _ruleAction +
			", install: " + _installGateway;
		if (_comment != null)
			s+= ", # "	+ _comment;
		return s;
	}

}
