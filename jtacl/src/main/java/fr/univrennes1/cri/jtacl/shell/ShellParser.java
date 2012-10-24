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
import org.parboiled.Rule;

/**
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class ShellParser extends CommonRules<Object> {

	protected String _command = "";
	protected String _equipments;
	protected ProbeCommandTemplate _probeCmdTemplate;
	protected String _setValueName;
	protected String _setValueValue;
	protected String _helpTopic;
	protected String _topologyOption;
	protected String _subCommand;
	protected String _groovyDirectory;
	protected String _groovyScript;
	protected String _groovyArgs;
	protected String _addressArg;

	protected boolean clear() {
		_command = "";
		_probeCmdTemplate = new ProbeCommandTemplate();
		_equipments = null;
		_setValueName = null;
		_setValueValue = null;
		_helpTopic = null;
		_topologyOption = null;
		_subCommand = null;
		_addressArg = null;
		return true;
	}

	public String getCommand() {
		return _command;
	}

	public boolean setCommand(String command) {
		_command = command;
		return true;
	}

	public String getEquipments() {
		return _equipments;
	}

	public boolean setEquipments(String equipments) {
		_equipments = equipments;
		return true;
	}

	public String getSetValueName() {
		return _setValueName;
	}

	public boolean setSetValueName(String setValueName) {
		_setValueName = setValueName;
		return true;
	}

	public String getSetValueValue() {
		return _setValueValue;
	}

	public boolean setSetValueValue(String setValueValue) {
		_setValueValue = setValueValue;
		return true;
	}

	public String getHelpTopic() {
		return _helpTopic;
	}

	public boolean setHelpTopic(String helpTopic) {
		_helpTopic = helpTopic;
		return true;
	}

	public String getTopologyOption() {
		return _topologyOption;
	}

	public boolean setTopologyOption(String topologyOption) {
		_topologyOption = topologyOption;
		return true;
	}

	public String getSubCommand() {
		return _subCommand;
	}

	public boolean setSubCommand(String subCommand) {
		_subCommand = subCommand;
		return true;
	}

	public boolean setGroovyDirectory(String directory) {
		_groovyDirectory = directory;
		return true;
	}

	public String getGroovyDirectory() {
		return _groovyDirectory;
	}

	public boolean setGroovyScript(String script) {
		_groovyScript = script;
		return true;
	}

	public String getGroovyScript() {
		return _groovyScript;
	}

	public String getGroovyArgs() {
		return _groovyArgs;
	}

	public boolean setGroovyArgs(String groovyArgs) {
		_groovyArgs = groovyArgs;
		return true;
	}

	public ProbeCommandTemplate getProbeCmdTemplate() {
		return _probeCmdTemplate;
	}
	
	public boolean setAddressArg(String addressArg) {
		_addressArg = addressArg;
		return true;
	}

	public String getAddressArg() {
		return _addressArg;
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
					CommandHost6()
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
				setCommand("quit")
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
							setTopologyOption(match())
						)
					),
					Optional(
						Sequence(
							WhiteSpaces(),
							StringAtom(),
							setEquipments(match())
						)
					),
					EOI,
					setCommand("topology")
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
							setEquipments(match())
						)
					),
					setCommand("route")
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
						setSetValueName(match()),
						Optional(
							Sequence(
								SkipSpaces(),
								IgnoreCase('='),
								SkipSpaces(),
								StringAtom(),
								setSetValueValue(match())
							)
						)
					)
				),
				EOI,
				setCommand("option")
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
						setSetValueName(match()),
						Optional(
							Sequence(
								SkipSpaces(),
								IgnoreCase('='),
								SkipSpaces(),
								UntilEOI(),
								setSetValueValue(match())
							)
						)
					)
				),
				EOI,
				setCommand("define")
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
							setHelpTopic(match())
						)
					),
					EOI,
					setCommand("help")
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
			setEquipments(match()),
			SkipSpaces(),
			UntilEOI(),
			setSubCommand(match()),
			setCommand("equipment")
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
						setEquipments(match())
					)
				),
				EOI,
				setCommand("reload")
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
				setCommand("probe")
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
					OptQuickDeny()
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
				StringAtom(),
				setGroovyDirectory(match()),
				WhiteSpaces(),
				StringAtom(),
				setGroovyScript(match()),
				SkipSpaces(),
				UntilEOI(),
				setGroovyArgs(match()),
				setCommand("groovy")
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
				setGroovyArgs(match()),
				setCommand("groovyconsole")
			);
	}

	public Rule CommandHost() {
		return
			Sequence(
				IgnoreCase("host"),
				WhiteSpaces(),
				UntilEOI(),
				setAddressArg(match()),
				setCommand("host")
			);
	}

	public Rule CommandHost6() {
		return
			Sequence(
				IgnoreCase("host6"),
				WhiteSpaces(),
				UntilEOI(),
				setAddressArg(match()),
				setCommand("host6")
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
