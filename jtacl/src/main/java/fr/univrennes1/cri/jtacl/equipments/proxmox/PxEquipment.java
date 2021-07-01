/*
 * Copyright (c) 2013 - 2021, Universite de Rennes 1
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

import fr.univrennes1.cri.jtacl.analysis.CrossRefContext;
import fr.univrennes1.cri.jtacl.analysis.IPCrossRef;
import fr.univrennes1.cri.jtacl.analysis.IPCrossRefMap;
import fr.univrennes1.cri.jtacl.analysis.ServiceCrossRef;
import fr.univrennes1.cri.jtacl.analysis.ServiceCrossRefContext;
import fr.univrennes1.cri.jtacl.analysis.ServiceCrossRefMap;
import fr.univrennes1.cri.jtacl.analysis.ServiceCrossRefType;
import fr.univrennes1.cri.jtacl.core.exceptions.JtaclConfigurationException;
import fr.univrennes1.cri.jtacl.core.exceptions.JtaclInternalException;
import fr.univrennes1.cri.jtacl.core.monitor.Log;
import fr.univrennes1.cri.jtacl.core.monitor.Monitor;
import fr.univrennes1.cri.jtacl.core.network.Iface;
import fr.univrennes1.cri.jtacl.core.network.IfaceLink;
import fr.univrennes1.cri.jtacl.core.network.Route;
import fr.univrennes1.cri.jtacl.core.network.Routes;
import fr.univrennes1.cri.jtacl.core.probing.FwResult;
import fr.univrennes1.cri.jtacl.core.probing.MatchResult;
import fr.univrennes1.cri.jtacl.core.probing.Probe;
import fr.univrennes1.cri.jtacl.core.probing.ProbeResults;
import fr.univrennes1.cri.jtacl.equipments.generic.GenericEquipment;
import fr.univrennes1.cri.jtacl.lib.ip.IPIcmpEnt;
import fr.univrennes1.cri.jtacl.lib.ip.IPNet;
import fr.univrennes1.cri.jtacl.lib.ip.IPRange;
import fr.univrennes1.cri.jtacl.lib.ip.IPRangeable;
import fr.univrennes1.cri.jtacl.lib.ip.PortRange;
import fr.univrennes1.cri.jtacl.lib.ip.PortSpec;
import fr.univrennes1.cri.jtacl.lib.ip.ProtocolsSpec;
import fr.univrennes1.cri.jtacl.lib.misc.Direction;
import fr.univrennes1.cri.jtacl.lib.misc.ParseContext;
import fr.univrennes1.cri.jtacl.lib.misc.StringsList;
import org.parboiled.Parboiled;
import org.parboiled.parserunners.BasicParseRunner;
import org.parboiled.support.ParsingResult;

import java.io.IOException;
import java.io.PrintStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

/**
 * Common implementation for PxGuest & PxHost
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public abstract class PxEquipment extends GenericEquipment {

	protected class PxgIface {
		protected Iface _iface;
		protected String _name;
		protected String _description;

		public PxgIface(Iface iface) {
			_iface = iface;
		}

		public Iface getIface() {
			return _iface;
		}

		public void setIface(Iface iface) {
			_iface = iface;
		}

		public String getDescription() {
			return _description;
		}

		public void setDescription(String description) {
			_description = description;
		}

		public String getName() {
			return _name;
		}

		public void setName(String name) {
			_name = name;
		}
	}

	/**
	 * interfaces
	 */
	protected HashMap<String, PxgIface> _pxgIfaces
		= new HashMap<>();

	protected PxOptions _options = PxOptions.ofDefault();
	protected HashMap<String, PxNetworkAlias> _aliases = new HashMap<>();
	protected HashMap<String, PxNetworkIpSet> _ipsets = new HashMap<>();
	protected HashMap<String, PxGroupRules> _groupsRules = new HashMap<>();
	protected PxGroupRules _rules = new PxGroupRules(null, null);

	protected PxEquipment _host;

    protected PxFwShell _shell = new PxFwShell(this);


	/**
	 * Parser
	 */
	protected PxVeParser _parser = Parboiled.createParser(PxVeParser.class);

	/**
	 * ParseRunner for section
	 */
	protected BasicParseRunner _parseRunSection =
			new BasicParseRunner(_parser.RSection());

	/**
	 * ParseRunner for options
	 */
	protected BasicParseRunner _parseRunOption =
			new BasicParseRunner(_parser.ROption());

	/**
	 * ParseRunner for aliases
	 */
	protected BasicParseRunner _parseRunAlias =
			new BasicParseRunner(_parser.RAlias());

	/**
	 * ParseRunner for ListIdents
	 */
	protected BasicParseRunner _parseRunListIdents =
			new BasicParseRunner(_parser.RListIdents());


	/**
	 * ParseRunner for Rule
	 */
	protected BasicParseRunner _parseRunRule =
			new BasicParseRunner(_parser.RveRule());


	/**
	 * IP cross references map
	 */
	protected IPCrossRefMap _netCrossRef = new IPCrossRefMap();

	/**
	 * Services cross references map
	 */
	protected ServiceCrossRefMap _serviceCrossRef = new ServiceCrossRefMap();

	/**
	 * parse context
	 */
	 protected ParseContext _parseContext = new ParseContext();

	/**
	 * IP cross references map
	 */
	IPCrossRefMap getNetCrossRef() {
		return _netCrossRef;
	}

	/**
	 * Services cross references map
	 */
	ServiceCrossRefMap getServiceCrossRef() {
		return _serviceCrossRef;
	}

	/**
	 * Create a new {@link PxEquipment} with this name and this comment.<br/>
	 * @param monitor the {@link Monitor} monitor associated with this equipment.
	 * @param name the name of the equipment.
	 * @param comment a free comment for this equipment.
	 * @param configurationFileName name of the configuration file to use (may be null).
	 */
	public PxEquipment(Monitor monitor, String name, String comment, String configurationFileName
		, PxEquipment host) {
		super(monitor, name, comment, configurationFileName);
		_host = host;
	}

	protected void throwCfgException(String msg, boolean context) {
		String s = "Equipment: " + _name + " ";
		if (context)
			s += _parseContext + msg;
		else
			s += msg;

		throw new JtaclConfigurationException(s);
	}

	protected void warnConfig(String msg, boolean context) {
		String s = "Equipment: " + _name + " ";
		if (context)
			s += _parseContext + msg;
		else
			s += msg;

		Log.config().warning(s);
	}

	public abstract boolean isHost();

	public boolean isGuest() {
		return !isHost();
	}

	public PxEquipment getHost() { return _host; }

	public void parsePolicy(String fileName) {
		StringsList lines = new StringsList();
		try {
			lines.readFromFile(fileName);
		} catch (IOException e) {
			throwCfgException("Cannot read policy file: " + fileName, false);
		}

		parseSections(fileName, lines);
	}

	public void parseSections(String fileName, StringsList lines) {
		int ln = 0;
		String sectionName = null;
		String sectionIdent = "";
		ParsingResult<?> result;
		for (String l: lines) {
			ln ++;
			_parseContext = ParseContext.of(fileName, ln, l);
			String cl = filter(PxVeParser.stripComment(l));
			if (cl.isEmpty())
				continue;

			// section
			result = _parseRunSection.run(cl);
			if (result.matched) {
				sectionName = _parser.getSectionTpl().getSectionName();
				sectionIdent = _parser.getSectionTpl().getName();
				if (sectionName == null)
					continue;

				if (sectionName.equalsIgnoreCase(PxSectionType.IPSET.name())) {
					PxNetworkIpSet ipset = _ipsets.get(sectionIdent);
					if (ipset == null) {
						ipset = new PxNetworkIpSet(sectionIdent, _parseContext);
						_ipsets.put(sectionIdent, ipset);
					}
				}

				if (sectionName.equalsIgnoreCase(PxSectionType.GROUP.name())) {
					PxGroupRules group = _groupsRules.get(sectionIdent);
					if (group == null) {
						group = new PxGroupRules(sectionIdent, _parseContext);
						_groupsRules.put(sectionIdent, group);
					}
				}
				continue;
			}

			// OPTIONS
			if (sectionName.equalsIgnoreCase(PxSectionType.OPTIONS.name())) {
				parseSectionOptions(cl);
				continue;
			}

			// ALIASES
			if (sectionName.equalsIgnoreCase(PxSectionType.ALIASES.name())) {
				parseSectionAliases(cl);
				continue;
			}

			// IPSET
			if (sectionName.equalsIgnoreCase(PxSectionType.IPSET.name())) {
				parseSectionIpSet(sectionIdent, cl);
				continue;
			}

			// GROUP
			if (sectionName.equalsIgnoreCase(PxSectionType.GROUP.name())) {
				parseSectionGroup(sectionIdent, cl);
				continue;
			}

			// RULES
			if (sectionName.equalsIgnoreCase(PxSectionType.RULES.name())) {
				parseSectionRules(sectionIdent, cl);
				continue;
			}
		}
	}

	public void parseSectionOptions(String cl) {

		ParsingResult<?> result = _parseRunOption.run(cl);
		if (result.matched) {
			PxOptionTemplate otpl = _parser.getOptionTpl();

			if (otpl.getName().equalsIgnoreCase("enable:")) {
				_options.setEnable(otpl.getValue().equalsIgnoreCase("1"));
				return;
			}

			if (otpl.getName().equalsIgnoreCase("policy_in:")) {
				_options.setPolicyIn(otpl.getValue().equalsIgnoreCase("ACCEPT") ? PxRuleAction.ACCEPT : PxRuleAction.DROP);
				return;
			}

			if (otpl.getName().equalsIgnoreCase("policy_out:")) {
				_options.setPolicyOut(otpl.getValue().equalsIgnoreCase("ACCEPT") ? PxRuleAction.ACCEPT : PxRuleAction.DROP);
				return;
			}
		}
	}

	public void parseSectionAliases(String cl) {

		ParsingResult<?> result = _parseRunAlias.run(cl);
		if (result.matched) {
			PxAliasTemplate atpl = _parser.getAliasTpl();
			IPRangeable ipRange = parseAlias(atpl);
			PxNetworkIp pxNetworkIp = new PxNetworkIp(ipRange);
			PxNetworkAlias pxNetworkAlias = new PxNetworkAlias(atpl.getName(), pxNetworkIp, _parseContext);
			_aliases.put(pxNetworkAlias.getName(), pxNetworkAlias);
			return;
		}
		warnConfig("Alias doesnt match parser rule", true);
	}

	public void parseSectionIpSet(String sectionIdent, String cl) {

		ParsingResult<?> result = _parseRunListIdents.run(cl);
		if (result.matched) {
			List<String> sipspec = _parser.getListIdents();
			PxNetworkIpSet ipset = _ipsets.get(sectionIdent);
			List<PxNetworkObject> ipspecs = parseIpspec(sipspec);
			for (PxNetworkObject pn: ipspecs) {
				ipset.addReference(pn.getName(), pn);
			}
			return;
		}

		warnConfig("IPSet doesnt match parser rule", true);
	}

	public void parseSectionGroup(String sectionIdent, String cl) {

		ParsingResult<?> result = _parseRunRule.run(cl);
		if (result.matched) {
			PxGroupRules group = _groupsRules.get(sectionIdent);
			PxRuleTemplate rtpl = _parser.getRuleTpl();
			PxRule rule = parseRule(group, rtpl);
			group.add(rule);
			return;
		}

		warnConfig("Rule doesnt match parser rule", true);
	}

	public void parseSectionRules(String sectionIdent, String cl) {

		ParsingResult<?> result = _parseRunRule.run(cl);
		if (result.matched) {
			PxRuleTemplate rtpl = _parser.getRuleTpl();
			if (rtpl.getMacro() != null) {
				warnConfig("lsfw doesn't handle proxmox macro", true);
				return;
			}
			PxRule rule = parseRule(null, rtpl);
			_rules.add(rule);
			return;
		}

		warnConfig("Rule doesnt match parser rule", true);
	}

	public IPRangeable parseAlias(PxAliasTemplate aliasTemplate) {

		IPRangeable ipRange = null;
		try {
			ipRange = new IPRange(aliasTemplate.getIpSpec().get(0));
		} catch (UnknownHostException e) {
			throwCfgException("Invalid alias", true);
		}
		return ipRange;
	}

	public List<PxNetworkObject> parseIpspec(List<String> ipspec) {
		List<PxNetworkObject> nobj = new ArrayList<>();

		for (String s: ipspec) {

			// ipset ?
			if (s.startsWith("+")) {
				if (s.length() <= 1)
					throwCfgException("Invalid Ipset name", true);
				String sname = s.substring(1);
				PxNetworkIpSet ipset = findIpSet(sname);
				if (ipset == null) {
					throwCfgException("Unknown Ipset " + sname, true);
				}
				nobj.add(ipset);
				continue;
			}

			// IP ?
			IPRangeable ipRange = null;
			try {
				ipRange = new IPRange(s);
				nobj.add(new PxNetworkIp(ipRange));
				continue;
			} catch (UnknownHostException e) {
				// do nothing, may be an alias
			}

			PxNetworkAlias alias = findAlias(s);
			if (alias == null)
				throwCfgException("Invalid IP or unknown alias " + s, true);
			nobj.add(alias);
		}
		return nobj;
	}

	public PxRule parseRule(PxGroupRules group, PxRuleTemplate rtpl) {
		boolean disabled;
		PxRuleDirection direction;
		PxRuleAction action;
		String ifaceName;
		PxFwIpSpec addrSource = new PxFwIpSpec();
		PxFwIpSpec addrDest = new PxFwIpSpec();
		ProtocolsSpec protocolsSpec = new ProtocolsSpec();
		IPIcmpEnt ipIcmp = null;
		PortSpec sourcePorts = null;
		PortSpec destPorts = null;

		disabled = rtpl.isDisabled();

		// Group Rule
		if (rtpl.isGroup()) {
			String name = rtpl.getGroupName();
			PxGroupRules agroup = findGroupRules(name);
			if (agroup == null)
				throwCfgException("unknown group rule " + name, true);
			PxRule rule = PxRule.ofGroupRule(group, name, disabled, _parseContext);
			return rule;
		}

		direction = rtpl.getDirection().equalsIgnoreCase("IN") ? PxRuleDirection.IN : PxRuleDirection.OUT;
		action = rtpl.getAction().equalsIgnoreCase("ACCEPT") ? PxRuleAction.ACCEPT : PxRuleAction.DROP;

		// interface
		ifaceName = rtpl.getIface();
		if (ifaceName != null) {
			Iface iface = getIface(ifaceName);
			if (iface == null && !disabled)
				throwCfgException("Unknown interface " + ifaceName, true);
		}

		// addresses
		if (rtpl.getSourceIpSpec() != null) {
			List<PxNetworkObject> sourceIpsec = parseIpspec(rtpl.getSourceIpSpec());
			for (PxNetworkObject pn: sourceIpsec)
				addrSource.addReference(pn.getName(), pn);
		}

		if (rtpl.getDestIpSpec() != null) {
			List<PxNetworkObject> destIpsec = parseIpspec(rtpl.getDestIpSpec());
			for (PxNetworkObject pn: destIpsec)
				addrDest.addReference(pn.getName(), pn);
		}

		// protocol
		if (rtpl.getProto() != null) {
			protocolsSpec = parseProtocol(rtpl.getProto());
		}

		// icmp type
		String sicmp = rtpl.getIcmpType();
		if (sicmp != null) {
			ipIcmp = _ipIcmp4Types.icmpLookup(sicmp);
			if (ipIcmp == null) {
				ipIcmp = _ipIcmp6Types.icmpLookup(sicmp);
			}
			if (ipIcmp == null) {
				throwCfgException("Unknown icmp type " + sicmp, true);
			}
		}

		// ports
		List<String> lpspec = rtpl.getSourcePortSpec();
		if (lpspec != null) {
			sourcePorts = parsePorts(lpspec);
		}
		lpspec = rtpl.getDestPortSpec();
		if (lpspec != null) {
			destPorts = parsePorts(lpspec);
		}

		return PxRule.ofRule(group, disabled, direction, action, ifaceName, addrSource, addrDest, protocolsSpec
			, ipIcmp, sourcePorts, destPorts, _parseContext);
	}

	public PortSpec parsePorts(List<String> ports) {
		PortSpec pspecs = new PortSpec();

		for (String sp: ports) {
			String sports[] = sp.split(":");
			if (sports.length > 2)
				throwCfgException("Invalid port range", true);
			String sfirst = sports[0];
			String slast = sports.length == 2 ? sports[1] : null;
			int first = parsePort(sfirst);
			if (slast != null) {
				int last = parsePort(slast);
				PortRange pr = new PortRange(first, last);
				pspecs.add(pr);
			} else {
				PortRange ps = new PortRange(first, first);
				pspecs.add(ps);
			}
		}
	return pspecs;
	}

	public int parsePort(String port) {
		int p = _ipServices.serviceLookup(port, "tcp");
		if (p == -1)
			throwCfgException("Invalid port " + port, true);

		return p;
	}

	public ProtocolsSpec parseProtocol(String protocols) {
		ProtocolsSpec protocolsSpec = new ProtocolsSpec();

		String protos[] = protocols.split("/");
		for (String p: protos) {
			int proto = _ipProtocols.protocolLookup(p);
			if (proto == -1)
				throwCfgException("Unknown protocol " + p, true);
			protocolsSpec.add(proto);
		}
		return protocolsSpec;
	}

	public PxNetworkIpSet findIpSet(String name) {
		PxNetworkIpSet ipset = _ipsets.get(name);
		if (ipset == null && isGuest())
			ipset = _host.findIpSet(name);
		return ipset;
	}

	public PxNetworkAlias findAlias(String name) {
		PxNetworkAlias alias = _aliases.get(name);
		if (alias == null && isGuest())
			alias = _host.findAlias(name);
		return alias;
	}

	public PxGroupRules findGroupRules(String name) {
		PxGroupRules groupRules = _groupsRules.get(name);
		if (groupRules == null && isGuest())
			groupRules = _host.findGroupRules(name);
		return groupRules;
	}

	public HashMap<String, PxNetworkAlias> getAllAliases() {
		HashMap<String, PxNetworkAlias> aliases = new HashMap<>();
		if (isGuest())  {
			aliases.putAll(getHost()._aliases);
		}
		aliases.putAll(_aliases);
		return aliases;
	}

	public HashMap<String, PxNetworkIpSet> getAllIpsets() {
		HashMap<String, PxNetworkIpSet> ipsets = new HashMap<>();
		if (isGuest())  {
			ipsets.putAll(getHost()._ipsets);
		}
		ipsets.putAll(_ipsets);
		return ipsets;
	}

	/*
	 * compute cross references
	 */
	protected void crossReference() {
		crossRefAliases();
		crossRefIpSets();
		crossRefRules();
	}

	protected IPCrossRef getIPNetCrossRef(IPRangeable ip) {
		if (!_monitorOptions.getXref())
			throw new JtaclInternalException(
					"Cross reference computing without crossreference option set");
		IPCrossRef ref = _netCrossRef.get(ip);
		if (ref == null) {
			ref = new IPCrossRef(ip);
			_netCrossRef.put(ref);
		}
		return ref;
	}

	protected void crossRefIpSets() {
		for (PxNetworkIpSet is: _ipsets.values()) {
				ParseContext pc = is.getContext();
				CrossRefContext refContext =
					new CrossRefContext("ipset", is.getName(), pc.getLine(), pc.getFileName(), pc.getLineNumber());

			crossRefIpSet(is, refContext);
		}
	}

	protected void crossRefIpSet(PxNetworkIpSet ipSet, CrossRefContext refContext) {

		for (PxNetworkObject po: ipSet.getBaseObjects().values()) {
			IPRangeable ip;
			switch (po.getType()) {
				case IPRANGE: ip = ((PxNetworkIp) po).getIpRange(); break;
				case IPALIAS: ip = ((PxNetworkAlias) po).getIpRange().getIpRange(); break;
				default: ip = null;
			}
			if (ip != null) {
				getIPNetCrossRef(ip).addContext(refContext);
			}
		}
	}

	protected void crossRefAliases() {
		for (PxNetworkAlias a: _aliases.values()) {
			ParseContext pc = a.getContext();
			CrossRefContext refContext = new CrossRefContext(
					pc.getLine(),
					"alias",
					a.getName(),
					pc.getFileName(),
					pc.getLineNumber()
			);
			IPRangeable ip = a.getIpRange().getIpRange();
			getIPNetCrossRef(ip).addContext(refContext);
		}
	}

	protected void crossRefRules() {
		for (PxGroupRules groupRules: _groupsRules.values()) {
			for (PxRule rule: groupRules) {
				crossRefRule(rule);
			}
		}
		for (PxRule rule: _rules) {
			if (rule.isGroupRule()) {
				PxGroupRules groupRules = findGroupRules(rule.getGroupIdent());
				for (PxRule grule: groupRules) {
					crossRefRule(grule);
				}
			} else {
				crossRefRule(rule);
			}
		}
	}

	protected void crossRefRule(PxRule rule) {

		if (rule.isGroupRule())
			return;

		ParseContext pc = rule.getContext();
		CrossRefContext refContext =
			new CrossRefContext("rule", rule.getAction().name(), pc.getLine(), pc.getFileName(), pc.getLineNumber());

		PxFwIpSpec spec = rule.getAddrSource();
		if (spec != null)
			crossRefIpSet(spec.getNetworks(), refContext);

		spec = rule.getAddrDest();
		if (spec != null)
			crossRefIpSet(spec.getNetworks(), refContext);

		if (rule.getProtocolsSpec() != null) {
			if (rule.getSourcePorts() != null) {
				ServiceCrossRefContext scontext = new ServiceCrossRefContext(
						rule.getProtocolsSpec(),
						ServiceCrossRefType.FROM,
						pc.getLine(), "rule", rule.getAction().name(), pc.getFileName(), pc.getLineNumber());
				crossRefPortSpec(rule.getSourcePorts(), scontext);
			}

			if (rule.getDestPorts() != null) {
				ServiceCrossRefContext scontext = new ServiceCrossRefContext(
						rule.getProtocolsSpec(),
						ServiceCrossRefType.TO,
						pc.getLine(), "rule", rule.getAction().name(), pc.getFileName(), pc.getLineNumber());
				crossRefPortSpec(rule.getDestPorts(), scontext);
			}
		}
	}

	protected void crossRefPortSpec(PortSpec portSpec, ServiceCrossRefContext refContext) {

		for (PortRange range: portSpec.getRanges()) {
			ServiceCrossRef refService = getServiceCrossRef(range);
			refService.addContext(refContext);
		}
	}

	protected ServiceCrossRef getServiceCrossRef(PortRange portrange) {
		if (!_monitorOptions.getXref())
			throw new JtaclInternalException(
					"Cross reference computing without crossreference option set");
		ServiceCrossRef ref = _serviceCrossRef.get(portrange);
		if (ref == null) {
			ref = new ServiceCrossRef(portrange);
			_serviceCrossRef.put(ref);
		}
		return ref;
	}

	@Override
	public void runShell(String command, PrintStream output) {
		_shell.shellCommand(command, output);
	}

    @Override
    public void incoming(IfaceLink link, Probe probe) {
   		if (Log.debug().isLoggable(Level.INFO))
    		Log.debug().info("probe" + probe.uidToString() + " incoming on " + _name);

		if (!link.isLoopback()) {
            probe.decTimeToLive();
            if (!probe.isAlive()) {
                probe.killError("TimeToLive expiration");
                return;
            }

            /*
             * Filter in the probe
             */
            packetFilter(link, Direction.IN, probe);
        }

		/*
		 * Check if the destination of the probe is on this equipment.
		 */
		IPNet ipdest = probe.getDestinationAddress().toIPNet();
		if (ipdest != null) {
			IfaceLink ilink = getIfaceLink(ipdest);
			if (ilink != null) {
				/*
				 * Set the probe's final position and notify the monitor
				 */
				probe.setOutgoingLink(ilink, ipdest);
				probe.destinationReached("destination reached");
				return;
			}
		}
		/*
		 * Route the probe.
		 */
		Routes routes;
		routes = _routingEngine.getRoutes(probe);
		if (routes.isEmpty()) {
			probe.killNoRoute("No route to " + probe.getDestinationAddress());
			return;
		}
		probe.routed(probe.getDestinationAddress().toString("i::"));

		if (Log.debug().isLoggable(Level.INFO)) {
			for (Route r: routes) {
				Log.debug().info("route: " + r.toString());
			}
		}

		/*
		 * if we have several routes for a destination, we have to probe these
		 * routes too because our goal is to know if a probe is able to
		 * reach a destination, regardless of the route taken.
		 */
		ArrayList<Probe> probes = new ArrayList<>();
		probes.add(probe);

		/*
		 * Create copies of the incoming probe to describe the other routes.
		 */
		for (int i = 1; i < routes.size(); i ++) {
			probes.add(probe.newInstance());
		}

		/*
		 * Set the position of the probes.
		 */
		for (int i = 0; i < routes.size(); i ++) {
			//noinspection unchecked
			Route<IfaceLink> route = routes.get(i);
			probes.get(i).setOutgoingLink(route.getLink(), route.getNextHop());
		}

		/*
		 * Filter out the probes
		 */
		for (Probe p: probes) {
			packetFilter(p.getOutgoingLink(), Direction.OUT, p);
		}

		/*
		 * Send the probes over the network.
		 */
		for (Probe p: probes) {
			/*
			 * Do not send the probe if the outgoing link is the same as the input.
			 */
			if (!p.getOutgoingLink().equals(link))
				outgoing(p.getOutgoingLink(), p, p.getNextHop());
			else
				probe.killLoop("same incoming and outgoing link");
		}
	}

	/**
	 * Packet filter
	 */
	protected void packetFilter (IfaceLink link, Direction direction, Probe probe) {

		PxGroupRules rules = _rules;
		groupRulesFilter(link, direction, probe, rules);
	}

	protected void groupRulesFilter(IfaceLink link, Direction direction, Probe probe,
									PxGroupRules groupRules) {

        String ifaceName = link.getIfaceName();
        String ifaceComment = link.getIface().getComment();
        ProbeResults results = probe.getResults();

        MatchResult match;
		for (PxRule rule: groupRules) {
			String ruleText = rule.toText();

			if (rule.isGroupRule() && _options.isEnable()) {
				FwResult aclResult = new FwResult(FwResult.MATCH);
                results.addMatchingAcl(direction, ruleText,
                        aclResult);
                results.setInterface(direction,
                        ifaceName + " (" + ifaceComment + ")");
				PxGroupRules next = findGroupRules(rule.getGroupIdent());
				groupRulesFilter(link, direction, probe, next);
			} else {
				if (!_options.isEnable()) {
					ruleText = "***firewall is disabled***";
					match = MatchResult.ALL;
				} else {
					match = ruleFilter(link, direction, probe, rule);
				}
	            if (match != MatchResult.NOT) {
					/*
					 * store the result in the probe
					 */
					FwResult aclResult = new FwResult();
					if (match != MatchResult.ALL)
						aclResult.addResult(FwResult.MAY);

					switch (rule.getAction()) {
						case ACCEPT: aclResult.addResult(FwResult.ACCEPT); break;
						case DROP: aclResult.addResult(FwResult.DENY); break;
					}

					results.addMatchingAcl(direction, ruleText, aclResult);

					results.setInterface(direction,
							ifaceName + " (" + ifaceComment + ")");

					/*
					 * the active acl is the acl accepting or denying the packet.
					 * this is the first acl that match the packet.
					 */
					if (results.getActiveAcl(direction).isEmpty() && (aclResult.hasAccept() || aclResult.hasDeny())) {
						results.addActiveAcl(direction,
								ruleText,
								aclResult);
						results.setAclResult(direction,
								aclResult);
					}
				}
			}
		}
	}


	protected MatchResult ruleFilter(IfaceLink link, Direction direction, Probe probe, PxRule rule) {
		int may = 0;

	    // disabled
	    if (rule.isDisabled()) return MatchResult.NOT;

	    // direction
		switch (direction) {
			case IN: if (rule.getDirection() != PxRuleDirection.IN) return MatchResult.NOT; break;
			case OUT: if (rule.getDirection() != PxRuleDirection.OUT) return MatchResult.NOT; break;
			default: return MatchResult.NOT;
		}

	    // implicit policy
        if (rule.isImplicit()) return MatchResult.ALL;

        // interface
        String iface = rule.getIfaceName();
        if (iface != null) {
        	if (!link.getIfaceName().equalsIgnoreCase(iface)) return MatchResult.NOT;
		}

        // source ip
		PxFwIpSpec ipspec = rule.getAddrSource();
		MatchResult mIpSource = ipSpecFilter(ipspec, probe.getSourceAddress());
		if (mIpSource == MatchResult.NOT)
			return MatchResult.NOT;
		if (mIpSource != MatchResult.ALL)
			may++;

		// destination IP
		ipspec = rule.getAddrDest();
		MatchResult mIpDest =ipSpecFilter(ipspec, probe.getDestinationAddress());
		if (mIpDest == MatchResult.NOT)
			return MatchResult.NOT;
		if (mIpDest != MatchResult.ALL)
			may++;

		// check protocol
		ProtocolsSpec proto = rule.getProtocolsSpec();
		if (probe.getRequest().getProtocols() != null) {
			if (proto != null && !proto.isEmpty()) {
				if (rule.getProtocolsSpec().matches(probe.getRequest().getProtocols()) == MatchResult.NOT) {
					return MatchResult.NOT;
				}
			}
		}

		// check icmp type
		IPIcmpEnt icmpEnt = rule.getIpIcmp();
		if (icmpEnt != null) {
			MatchResult mIcmpType = icmpTypeFilter(icmpEnt, probe);
			if (mIcmpType == MatchResult.NOT)
				return MatchResult.NOT;
		}

		// check source ports
		MatchResult mSourcePorts;
		PortSpec portSpec = rule.getSourcePorts();
		if (portSpec != null && probe.getRequest().getSourcePort() != null) {
			mSourcePorts = portSpec.matches(probe.getRequest().getSourcePort());
			if (mSourcePorts == MatchResult.NOT)
				return MatchResult.NOT;
		} else {
			mSourcePorts = MatchResult.ALL;
		}
		if (mSourcePorts != MatchResult.ALL)
			may++;

		// check destination ports
		MatchResult mdDestPorts;
		portSpec = rule.getDestPorts();
		if (portSpec != null && probe.getRequest().getDestinationPort() != null) {
			mdDestPorts = portSpec.matches(probe.getRequest().getDestinationPort());
			if (mdDestPorts == MatchResult.NOT)
				return MatchResult.NOT;
		} else {
			mdDestPorts = MatchResult.ALL;
		}
		if (mdDestPorts != MatchResult.ALL)
			may++;

		return (may > 0) ? MatchResult.MATCH : MatchResult.ALL;
	}

	protected MatchResult ipSpecFilter(PxFwIpSpec ipSpec, IPRangeable range) {

		if (ipSpec.getNetworks().getBaseObjects().isEmpty())
			return MatchResult.ALL;
		MatchResult mres = ipSpec.getNetworks().matches(range);
		return mres;
	}

	protected MatchResult icmpTypeFilter(IPIcmpEnt ipIcmpEnt, Probe probe) {
		/*
		 * icmp type and code
		 */
		Integer icmpType = probe.getRequest().getSubType();
		if (icmpType == null) {
			return MatchResult.ALL;
		}

		if (icmpType != ipIcmpEnt.getIcmp()) {
			return MatchResult.NOT;
		}

		Integer icmpCode = probe.getRequest().getCode();
		if (icmpCode == null) {
			return MatchResult.ALL;
		}

		if (icmpCode == ipIcmpEnt.getCode()) {
			return MatchResult.ALL;
		}
		return MatchResult.NOT;
	}
}
