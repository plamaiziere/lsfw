/*
 * Copyright (c) 2010, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.equipments.cisco.router;

import fr.univrennes1.cri.jtacl.core.exceptions.JtaclConfigurationException;
import fr.univrennes1.cri.jtacl.lib.ip.IPIcmp4;
import fr.univrennes1.cri.jtacl.lib.ip.IPIcmpEnt;
import fr.univrennes1.cri.jtacl.lib.ip.IPversion;
import fr.univrennes1.cri.jtacl.parsers.CommonRules;
import org.parboiled.Rule;
import org.parboiled.annotations.SuppressSubnodes;

/**
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class IOSParser extends CommonRules<Object> {

	protected String _name;
	protected String _direction;
	protected String _ipAddress;
	protected String _ipNetmask;
	protected String 	_nexthop;
	protected String _description;
	protected String _portOperator;
	protected String _firstPort;
	protected String _lastPort;
	protected String _subType;
	protected String _ruleName;
	protected AclTemplate _acl;
	protected AceTemplate _ace;
	protected AccessList _aclContext;

	protected boolean checkAclContext(AclType type) {
		return _aclContext != null && _aclContext.getAclType() == type;
	}

	protected boolean checkAclType(AclType type) {
		return _acl != null && _acl.getAclType() == type;
	}

	protected boolean newAclTemplate(String type) {
		_acl = new AclTemplate();
		if (type.equals("standard"))
			_acl.setAclType(AclType.IPSTD);
		else
			_acl.setAclType(AclType.IPEXT);
		return true;
	}

	protected boolean newAclTemplateNumber(String number) {
		int n = Integer.valueOf(number);
		_acl = new AclTemplate();
		_acl.setNumber(n);
		_acl.setIpVersion(IPversion.IPV4);
		_acl.setAclType(AclType.getType(n));
		return true;
	}

	protected boolean newAceTemplate() {
		_ace = new AceTemplate();
		return true;
	}

	protected boolean setAceIcmp4(String icmp) {
		IPIcmpEnt icmpEnt = IPIcmp4.getInstance().icmpLookup(icmp);
		if (icmpEnt != null) {
			_ace.setSubType(icmp);
			_ace.setCode(icmpEnt.getCode());
			return true;
		}
		throw new JtaclConfigurationException("unknown icmp type or message: "
				+ icmp);
	}

	public String getDirection() {
		return _direction;
	}

	public boolean setDirection(String direction) {
		_direction = direction;
		return true;
	}

	public String getName() {
		return _name;
	}

	public boolean setName(String name) {
		_name = name;
		return true;
	}

	public String getDescription() {
		return _description;
	}

	public boolean setDescription(String description) {
		_description = description;
		return true;
	}

	public String getIpAddress() {
		return _ipAddress;
	}

	public boolean setIpAddress(String ipAddress) {
		_ipAddress = ipAddress;
		return true;
	}
	
	public String getIpNetmask() {
		return _ipNetmask;
	}

	public boolean setIpNetmask(String ipNetmask) {
		_ipNetmask = ipNetmask;
		return true;
	}

	public String getNexthop() {
		return _nexthop;
	}

	public boolean setNexthop(String nexthop) {
		_nexthop = nexthop;
		return true;
	}

	public String getRuleName() {
		return _ruleName;
	}

	public boolean setRuleName(String ruleName) {
		_ruleName = ruleName;
		return true;
	}

	public AclTemplate getAclTemplate() {
		return _acl;
	}

	public AceTemplate getAceTemplate() {
		return _ace;
	}

	public void setAclContext(AccessList acl) {
		_aclContext = acl;
	}

	public String getFirstPort() {
		return _firstPort;
	}

	public boolean setFirstPort(String firstPort) {
		_firstPort = firstPort;
		return true;
	}

	public String getLastPort() {
		return _lastPort;
	}

	public boolean setLastPort(String lastPort) {
		_lastPort = lastPort;
		return true;
	}

	public String getPortOperator() {
		return _portOperator;
	}

	public boolean setPortOperator(String portOperator) {
		_portOperator = portOperator;
		return true;
	}

	public String getSubType() {
		return _subType;
	}

	public boolean setSubType(String subType) {
		_subType = subType;
		return true;
	}

	/**
	 * Resets the resulting values of the parsing to null.
	 */
	protected boolean clear() {
		_name = null;
		_direction = null;
		_description = null;
		_ipAddress = null;
		_ipNetmask = null;
		_nexthop = null;
		_acl = null;
		_ace = null;
		_portOperator = null;
		_firstPort = null;
		_lastPort = null;
		_subType = null;
		_ruleName = null;
		return true;
	}

	/**
	 * Returns true if the line in argument should match a rule in main context.
	 * @param line
	 * @return true if the line in argument should match a rule in main context.
	 */
	public boolean shouldMatchInMain(String line) {
		String [] should = {
			"ip route ",
			"ipv6 route ",
			"access-list ",
			"permit ",
			"deny ",
			"ip access-list ",
			"ipv6 access-list "
		};

		for (String s: should) {
			if (line.startsWith(s))
				return true;
		}
		return false;
	}

	/**
	 * Returns true if the line in argument should match a rule in interface
	 * context.
	 * @param line
	 * @return true if the line in argument should match a rule in interface
	 * context.
	 */
	public boolean shouldMatchInInterface(String line) {
		String [] should = {
			"description",
			"ip address",
			"ipv6 address",
			"ip access-group",
			"ipv6 traffic-filter",
			"shutdown"
		};

		for (String s: should) {
			if (line.startsWith(s))
				return true;
		}
		return false;
	}

	/**
	 * Strip IOS comment from the string in argument.
	 * @param str String to strip.
	 */
	public String stripComment(String str) {
		String res = null;
		int p = str.indexOf('!');
		if (p < 0)
			res = str;
		if (p == 0)
			res = "";
		if (p > 0)
			res = str.substring(0, p);
		p = res.indexOf('#');
		if (p == 0)
			res = "";
		if (p > 0)
			res = str.substring(0, p);
		return res;
	}

	/**
	 * Matches IOS command 'interface'.
	 * @return a {@link Rule}
	 */
	@SuppressSubnodes
	public Rule Interface() {
		return Sequence(
				clear(),
				String("interface"),
				WhiteSpaces(),
				UntilEOI(),
				setName(match()),
				setRuleName("interface")
			);
	}

	/**
	 * Matches IOS commands outside an interface context.
	 * So we exit the interface context.
	 * @return a {@link Rule}
	 */
	@SuppressSubnodes
	public Rule ExitInterface() {
		return Sequence (
					clear(),
					FirstOf(
						String("interface"),
						String("router"),
						String("ip access-list"),
						String("object-group"),
						String("boot"),
						String("ftp"),
						String("dns"),
						String("clock"),
						String("passwd"),
						String("access-list")
					),
					UntilEOI()
			);
	}

	/**
	 * Matches IOS commands in the interface context.
	 * (only commands in our interest).
	 * @return a {@link Rule}
	 */
	@SuppressSubnodes
	public Rule InInterface() {
		return Sequence(
					clear(),
					FirstOf(
						IfDescription(),
						IfIpv6Address(),
						IfIpAddress(),
						IfIpAccessGroup(),
						IfIpv6TrafficFilter(),
						IfShutdown()
					)
			);
	}

	/**
	 * Matches IOS command 'decription'
	 * @return a {@link Rule}
	 */
	public Rule IfDescription() {
		return Sequence(
				String("description"),
				WhiteSpaces(),
				UntilEOI(),
				setDescription(match()),
				setRuleName("description")
		);
	}

	/**
	 * Matches IOS command 'shutdown'
	 * @return a {@link Rule}
	 */
	public Rule IfShutdown() {
		return Sequence(
				String("shutdown"),
				UntilEOI(),
				setRuleName("shutdown")
			);
	}

	/**
	 * Matches IOS command 'ip address'
	 * ip address ip-address mask [secondary [vrf  vrf-name]]
	 * @return a {@link Rule}
	 */
	public Rule IfIpAddress() {
		return Sequence(
					String("ip"),
					WhiteSpaces(),
					String("address"),
					WhiteSpaces(),
					StringAtom(),
					setIpAddress(match()),
					WhiteSpaces(),
					StringAtom(),
					setIpNetmask(match()),
					setRuleName("ip address"),
					// we don't care about the rest if any.
					UntilEOI()
				);
	}

	/**
	 * Matches IOS command 'ipv6 address'
	 * TODO: cisco reference
	 * @return a {@link Rule}
	 */
	public Rule IfIpv6Address() {
		return Sequence(
					String("ipv6"),
					WhiteSpaces(),
					String("address"),
					WhiteSpaces(),
					StringAtom(),
					setIpAddress(match()),
					setRuleName("ipv6 address"),
					EOI
				);
	}

	/**
	 * Matches ip access-group. <br/>
	 * reference Cisco TODO: : <br/>
	 * ip access-group access-list in | out
	 * @return a {@link Rule}
	 */
	public Rule IfIpAccessGroup() {
		return	Sequence(
					String("ip"),
					WhiteSpaces(),
					String("access-group"),
					WhiteSpaces(),
					StringAtom(),
					setName(match()),
					WhiteSpaces(),
					FirstOf(
						String("in"),
						String("out")
					),
					setDirection(match()),
					setRuleName("ip access-group"),
					UntilEOI()
				);
	}

	/**
	 * Matches ipv6 traffic-filter  <br/>
	 * reference Cisco: <br/>
	 * ipv6 traffic-filter access-list in | out
	 * @return a {@link Rule}
	 */
	public Rule IfIpv6TrafficFilter() {
		return	Sequence(
					String("ipv6"),
					WhiteSpaces(),
					String("traffic-filter"),
					WhiteSpaces(),
					StringAtom(),
					setName(match()),
					WhiteSpaces(),
					FirstOf(
						String("in"),
						String("out")
					),
					setDirection(match()),
					setRuleName("ipv6 traffic-filter"),
					UntilEOI()
				);
	}

	/**
	 * Parsing of route, acl ...
	 * @return a {@link Rule}
	 */
	@SuppressSubnodes
	public Rule Parse() {
		return Sequence(
				clear(),
				FirstOf(
					Ignore(),
					IpRoute(),
					Ipv6Route(),
					IpAccessList(),
					Ipv6AccessList(),
					AccessListRemark(),
					AccessList(),
					/*
					 * parsing of ACE depends on the acl context
					 */
					FirstOf(
						Sequence(
							checkAclContext(AclType.IPSTD),
							AceStandard()
						),
						Sequence(
							checkAclContext(AclType.IPEXT),
							AceExtended()
						)
					)
				)
			);
	}

	/**
	 * Matches commands found in access list context.
	 * @return a {@link Rule}
	 */
	@SuppressSubnodes
	public Rule InAclContext() {
		return Sequence(
					Optional(
						Sequence(
							String("no"),
							WhiteSpaces()
						)
					),
					FirstOf(
						String("permit"),
						String("deny"),
						String("evaluate"),
						String("remark")
					),
					UntilEOI()
				);
	}

	/**
	 * Matches commands we ignore
	 * @return a {@link Rule}
	 */
	public Rule Ignore() {
		return Sequence(
			FirstOf(
				// access-list compiled
				Sequence(
					String("access-list"),
					WhiteSpaces(),
					String("compiled"),
					UntilEOI()
				),
				// place holder for new rules
				String("###### PLACE HOLDER #######")
			),
			setRuleName("Ignore")
		);
	}

	/**
	 * Matches IOS command 'ip route'
	 * ip route ip-address mask nexthop
	 *
	 * reference Cisco :
	 *	ip route prefix mask
	 *		{ip-address | interface-type  interface-number [ip-address]}
	 *		[dhcp] [distance] [name next-hop-name]
	 *		[permanent | track number] [tag tag]
	 *
	 * @return a {@link Rule}
	 */
	 public Rule IpRoute() {
		 return Sequence(
					String("ip"),
					WhiteSpaces(),
					String("route"),
					WhiteSpaces(),
					StringAtom(),
					setIpAddress(match()),
					WhiteSpaces(),
					StringAtom(),
					setIpNetmask(match()),
					WhiteSpaces(),
					StringAtom(),
					setNexthop(match()),
					setRuleName("ip route"),
					EOI
			);		 
	 }

	/**
	 * Matches IOS command 'ipv6 route'
	 * ip route ip-address/len nexthop
	 *
	 * reference Cisco:
	 * ipv6 route [vrf vrf-name] ipv6-prefix/prefix-length
	 *	{ipv6-address | interface-type interface-number [ipv6-address]}
	 *	[nexthop-vrf [vrf-name1 | default]]
	 *	[administrative-distance]
	 *	[administrative-multicast-distance | unicast | multicast]
	 *	[next-hop-address] [tag tag]
	 *
	 * @return a {@link Rule}
	 */
	 public Rule Ipv6Route() {
		 return Sequence(
					String("ipv6"),
					WhiteSpaces(),
					String("route"),
					WhiteSpaces(),
					StringAtom(),
					setIpAddress(match()),
					WhiteSpaces(),
					StringAtom(),
					setNexthop(match()),
					setRuleName("ipv6 route"),
					EOI
			);
	 }

	/**
	 * Matches access-list containing 'remark'
	 * return a {@link Rule}
	 */
	public Rule AccessListRemark() {
		return Sequence(
					String("access-list"),
					WhiteSpaces(),
					ZeroOrMore(
						Sequence(
							TestNot(String("remark")),
							ANY
						)
					),
					String("remark"),
					setRuleName("access-list remark")
				);
	}

	/**
	 * Matches ip access-list <br/>
	 * reference: <br/>
	 * ip access-list standard | extended name
	 */
	public Rule IpAccessList() {
		return
			Sequence(
				String("ip"),
				WhiteSpaces(),
				String("access-list"),
				WhiteSpaces(),
				FirstOf(
					String("standard"),
					String("extended")
				),
				newAclTemplate(match()),
				WhiteSpaces(),
				StringAtom(),
				_acl.setName(match()),
				_acl.setIpVersion(IPversion.IPV4),
				setRuleName("ip access-list named"),
				EOI
			);
	}

	/**
	 * Matches ipv6 access-list <br/>
	 * reference: <br/>
	 * ipv6 access-list standard | extended name
	 */
	public Rule Ipv6AccessList() {
		return
			Sequence(
				String("ipv6"),
				WhiteSpaces(),
				String("access-list"),
				newAclTemplate("extended"),
				WhiteSpaces(),
				StringAtom(),
				_acl.setName(match()),
				_acl.setIpVersion(IPversion.IPV6),
				setRuleName("ipv6 access-list named"),
				EOI
			);
	}

	/**
	 * Main rule for AccessList
	 * Sub-rules are determined by the type of the access-list (ie the number).
	 * @return a {@link Rule}
	 */
	public Rule AccessList() {
		return
			Sequence(
				String("access-list"),
				WhiteSpaces(),
				Number(),
				newAclTemplateNumber(match()),
				WhiteSpaces(),
				/*
				 * Test the acl type to get the rule.
				 */
				FirstOf(
					/* standard */
					Sequence(
						checkAclType(AclType.IPSTD),
						AceStandard()
					),
					Sequence(
						checkAclType(AclType.IPEXT),
						AceExtended()
					)
				),
				setRuleName("access-list")
			);
	 }

	 /**
	 * Matches a standard access-list element. <br/>
	 * reference: <br/>
	 * {deny | permit} <br/>
	 * source [source-wildcard] <br/>
	 * [log [word]]
	 */	 
	public Rule AceStandard() {
		return
			Sequence(
				newAceTemplate(),
				FirstOf(
					String("permit"),
					String("deny")
				),
				_ace.setAction(match()),
				WhiteSpaces(),
				FirstOf(
					String("any"),
					Sequence(
						String("host"),
						WhiteSpaces(),
						StringAtom(),
						_ace.setSrcIp(match())
					),
					Sequence(
						StringAtom(),
						_ace.setSrcIp(match()),
						Optional(
							Sequence(
								WhiteSpaces(),
								TestNot(String("log")),
								StringAtom(),
								_ace.setSrcIpMask(match())
							)
						)
					)
				),
				setRuleName("ace standard"),
				UntilEOI()
			);
	}

	/**
	 * Matches tcp / udp extended ACE
	 * @return a {@link Rule}
	 */
	public Rule AceExtendedTcpUdp() {
		return
			Sequence(
				// source
				AclIpAddress(),
				_ace.setSrcIp(_ipAddress),
				_ace.setSrcIpMask(_ipNetmask),
				setIpAddress(null),
				setIpNetmask(null),
				WhiteSpaces(),
				Optional(
					Sequence(
						PortOperatorOrRange(),
						_ace.setSrcPortOperator(_portOperator),
						_ace.setSrcFirstPort(_firstPort),
						_ace.setSrcLastPort(_lastPort),
						setPortOperator(null),
						setFirstPort(null),
						setLastPort(null),
						WhiteSpaces()
					)
				),
				// destination
				AclIpAddress(),
				_ace.setDstIp(_ipAddress),
				_ace.setDstIpMask(_ipNetmask),
				setIpAddress(null),
				setIpNetmask(null),
				WhiteSpacesOrEoi(),
				Optional(
					Sequence(
						PortOperatorOrRange(),
						_ace.setDstPortOperator(_portOperator),
						_ace.setDstFirstPort(_firstPort),
						_ace.setDstLastPort(_lastPort),
						setPortOperator(null),
						setFirstPort(null),
						setLastPort(null)
					)
				),
				SkipSpaces(),
				Optional(
					AceTcpFlags()
				)
			);
	}

	/**
	 * Matches tcp flags in extended ACE.
	 * @return a Rule
	 */
	public Rule AceTcpFlags() {
		return
			FirstOf(
				AceTcpEstablished(),
				AceTcpNewFormat(),
				AceTcpOldFormat()
			);
	}

	/**
	 * Matches "established" in extended ACE.
	 * @return a Rule
	 */
	public Rule AceTcpEstablished() {
		return
			Sequence(
				String("established"),
				/*
				 * established : ack or rst
				 */
				_ace.setTcpKeyword("match-any"),
				_ace.getTcpFlags().add("+ack"),
				_ace.getTcpFlags().add("+rst")
			);
	}

	/**
	 * Matches new TCP flags format.
	 * match-any | match-all tcpflags
	 * @return a Rule
	 */
	public Rule AceTcpNewFormat() {
		return
			Sequence(
				FirstOf(
					String("match-any"),
					String("match-all")
				),
				_ace.setTcpKeyword(match()),
				WhiteSpaces(),
				OneOrMore(
					AceTcpNewFlag()
				)
			);
	}

	/**
	 * Matches new TCP flag format.
	 * @return a Rule
	 */
	public Rule AceTcpNewFlag() {
		return
			Sequence(
				FirstOf(
					String("+ack"),
					String("+fin"),
					String("+psh"),
					String("+rst"),
					String("+syn"),
					String("+urg"),
					String("-ack"),
					String("-fin"),
					String("-psh"),
					String("-rst"),
					String("-syn"),
					String("-urg")
				),
				_ace.getTcpFlags().add(match()),
				SkipSpaces()
			);
	}

	/**
	 * Matches old TCP flags format.
	 * @return a Rule.
	 */
	public Rule AceTcpOldFormat() {
		return
			ZeroOrMore(
				AceTcpOldFlag()
			);
	}

	/**
	 * Matches old TCP flags format.
	 * @return a Rule.
	 */
	public Rule AceTcpOldFlag() {
		return
			Sequence(
				FirstOf(
					String("ack"),
					String("fin"),
					String("psh"),
					String("rst"),
					String("syn"),
					String("urg")
				),
				_ace.getTcpFlags().add("+".concat(match())),
				_ace.setTcpKeyword("match-any"),
				SkipSpaces()
			);
	}

	/**
	 * Matches other protocol in extended ACE (ip, ipv6, igmp...)
	 * @return a {@link Rule}
	 */
	public Rule AceExtendedOtherProtocol() {
		return
			Sequence(
				// source
				AclIpAddress(),
				_ace.setSrcIp(_ipAddress),
				_ace.setSrcIpMask(_ipNetmask),
				setIpAddress(null),
				setIpNetmask(null),
				WhiteSpaces(),
				// destination
				AclIpAddress(),
				_ace.setDstIp(_ipAddress),
				_ace.setDstIpMask(_ipNetmask),
				setIpAddress(null),
				setIpNetmask(null)
			);
	}


	/**
	 * Matches icmp extended ACE
	 * @return a {@link Rule}
	 */
	public Rule AceExtendedIcmp() {
		return
			Sequence(
				// source
				AclIpAddress(),
				_ace.setSrcIp(_ipAddress),
				_ace.setSrcIpMask(_ipNetmask),
				setIpAddress(null),
				setIpNetmask(null),
				WhiteSpaces(),
				// destination
				AclIpAddress(),
				_ace.setDstIp(_ipAddress),
				_ace.setDstIpMask(_ipNetmask),
				setIpAddress(null),
				setIpNetmask(null),
				Optional(
					Sequence(
						WhiteSpaces(),
						AclSubType(),
						setAceIcmp4(match())
					)
				)
			);
	}


	/**
	 * Matches an extended access-list element. <br/>
	 * reference: <br/>
	 *	{deny | permit} protocol source source-wildcard<br/>
	 * destination destination-wildcard<br/>
	 * [precedence precedence] <br/>
	 * [tos tos] <br/>
	 * [time-range time-range-name]<br/>
	 * [fragments] <br/>
	 * [log [word] | log-input [word]]<br/>
	 * <br/>
	 * ICMP:<br/>
	 * {deny | permit} icmp source source-wildcard<br/>
	 * destination destination-wildcard<br/>
	 * [icmp-type [icmp-code] | icmp-message]<br/>
	 * [precedence precedence]<br/>
	 * [tos tos]<br/>
	 * [time-range time-range-name]<br/>
	 * [fragments]<br/>
	 * [log [word] | log-input [word]]<br/>
	 *<br/>
	 * IGMP:<br/>
	 * {deny | permit} igmp source source-wildcard<br/>
	 * destination destination-wildcard <br/>
	 * [igmp-type]<br/>
	 * [precedence precedence]<br/>
	 * [tos tos]<br/>
	 * [time-range time-range-name]<br/>
	 * [fragments]<br/>
	 * [log [word] | log-input [word]]<br/>
	 * <br/>
	 * TCP/UDP (established for TCP only):<br/>
	 * {deny | permit} tcp source source-wildcard [operator [port]]<br/>
	 * destination destination-wildcard [operator [port]]<br/>
	 * [established]<br/>
	 * [precedence precedence]<br/>
	 * [tos tos]<br/>
	 * [time-range time-range-name]<br/>
	 * [fragments]<br/>
	 * [log [word] | log-input [word]]<br/>
	 *
	 * @return a {@link Rule}
	 */
	public Rule AceExtended() {
		return
			Sequence(
				newAceTemplate(),
				// permit | deny
				FirstOf(
					String("permit"),
					String("deny")
				),
				_ace.setAction(match()),
				WhiteSpaces(),
				FirstOf(
					// tcp | udp
					Sequence(
						FirstOf(
							String("tcp"),
							String("udp")
						),
						_ace.setProtocol(match()),
						WhiteSpaces(),
						AceExtendedTcpUdp()
					),
					// icmp
					Sequence(
						String("icmp"),
						_ace.setProtocol(match()),
						WhiteSpaces(),
						AceExtendedIcmp()
					),
					// other protocol
					Sequence(
						StringAtom(),
						_ace.setProtocol(match()),
						WhiteSpaces(),
						AceExtendedOtherProtocol()
					)
				),
				setRuleName("ace extended"),
				UntilEOI()
			);
	}

	/**
	 * Matches ip mask | any | host ip
	 * @return a {@link Rule}
	 */
	public Rule AclIpAddress() {
		return 
				FirstOf(
					// any
					Sequence(
						String("any"),
						setIpAddress("any")
					),
					// host ip
					Sequence(
						String("host"),
						WhiteSpaces(),
						StringAtom(),
						setIpAddress(match())
					),
					// ip [netmask] (no netmask if ipv6)
					Sequence(
						StringAtom(),
						setIpAddress(match()),
						// netmask | nothing (ipv6)
						FirstOf(
							_ipAddress.contains(":"),
							Sequence(
								WhiteSpaces(),
								StringAtom(),
								setIpNetmask(match())
							)
						)
					)
				);
	}

	public Rule AclSubType() {
		return Sequence(
			TestNot(AclKeyword()),
			StringAtom(),
			setSubType(match())
		);
	}


	public Rule AclKeyword() {
		return FirstOf(
					String("fragments"),
					String("log"),
					String("precedence"),
					String("reflect"),
					String("time-out"),
					String("time-range"),
					String("tos")
				);
	}

	/**
	 * Matches port operator | port range
	 */
	public Rule PortOperatorOrRange() {
		return FirstOf(
					PortOperator(),
					PortOperatorRange()
		);
	}

	/**
	 * Matches port operator 'eq', 'neq', 'lt', 'gt'
	 * @return A {@link Rule}
	 */
	public Rule PortOperator() {
		return Sequence(
					FirstOf(
						String("eq"),
						String("neq"),
						String("lt"),
						String("gt")
					),
					setPortOperator(match()),
					WhiteSpaces(),
					StringAtom(),
					setFirstPort(match())
			);
	}

	/**
	 * Matches port operator 'range'
	 * @return A {@link Rule}
	 */
	public Rule PortOperatorRange() {
		return Sequence(
					String("range"),
					setPortOperator(match()),
					WhiteSpaces(),
					StringAtom(),
					setFirstPort(match()),
					WhiteSpaces(),
					StringAtom(),
					setLastPort(match())
			);
		}

}
