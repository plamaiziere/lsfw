/*
 * Copyright (c) 2010, Université de Rennes 1
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

package fr.univrennes1.cri.jtacl.equipments.cisco.pix;

import fr.univrennes1.cri.jtacl.lib.ip.IPIcmp4;
import fr.univrennes1.cri.jtacl.lib.misc.CommonRules;
import org.parboiled.Action;
import org.parboiled.Context;
import org.parboiled.Rule;
import org.parboiled.annotations.SuppressSubnodes;

/**
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PixParser extends CommonRules<Object> {

	private String _name;
	private String _ipAddress;
	private String _ipNetmask;
	private String _nexthop;
	private String _interface;
	private String _shutdown;
	private String _ruleName;
	private String _groupId;
	private String _protocol;
	private String _portOperator;
	private String _firstPort;
	private String _lastPort;
	private String _direction;

	private AclTemplate _acl;
	private GroupTypeSearchable _groupTypeSearch;

	public String getName() {
		return _name;
	}

	public String getIpAddress() {
		return _ipAddress;
	}

	public String getIpNetmask() {
		return _ipNetmask;
	}

	public String getNexthop() {
		return _nexthop;
	}

	public String getInterface() {
		return _interface;
	}

	public String getRuleName() {
		return _ruleName;
	}

	public String getGroupId() {
		return _groupId;
	}

	public String getProtocol() {
		return _protocol;
	}

	public String getPortOperator() {
		return _portOperator;
	}

	public String getFirstPort() {
		return _firstPort;
	}

	public String getLastPort() {
		return _lastPort;
	}

	public String getDirection() {
		return _direction;
	}

	public AclTemplate getAcl() {
		return _acl;
	}

	public String getShutdown() {
		return _shutdown;
	}

	public void setGroupTypeSearch(GroupTypeSearchable groupType) {
		_groupTypeSearch = groupType;
	}

	/**
	 * Resets the resulting values of the parsing to null.
	 */
	public void clear() {
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
			"access-list"
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
				String("interface"),
				WhiteSpaces(),
				UntilShutdown(),
				new Action() {
					public boolean run(Context context) {
						_name = context.getPrevText().trim();
						return true;
					}
				},
				Optional(
					Sequence(
						String("shutdown"),
						new Action() {
							public boolean run(Context context) {
								_shutdown = "shutdown";
								return true;
							}
						}
					)
				),
				UntilEOI(),
				new Action() {
					public boolean run(Context context) {
						_ruleName = "interface";
						return true;
					}
				}
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
		return FirstOf(
					IfName(),
					IfIpv6Address(),
					IfIpAddress()
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
					new Action() {
						public boolean run(Context context) {
							_ipAddress = context.getPrevText();
							return true;
						}
					},
					WhiteSpaces(),
					StringAtom(),
					new Action() {
						public boolean run(Context context) {
							_ipNetmask = context.getPrevText();
							_ruleName = "ip address";
							return true;
						}
					},
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
					new Action() {
						public boolean run(Context context) {
							_ipAddress = context.getPrevText();
							_ruleName = "ipv6 address";
							return true;
						}
					},
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
				new Action() {
					public boolean run(Context context) {
						_name = context.getPrevText();
						_ruleName = "nameif";
						return true;
					}
				}
			);
	}

	/**
	 * Parsing of route, acl ...
	 * @return a {@link Rule}
	 */
	@SuppressSubnodes
	public Rule Parse() {
		return FirstOf(
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
				AccessListAcl()
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
					new Action() {
						public boolean run(Context context) {
							_ipAddress = context.getPrevText();
							return true;
						}
					},
					WhiteSpaces(),
					StringAtom(),
					new Action() {
						public boolean run(Context context) {
							_name = context.getPrevText();
							_ruleName = "name";
							return true;
						}
					},
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
					new Action() {
						public boolean run(Context context) {
							_interface = context.getPrevText();
							return true;
						}
					},
					WhiteSpaces(),
					StringAtom(),
					new Action() {
						public boolean run(Context context) {
							_ipAddress = context.getPrevText();
							return true;
						}
					},
					WhiteSpaces(),
					StringAtom(),
					new Action() {
						public boolean run(Context context) {
							_ipNetmask = context.getPrevText();
							return true;
						}
					},
					WhiteSpaces(),
					StringAtom(),
					new Action() {
						public boolean run(Context context) {
							_nexthop = context.getPrevText();
							_ruleName = "route";
							return true;
						}
					},
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
					new Action() {
						public boolean run(Context context) {
							_interface = context.getPrevText();
							return true;
						}
					},
					WhiteSpaces(),
					StringAtom(),
					new Action() {
						public boolean run(Context context) {
							_ipAddress = context.getPrevText();
							return true;
						}
					},
					WhiteSpaces(),
					StringAtom(),
					new Action() {
						public boolean run(Context context) {
							_nexthop = context.getPrevText();
							_ruleName = "ipv6 route";
							return true;
						}
					},
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
					new Action() {
						public boolean run(Context context) {
							_groupId = context.getPrevText();
							_ruleName = "object-group network";
							return true;
						}
					},
					Eoi()
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
					new Action() {
						public boolean run(Context context) {
							_groupId = context.getPrevText();
							_ruleName = "object-group protocol";
							return true;
						}
					},
					Eoi()
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
					new Action() {
						public boolean run(Context context) {
							_groupId = context.getPrevText();
							_ruleName = "object-group icmp-type";
							return true;
						}
					},
					Eoi()
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
							new Action() {
								public boolean run(Context context) {
									_ipAddress = context.getPrevText();
									return true;
								}
							},
							Eoi()
						),
						/*
						 * ip [netmask]
						 */
						Sequence(
							StringAtom(),
							new Action() {
								public boolean run(Context context) {
									_ipAddress = context.getPrevText();
									return true;
								}
							},
							Optional(
								Sequence(
									WhiteSpaces(),
									StringAtom(),
									new Action() {
										public boolean run(Context context) {
											_ipNetmask = context.getPrevText();
											return true;
										}
									}
								)
							),
							Eoi()
						)
					),
					new Action() {
						public boolean run(Context context) {
							_ruleName = "network-object";
							return true;
						}
					}
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
					new Action() {
						public boolean run(Context context) {
							_protocol = context.getPrevText();
							_ruleName = "protocol-object";
							return true;
						}
					},
					Eoi()
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
					new Action() {
						public boolean run(Context context) {
							_groupId = context.getPrevText();
							return true;
						}
					},
					Optional(
						Sequence(
							WhiteSpaces(),
							FirstOf(
									String("tcp-udp"),
									String("tcp"),
									String("udp")
							),
							new Action() {
								public boolean run(Context context) {
									_protocol = context.getPrevText();
									return true;
								}
							}
						)
					),
					new Action() {
						public boolean run(Context context) {
							_ruleName = "object-group service";
							return true;
						}
					},
					Eoi()
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
					new Action() {
						public boolean run(Context context) {
							_groupId = context.getPrevText();
							_ruleName = "group-object";
							return true;
						}
					},
					Eoi()
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
					new Action() {
						public boolean run(Context context) {
							_protocol = context.getPrevText();
							return true;
						}
					},
					Optional(
						Sequence(
							WhiteSpaces(),
							PortOperatorOrRange()
						)
					),
					new Action() {
						public boolean run(Context context) {
							_ruleName = "service-object";
							return true;
						}
					},
					Eoi()
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
					new Action() {
						public boolean run(Context context) {
							_ruleName = "port-object";
							return true;
						}
					},
					Eoi()
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
					new Action() {
						public boolean run(Context context) {
							_protocol = context.getPrevText();
							_ruleName = "icmp-object";
							return true;
						}
					},
					Eoi()
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
					new Action() {
						public boolean run(Context context) {
							_name = context.getPrevText();
							return true;
						}
					},
					WhiteSpaces(),
					FirstOf(
						String("in"),
						String("out")
					),
					new Action() {
						public boolean run(Context context) {
							_direction  = context.getPrevText();
							return true;
						}
					},
					WhiteSpaces(),
					String("interface"),
					WhiteSpaces(),
					StringAtom(),
					new Action() {
						public boolean run(Context context) {
							_interface = context.getPrevText();
							_ruleName = "access-group";
							return true;
						}
					},
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
					new Action() {
						public boolean run(Context context) {
							_portOperator = context.getPrevText();
							return true;
						}
					},
					WhiteSpaces(),
					StringAtom(),
					new Action() {
						public boolean run(Context context) {
							_firstPort = context.getPrevText();
							return true;
						}
					}
			);
	}

	/**
	 * Match port operator 'range'
	 * @return A {@link Rule}
	 */
	public Rule PortOperatorRange() {
		return Sequence(
					String("range"),
					new Action() {
						public boolean run(Context context) {
							_portOperator = context.getPrevText();
							return true;
						}
					},
					WhiteSpaces(),
					StringAtom(),
					new Action() {
						public boolean run(Context context) {
							_firstPort = context.getPrevText();
							return true;
						}
					},
					WhiteSpaces(),
					StringAtom(),
					new Action() {
						public boolean run(Context context) {
							_lastPort = context.getPrevText();
							return true;
						}
					}
			);
		}

	/**
	 * Match access-list containing 'remark'
	 */
	public Rule AccessListRemark() {
		return Sequence(
					String("access-list"),
					WhiteSpaces(),
					ZeroOrMore(
						Sequence(
							TestNot(String("remark")),
							Any()
						)
					),
					String("remark"),
					new Action() {
						public boolean run(Context context) {
							_ruleName = "access-list remark";
							return true;
						}
					}
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
					new Action() {
						public boolean run(Context context) {
							_protocol = context.getPrevText();
							return true;
					
						}
					}
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
					new Action() {
						public boolean run(Context context) {
							String id = context.getPrevText();
							ObjectGroupType type =
									_groupTypeSearch.getGroupType(id);
							if (type != null && type == ObjectGroupType.PROTOCOL) {
								_groupId = id;
								return true;
							}
							return false;
						}
					}
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
					new Action() {
						public boolean run(Context context) {
							String id = context.getPrevText();
							ObjectGroupType type =
									_groupTypeSearch.getGroupType(id);
							if (type != null && type == ObjectGroupType.SERVICE) {
								_groupId = id;
								return true;
							}
							return false;
						}
					}
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
					new Action() {
						public boolean run(Context context) {
							String id = context.getPrevText();
							ObjectGroupType type =
									_groupTypeSearch.getGroupType(id);
							if (type != null && type == ObjectGroupType.ENHANCED) {
								_groupId = id;
								return true;
							}
							return false;
						}
					}
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
					new Action() {
						public boolean run(Context context) {
							String id = context.getPrevText();
							ObjectGroupType type =
									_groupTypeSearch.getGroupType(id);
							if (type != null && type == ObjectGroupType.NETWORK) {
								_groupId = id;
								return true;
							}
							return false;
						}
					}
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
					new Action() {
						public boolean run(Context context) {
							String id = context.getPrevText();
							ObjectGroupType type =
									_groupTypeSearch.getGroupType(id);
							if (type != null && type == ObjectGroupType.ICMP) {
								_groupId = id;
								return true;
							}
							return false;
						}
					}
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
						new Action() {
							public boolean run(Context context) {
								_ipAddress = "any";
								return true;
							}
						}
					),
					Sequence(
						String("host"),
						WhiteSpaces(),
						StringAtom(),
						new Action() {
							public boolean run(Context context) {
								_ipAddress = context.getPrevText();
								return true;
							}
						}
					),
					Sequence(
						StringAtomNotGroup(),
						new Action() {
							public boolean run(Context context) {
								_ipAddress = context.getPrevText();
								return true;
							}
						},
						WhiteSpaces(),
						StringAtom(),
						new Action() {
							public boolean run(Context context) {
								_ipNetmask = context.getPrevText();
								return true;
							}
						}
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
						new Action() {
							public boolean run(Context context) {
								_name = context.getPrevText();
								return true;
							}
						}
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
					new Action() {
						public boolean run(Context context) {
							_acl.setIcmpGroup(_groupId);
							_groupId = null;
							return true;
						}
					}
				),
				Sequence(
					StringAtomNotGroup(),
					new Action() {
						public boolean run(Context context) {
							String icmp = context.getPrevText();
							if (IPIcmp4.getInstance().icmpLookup(icmp) != null) {
								_acl.setIcmp(icmp);
								return true;
							}
							return false;
						}
					}
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
					new Action() {
						public boolean run(Context context) {
							_acl = new AclTemplate();
							return true;
						}
					},
					String("access-list"),
					WhiteSpaces(),
					StringAtom(),
					new Action() {
						public boolean run(Context context) {
							_acl.setAccessListId(context.getPrevText());
							return true;
						}
					},
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
					new Action() {
						public boolean run(Context context) {
							_acl.setAction(context.getPrevText());
							return true;
						}
					},
					WhiteSpaces(),
					new Action() {
						public boolean run(Context context) {
							_protocol = null;
							_groupId = null;
							return true;
						}
					},
					FirstOf(
						Sequence(
							ProtocolOrGroup(),
							new Action() {
								public boolean run(Context context) {
									_acl.setProtocol(_protocol);
									_acl.setProtocolGroupId(_groupId);
									_groupId = null;
									return true;
								}
							},
							WhiteSpaces()
						),
						Sequence(
							ObjectGroupTypeEnhanced(),
							new Action() {
								public boolean run(Context context) {
									_acl.setDstEnhancedServiceGroup(_groupId);
									_groupId = null;
									return true;
								}
							},
							WhiteSpaces()
						)
					),
					// source
					AclNetwork(),
					new Action() {
						public boolean run(Context context) {
							_acl.setSrcIfName(_name);
							_acl.setSrcIp(_ipAddress);
							_acl.setSrcIpMask(_ipNetmask);
							_acl.setSrcNetworkGroup(_groupId);
							_name = null;
							_ipAddress = null;
							_ipNetmask = null;
							_groupId = null;
							return true;
						}
					},
					WhiteSpaces(),
					Optional(
						Sequence(
							AclPort(),
							new Action() {
								public boolean run(Context context) {
									_acl.setSrcPortOperator(_portOperator);
									_acl.setSrcFirstPort(_firstPort);
									_acl.setSrcLastPort(_lastPort);
									_acl.setSrcServiceGroup(_groupId);
									_portOperator = null;
									_firstPort = null;
									_lastPort = null;
									_groupId = null;
									return true;
								}
							},
							WhiteSpaces()
						)
					),
					// destination
					AclNetwork(),
					new Action() {
						public boolean run(Context context) {
							_acl.setDstIfName(_name);
							_acl.setDstIp(_ipAddress);
							_acl.setDstIpMask(_ipNetmask);
							_acl.setDstNetworkGroup(_groupId);
							_name = null;
							_ipAddress = null;
							_ipNetmask = null;
							_groupId = null;
							return true;
						}
					},
					Optional(
						Sequence(
							WhiteSpaces(),
							Optional(
								FirstOf(
									Sequence(
										AclPort(),
										new Action() {
											public boolean run(Context context) {
												_acl.setDstPortOperator(_portOperator);
												_acl.setDstFirstPort(_firstPort);
												_acl.setDstLastPort(_lastPort);
												_acl.setDstServiceGroup(_groupId);
												_portOperator = null;
												_firstPort = null;
												_lastPort = null;
												_groupId = null;
												return true;
											}
										}
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
							new Action() {
								public boolean run(Context context) {
									_acl.setInactive(true);
									return true;
								}
							}
						)
					),
					UntilEOI(),
					new Action() {
						public boolean run(Context context) {
							_ruleName = "access-list acl";
							return true;
						}
					}

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
						Any()
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
						Any()
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

