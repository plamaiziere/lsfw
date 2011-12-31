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
	protected String _srcAddress;
	protected String _destAddress;
	protected String _equipments;
	protected String _protoSpecification;
	protected String _portSource;
	protected String _portDest;

	protected String _setValueName;
	protected String _setValueValue;
	protected String _helpTopic;
	protected String _topologyOption;
	protected String _probeExpect;
	protected boolean _probe6flag;
	protected boolean _probeOptNoAction;
	protected String _subCommand;
	protected StringsList _tcpFlags;
	protected String _groovyDirectory;
	protected String _groovyScript;
	protected String _groovyArgs;

	protected boolean clear() {
		_command = "";
		_srcAddress = null;
		_destAddress = null;
		_equipments = null;
		_protoSpecification = null;
		_portSource = null;
		_portDest = null;
		_setValueName = null;
		_setValueValue = null;
		_helpTopic = null;
		_topologyOption = null;
		_probeExpect = null;
		_subCommand = null;
		_tcpFlags = null;
		_probeOptNoAction = false;
		_probe6flag = false;
		return true;
	}

	public String getCommand() {
		return _command;
	}

	public boolean setCommand(String command) {
		_command = command;
		return true;
	}

	public String getDestAddress() {
		return _destAddress;
	}

	public boolean setDestAddress(String destAddress) {
		_destAddress = destAddress;
		return true;
	}

	public String getSrcAddress() {
		return _srcAddress;
	}

	public boolean setSrcAddress(String srcAddress) {
		_srcAddress = srcAddress;
		return true;
	}

	public String getEquipments() {
		return _equipments;
	}

	public boolean setEquipments(String equipments) {
		_equipments = equipments;
		return true;
	}

	public String getProtoSpecification() {
		return _protoSpecification;
	}

	public boolean setProtoSpecification(String protoSpecification) {
		_protoSpecification = protoSpecification;
		return true;
	}

	public String getPortDest() {
		return _portDest;
	}

	public boolean setPortDest(String portDest) {
		_portDest = portDest;
		return true;
	}

	public String getPortSource() {
		return _portSource;
	}

	public boolean setPortSource(String portSource) {
		_portSource = portSource;
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

	public String getProbeExpect() {
		return _probeExpect;
	}

	public boolean setProbeExpect(String probeExpect) {
		_probeExpect = probeExpect;
		return true;
	}

	public String getSubCommand() {
		return _subCommand;
	}

	public boolean setSubCommand(String subCommand) {
		_subCommand = subCommand;
		return true;
	}

	public StringsList getTcpFlags() {
		return _tcpFlags;
	}

	public boolean setTcpFlags(StringsList tcpFlags) {
		_tcpFlags = tcpFlags;
		return true;
	}

	public boolean addTcpFlag(String tcpFlag) {
		return _tcpFlags.add(tcpFlag);
	}

	public boolean getProbe6flag() {
		return _probe6flag;
	}

	public boolean setProbe6flag(boolean probe6flag) {
		_probe6flag = probe6flag;
		return true;
	}

	public boolean setProbeOptNoAction(boolean probeOptNoAction) {
		_probeOptNoAction = probeOptNoAction;
		return true;
	}

	public boolean getProbeOptNoAction() {
		return _probeOptNoAction;
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
					CommandGroovyConsole()
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
		return AnyOf("=,:");
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
		return (Rule) Sequence(
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
				setProbe6flag(false),
				FirstOf(
					Sequence(
						IgnoreCase("probe6"),
						setProbe6flag(true)
					),
					Sequence(
						IgnoreCase("p6"),
						setProbe6flag(true)
					),
					IgnoreCase("probe"),
					IgnoreCase("p")
				),
				WhiteSpaces(),
				Optional(ProbeOptions()),
				SourceSpecification(),
				setSrcAddress(match()),
				WhiteSpaces(),
				DestinationSpecification(),
				setDestAddress(match()),
				Optional(
					Sequence(
						WhiteSpaces(),
						ProtoSpecification()
					)
				),
				EOI,
				FirstOf(
					Sequence(
						getProbe6flag(),
						setCommand("probe6")
					),
					setCommand("probe")
				)
		);
	}

	/*
	 * ProbeOptions: ( ProbeExpect | OnEquipments | OptNoAction) ProbeOptions
	 */
	public Rule ProbeOptions() {
		return
			Sequence(
				FirstOf(
					ProbeExpect(),
					OnEquipments(),
					OptNoAction()
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
					setProbeExpect(match()),
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
			setEquipments(match()),
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
				setProbeOptNoAction(true)
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
					setProtoSpecification(match()),
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
					setProtoSpecification(match()),
					Optional(
						Sequence(
							WhiteSpaces(),
							Identifier(),
							setPortSource(match())
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
					Identifier(),
					setPortSource(match()),
					setPortDest(null),
					String(":"),
					Optional(
						Sequence(
							Identifier(),
							setPortDest(match())
						)
					)
				),
				Sequence(
					TestNot(IgnoreCase("flags")),
					Identifier(),
					setPortDest(match()),
					setPortSource(null)
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
				setTcpFlags(new StringsList()),
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

	/*
	 * atom
	 */
	public Rule TcpFlagSpec() {
		return
			Sequence(
				StringAtom(),
				addTcpFlag(match())
			);
	}
}
