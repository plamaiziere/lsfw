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

package fr.univrennes1.cri.jtacl.equipments.cisco.pix;

import fr.univrennes1.cri.jtacl.lib.misc.CommonRules;
import java.util.ArrayList;
import java.util.List;
import org.parboiled.Action;
import org.parboiled.Context;
import org.parboiled.Rule;

/**
 * PIX Jtacl sub shell parser
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PixShellParser extends CommonRules<Object> {

	protected String _command = "";
	protected List<String> _param = null;

	public void clear() {
		_command = "";
		_param = new ArrayList<String>();
	}

	public String getCommand() {
		return _command;
	}

	public List<String> getParam() {
		return _param;
	}

	Rule CommandLine() {
		return Sequence(
			new Action() {
				public boolean run(Context context) {
					clear();
					return true;
				}
			},
			FirstOf(
				CommandHelp(),
				CommandShow(),
				String("PLACE-HOLDER")
			)
		);
	}

	Rule CommandHelp() {
		return Sequence(
			StringIgnoreCase("help"),
			new Action() {
				public boolean run(Context context) {
					_command = "help";
					return true;
				}
			},
			Eoi()
		);
	}

	/**
	 * show (names ¡ enhanced-service | icmp-group | network-group | protocol-group
	 * | service-group) | (used | unused)
	 */
	Rule CommandShow() {
		return Sequence(
			StringIgnoreCase("show"),
			WhiteSpaces(),
			FirstOf(
				StringIgnoreCase("names"),
				StringIgnoreCase("enhanced-service"),
				StringIgnoreCase("icmp-group"),
				StringIgnoreCase("network-group"),
				StringIgnoreCase("protocol-group"),
				StringIgnoreCase("service-group")
			),
			new Action() {
				public boolean run(Context context) {
					_command = "show-" + context.getPrevText().toLowerCase();
					return true;
				}
			},
			Optional(
				Sequence(
					WhiteSpace(),
					FirstOf(
						StringIgnoreCase("used"),
						StringIgnoreCase("unused")
					),
					new Action() {
						public boolean run(Context context) {
							_param.add(context.getPrevText().toLowerCase());
							return true;
						}
					}
				)
			),
			Eoi()
		);
	}

}
