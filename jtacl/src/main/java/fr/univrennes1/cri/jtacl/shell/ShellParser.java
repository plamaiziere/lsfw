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

package fr.univrennes1.cri.jtacl.shell;

import fr.univrennes1.cri.jtacl.lib.misc.CommonRules;
import fr.univrennes1.cri.jtacl.lib.misc.StringsList;
import org.parboiled.Action;
import org.parboiled.Context;
import org.parboiled.Rule;

/**
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class ShellParser extends CommonRules<Object> {

	protected String _command = "";
	protected String _srcAddress;
	protected String _destAddress;
	protected String _equipments;
	protected String _protoSpecification;
	protected String _protoSource;
	protected String _protoDest;

	protected String _setValueName;
	protected String _setValueValue;
	protected String _helpTopic;
	protected String _topologyOption;
	protected String _probeExpect;
	protected boolean _probe6flag;
	protected String _subCommand;
	protected StringsList _tcpFlags;

	public void clear() {
		_command = "";
		_srcAddress = null;
		_destAddress = null;
		_equipments = null;
		_protoSpecification = null;
		_protoSource = null;
		_protoDest = null;
		_setValueName = null;
		_setValueValue = null;
		_helpTopic = null;
		_topologyOption = null;
		_probeExpect = null;
		_subCommand = null;
		_tcpFlags = null;
	}

	public String getCommand() {
		return _command;
	}

	public String getDestAddress() {
		return _destAddress;
	}

	public String getSrcAddress() {
		return _srcAddress;
	}

	public String getEquipments() {
		return _equipments;
	}

	public String getProtoSpecification() {
		return _protoSpecification;
	}

	public String getProtoDest() {
		return _protoDest;
	}

	public String getProtoSource() {
		return _protoSource;
	}

	public String getSetValueName() {
		return _setValueName;
	}

	public String getSetValueValue() {
		return _setValueValue;
	}

	public String getHelpTopic() {
		return _helpTopic;
	}

	public String getTopologyOption() {
		return _topologyOption;
	}

	public String getProbeExpect() {
		return _probeExpect;
	}

	public String getSubCommand() {
		return _subCommand;
	}

	public StringsList getTcpFlags() {
		return _tcpFlags;
	}

	Rule CommandLine() {
		return FirstOf(
				CommandProbe(),
				CommandQuit(),
				CommandDefine(),
				CommandOption(),
				CommandTopology(),
				CommandRoute(),
				CommandHelp(),
				CommandEquipment(),
				CommandReload()
			);
	}

	Rule CommandQuit() {
		return Sequence(
				FirstOf(
					IgnoreCase("quit"),
					IgnoreCase("exit"),
					IgnoreCase("q"),
					IgnoreCase("e")
				),
				EOI,
				new Action() {
					public boolean run(Context context) {
						_command = "quit";
						return true;
					}
				}
		);
	}

	/*
	 * (topology | t) [connected | !connected] [atom]
	 *
	 */
	Rule CommandTopology() {
		return Sequence(
					FirstOf(
						IgnoreCase("topology"),
						IgnoreCase("t")
					),
					Optional(
						Sequence(
							WhiteSpaces(),
							FirstOf(
								IgnoreCase("connected"),
								IgnoreCase("!connected")
							),
							new Action() {
								public boolean run(Context context) {
									_topologyOption = context.getMatch();
									return true;
								}
							}
						)
					),
					Optional(
						Sequence(
							WhiteSpaces(),
							StringAtom(),
							new Action() {
								public boolean run(Context context) {
									_equipments = context.getMatch();
									return true;
								}
							}
						)
					),
					EOI,
					new Action() {
						public boolean run(Context context) {
							_command = "topology";
							return true;
						}
					}
			);
	}

	/*
	 * route [atom]
	 */
	Rule CommandRoute() {
		return Sequence(
					IgnoreCase("route"),
					Optional(
						Sequence(
							WhiteSpaces(),
							StringAtom(),
							new Action() {
								public boolean run(Context context) {
									_equipments = context.getMatch();
									return true;
								}
							}
						)
					),
					new Action() {
						public boolean run(Context context) {
							_command = "route";
							return true;
						}
					}
			);
	}

	/*
	 * (option | o) [identifier [= atom]]
	 */
	Rule CommandOption() {
		return Sequence(
				FirstOf(
					IgnoreCase("option"),
					IgnoreCase("o")
				),
				Optional(
					Sequence(
						WhiteSpaces(),
						Identifier(),
						new Action() {
							public boolean run(Context context) {
								_setValueName = context.getMatch();
								return true;
							}
						},
						Optional(
							Sequence(
								SkipSpaces(),
								IgnoreCase('='),
								SkipSpaces(),
								StringAtom(),
								new Action() {
									public boolean run(Context context) {
										_setValueValue = context.getMatch();
										return true;
									}
								}
							)
						)
					)
				),
				EOI,
				new Action() {
					public boolean run(Context context) {
						_command = "option";
						return true;
					}
				}
		);
	}

	/*
	 * (define | d) [identifier [ = string]]
	 *
	 */
	Rule CommandDefine() {
		return Sequence(
				FirstOf(
					IgnoreCase("define"),
					IgnoreCase("d")
				),
				Optional(
					Sequence(
						WhiteSpaces(),
						Identifier(),
						new Action() {
							public boolean run(Context context) {
								_setValueName = context.getMatch();
								return true;
							}
						},
						Optional(
							Sequence(
								SkipSpaces(),
								IgnoreCase('='),
								SkipSpaces(),
								UntilEOI(),
								new Action() {
									public boolean run(Context context) {
										_setValueValue = context.getMatch();
										return true;
									}
								}
							)
						)
					)
				),
				EOI,
				new Action() {
					public boolean run(Context context) {
						_command = "define";
						return true;
					}
				}
		);
	}

	/*
	 * help [atom]
	 */
	Rule CommandHelp() {
		return Sequence(
					IgnoreCase("help"),
					Optional(
						Sequence(
							WhiteSpaces(),
							StringAtom(),
							new Action() {
								public boolean run(Context context) {
									_helpTopic = context.getMatch();
									return true;
								}
							}
						)
					),
					EOI,
					new Action() {
						public boolean run(Context context) {
							_command = "help";
							return true;
						}
					}
				);
	}

	Rule Identifier() {
			return OneOrMore(
						Sequence(
							TestNot(WhiteSpaces()),
							TestNot(Special()),
							ANY
						)
					);
	}

	Rule Special() {
		return AnyOf("=,:");
	}

	/*
	 * (equipment | eq) atom string
	 */
	Rule CommandEquipment() {
		return Sequence(
			FirstOf(
				IgnoreCase("equipment"),
				IgnoreCase("eq")
			),
			WhiteSpaces(),
			StringAtom(),
			new Action() {
				public boolean run(Context context) {
					_equipments = context.getMatch();
					return true;
				}
			},
			SkipSpaces(),
			UntilEOI(),
			new Action() {
				public boolean run(Context context) {
					_subCommand = context.getMatch();
					_command = "equipment";
					return true;
				}
			}
		);
	}

	/*
	 * reload [atom]
	 */
	Rule CommandReload() {
		return Sequence(
			IgnoreCase("reload"),
			Optional(
				Sequence(
					WhiteSpaces(),
					StringAtom(),
					new Action() {
						public boolean run(Context context) {
							_equipments = context.getMatch();
							return true;
						}
					}
				)
			),
			EOI,
			new Action() {
				public boolean run(Context context) {
					_command = "reload";
					return true;
				}
			}
		);
	}

	/*
	 * (probe | p | probe6 | p6) [ProbeExpect] [OnEquipment]
	 *		SourceSpec DestSpec [ProtoSpec]
	 */
	Rule CommandProbe() {
		return Sequence(
				new Action() {
					public boolean run(Context context) {
						_probe6flag = false;
						return true;
					}
				},
				FirstOf(
					Sequence(
						IgnoreCase("probe6"),
						new Action() {
							public boolean run(Context context) {
								_probe6flag = true;
								return true;
							}
						}
					),
					Sequence(
						IgnoreCase("p6"),
						new Action() {
							public boolean run(Context context) {
								_probe6flag = true;
								return true;
							}
						}
					),
					IgnoreCase("probe"),
					IgnoreCase("p")
				),
				WhiteSpaces(),
				Optional(ProbeExpect()),
				Optional(OnEquipments()),
				SourceSpecification(),
				new Action() {
					public boolean run(Context context) {
						_srcAddress = context.getMatch();
						return true;
					}
				},
				WhiteSpaces(),
				DestinationSpecification(),
				new Action() {
					public boolean run(Context context) {
						_destAddress = context.getMatch();
						return true;
					}
				},
				Optional(
					Sequence(
						WhiteSpaces(),
						ProtoSpecification()
					)
				),
				EOI,
				new Action() {
					public boolean run(Context context) {
						if (_probe6flag)
							_command = "probe6";
						else
							_command ="probe";
						return true;
					}
				}
		);
	}

	/*
	 * expect atom
	 */
	Rule ProbeExpect() {
		return Sequence(
					IgnoreCase("expect"),
					WhiteSpaces(),
					StringAtom(),
					new Action() {
						public boolean run(Context context) {
							_probeExpect = context.getMatch();
							return true;
						}
					},
					WhiteSpaces()
			);
	}

	/*
	 * on atom
	 */
	Rule OnEquipments() {
		return Sequence(
			IgnoreCase("on"),
			WhiteSpaces(),
			StringAtom(),
			new Action() {
				public boolean run(Context context) {
					_equipments = context.getMatch();
					return true;
				}
			},
			WhiteSpaces()
		);
	}

	Rule SourceSpecification() {
		return StringAtom();
	}

	Rule DestinationSpecification() {
		return StringAtom();
	}

	/*
	 * ( (udp | tcp) [TcpUdpSpec] [TcpFlagsSpec] )
	 *   | (atom [Identifier])
	 */
	Rule ProtoSpecification() {
		return
			FirstOf(
				Sequence(
					FirstOf(
						String("udp"),
						String("tcp")
					),
					new Action() {
						public boolean run(Context context) {
							_protoSpecification = context.getMatch();
							return true;
						}
					},
					Optional(
						Sequence(
							WhiteSpaces(),
							TcpUdpSpecification()
						)
					),
					Optional(
						Sequence(
							WhiteSpaces(),
							TcpFlagsSpec()
						)
					)
				),
				Sequence(
					StringAtom(),
					new Action() {
						public boolean run(Context context) {
							_protoSpecification = context.getMatch();
							return true;
						}
					},
					Optional(
						Sequence(
							WhiteSpaces(),
							Identifier(),
							new Action() {
								public boolean run(Context context) {
									_protoSource = context.getMatch();
									return true;
								}
							}
						)
					)
				)
			);
	}

	/*
	 * ([Identifier]:[Identifier]) | Identifier
	 */
	Rule TcpUdpSpecification() {
		return
			FirstOf(
				Sequence(
					TestNot(IgnoreCase("flags")),
					Identifier(),
					new Action() {
						public boolean run(Context context) {
							_protoSource = context.getMatch();
							_protoDest = null;
							return true;
						}
					},
					String(":"),
					Optional(
						Sequence(
							Identifier(),
							new Action() {
								public boolean run(Context context) {
									_protoDest = context.getMatch();
									return true;
								}
							}
						)
					)
				),
				Sequence(
					TestNot(IgnoreCase("flags")),
					Identifier(),
					new Action() {
						public boolean run(Context context) {
							_protoSource = null;
							_protoDest = context.getMatch();
							return true;
						}
					}
				)
			);
	}

	/*
	 * flags TcpFlagsSpec+
	 */
	Rule TcpFlagsSpec() {
		return
			Sequence(
				IgnoreCase("flags"),
				WhiteSpaces(),
				new Action() {
					public boolean run(Context context) {
						_tcpFlags = new StringsList();
						return true;
					}
				},
				OneOrMore(
					Sequence(
						TcpFlagSpec(),
						SkipSpaces()
					)
				)
			);
	}

	/*
	 * atom
	 */
	Rule TcpFlagSpec() {
		return
			Sequence(
				StringAtom(),
				new Action() {
					public boolean run(Context context) {
						_tcpFlags.add(context.getMatch());
						return true;
					}
				}
			);
	}
}
