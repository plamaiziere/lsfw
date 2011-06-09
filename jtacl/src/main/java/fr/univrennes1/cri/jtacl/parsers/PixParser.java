/*
 * Copyright (c) 2010, Universite de Rennes 1
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

package fr.univrennes1.cri.jtacl.parsers;

import fr.univrennes1.cri.jtacl.equipments.cisco.pix.AclTemplate;
import fr.univrennes1.cri.jtacl.equipments.cisco.pix.GroupTypeSearchable;
import fr.univrennes1.cri.jtacl.equipments.cisco.pix.ObjectGroupType;
import fr.univrennes1.cri.jtacl.lib.ip.IPIcmp4;
import org.parboiled.Rule;
import org.parboiled.annotations.SuppressSubnodes;

/**
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PixParser extends CommonRules<Object> {

	protected String _name;
	protected String _ipAddress;
	protected String _ipNetmask;
	protected String _nexthop;
	protected String _interface;
	protected String _shutdown;
	protected String _ruleName;
	protected String _groupId;
	protected String _protocol;
	protected String _portOperator;
	protected String _firstPort;
	protected String _lastPort;
	protected String _direction;

	protected AclTemplate _acl;
	protected GroupTypeSearchable _groupTypeSearch;

	protected boolean newAclTemplate() {
		_acl = new AclTemplate();
		return true;
	}

	protected boolean checkObjectGroupType(String id, ObjectGroupType type) {
		ObjectGroupType t =	_groupTypeSearch.getGroupType(id);
		if (t != null && t == type)
			return true;
		return false;
	}

	protected boolean aclSetIcmp(String icmp) {
		if (IPIcmp4.getInstance().icmpLookup(icmp) != null) {
			_acl.setIcmp(icmp);
			return true;
		}
		return false;
	}


	public AclTemplate getAcl() {
		return _acl;
	}

	public boolean setAcl(AclTemplate acl) {
		_acl = acl;
		return true;
	}

	public String getDirection() {
		return _direction;
	}

	public boolean setDirection(String direction) {
		_direction = direction;
		return true;

	}

	public String getFirstPort() {
		return _firstPort;
	}

	public boolean setFirstPort(String firstPort) {
		_firstPort = firstPort;
		return true;
	}

	public String getGroupId() {
		return _groupId;
	}

	public boolean setGroupId(String groupId) {
		_groupId = groupId;
		return true;
	}

	public String getInterface() {
		return _interface;
	}

	public boolean setInterface(String iface) {
		_interface = iface;
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

	public String getLastPort() {
		return _lastPort;
	}

	public boolean setLastPort(String lastPort) {
		_lastPort = lastPort;
		return true;
	}

	public String getName() {
		return _name;
	}

	public boolean setName(String name) {
		_name = name;
		return true;
	}

	public String getNexthop() {
		return _nexthop;
	}

	public boolean setNexthop(String nexthop) {
		_nexthop = nexthop;
		return true;
	}

	public String getPortOperator() {
		return _portOperator;
	}

	public boolean setPortOperator(String portOperator) {
		_portOperator = portOperator;
		return true;
	}

	public String getProtocol() {
		return _protocol;
	}

	public boolean setProtocol(String protocol) {
		_protocol = protocol;
		return true;
	}

	public String getRuleName() {
		return _ruleName;
	}

	public boolean setRuleName(String ruleName) {
		_ruleName = ruleName;
		return true;
	}

	public String getShutdown() {
		return _shutdown;
	}

	public boolean setShutdown(String shutdown) {
		_shutdown = shutdown;
		return true;
	}

	public void setGroupTypeSearch(GroupTypeSearchable groupType) {
		_groupTypeSearch = groupType;
	}

	/**
	 * Resets the resulting values of the parsing to null.
	 */
	protected boolean clear() {
		_name = null;
		_ipAddress = null;
		_ipNetmask = null;
		_nexthop = null;
		_ruleName = null;
		_interface = null;
		_groupId = null;
		_protocol = null;
		_portOperator = null;
		_firstPort = null;
		_lastPort = null;
		_direction = null;
		_acl = null;
		_shutdown = null;
		return true;
	}

	/**
	 * Returns true if the line in argument should match a rule in main context.
	 * @param line
	 * @return true if the line in argument should match a rule in main context.
	 */
	public boolean shouldMatchInMain(String line) {
		String [] should = {
			"ip route",
			"ipv6 route",
			"name ",
			"object-group",
			"network-object",
			"protocol-object",
			"service-object",
			"port-object",
			"icmp-object",
			"access-group",
			"group-object",
			"access-list",
			"description"
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
			"nameif",
			"ip address",
			"ipv6 address",
		};

		for (String s: should) {
			if (line.startsWith(s))
				return true;
		}
		return false;
	}

	/**
	 * Strip comment from the string in argument.
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
	 * Match command 'interface'.
	 * @return a {@link Rule}
	 */
	@SuppressSubnodes
	public Rule Interface() {
		return Sequence(
				clear(),
				String("interface"),
				WhiteSpaces(),
				UntilShutdown(),
				setName(match().trim()),
				Optional(
					Sequence(
						String("shutdown"),
						setShutdown("shutdown")
					)
				),
				UntilEOI(),
				setRuleName("interface")
			);
	}

	/**
	 * Match commands outside an interface context.
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
	 * Match commands in the interface context.
	 * (only commands in our interest).
	 * @return a {@link Rule}
	 */
	@SuppressSubnodes
	public Rule InInterface() {
		return
			Sequence(
				clear(),
				FirstOf(
					IfName(),
					IfIpv6Address(),
					IfIpAddress()
				)
			);
	}

	/**
	 * Match command 'ip address' <br/>
	 * TODO: ref
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
	 * Match command 'ipv6 address'. <br/>
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
					UntilEOI()
				);
	}


	/**
	 * Match command nameif. <br/>
	 * TODO: reference
	 * @return a {@link Rule}
	 */
	public Rule IfName() {
		return Sequence(
				String("nameif"),
				WhiteSpaces(),
				UntilEOI(),
				setName(match()),
				setRuleName("nameif")
			);
	}

	/**
	 * Parsing of route, acl ...
	 * @return a {@link Rule}
	 */
	@SuppressSubnodes
	public Rule Parse() {
		return
			Sequence(
				clear(),
				FirstOf(
					Name(),
					Route(),
					Ipv6Route(),
					ObjectGroupNetwork(),
					ObjectGroupService(),
					ObjectGroupProtocol(),
					ObjectGroupIcmp(),
					NetworkObject(),
					ProtocolObject(),
					ServiceObject(),
					PortObject(),
					GroupObject(),
					IcmpObject(),
					AccessGroup(),
					AccessListRemark(),
					AccessListAcl(),
					Description()
				)
			);
	}

	/**
	 * Match command name. <br/>
	 * reference Cisco: <br/>
	 * name ip_address name [description text]]
	 *
	 * @return a {@link Rule}
	 */
	public Rule Name() {
		 return Sequence(
					String("name"),
					WhiteSpaces(),
					StringAtom(),
					setIpAddress(match()),
					WhiteSpaces(),
					StringAtom(),
					setName(match()),
					setRuleName("name"),
					UntilEOI()
				);
	}

	/**
	 * Match command 'route'. <br/>
	 * reference Cisco: <br/>
	 *	route interface_name ip_address netmask gateway_ip <br/>
	 *		[[metric] [track number] | tunneled] <br/>
	 * @return a {@link Rule}
	 */
	public Rule Route() {
		return Sequence(
					String("route"),
					WhiteSpaces(),
					StringAtom(),
					setInterface(match()),
					WhiteSpaces(),
					StringAtom(),
					setIpAddress(match()),
					WhiteSpaces(),
					StringAtom(),
					setIpNetmask(match()),
					WhiteSpaces(),
					StringAtom(),
					setNexthop(match()),
					setRuleName("route"),
					UntilEOI()
			);
	}

	/**
	 * Match command 'ipv6 route'.<br/>
	 *
	 * reference Cisco:<br/>
	 * ipv6 route <br/>
	 *   if_name <br/>
	 *   ipv6-prefix/prefix-length <br/>
	 *   ipv6-address <br/>
	 *   [administrative-distance] <br/>
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
					setInterface(match()),
					WhiteSpaces(),
					StringAtom(),
					setIpAddress(match()),
					WhiteSpaces(),
					StringAtom(),
					setNexthop(match()),
					setRuleName("ipv6 route"),
					UntilEOI()
			);
	}

	/**
	 * Match command object-group network. <br/>
	 * reference Cisco: <br/>
	 * object-group network group_id
	 * @return a {@link Rule}
	 */
	public Rule ObjectGroupNetwork() {
		return Sequence(
					String("object-group"),
					WhiteSpaces(),
					String("network"),
					WhiteSpaces(),
					StringAtom(),
					setGroupId(match()),
					setRuleName("object-group network"),
					EOI
				);
	}

	/**
	 * Match command object-group protocol. <br/>
	 * reference Cisco: <br/>
	 * object-group protocol group_id
	 * @return a {@link Rule}
	 */
	public Rule ObjectGroupProtocol() {
		return Sequence(
					String("object-group"),
					WhiteSpaces(),
					String("protocol"),
					WhiteSpaces(),
					StringAtom(),
					setGroupId(match()),
					setRuleName("object-group protocol"),
					EOI
				);
	}

	/**
	 * Match command description. <br/>
	 * reference Cisco: <br/>
	 * description text
	 * @return a {@link Rule}
	 */
	public Rule Description() {
		return Sequence(
					String("description"),
					WhiteSpaces(),
					UntilEOI(),
					setName(match()),
					setRuleName("description")
				);
	}

	/**
	 * Match command object-group icmp-type. <br/>
	 * reference Cisco: <br/>
	 * object-group icmp-type group_id
	 * @return a {@link Rule}
	 */
	public Rule ObjectGroupIcmp() {
		return Sequence(
					String("object-group"),
					WhiteSpaces(),
					String("icmp-type"),
					WhiteSpaces(),
					StringAtom(),
					setGroupId(match()),
					setRuleName("object-group icmp-type"),
					EOI
				);
	}

	/**
	 * Match command network-object.<br/>
	 * reference Cisco: <br/>
	 * network-object host IP <br/>
	 * network-object IP NETMASK <br/>
	 * network-object IP (ipv6)
	 * @return a {@link Rule}
	 */
	public Rule NetworkObject() {
		return Sequence(
					String("network-object"),
					WhiteSpaces(),
					FirstOf(
						/*
						 * host
						 */
						Sequence(
							String("host"),
							WhiteSpaces(),
							StringAtom(),
							setIpAddress(match()),
							EOI
						),
						/*
						 * ip [netmask]
						 */
						Sequence(
							StringAtom(),
							setIpAddress(match()),
							Optional(
								Sequence(
									WhiteSpaces(),
									StringAtom(),
									setIpNetmask(match())
								)
							),
							EOI
						)
					),
					setRuleName("network-object")
				);
	}

	/**
	 * Match command protocol-object.<br/>
	 * reference Cisco: <br/>
	 * protocol-object protocol
	 * @return a {@link Rule}
	 */
	public Rule ProtocolObject() {
		return Sequence(
					String("protocol-object"),
					WhiteSpaces(),
					StringAtom(),
					setProtocol(match()),
					setRuleName("protocol-object"),
					EOI
				);
	}

	/**
	 * Match command object-group service.
	 * reference Ciso: <br/>
	 *	object-group service obj_grp_id {tcp | udp | tcp-udp}
	 *
	 * note: if the protocol is ommited,
	 *		it describes an enhanced service group
	 * @return a {@link Rule}
	 */
	public Rule ObjectGroupService() {
		return Sequence(
					String("object-group"),
					WhiteSpaces(),
					String("service"),
					WhiteSpaces(),
					StringAtom(),
					setGroupId(match()),
					Optional(
						Sequence(
							WhiteSpaces(),
							FirstOf(
									String("tcp-udp"),
									String("tcp"),
									String("udp")
							),
							setProtocol(match())
						)
					),
					setRuleName("object-group service"),
					EOI
				);
	}

	/**
	 * Match command group-object. <br/>
	 * reference Cisco: <br/>
	 * group-object group_id
	 * @return a {@link Rule}
	 */
	public Rule GroupObject() {
		return Sequence(
					String("group-object"),
					WhiteSpaces(),
					StringAtom(),
					setGroupId(match()),
					setRuleName("group-object"),
					EOI
				);
	}

	/**
	 * Match command service-object. <br/>
	 * reference (?): <br/>.
	 *		service-object protocol [port operator]
	 * @return a {@link Rule}
	 */
	public Rule ServiceObject() {
		 return Sequence(
					String("service-object"),
					WhiteSpaces(),
					StringAtom(),
					setProtocol(match()),
					Optional(
						Sequence(
							WhiteSpaces(),
							PortOperatorOrRange()
						)
					),
					setRuleName("service-object"),
					EOI
			);
	}

	/**
	 * Match command port-object. <br/>
	 * reference Cisco: <br/>
	 * port-object eq port <br/>
	 * port-object range port port <br/>
	 * port-object lt port <br/>
	 * port-object gt port <br/>
	 * port-object neq port <br>
	 * @return a {@link Rule}
	 */
	public Rule PortObject() {
		return	Sequence(
					String("port-object"),
					WhiteSpaces(),
					PortOperatorOrRange(),
					setRuleName("port-object"),
					EOI
				);
	 }

	/**
	 * Match command icmp-object
	 * reference Cisco:
	 * 	icmp-object icmp_type
	 * @return a {@link Rule}
	 */
	public Rule IcmpObject() {
		return Sequence(
					String("icmp-object"),
					WhiteSpaces(),
					StringAtom(),
					setProtocol(match()),
					setRuleName("icmp-object"),
					EOI
				);
	}

	/**
	 * Match command access-group. <br/>
	 * reference Cisco: <br/>
	 * access-group access-list in interface interface_name [per-user-override]
	 * access-group id in|out interface inter <br/>
	 * @return a {@link Rule}
	 */
	public Rule AccessGroup() {
		return	Sequence(
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
					WhiteSpaces(),
					String("interface"),
					WhiteSpaces(),
					StringAtom(),
					setInterface(match()),
					setRuleName("access-group"),
					UntilEOI()
				);
	}

	/**
	 * Match port operator | port range
	 */
	public Rule PortOperatorOrRange() {
		return FirstOf(
					PortOperator(),
					PortOperatorRange()
		);
	}

	/**
	 * Match port operator 'eq', 'neq', 'lt', 'gt'
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
	 * Match port operator 'range'
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

	/**
	 * Match access-list containing 'remark'
	 */
	public Rule AccessListRemark() {
		return Sequence(
					newAclTemplate(),
					String("access-list"),
					WhiteSpaces(),
					StringAtom(),
					_acl.setAccessListId(match()),
					WhiteSpaces(),
					String("remark"),
					SkipSpaces(),
					UntilEOI(),
					_acl.setRemark(match()),
					setRuleName("access-list remark")
				);
	}

	public Rule ProtocolOrGroup() {
		return FirstOf(
					ObjectGroupTypeProtocol(),
					Protocol()
				);
	}

	public Rule Protocol() {
		return Sequence(
					StringAtomNotGroup(),
					setProtocol(match())
			);
	}

	/**
	 * Match object group of type protocol.
	 * @return {@link Rule}
	 */
	public Rule ObjectGroupTypeProtocol() {
		return Sequence(
					String("object-group"),
					WhiteSpaces(),
					StringAtom(),
					setGroupId(match()),
					checkObjectGroupType(_groupId, ObjectGroupType.PROTOCOL)
				);
	}

	/**
	 * Match object-group of type service.
	 * @return a {@link Rule}
	 */
	public Rule ObjectGroupTypeService() {
		return Sequence(
					String("object-group"),
					WhiteSpaces(),
					StringAtom(),
					setGroupId(match()),
					checkObjectGroupType(_groupId, ObjectGroupType.SERVICE)
				);
	}

	/**
	 * Match object-group of type enhanced service.
	 * @return a {@link Rule}
	 */
	public Rule ObjectGroupTypeEnhanced() {
		return Sequence(
					String("object-group"),
					WhiteSpaces(),
					StringAtom(),
					setGroupId(match()),
					checkObjectGroupType(_groupId, ObjectGroupType.ENHANCED)
				);
	}

	/**
	 * Match object-group of type network.
	 * @return a {@link Rule}
	 */
	public Rule ObjectGroupTypeNetwork() {
		return Sequence(
					String("object-group"),
					WhiteSpaces(),
					StringAtom(),
					setGroupId(match()),
					checkObjectGroupType(_groupId, ObjectGroupType.NETWORK)
				);
	}

	/**
	 * Match object-group of type icmp-type.
	 * @return a {@link Rule}
	 */
	public Rule ObjectGroupTypeIcmp() {
		return Sequence(
					String("object-group"),
					WhiteSpaces(),
					StringAtom(),
					setGroupId(match()),
					checkObjectGroupType(_groupId, ObjectGroupType.ICMP)
				);
	}
	
	/**
	 * Match ip mask | any | host ip
	 * @return a {@link Rule}
	 */
	public Rule AclIpAddress() {
		return FirstOf(
					Sequence(
						String("any"),
						setIpAddress("any")
					),
					Sequence(
						String("host"),
						WhiteSpaces(),
						StringAtom(),
						setIpAddress(match())
					),
					Sequence(
						StringAtomNotGroup(),
						setIpAddress(match()),
						WhiteSpaces(),
						StringAtom(),
						setIpNetmask(match())
					)
				);
	}

	/**
	 * Match ip mask | interface ifc_name | object-group network_obj_grp_id
	 * @return a {@link Rule}
	 */
	public Rule AclNetwork() {
		return FirstOf(
					Sequence(
						String("interface"),
						WhiteSpaces(),
						StringAtom(),
						setName(match())
					),
					ObjectGroupTypeNetwork(),
					AclIpAddress()
				);
	}

	/**
	 * Match [operator port | object-group service_obj_grp_id]
	 * @return a {@link Rule}
	 */
	 public Rule AclPort() {
		 return FirstOf(
					ObjectGroupTypeService(),
					PortOperator(),
					PortOperatorRange()
				);
	 }

	/**
	 * Match [icmp-type | object-group icmp-type]
	 * @return a {@link Rule}
	 */
	public Rule AclIcmp() {
		return FirstOf(
				Sequence(
					ObjectGroupTypeIcmp(),
					_acl.setIcmpGroup(_groupId),
					setGroupId(null)
				),
				Sequence(
					StringAtomNotGroup(),
					aclSetIcmp(match())
				)
			);
	}

	/**
	 * Match access-list for an extended acl. <br/>
	 * Reference cisco: <br/>
	 * access-list id [line line-number] [extended] {deny | permit} <br/>
	 *	{protocol | object-group protocol_obj_grp_id | object group enhanced_grp_id} <br/>
	 *	{src_ip mask | interface ifc_name | object-group network_obj_grp_id} <br/>
	 *	[operator port | object-group service_obj_grp_id] <br/>
	 *	{dest_ip mask | interface ifc_name | object-group network_obj_grp_id} <br/>
	 *	[operator port | object-group service_obj_grp_id | object-group icmp_type_obj_grp_id] <br/>
	 *	[log [[level] [interval secs] | disable | default]] <br/>
	 *	[inactive | time-range time_range_name] <br/>
	 *
	 * @return a {@link Rule}
	 */
	public Rule AccessListAcl() {
		return Sequence(
					newAclTemplate(),
					String("access-list"),
					WhiteSpaces(),
					StringAtom(),
					_acl.setAccessListId(match()),
					WhiteSpaces(),
					Optional(
						Sequence(
							String("line"),
							WhiteSpaces(),
							StringAtom(),
							WhiteSpaces()
						)
					),
					Optional(
						Sequence(
							String("extended"),
							WhiteSpaces()
						)
					),
					FirstOf(
						String("permit"),
						String("deny")
					),
					_acl.setAction(match()),
					WhiteSpaces(),
					setProtocol(null),
					setGroupId(null),
					FirstOf(
						Sequence(
							ProtocolOrGroup(),
							_acl.setProtocol(_protocol),
							_acl.setProtocolGroupId(_groupId),
							setGroupId(null),
							WhiteSpaces()
						),
						Sequence(
							ObjectGroupTypeEnhanced(),
							_acl.setDstEnhancedServiceGroup(_groupId),
							setGroupId(null),
							WhiteSpaces()
						)
					),
					// source
					AclNetwork(),
					_acl.setSrcIfName(_name),
					_acl.setSrcIp(_ipAddress),
					_acl.setSrcIpMask(_ipNetmask),
					_acl.setSrcNetworkGroup(_groupId),
					setName(null),
					setIpAddress(null),
					setIpNetmask(null),
					setGroupId(null),
					WhiteSpaces(),
					Optional(
						Sequence(
							AclPort(),
							_acl.setSrcPortOperator(_portOperator),
							_acl.setSrcFirstPort(_firstPort),
							_acl.setSrcLastPort(_lastPort),
							_acl.setSrcServiceGroup(_groupId),
							setPortOperator(null),
							setFirstPort(null),
							setLastPort(null),
							setGroupId(null),
							WhiteSpaces()
						)
					),
					// destination
					AclNetwork(),
					_acl.setDstIfName(_name),
					_acl.setDstIp(_ipAddress),
					_acl.setDstIpMask(_ipNetmask),
					_acl.setDstNetworkGroup(_groupId),
					setName(null),
					setIpAddress(null),
					setIpNetmask(null),
					setGroupId(null),
					Optional(
						Sequence(
							WhiteSpaces(),
							Optional(
								FirstOf(
									Sequence(
										AclPort(),
										_acl.setDstPortOperator(_portOperator),
										_acl.setDstFirstPort(_firstPort),
										_acl.setDstLastPort(_lastPort),
										_acl.setDstServiceGroup(_groupId),
										setPortOperator(null),
										setFirstPort(null),
										setLastPort(null),
										setGroupId(null)
									),
									AclIcmp()
								)
							)
						)
					),
					UntilInactive(),
					/*
					 * look for "inactive keyword"
					 */
					Optional(
						Sequence(
							Test(String("inactive")),
							_acl.setInactive(true)
						)
					),
					UntilEOI(),
					setRuleName("access-list acl")
				);
	}

	/**
	 * match anything until "inactive".
	 * @return a {@link Rule}
	 */
	public Rule UntilInactive() {
		return ZeroOrMore(
					Sequence(
						TestNot(String("inactive")),
						ANY
					)
				);
	}

	/**
	 * Matches anything until "shutdown"
	 * @return a {@link Rule}
	 */
	public Rule UntilShutdown() {
		return ZeroOrMore(
					Sequence(
						TestNot(String("shutdown")),
						ANY
					)
				);
	}

	/**
	 * Match a StringAtom and not "object-group"
	 * @return {@link Rule}
	 */
	 public Rule StringAtomNotGroup() {
		 return Sequence(
					TestNot(String("object-group ")),
					StringAtom()
				);
	 }
}

