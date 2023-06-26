/*
 * Copyright (c) 2010, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.equipments.openbsd;

import fr.univrennes1.cri.jtacl.equipments.generic.GenericEquipmentShellParser;
import org.parboiled.Rule;

/**
 * PacketFilter Jtacl sub shell parser
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PacketFilterShellParser extends GenericEquipmentShellParser {

	protected String fortiEqname;

	@Override
	protected boolean clear() {
		fortiEqname = null;
		return super.clear();
	}

	protected boolean setFortiEqName(String s) {
		fortiEqname = s;
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
					CommandConvert()
				)
			);
	}

	public Rule CommandConvert() {
		return Sequence(
			IgnoreCase("to-fortigate"),
			WhiteSpaces(),
			StringAtom(),
			setCommand("to-fortigate"),
			setFortiEqName(match())
		);
	}

}
