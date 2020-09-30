/*
 * Copyright (c) 2013 - 2020, Universite de Rennes 1
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

package fr.univrennes1.cri.jtacl.equipments.fortigate;

import java.util.List;

/**
 * Fortigate firewall rule
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class FgFwRule extends FgObject {

	protected Integer _number;
	protected Integer _toNumber;
	protected boolean _disabled;
	protected FgFwRuleAction _ruleAction;
	protected FgFwIpSpec _sourceIp;
	protected FgFwIpSpec _destIp;
	protected FgFwServicesSpec _services;
	protected String _comment;
	protected String _uid;
	protected List<String> _sourceIfaces;
	protected List<String> _destIfaces;

	protected boolean _implicitDrop;

    protected String ruleActionTxt() {
        return _ruleAction.toString();
    }

	public Integer getNumber() {
		return _number;
	}

	public boolean isDisabled() {
		return _disabled;
	}

	public FgFwRuleAction getRuleAction() {
		return _ruleAction;
	}

	public void setRuleAction(FgFwRuleAction ruleAction) {
		_ruleAction = ruleAction;
	}

    public boolean ruleActionIsAccept() {
		return _ruleAction != null && _ruleAction == FgFwRuleAction.ACCEPT;
	}

    public FgFwIpSpec getSourceIp() {
		return _sourceIp;
	}

	public FgFwIpSpec getDestIp() {
		return _destIp;
	}

	public FgFwServicesSpec getServices() {
		return _services;
	}

	public boolean isImplicitDrop() {
		return _implicitDrop;
	}

	public List<String> getSourceIfaces() { return _sourceIfaces; }

	public List<String> getDestIfaces() { return _destIfaces; }

	private FgFwRule(String name, String originKey, String comment, String uid) {
	    super(name, originKey);
	    _comment = comment;
	    _uid = uid;
    }

    /**
     * return a new security rule
     * @param name name
     * @param originKey fortigate origin key
     * @param comment comment
     * @param uid object's uid
     * @param number rule number
     * @param disabled rule disabled
     * @param sourceIfaces source interfaces
     * @param destIfaces destination interfaces
     * @param srcIpSpec source IP specification
     * @param dstIpSpec destination IP specification
     * @param servicesSpec services specification
     * @param ruleAction rule action (Accept, drop etc)
     * @return new security rule
     */
    static public FgFwRule newSecurityRule(String name, String originKey, String comment, String uid,
                                    Integer number, boolean disabled,
                                    List<String> sourceIfaces, List<String> destIfaces,
                                    FgFwIpSpec srcIpSpec, FgFwIpSpec dstIpSpec,
                                    FgFwServicesSpec servicesSpec,
                                    FgFwRuleAction ruleAction) {

	    FgFwRule rule = new FgFwRule(name, originKey, comment, uid);
        rule._number = number;
        rule._disabled = disabled;
        rule._sourceIfaces = sourceIfaces;
        rule._destIfaces = destIfaces;
        rule._sourceIp = srcIpSpec;
        rule._destIp = dstIpSpec;
        rule._services = servicesSpec;
        rule._ruleAction = ruleAction;
        return rule;
    }

    /**
     * return a new "implicit drop" rule
     * @return new rule
     */
    static public FgFwRule newImplicitDropRule() {

        FgFwRule rule = new FgFwRule("implict drop", null, null, null);
		rule._number = 999999;
		rule._ruleAction = FgFwRuleAction.DROP;
		rule._implicitDrop = true;
		return rule;
	}


	@Override
	public String toString() {
	    String s = "rule name=" + _name + ", originKey=" + _originKey
				+ ", comment=" + _comment;
	    s+= ", number=" + _number
                + ", to=" + _toNumber
                + ", disabled=" + _disabled + ", implicit= " + _implicitDrop
				+ ", ruleAction=" + _ruleAction
                + ", srcIfaces=" + _sourceIfaces
                + ", dstIfaces=" + _destIfaces
				+ ", sourceIp=" + _sourceIp
				+ ", destIp=" + _destIp	+ " services=" + _services;
	    return s;
	}

	public String toText() {
		if (isImplicitDrop())
			return "*** implicit drop ***";

		String s = "#" + _number;
		s += ", name: ";
		if (_name != null)
			s+= _name;

		String senabled = _disabled ? "disabled" : "enabled";
		String srcNot = _sourceIp.isNotIn() ? "!" : "";
		String destNot = _destIp.isNotIn() ? "!" : "";
		String servicesNot = _services.isNotIn() ? "!" : "";

		s += ", " + senabled + ", srcIfaces: " + _sourceIfaces + ", dstIfaces=" + _destIfaces + ", from: " + srcNot +
			_sourceIp.getNetworks().getBaseReferencesName() +
			", to: " + destNot +
			_destIp.getNetworks().getBaseReferencesName() +
			", services: " + servicesNot +
			_services.getServices().getReferencesName() +
			", ruleAction: " + ruleActionTxt();

		if (_comment != null)
			s+= ", # "	+ _comment;
		return s;
	}
}
