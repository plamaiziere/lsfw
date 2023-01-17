/*
 * Copyright (c) 2013 - 2018, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
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
	protected Integer _toNumber;
	protected boolean _disabled;
	protected boolean _access_section;

	protected CpFwRuleAction _ruleAction;
	protected String _layerCall;

	protected CpFwIpSpec _sourceIp;
	protected CpFwIpSpec _destIp;
	protected CpFwServicesSpec _services;

	protected boolean _implicitDrop;

	protected List<String> _installGateway;

	protected String lineNumber() {
	    Integer linecall = _layer.getLayerCallRuleNumber();
	    if (linecall != null) {
            if (linecall != 0) {
                return linecall.toString() + "." + _number.toString();
            } else {
                return _number.toString();
            }
        }
        return "." + _number.toString();
    }

    protected String ruleActionTxt() {
        if (ruleActionIsLayerCall())
            return _ruleAction.toString() + "(" + _layerCall + ")";
        return _ruleAction.toString();
    }

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

	private CpFwRule(String name, String className, String comment, String uid) {
	    super(name, className, comment, uid);
    }

    /**
     * return a new security rule
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
     * @return new security rule
     */
    static public CpFwRule newSecurityRule(String name, String className, String comment, String uid, CpLayer layer,
                                    Integer number, boolean disabled,
                                    CpFwIpSpec srcIpSpec, CpFwIpSpec dstIpSpec,
                                    CpFwServicesSpec servicesSpec,
                                    CpFwRuleAction ruleAction,
                                    String layerCall,
                                    List<String> installGateway) {

	    CpFwRule rule = new CpFwRule(name, className, comment, uid);
        rule._layer = layer;
        rule._number = number;
        rule._disabled = disabled;
        rule._sourceIp = srcIpSpec;
        rule._destIp = dstIpSpec;
        rule._services = servicesSpec;
        rule._ruleAction = ruleAction;
        rule._layerCall = layerCall;
        rule._installGateway = installGateway;
        return rule;
    }

    /**
     * return a new "implicit drop" rule
     * @param layer rule's layer
     * @return new rule
     */
    static public CpFwRule newImplicitDropRule(CpLayer layer) {

        CpFwRule rule = new CpFwRule("implict drop", null, null, null);
		rule._number = 999999;
		rule._ruleAction = CpFwRuleAction.DROP;
		rule._implicitDrop = true;
		return rule;
	}

	/**
	 * return a new access-section rule
	 * @param name name
	 * @param className class name
	 * @param comment comment
     * @param uid object's uid
     * @param layer rule's layer
     * @param number rule number
     * @param toNumber rule toNumber
     * @return new rule
	 */
	static public CpFwRule newAccessSectionRule(String name, String className, String comment, String uid, CpLayer layer, Integer number, Integer toNumber) {
	    CpFwRule rule = new CpFwRule(name, className, comment, uid);
	    rule._layer = layer;
		rule._number = number;
		rule._toNumber = toNumber;
		rule._access_section = true;
		return rule;
	}

	@Override
	public String toString() {
	    String s = "rule name=" + _name + ", className=" + _className
				+ ", comment=" + _comment + ", layer=";
	    if (_layer != null)
	        s+= _layer.getName();
	    else
	        s+= "null";
	    s+= ", number=" + lineNumber()
                + ", to=" + _toNumber
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

		if (isAccessSection()) {
            if (_number == 0)
                return "### " + _name + " (No Rules), layer: " + _layer.getName();
            if (_number < _toNumber)
                return "### " + _name + " (" + _number.toString() + "-" + _toNumber.toString() + "), layer: " + _layer.getName();
            return "### " + _name + " (" + _number.toString() + "), layer: " + _layer.getName();
        }

		String s = "#" + lineNumber();
		s += ", name: ";
		if (_name != null)
			s+= _name;
		s+= ", layer: " + _layer.getName();
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
			", ruleAction: " + ruleActionTxt() +
			", install: " + _installGateway;
		if (_comment != null)
			s+= ", # "	+ _comment;
		return s;
	}
}
