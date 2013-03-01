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

import fr.univrennes1.cri.jtacl.lib.misc.StringsList;
import fr.univrennes1.cri.jtacl.parsers.CommonRules;
import java.util.HashMap;
import org.parboiled.Rule;

/**
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class ShellParser extends CommonRules<Object> {

	protected HashMap <String, String> _strings = new HashMap<String, String>();
	protected ProbeCommandTemplate _probeCmdTemplate;

	protected boolean clear() {
		_strings.clear();
		_probeCmdTemplate = new ProbeCommandTemplate();
		return true;
	}

	public boolean setString(String key, String value) {
		_strings.put(key, value);
		return true;
	}

	public String getString(String key) {
		return _strings.get(key);
	}

	public ProbeCommandTemplate getProbeCmdTemplate() {
		return _probeCmdTemplate;
	}

	public Rule CommandLine() {
		return
			Sequence(
				clear(),
				FirstOf(
					CommandProbe(),
					CommandQuit(),
					CommandDefine(),
					CommandOption(),
					CommandTopology(),
					CommandRoute(),
					CommandHelp(),
					CommandEquipment(),
					CommandReload(),
					CommandGroovy(),
					CommandGroovyConsole(),
					CommandHost(),
					CommandHost6(),
					CommandPolicyLoad(),
					CommandPolicyProbe()
				)
			);
	}

	public Rule CommandQuit() {
		return Sequence(
				FirstOf(
					IgnoreCase("quit"),
					IgnoreCase("exit"),
					IgnoreCase("q"),
					IgnoreCase("e")
				),
				EOI,
				setString("Command", "quit")
		);
	}

	/*
	 * (topology | t) [connected | !connected] [atom]
	 *
	 */
	public Rule CommandTopology() {
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
							setString("TopologyOption", match())
						)
					),
					Optional(
						Sequence(
							WhiteSpaces(),
							StringAtom(),
							setString("Equipments", match())
						)
					),
					EOI,
					setString("Command", "topology")
			);
	}

	/*
	 * route [atom]
	 */
	public Rule CommandRoute() {
		return Sequence(
					IgnoreCase("route"),
					Optional(
						Sequence(
							WhiteSpaces(),
							StringAtom(),
							setString("Equipments", match())
						)
					),
					setString("Command", "route")
			);
	}

	/*
	 * (option | o) [identifier [= atom]]
	 */
	public Rule CommandOption() {
		return Sequence(
				FirstOf(
					IgnoreCase("option"),
					IgnoreCase("o")
				),
				Optional(
					Sequence(
						WhiteSpaces(),
						Identifier(),
						setString("SetValueName", match()),
						Optional(
							Sequence(
								SkipSpaces(),
								IgnoreCase('='),
								SkipSpaces(),
								StringAtom(),
								setString("SetValueValue", match())
							)
						)
					)
				),
				EOI,
				setString("Command", "option")
		);
	}

	/*
	 * (define | d) [identifier [ = string]]
	 *
	 */
	public Rule CommandDefine() {
		return Sequence(
				FirstOf(
					IgnoreCase("define"),
					IgnoreCase("d")
				),
				Optional(
					Sequence(
						WhiteSpaces(),
						Identifier(),
						setString("SetValueName", match()),
						Optional(
							Sequence(
								SkipSpaces(),
								IgnoreCase('='),
								SkipSpaces(),
								UntilEOI(),
								setString("SetValueValue", match())
							)
						)
					)
				),
				EOI,
				setString("Command", "define")
		);
	}

	/*
	 * policy load filename
	 */
	public Rule CommandPolicyLoad() {
		return Sequence(
					IgnoreCase("policy"),
					WhiteSpaces(),
					IgnoreCase("load"),
					WhiteSpaces(),
					StringOrQuotedString(),
					setString("FileName", getLastQuotedString()),
					EOI,
					setString("Command", "policy-load")
				);
	}

	/*
	 * policy probe policyname [from ip] [to ip]
	 */
	public Rule CommandPolicyProbe() {
		return Sequence (
					IgnoreCase("policy"),
					WhiteSpaces(),
					IgnoreCase("probe"),
					WhiteSpaces(),
					StringAtom(),
					setString("PolicyName", match()),
					Optional(
						Sequence(
							WhiteSpaces(),
							IgnoreCase("from"),
							WhiteSpaces(),
							StringAtom(),
							setString("PolicyFrom", match())
						)
					),
					Optional(
						Sequence(
							WhiteSpaces(),
							IgnoreCase("to"),
							WhiteSpaces(),
							StringAtom(),
							setString("PolicyTo", match())
						)
					),
					EOI,
					setString("Command", "policy-probe")
				);
	}

	/*
	 * help [atom]
	 */
	public Rule CommandHelp() {
		return Sequence(
					IgnoreCase("help"),
					Optional(
						Sequence(
							WhiteSpaces(),
							StringAtom(),
							setString("HelpTopic", match())
						)
					),
					EOI,
					setString("Command", "help")
				);
	}

	public Rule Identifier() {
			return OneOrMore(
						Sequence(
							TestNot(WhiteSpaces()),
							TestNot(Special()),
							ANY
						)
					);
	}

	public Rule Special() {
		return AnyOf("=,:()");
	}

	/*
	 * (equipment | eq) atom string
	 */
	public Rule CommandEquipment() {
		return Sequence(
			FirstOf(
				IgnoreCase("equipment"),
				IgnoreCase("eq")
			),
			WhiteSpaces(),
			StringAtom(),
			setString("Equipments", match()),
			SkipSpaces(),
			UntilEOI(),
			setString("SubCommand", match()),
			setString("Command", "equipment")
		);
	}

	/*
	 * reload [atom]
	 */
	public Rule CommandReload() {
		return Sequence(
				IgnoreCase("reload"),
				Optional(
					Sequence(
						WhiteSpaces(),
						StringAtom(),
						setString("Equipments", match())
					)
				),
				EOI,
				setString("Command", "reload")
		);
	}

	/*
	 * (probe | p | probe6 | p6) [ProbeOptions]
	 *		SourceSpec DestSpec [ProtoSpec]
	 */
	public Rule CommandProbe() {
		return Sequence(
				_probeCmdTemplate.setProbe6flag(false),
				FirstOf(
					Sequence(
						IgnoreCase("probe6"),
						_probeCmdTemplate.setProbe6flag(true)
					),
					Sequence(
						IgnoreCase("p6"),
						_probeCmdTemplate.setProbe6flag(true)
					),
					IgnoreCase("probe"),
					IgnoreCase("p")
				),
				WhiteSpaces(),
				Optional(ProbeOptions()),
				SourceSpecification(),
				_probeCmdTemplate.setSrcAddress(match()),
				WhiteSpaces(),
				DestinationSpecification(),
				_probeCmdTemplate.setDestAddress(match()),
				Optional(
					Sequence(
						WhiteSpaces(),
						ProtoSpecification()
					)
				),
				EOI,
				setString("Command", "probe")
		);
	}

	/*
	 * ProbeOptions: ( ProbeExpect | OnEquipments | OptNoAction | OptVerbose)
	 *					ProbeOptions
	 */
	public Rule ProbeOptions() {
		return
			Sequence(
				FirstOf(
					ProbeExpect(),
					OnEquipments(),
					OptNoAction(),
					OptVerbose(),
					OptActive(),
					OptMatching(),
					OptLearn(),
					OptQuickDeny(),
					OptState()
				),
				Optional(
					ProbeOptions()
				)
			);
	}

	/*
	 * expect atom
	 */
	public Rule ProbeExpect() {
		return Sequence(
					IgnoreCase("expect"),
					WhiteSpaces(),
					StringAtom(),
					_probeCmdTemplate.setProbeExpect(match()),
					WhiteSpaces()
			);
	}

	/*
	 * on atom
	 */
	public Rule OnEquipments() {
		return Sequence(
			IgnoreCase("on"),
			WhiteSpaces(),
			StringAtom(),
			_probeCmdTemplate.setEquipments(match()),
			WhiteSpaces()
		);
	}

	/*
	 * OptNoAction: no-action | na
	 */
	public Rule OptNoAction() {
		return
			Sequence(
				FirstOf(
					IgnoreCase("no-action"),
					IgnoreCase("na")
				),
				WhiteSpaces(),
				_probeCmdTemplate.setProbeOptNoAction(true)
			);
	}

	/*
	 * OptVerbose: verbose
	 */
	public Rule OptVerbose() {
		return
			Sequence(
				IgnoreCase("verbose"),
				WhiteSpaces(),
				_probeCmdTemplate.setProbeOptVerbose(true)
			);
	}

	/*
	 * OptActive: active | act
	 */
	public Rule OptActive() {
		return
			Sequence(
				FirstOf(
					IgnoreCase("active"),
					IgnoreCase("act")
				),
				WhiteSpaces(),
				_probeCmdTemplate.setProbeOptActive(true)
			);
	}

	/*
	 * OptMatching: match
	 */
	public Rule OptMatching() {
		return
			Sequence(
				IgnoreCase("match"),
				WhiteSpaces(),
				_probeCmdTemplate.setProbeOptMatching(true)
			);
	}

	/*
	 * OptLearn: learn
	 */
	public Rule OptLearn() {
		return
			Sequence(
				IgnoreCase("learn"),
				WhiteSpaces(),
				_probeCmdTemplate.setProbeOptLearn(true)
			);
	}

	/*
	 * OptQuickDeny: quick-deny | qd
	 */
	public Rule OptQuickDeny() {
		return
			Sequence(
				FirstOf (
					IgnoreCase("quick-deny"),
					IgnoreCase("qd")
				),
				WhiteSpaces(),
				_probeCmdTemplate.setProbeOptQuickDeny(true)
			);
	}

	/*
	 * OptState: state
	 */
	public Rule OptState() {
		return
			Sequence(
				IgnoreCase("state"),
				WhiteSpaces(),
				_probeCmdTemplate.setProbeOptState(true)
			);
	}

	public Rule SourceSpecification() {
		return StringAtom();
	}

	public Rule DestinationSpecification() {
		return StringAtom();
	}

	/*
	 * ( (udp | tcp) [TcpUdpSpec] [TcpFlagsSpec] )
	 *   | (atom [Identifier])
	 */
	public Rule ProtoSpecification() {
		return
			FirstOf(
				Sequence(
					FirstOf(
						String("udp"),
						String("tcp")
					),
					_probeCmdTemplate.setProtoSpecification(match()),
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
					_probeCmdTemplate.setProtoSpecification(match()),
					Optional(
						Sequence(
							WhiteSpaces(),
							PortSpec(),
							_probeCmdTemplate.setPortSource(match())
						)
					)
				)
			);
	}

	/*
	 * ([Identifier]:[Identifier]) | Identifier
	 */
	public Rule TcpUdpSpecification() {
		return
			FirstOf(
				Sequence(
					TestNot(IgnoreCase("flags")),
					PortSpec(),
					_probeCmdTemplate.setPortSource(match()),
					_probeCmdTemplate.setPortDest(null),
					String(":"),
					Optional(
						Sequence(
							PortSpec(),
							_probeCmdTemplate.setPortDest(match())
						)
					)
				),
				Sequence(
					TestNot(IgnoreCase("flags")),
					PortSpec(),
					_probeCmdTemplate.setPortDest(match()),
					_probeCmdTemplate.setPortSource(null)
				)
			);
	}

	/*
	 * portspec : identifier | '(' identifier ',' identifier ')'
	 */
	public Rule PortSpec() {
		return
			FirstOf(
				Identifier(),
				Sequence(
					Ch('('),
					Identifier(),
					Ch(','),
					Identifier(),
					Ch(')')
				)
			);
	}

	/*
	 * flags TcpFlagsSpec+
	 */
	public Rule TcpFlagsSpec() {
		return
			Sequence(
				IgnoreCase("flags"),
				WhiteSpaces(),
				_probeCmdTemplate.setTcpFlags(new StringsList()),
				OneOrMore(
					Sequence(
						TcpFlagSpec(),
						SkipSpaces()
					)
				)
			);
	}

	/*
	 * groovy string string
	 */
	public Rule CommandGroovy() {
		return
			Sequence(
				FirstOf(
					IgnoreCase("groovy"),
					IgnoreCase("g")
				),
				WhiteSpaces(),
				StringOrQuotedString(),
				setString("GroovyDirectory", getLastQuotedString()),
				WhiteSpaces(),
				StringOrQuotedString(),
				setString("GroovyScript", getLastQuotedString()),
				SkipSpaces(),
				UntilEOI(),
				setString("GroovyArgs",match()),
				setString("Command", "groovy")
			);
	}

	/*
	 * groovyConsole | gc
	 */
	public Rule CommandGroovyConsole() {
		return
			Sequence(
				FirstOf(
					IgnoreCase("groovyconsole"),
					IgnoreCase("gc")
				),
				SkipSpaces(),
				UntilEOI(),
				setString("GroovyArgs", match()),
				setString("Command", "groovyconsole")
			);
	}

	public Rule CommandHost() {
		return
			Sequence(
				IgnoreCase("host"),
				WhiteSpaces(),
				UntilEOI(),
				setString("AddressArg", match()),
				setString("Command", "host")
			);
	}

	public Rule CommandHost6() {
		return
			Sequence(
				IgnoreCase("host6"),
				WhiteSpaces(),
				UntilEOI(),
				setString("AddressArg", match()),
				setString("Command", "host6")
			);
	}

	/*
	 * atom
	 */
	public Rule TcpFlagSpec() {
		return
			Sequence(
				StringAtom(),
				_probeCmdTemplate.addTcpFlag(match())
			);
	}
}
