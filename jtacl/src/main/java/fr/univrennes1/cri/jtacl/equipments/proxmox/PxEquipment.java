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
import fr.univrennes1.cri.jtacl.equipments.generic.GenericEquipment;
import fr.univrennes1.cri.jtacl.lib.ip.IPIcmpEnt;
import fr.univrennes1.cri.jtacl.lib.ip.IPRange;
import fr.univrennes1.cri.jtacl.lib.ip.IPRangeable;
import fr.univrennes1.cri.jtacl.lib.ip.PortRange;
import fr.univrennes1.cri.jtacl.lib.ip.PortSpec;
import fr.univrennes1.cri.jtacl.lib.ip.ProtocolsSpec;
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

	protected HashMap<String, PxNetworkAlias> _aliases = new HashMap<>();
	protected HashMap<String, PxNetworkIpSet> _ipsets = new HashMap<>();
	protected HashMap<String, PxGroupRules> _groupsRules = new HashMap<>();
	protected PxGroupRules _rules = new PxGroupRules(null, null);

	protected PxEquipment _host;

    protected PxFwShell _shell = new PxFwShell(this);


	/**
	 * Parser
	 */
	protected VeParser _parser = Parboiled.createParser(VeParser.class);

	/**
	 * ParseRunner for section
	 */
	protected BasicParseRunner _parseRunSection =
			new BasicParseRunner(_parser.RSection());

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
			String cl = VeParser.stripComment(l);
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

	public void parseSectionAliases(String cl) {

		ParsingResult<?> result = _parseRunAlias.run(cl);
		if (result.matched) {
			AliasTemplate atpl = _parser.getAliasTpl();
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
			RuleTemplate rtpl = _parser.getRuleTpl();
			PxRule rule = parseRule(group, rtpl);
			group.add(rule);
			return;
		}

		warnConfig("Rule doesnt match parser rule", true);
	}

	public void parseSectionRules(String sectionIdent, String cl) {

		ParsingResult<?> result = _parseRunRule.run(cl);
		if (result.matched) {
			RuleTemplate rtpl = _parser.getRuleTpl();
			PxRule rule = parseRule(null, rtpl);
			_rules.add(rule);
			return;
		}

		warnConfig("Rule doesnt match parser rule", true);
	}

	public IPRangeable parseAlias(AliasTemplate aliasTemplate) {

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

	public PxRule parseRule(PxGroupRules group, RuleTemplate rtpl) {
		boolean disabled;
		RuleDirection direction;
		RuleAction action;
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

		direction = rtpl.getDirection().equalsIgnoreCase("IN") ? RuleDirection.IN : RuleDirection.OUT;
		action = rtpl.getAction().equalsIgnoreCase("ACCEPT") ? RuleAction.ACCEPT : RuleAction.DROP;

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

}
