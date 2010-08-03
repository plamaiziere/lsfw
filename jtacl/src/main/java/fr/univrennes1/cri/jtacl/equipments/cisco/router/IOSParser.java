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

package fr.univrennes1.cri.jtacl.equipments.cisco.router;

import fr.univrennes1.cri.jtacl.core.exceptions.JtaclConfigurationException;
import fr.univrennes1.cri.jtacl.lib.ip.IPIcmp4;
import fr.univrennes1.cri.jtacl.lib.ip.IPIcmpEnt;
import fr.univrennes1.cri.jtacl.lib.ip.IPversion;
import fr.univrennes1.cri.jtacl.lib.misc.CommonRules;
import org.parboiled.Action;
import org.parboiled.Context;
import org.parboiled.Rule;
import org.parboiled.annotations.SuppressSubnodes;

/**
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class IOSParser extends CommonRules<Object> {

	private String _name;
	private String _direction;
	private String _ipAddress;
	private String _ipNetmask;
	private String 	_nexthop;
	private String _description;
	private String _portOperator;
	private String _firstPort;
	private String _lastPort;
	private String _subType;
	private String _ruleName;
	private AclTemplate _acl;
	private AceTemplate _ace;
	private AccessList _aclContext;

	public String getDirection() {
		return _direction;
	}

	public String getName() {
		return _name;
	}

	public String getDescription() {
		return _description;
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

	public String getRuleName() {
		return _ruleName;
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
	
	/**
	 * Resets the resulting values of the parsing to null.
	 */
	public void clear() {
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
				String("interface"),
				WhiteSpaces(),
				UntilEOI(),
				new Action() {
					public boolean run(Context context) {
						_name = context.getPrevText();
						_ruleName = "interface";
						return true;
					}
				}
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
		return FirstOf(
					IfDescription(),
					IfIpv6Address(),
					IfIpAddress(),
					IfIpAccessGroup(),
					IfShutdown()
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
				new Action() {
					public boolean run(Context context) {
						_description = context.getPrevText();
						_ruleName = "description";
						return true;
					}
				}
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
				new Action() {
					public boolean run(Context context) {
						_ruleName = "shutdown";
						return true;
					}
				}
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
					new Action() {
						public boolean run(Context context) {
							_ipAddress = context.getPrevText();
							_ruleName = "ipv6 address";
							return true;
						}
					},
					Eoi()
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
							_ruleName = "ip access-group";
							return true;
						}
					},
					UntilEOI()
				);
	}

	/**
	 * Parsing of route, acl ...
	 * @return a {@link Rule}
	 */
	@SuppressSubnodes
	public Rule Parse() {
		return FirstOf(
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
						new Action() {
							public boolean run(Context context) {
								return _aclContext != null &&
										_aclContext.getAclType() == AclType.IPSTD;
							}
						},
						AceStandard()
					),
					Sequence(
						new Action() {
							public boolean run(Context context) {
								return _aclContext != null &&
										_aclContext.getAclType() == AclType.IPEXT;
							}
						},
						AceExtended()
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
			new Action() {
				public boolean run(Context context) {
					_ruleName = "Ignore";
					return true;
				}
			}
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
							_ruleName = "ip route";
							return true;
						}
					},
					Eoi()
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
					Eoi()
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
				new Action() {
					public boolean run(Context context) {
						String type = context.getPrevText();
						_acl = new AclTemplate();
						if (type.equals("standard"))
							_acl.setAclType(AclType.IPSTD);
						else
							_acl.setAclType(AclType.IPEXT);
						return true;
					}
				},
				WhiteSpaces(),
				StringAtom(),
				new Action() {
					public boolean run(Context context) {
						_acl.setName(context.getPrevText());
						_acl.setIpVersion(IPversion.IPV4);
						_ruleName = "ip access-list named";
						return true;
					}
				},
				Eoi()
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
				WhiteSpaces(),
				StringAtom(),
				new Action() {
					public boolean run(Context context) {
						_acl = new AclTemplate();
						_acl.setName(context.getPrevText());
						_acl.setIpVersion(IPversion.IPV6);
						_acl.setAclType(AclType.IPEXT);
						_ruleName = "ipv6 access-list named";
						return true;
					}
				},
				Eoi()
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
				new Action() {
					public boolean run(Context context) {
						int number = Integer.valueOf(context.getPrevText());
						_acl = new AclTemplate();
						_acl.setNumber(number);
						_acl.setIpVersion(IPversion.IPV4);
						_acl.setAclType(AclType.getType(number));
						return true;
					}
				},
				WhiteSpaces(),
				/*
				 * Test the acl type to get the rule.
				 */
				FirstOf(
					/* standard */
					Sequence(
						new Action() {
							public boolean run(Context context) {
								AclType type = _acl.getAclType();
								return type == AclType.IPSTD;
							}
						},
						AceStandard()
					),
					Sequence(
						new Action() {
							public boolean run(Context context) {
								AclType type = _acl.getAclType();
								return type == AclType.IPEXT;
							}
						},
						AceExtended()
					)
				),
				new Action() {
					public boolean run(Context context) {
						_ruleName = "access-list";
						return true;
					}
				}
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
				new Action() {
					public boolean run(Context context) {
						_ace = new AceTemplate();
						return true;
					}
				},
				FirstOf(
					String("permit"),
					String("deny")
				),
				new Action() {
					public boolean run(Context context) {
						_ace.setAction(context.getPrevText());
						return true;
					}
				},
				WhiteSpaces(),
				FirstOf(
					String("any"),
					Sequence(
						String("host"),
						WhiteSpaces(),
						StringAtom(),
						new Action() {
							public boolean run(Context context) {
								_ace.setSrcIp(context.getPrevText());
								return true;
							}
						}
					),
					Sequence(
						StringAtom(),
						new Action() {
							public boolean run(Context context) {
								_ace.setSrcIp(context.getPrevText());
								return true;
							}
						},
						Optional(
							Sequence(
								WhiteSpaces(),
								TestNot(String("log")),
								StringAtom(),
								new Action() {
									public boolean run(Context context) {
										_ace.setSrcIpMask(context.getPrevText());
										return true;
									}
								}
							)
						)
					)
				),
				new Action() {
					public boolean run(Context context) {
						_ruleName = "ace standard";
						return true;
					}
				},
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
				new Action() {
					public boolean run(Context context) {
						_ace.setSrcIp(_ipAddress);
						_ace.setSrcIpMask(_ipNetmask);
						_ipAddress = null;
						_ipNetmask = null;
						return true;
					}
				},
				WhiteSpaces(),
				Optional(
					Sequence(
						PortOperatorOrRange(),
						new Action() {
							public boolean run(Context context) {
								_ace.setSrcPortOperator(_portOperator);
								_ace.setSrcFirstPort(_firstPort);
								_ace.setSrcLastPort(_lastPort);
								_portOperator = null;
								_firstPort = null;
								_lastPort = null;
								return true;
							}
						},
						WhiteSpaces()
					)
				),
				// destination
				AclIpAddress(),
				new Action() {
					public boolean run(Context context) {
						_ace.setDstIp(_ipAddress);
						_ace.setDstIpMask(_ipNetmask);
						_ipAddress = null;
						_ipNetmask = null;
						return true;
					}
				},
				WhiteSpacesOrEoi(),
				Optional(
					Sequence(
						PortOperatorOrRange(),
						new Action() {
							public boolean run(Context context) {
								_ace.setDstPortOperator(_portOperator);
								_ace.setDstFirstPort(_firstPort);
								_ace.setDstLastPort(_lastPort);
								_portOperator = null;
								_firstPort = null;
								_lastPort = null;
								return true;
							}
						}
					)
				)
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
				new Action() {
					public boolean run(Context context) {
						_ace.setSrcIp(_ipAddress);
						_ace.setSrcIpMask(_ipNetmask);
						_ipAddress = null;
						_ipNetmask = null;
						return true;
					}
				},
				WhiteSpaces(),
				// destination
				AclIpAddress(),
				new Action() {
					public boolean run(Context context) {
						_ace.setDstIp(_ipAddress);
						_ace.setDstIpMask(_ipNetmask);
						_ipAddress = null;
						_ipNetmask = null;
						return true;
					}
				}
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
				new Action() {
					public boolean run(Context context) {
						_ace.setSrcIp(_ipAddress);
						_ace.setSrcIpMask(_ipNetmask);
						_ipAddress = null;
						_ipNetmask = null;
						return true;
					}
				},
				WhiteSpaces(),
				// destination
				AclIpAddress(),
				new Action() {
					public boolean run(Context context) {
						_ace.setDstIp(_ipAddress);
						_ace.setDstIpMask(_ipNetmask);
						_ipAddress = null;
						_ipNetmask = null;
						return true;
					}
				},
				Optional(
					Sequence(
						WhiteSpaces(),
						AclSubType(),
						new Action() {
							public boolean run(Context context) {
								String icmp = context.getPrevText();
								IPIcmpEnt icmpEnt = 
									IPIcmp4.getInstance().icmpLookup(icmp);
								if (icmpEnt != null) {
									_ace.setSubType(icmp);
									_ace.setCode(icmpEnt.getCode());
									return true;
								}
								throw new JtaclConfigurationException(
										"unknown icmp type or message: " + icmp);
							}
						}
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
				new Action() {
					public boolean run(Context context) {
						_ace = new AceTemplate();
						return true;
					}
				},
				// permit | deny
				FirstOf(
					String("permit"),
					String("deny")
				),
				new Action() {
					public boolean run(Context context) {
						_ace.setAction(context.getPrevText());
						return true;
					}
				},
				WhiteSpaces(),
				FirstOf(
					// tcp | udp
					Sequence(
						FirstOf(
							String("tcp"),
							String("udp")
						),
						new Action() {
							public boolean run(Context context) {
								_ace.setProtocol(context.getPrevText());
								return true;
							}
						},
						WhiteSpaces(),
						AceExtendedTcpUdp()
					),
					// icmp
					Sequence(
						String("icmp"),
						new Action() {
							public boolean run(Context context) {
								_ace.setProtocol(context.getPrevText());
								return true;
							}
						},
						WhiteSpaces(),
						AceExtendedIcmp()
					),
					// other protocol
					Sequence(
						StringAtom(),
						new Action() {
							public boolean run(Context context) {
								_ace.setProtocol(context.getPrevText());
								return true;
							}
						},
						WhiteSpaces(),
						AceExtendedOtherProtocol()
					)
				),
				new Action() {
					public boolean run(Context context) {
						_ruleName = "ace extended";
						return true;
					}
				},
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
						new Action() {
							public boolean run(Context context) {
								_ipAddress = "any";
								return true;
							}
						}
					),
					// host ip
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
					// ip [netmask] (no netmask if ipv6)
					Sequence(
						StringAtom(),
						new Action() {
							public boolean run(Context context) {
								_ipAddress = context.getPrevText();
								return true;
							}
						},
						// netmask | nothing (ipv6)
						FirstOf(
							new Action() {
								public boolean run(Context context) {
									// no mask if ipv6 address
									return _ipAddress.contains(":");
								}
							},
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
						)
					)
				);
	}

	public Rule AclSubType() {
		return Sequence(
			TestNot(AclKeyword()),
			StringAtom(),
			new Action() {
				public boolean run(Context context) {
					_subType = context.getPrevText();
					return true;
				}
			}
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
	 * Matches port operator 'range'
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


}
