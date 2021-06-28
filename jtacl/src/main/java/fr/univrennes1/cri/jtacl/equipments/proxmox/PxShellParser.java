/*
 * Copyright (c) 2021, Universite de Rennes 1
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

package fr.univrennes1.cri.jtacl.equipments.proxmox;

import fr.univrennes1.cri.jtacl.equipments.generic.GenericEquipmentShellParser;
import org.parboiled.Rule;

/**
 * proxmox sub shell parser
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PxShellParser extends GenericEquipmentShellParser {

	protected String _ident;


	@Override
	protected boolean clear() {
		_ident = null;
		return super.clear();
	}

	public String getIdent() {
		return _ident;
	}

	public boolean setIdent(String ident) {
		_ident = ident;
		return true;
	}

	public Rule CommandLine() {
		return
			Sequence(
				clear(),
				FirstOf(
					CommandHelp(),
					CommandXrefIp(),
					CommandXrefService(),
					CommandShowAlias(),
					CommandShowIpSet(),
					CommandShowRules()
				)
			);
	}

	public Rule CommandShowAlias() {
		return
			Sequence(
				IgnoreCase("show"),
				WhiteSpaces(),
				IgnoreCase("alias"),
				FirstOf(
					WhiteSpaces(),
					EOI
				),
				UntilEOI(),
				setIdent(match().trim()),
				setCommand("show-alias")
		);
	}

	public Rule CommandShowIpSet() {
		return
			Sequence(
				IgnoreCase("show"),
				WhiteSpaces(),
				IgnoreCase("ipset"),
				FirstOf(
					WhiteSpaces(),
					EOI
				),
				UntilEOI(),
				setIdent(match().trim()),
				setCommand("show-ipset")
		);
	}

	public Rule CommandShowRules() {
		return
			Sequence(
				IgnoreCase("show"),
				WhiteSpaces(),
				IgnoreCase("rules"),
				FirstOf(
					WhiteSpaces(),
					EOI
				),
				UntilEOI(),
				setIdent(match().trim()),
				setCommand("show-rules")
		);
	}

}
