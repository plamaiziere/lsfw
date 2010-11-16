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

package fr.univrennes1.cri.jtacl.shell;

import fr.univrennes1.cri.jtacl.lib.misc.CommonRules;
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

	Rule CommandLine() {
		return FirstOf(
				CommandProbe(),
				CommandQuit(),
				CommandDefine(),
				CommandOption(),
				CommandTopology(),
				CommandRoute(),
				CommandHelp(),
				CommandEquipment()
			);
	}

	Rule CommandQuit() {
		return Sequence(
				FirstOf(
					StringIgnoreCase("quit"),
					StringIgnoreCase("exit"),
					StringIgnoreCase("q"),
					StringIgnoreCase("e")
				),
				Eoi(),
				new Action() {
					public boolean run(Context context) {
						_command = "quit";
						return true;
					}
				}
		);
	}

	Rule CommandTopology() {
		return Sequence(
					FirstOf(
						StringIgnoreCase("topology"),
						StringIgnoreCase("t")
					),
					Optional(
						Sequence(
							WhiteSpaces(),
							FirstOf(
								StringIgnoreCase("connected"),
								StringIgnoreCase("!connected")
							),
							new Action() {
								public boolean run(Context context) {
									_topologyOption = context.getPrevText();
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
									_equipments = context.getPrevText();
									return true;
								}
							}
						)
					),
					Eoi(),
					new Action() {
						public boolean run(Context context) {
							_command = "topology";
							return true;
						}
					}
			);
	}

	Rule CommandRoute() {
		return Sequence(
					StringIgnoreCase("route"),
					Optional(
						Sequence(
							WhiteSpaces(),
							StringAtom(),
							new Action() {
								public boolean run(Context context) {
									_equipments = context.getPrevText();
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

	Rule CommandOption() {
		return Sequence(
				FirstOf(
					StringIgnoreCase("option"),
					StringIgnoreCase("o")
				),
				Optional(
					Sequence(
						WhiteSpaces(),
						Identifier(),
						new Action() {
							public boolean run(Context context) {
								_setValueName = context.getPrevText();
								return true;
							}
						},
						Optional(
							Sequence(
								SkipSpaces(),
								CharIgnoreCase('='),
								SkipSpaces(),
								StringAtom(),
								new Action() {
									public boolean run(Context context) {
										_setValueValue = context.getPrevText();
										return true;
									}
								}
							)
						)
					)
				),
				Eoi(),
				new Action() {
					public boolean run(Context context) {
						_command = "option";
						return true;
					}
				}
		);
	}

	Rule CommandDefine() {
		return Sequence(
				FirstOf(
					StringIgnoreCase("define"),
					StringIgnoreCase("d")
				),
				Optional(
					Sequence(
						WhiteSpaces(),
						Identifier(),
						new Action() {
							public boolean run(Context context) {
								_setValueName = context.getPrevText();
								return true;
							}
						},
						Optional(
							Sequence(
								SkipSpaces(),
								CharIgnoreCase('='),
								SkipSpaces(),
								UntilEOI(),
								new Action() {
									public boolean run(Context context) {
										_setValueValue = context.getPrevText();
										return true;
									}
								}
							)
						)
					)
				),
				Eoi(),
				new Action() {
					public boolean run(Context context) {
						_command = "define";
						return true;
					}
				}
		);
	}


	Rule CommandHelp() {
		return Sequence(
					StringIgnoreCase("help"),
					Optional(
						Sequence(
							WhiteSpaces(),
							StringAtom(),
							new Action() {
								public boolean run(Context context) {
									_helpTopic = context.getPrevText();
									return true;
								}
							}
						)
					),
					Eoi(),
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
							Any()
						)
					);
	}

	Rule Special() {
		return CharSet('=', ',', ':');
	}

	Rule CommandEquipment() {
		return Sequence(
			FirstOf(
				StringIgnoreCase("equipment"),
				StringIgnoreCase("eq")
			),
			WhiteSpaces(),
			StringAtom(),
			new Action() {
				public boolean run(Context context) {
					_equipments = context.getPrevText();
					return true;
				}
			},
			SkipSpaces(),
			UntilEOI(),
			new Action() {
				public boolean run(Context context) {
					_subCommand = context.getPrevText();
					_command = "equipment";
					return true;
				}
			}
		);
	}

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
						StringIgnoreCase("probe6"),
						new Action() {
							public boolean run(Context context) {
								_probe6flag = true;
								return true;
							}
						}
					),
					Sequence(
						StringIgnoreCase("p6"),
						new Action() {
							public boolean run(Context context) {
								_probe6flag = true;
								return true;
							}
						}
					),
					StringIgnoreCase("probe"),
					StringIgnoreCase("p")
				),
				WhiteSpaces(),
				Optional(ProbeExpect()),
				Optional(OnEquipments()),
				SourceSpecification(),
				new Action() {
					public boolean run(Context context) {
						_srcAddress = context.getPrevText();
						return true;
					}
				},
				WhiteSpaces(),
				DestinationSpecification(),
				new Action() {
					public boolean run(Context context) {
						_destAddress = context.getPrevText();
						return true;
					}
				},
				Optional(
					Sequence(
						WhiteSpaces(),
						ProtoSpecification()
					)
				),
				Eoi(),
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

	Rule ProbeExpect() {
		return Sequence(
					StringIgnoreCase("expect"),
					WhiteSpaces(),
					StringAtom(),
					new Action() {
						public boolean run(Context context) {
							_probeExpect = context.getPrevText();
							return true;
						}
					},
					WhiteSpaces()
			);
	}

	Rule OnEquipments() {
		return Sequence(
			StringIgnoreCase("on"),
			WhiteSpaces(),
			StringAtom(),
			new Action() {
				public boolean run(Context context) {
					_equipments = context.getPrevText();
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
							_protoSpecification = context.getPrevText();
							return true;
						}
					},
					Optional(
						Sequence(
							WhiteSpaces(),
							TcpUdpSpecification()
						)
					)
				),
				Sequence(
					StringAtom(),
					new Action() {
						public boolean run(Context context) {
							_protoSpecification = context.getPrevText();
							return true;
						}
					},
					Optional(
						Sequence(
							WhiteSpaces(),
							Identifier(),
							new Action() {
								public boolean run(Context context) {
									_protoSource = context.getPrevText();
									return true;
								}
							}
						)
					)
				)
			);
	}

	Rule TcpUdpSpecification() {
		return
			FirstOf(
				Sequence(
					Identifier(),
					new Action() {
						public boolean run(Context context) {
							_protoSource = context.getPrevText();
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
									_protoDest = context.getPrevText();
									return true;
								}
							}
						)
					)
				),
				Sequence(
					Identifier(),
					new Action() {
						public boolean run(Context context) {
							_protoSource = null;
							_protoDest = context.getPrevText();
							return true;
						}
					}
				)
			);
	}

}
