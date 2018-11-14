/*
 * Copyright (c) 2013 - 2018, Universite de Rennes 1
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

package fr.univrennes1.cri.jtacl.equipments.checkpointR80;

import java.util.List;

/**
 * Checkpoint firewall rule
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class CpFwRule extends CpObject {

    protected CpLayer _layer;
	protected Integer _number;
	protected boolean _disabled;
	protected boolean _access_section;

	protected CpFwRuleAction _ruleAction;
	protected String _layerCall;

	protected CpFwIpSpec _sourceIp;
	protected CpFwIpSpec _destIp;
	protected CpFwServicesSpec _services;

	protected boolean _implicitDrop;

	protected List<String> _installGateway;

    public CpLayer getLayer() { return _layer; }

	public Integer getNumber() {
		return _number;
	}

	public boolean isDisabled() {
		return _disabled;
	}

	public boolean isAccessSection() {
		return _access_section;
	}

	public boolean isSecurityRule() {
		return !_access_section;
	}

	public CpFwRuleAction getRuleAction() {
		return _ruleAction;
	}

	public void setRuleAction(CpFwRuleAction ruleAction) {
		_ruleAction = ruleAction;
	}

    public String getLayerCall() {
        return _layerCall;
    }

    public void setLayerCall(String layerCall) {
        _layerCall = layerCall;
    }

    public boolean ruleActionIsAccept() {
		return _ruleAction != null &&
			(_ruleAction == CpFwRuleAction.ACCEPT
				|| _ruleAction == CpFwRuleAction.AUTH);
	}

    public boolean ruleActionIsLayerCall() {
        return _ruleAction != null && _ruleAction == CpFwRuleAction.LAYER_CALL;
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
     * @param uid object's uid
     * @param layer rule's layer
     * @param number rule number
	 * @param disabled rule disabled
	 * @param srcIpSpec source IP specification
	 * @param dstIpSpec destination IP specification
	 * @param servicesSpec services specification
	 * @param ruleAction rule action (Accept, drop etc)
     * @param layerCall action call to a layer.
	 * @param installGateway rule gateway installation
	 */
	public CpFwRule(String name, String className, String comment, String uid, CpLayer layer,
                    Integer number, boolean disabled,
                    CpFwIpSpec srcIpSpec, CpFwIpSpec dstIpSpec,
                    CpFwServicesSpec servicesSpec,
                    CpFwRuleAction ruleAction,
                    String layerCall,
                    List<String> installGateway) {
	    super(name, className, comment, uid);
	    _layer = layer;
		_number = number;
		_disabled = disabled;
		_sourceIp = srcIpSpec;
		_destIp = dstIpSpec;
		_services = servicesSpec;
		_ruleAction = ruleAction;
		_layerCall = layerCall;
		_installGateway = installGateway;
	}

	/**
	 * Construct a new fw rule (implicit drop)
	 * @param name name
	 * @param className class name
	 * @param installGateway rule gateway installation
	 */
	public CpFwRule(String name, String className, List<String> installGateway) {
	    super(name, className, "implicit drop rule", "0");
		_number = 999999;
		_ruleAction = CpFwRuleAction.DROP;
		_implicitDrop = true;
		_installGateway = installGateway;
	}

	/**
	 * Construct a new fw rule (access-section)
	 * @param name name
	 * @param className class name
	 * @param comment header text
     * @param uid object's uid
     * @param layer rule's layer
     * @param number rule number
	 */
	public CpFwRule(String name, String className, String comment, String uid, CpLayer layer, Integer number) {
	    super(name, className, comment, uid);
		_layer = layer;
		_number = number;
		_access_section = true;
	}

	@Override
	public String toString() {
	    String s = "rule name=" + _name + ", className=" + _className
				+ ", comment=" + _comment + ", layer=" + _layer.getName() + ", number=" + _number
                + ", disabled=" + _disabled + ", implicit= " + _implicitDrop
				+ ", ruleAction=" + _ruleAction
				+ ", sourceIp=" + _sourceIp
				+ ", destIp=" + _destIp	+ " services=" + _services
				+ ", install=" + _installGateway;
	    return s;
	}

	public String toText() {
		if (isImplicitDrop())
			return "*** implicit drop ***";

		if (isAccessSection())
			return "### " + _name;

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
