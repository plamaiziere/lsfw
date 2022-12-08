/*
 * Copyright (c) 2022, Universite de Rennes 1
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

import java.util.Comparator;

/**
 * Fortigate Central SNAT Rule
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */

public class FgSnatRule  extends FgObject {

	protected Integer _number;
	protected boolean _disabled;
	protected FgFwIpSpec _sourceIp;
	protected FgFwIpSpec _destIp;
	protected String _comment;
	protected String _uid;
	protected FgIfacesSpec _sourceIfaces;
	protected FgIfacesSpec _destIfaces;
	protected FgIpPoolSpec _ippools;

	public Integer getNumber() {
		return _number;
	}

	public boolean isDisabled() {
		return _disabled;
	}

    public FgFwIpSpec getSourceIp() {
		return _sourceIp;
	}

	public FgFwIpSpec getDestIp() {
		return _destIp;
	}

	public FgIfacesSpec getSourceIfaces() { return _sourceIfaces; }

	public FgIfacesSpec getDestIfaces() { return _destIfaces; }

	public FgIpPoolSpec getIpPools() { return _ippools; }

	private FgSnatRule(String originKey, String comment, String uid) {
	    super("", originKey);
	    _comment = comment;
	    _uid = uid;
    }

    /**
     * return a new central SNAT rule
     * @param originKey fortigate origin key
     * @param comment comment
     * @param uid object's uid
     * @param number rule number
     * @param disabled rule disabled
     * @param sourceIfaces source interfaces
     * @param destIfaces destination interfaces
     * @param srcIpSpec source IP specification
     * @param dstIpSpec destination IP specification
	 * @param ipPools IP pools specification
     * @return new central SNAT rule
     */
    static public FgSnatRule newSnatRule(String originKey, String comment, String uid,
                                    Integer number, boolean disabled,
                                    FgIfacesSpec sourceIfaces, FgIfacesSpec destIfaces,
                                    FgFwIpSpec srcIpSpec, FgFwIpSpec dstIpSpec,
									FgIpPoolSpec ipPools) {

	    FgSnatRule rule = new FgSnatRule(originKey, comment, uid);
        rule._number = number;
        rule._disabled = disabled;
        rule._sourceIfaces = sourceIfaces;
        rule._destIfaces = destIfaces;
        rule._sourceIp = srcIpSpec;
        rule._destIp = dstIpSpec;
		rule._ippools = ipPools;
        return rule;
    }

	static class FgSnatRuleComparator implements Comparator<FgSnatRule> {

		@Override
		public int compare(FgSnatRule rule1, FgSnatRule rule2) {
			return rule1.getNumber().compareTo(rule2.getNumber());
		}
	}

	@Override
	public String toString() {
	    String s = "originKey=" + _originKey
				+ ", comment=" + _comment + ", id=" + _uid;
	    s+= ", number=" + _number
                + ", disabled=" + _disabled
                + ", srcIfaces=" + _sourceIfaces
                + ", dstIfaces=" + _destIfaces
				+ ", sourceIp=" + _sourceIp
				+ ", destIp=" + _destIp
				+ " ip pools=" + _ippools;
	    return s;
	}

	public String toText() {
		String s = "SNAT #" + _number + ", id: " + _uid;
		String senabled = _disabled ? "disabled" : "enabled";

		s += ", " + senabled + ", srcIfaces: " + _sourceIfaces + ", dstIfaces=" + _destIfaces
				+ ", from: " + _sourceIp
				+ ", to: " + _destIp
			    + ", ippools: " + _ippools.toText();

		if (_comment != null)
			s+= ", # "	+ _comment;
		return s;
	}
}

