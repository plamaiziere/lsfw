/*
 * Copyright (c) 2013 - 2021, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */
package fr.univrennes1.cri.jtacl.equipments.proxmox;

import fr.univrennes1.cri.jtacl.lib.ip.IPIcmpEnt;
import fr.univrennes1.cri.jtacl.lib.ip.PortSpec;
import fr.univrennes1.cri.jtacl.lib.ip.ProtocolsSpec;
import fr.univrennes1.cri.jtacl.lib.misc.ParseContext;

/**
 * Proxmox fw rule
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PxRule {

	protected boolean _disabled = false;
	protected boolean _implicit = false;
	protected String _groupIdent;
	protected PxGroupRules _groupOwner;
	protected PxRuleDirection _direction;
	protected PxRuleAction _action;
	protected String _ifaceName;
	protected PxFwIpSpec _addrSource;
	protected PxFwIpSpec _addrDest;
	protected ProtocolsSpec _protocolsSpec;
	protected IPIcmpEnt _ipIcmp;
    protected PortSpec _sourcePorts;
    protected PortSpec _destPorts;
    protected ParseContext _context;

	public static PxRule ofRule(PxGroupRules groupOwner, boolean disabled, PxRuleDirection direction, PxRuleAction action
			, String ifaceName, PxFwIpSpec addrSource, PxFwIpSpec addrDest
			, ProtocolsSpec protocolsSpec, IPIcmpEnt ipIcmp, PortSpec sourcePorts, PortSpec destPorts
			, ParseContext context) {

		PxRule rule = new PxRule();
		rule._implicit = false;
		rule._groupIdent = null;
		rule._groupOwner = groupOwner;
		rule._disabled = disabled;
		rule._direction = direction;
		rule._action = action;
		rule._ifaceName = ifaceName;
		rule._addrSource = addrSource;
		rule._addrDest = addrDest;
		rule._protocolsSpec = protocolsSpec;
		rule._ipIcmp = ipIcmp;
		rule._sourcePorts = sourcePorts;
		rule._destPorts = destPorts;
		rule._context = context;

		return rule;
	}

	public static PxRule ofGroupRule(PxGroupRules groupOwner, String groupIdent, boolean disabled
			, ParseContext context) {

		PxRule rule = new PxRule();
		rule._groupIdent = groupIdent;
		rule._groupOwner = groupOwner;
		rule._disabled = disabled;
		rule._context = context;
		return rule;
	}

	public static PxRule ofImplicitRule(PxGroupRules groupOwner, PxRuleDirection direction, PxRuleAction action) {

		PxRule rule = new PxRule();
		rule._implicit = true;
		rule._groupOwner = groupOwner;
		rule._direction = direction;
		rule._action = action;
		return rule;
	}

	public String getGroupIdent() { return _groupIdent; }

	public boolean isGroupRule() { return _groupIdent != null; }

	public PxGroupRules getGroupOwner() { return _groupOwner; }

	public boolean isDisabled() {
		return _disabled;
	}

	public PxRuleDirection getDirection() {
		return _direction;
	}

	public PxRuleAction getAction() {
		return _action;
	}

	public String getIfaceName() {
		return _ifaceName;
	}

	public PxFwIpSpec getAddrSource() {
		return _addrSource;
	}

	public PxFwIpSpec getAddrDest() {
		return _addrDest;
	}

	public ProtocolsSpec getProtocolsSpec() {
		return _protocolsSpec;
	}

	public IPIcmpEnt getIpIcmp() {
		return _ipIcmp;
	}

	public PortSpec getSourcePorts() {
		return _sourcePorts;
	}

	public PortSpec getDestPorts() {
		return _destPorts;
	}

	public ParseContext getContext() { return _context; }

	public String toText() {
		String s = (_groupOwner != null) ? "(" + _groupOwner.getName() + ") " : "";

		if (isImplicit()) {
			return s + _direction.name() + " " + _action + " ***implicit policy***";
		}
		return s + _context.getLine();
	}

	public boolean isImplicit() { return  _implicit; }
}
