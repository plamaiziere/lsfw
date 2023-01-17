/*
 * Copyright (c) 2022, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.equipments.fortigate;

import fr.univrennes1.cri.jtacl.lib.ip.IPNet;
import fr.univrennes1.cri.jtacl.lib.ip.ProtocolsSpec;

import java.util.Comparator;

/**
 * Fortigate policy route rule
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class FgPolicyRouteRule extends FgObject {

	protected Integer _number;
	protected boolean _disabled;
	protected FgFwIpSpec _sourceIp;
	protected FgFwIpSpec _destIp;
	protected ProtocolsSpec _protocol;
	protected FgPortsSpec _portsSpec;
	protected String _comment;
	protected FgIfacesSpec _sourceIfaces;
	protected IPNet _gateway;
	protected String _outputIface;

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

	public FgPortsSpec getportsSpec() {
		return _portsSpec;
	}

	public ProtocolsSpec getProtocol() { return _protocol; }

	public FgIfacesSpec getSourceIfaces() { return _sourceIfaces; }

	public IPNet getGateway() { return _gateway; }

	public String getOutputIface() { return _outputIface; }

	private FgPolicyRouteRule(String name, String originKey, String comment) {
	    super(name, originKey);
	    _comment = comment;
    }

    /**
     * return a new Policy Route rule
     * @param originKey fortigate origin key
     * @param comment comment
     * @param number rule number
     * @param disabled rule disabled
     * @param sourceIfaces source interfaces
     * @param srcIpSpec source IP specification
     * @param dstIpSpec destination IP specification
	 * @param protocolsSpec protocol specification
	 * @param portsSpec source ports / destination ports specififcation
	 * @param gateway route's gateway
	 * @param outputIface route's output iface
     * @return new policy route rule
     */
    static public FgPolicyRouteRule newPolicyRouteRule(String originKey, String comment,
                                    Integer number, boolean disabled,
                                    FgIfacesSpec sourceIfaces,
                                    FgFwIpSpec srcIpSpec, FgFwIpSpec dstIpSpec,
                                    ProtocolsSpec protocolsSpec,
									FgPortsSpec portsSpec,
									IPNet gateway,
									String outputIface) {

	    FgPolicyRouteRule rule = new FgPolicyRouteRule(originKey, originKey, comment);
        rule._number = number;
        rule._disabled = disabled;
        rule._sourceIfaces = sourceIfaces;
        rule._sourceIp = srcIpSpec;
        rule._destIp = dstIpSpec;
        rule._protocol = protocolsSpec;
		rule._portsSpec = portsSpec;
		rule._gateway = gateway;
		rule._outputIface = outputIface;
        return rule;
    }

	static class PolicyRouteRuleComparator implements Comparator<FgPolicyRouteRule> {

		@Override
		public int compare(FgPolicyRouteRule rule1, FgPolicyRouteRule rule2) {
			return rule1.getNumber().compareTo(rule2.getNumber());
		}
	}


	@Override
	public String toString() {
	    String s = "Route rule name=" + _name + ", originKey=" + _originKey
				+ ", comment=" + _comment;
	    s+= ", number=" + _number
                + ", disabled=" + _disabled
				+ ", srcIfaces: " + _sourceIfaces
				+ ", from: " + _sourceIp
			    + ", to: " + _destIp
		        + ", protocol: " + _protocol.toString()
			    + ", " + _portsSpec
			    + ", gateway: " + _gateway.toString("::i")
			    + ", outputIface: " + _outputIface;
	    return s;
	}

	public String toText() {
		String s = "ROUTE #" + _number;
		String senabled = _disabled ? "disabled" : "enabled";
		String ifacesNot = _sourceIfaces.hasNegate() ? "!" : "";
		String srcNot = _sourceIp.isNotIn() ? "!" : "";
		String destNot = _destIp.isNotIn() ? "!" : "";

		s += ", " + senabled + ", srcIfaces: " + ifacesNot + _sourceIfaces + ", from: " + srcNot +
			_sourceIp.getNetworks().getBaseReferencesName() +
			", to: " + destNot +
			_destIp.getNetworks().getBaseReferencesName()
		    + ", protocol: " + _protocol.toString()
			+ ", " + _portsSpec
			+ ", gateway: " + _gateway.toString("::i")
			+ ", outputIface: " + _outputIface;

		if (_comment != null)
			s+= ", # "	+ _comment;
		return s;
	}
}
