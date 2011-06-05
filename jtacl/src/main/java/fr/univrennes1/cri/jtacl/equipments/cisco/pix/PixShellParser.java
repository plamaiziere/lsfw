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

import fr.univrennes1.cri.jtacl.equipments.generic.GenericEquipmentShellParser;
import org.parboiled.Action;
import org.parboiled.Context;
import org.parboiled.Rule;

/**
 * PIX Jtacl sub shell parser
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PixShellParser extends GenericEquipmentShellParser {

	public void clear() {
		super.clear();
	}

	public Rule CommandLine() {
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
				CommandXref(),
				String("PLACE-HOLDER")
			)
		);
	}

	/**
	 * show (names | enhanced-service | icmp-group | network-group | protocol-group
	 * | service-group) | (used | unused)
	 */
	Rule CommandShow() {
		return Sequence(
			IgnoreCase("show"),
			WhiteSpaces(),
			FirstOf(
				IgnoreCase("name"),
				IgnoreCase("enhanced-service"),
				IgnoreCase("icmp-group"),
				IgnoreCase("network-group"),
				IgnoreCase("protocol-group"),
				IgnoreCase("service-group")
			),
			new Action() {
				public boolean run(Context context) {
					_command = "show-" + context.getMatch().toLowerCase();
					return true;
				}
			},
			Optional(
				Sequence(
					WhiteSpace(),
					FirstOf(
						IgnoreCase("used"),
						IgnoreCase("unused")
					),
					new Action() {
						public boolean run(Context context) {
							_param.add(context.getMatch().toLowerCase());
							return true;
						}
					}
				)
			),
			EOI
		);
	}

}
