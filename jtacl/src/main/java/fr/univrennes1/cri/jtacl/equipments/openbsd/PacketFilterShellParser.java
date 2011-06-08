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

package fr.univrennes1.cri.jtacl.equipments.openbsd;

import fr.univrennes1.cri.jtacl.equipments.generic.GenericEquipmentShellParser;
import fr.univrennes1.cri.jtacl.lib.misc.CommonRules;
import java.util.List;
import org.parboiled.Action;
import org.parboiled.Context;
import org.parboiled.Parboiled;
import org.parboiled.Rule;

/**
 * PacketFilter Jtacl sub shell parser
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PacketFilterShellParser extends CommonRules<Object> {

	protected GenericEquipmentShellParser _genericShell;

	public PacketFilterShellParser() {
		_genericShell = Parboiled.createParser(GenericEquipmentShellParser.class);
	}

	public void clear() {
		_genericShell.clear();
	}

	public String getCommand() {
		return _genericShell.getCommand();
	}

	public List<String> getParam() {
		return _genericShell.getParam();
	}

	public String getXrefIp() {
		return _genericShell.getXrefIp();
	}

	public String getXrefFormat() {
		return _genericShell.getXrefFormat();
	}

	public String getXrefObject() {
		return _genericShell.getXrefObject();
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
				_genericShell.CommandHelp(),
				_genericShell.CommandXref()
			)
		);
	}

}
