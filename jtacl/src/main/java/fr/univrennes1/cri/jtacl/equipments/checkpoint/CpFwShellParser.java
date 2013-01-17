/*
 * Copyright (c) 2013, Universite de Rennes 1
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

package fr.univrennes1.cri.jtacl.equipments.checkpoint;

import fr.univrennes1.cri.jtacl.equipments.generic.GenericEquipmentShellParser;
import org.parboiled.Rule;

/**
 * CpFw sub shell parser
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class CpFwShellParser extends GenericEquipmentShellParser {

	@Override
	protected boolean clear() {
		return super.clear();
	}

	public Rule CommandLine() {
		return
			Sequence(
				clear(),
				FirstOf(
					CommandHelp(),
					CommandXref()
				)
			);
	}

}
