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

import fr.univrennes1.cri.jtacl.core.probing.MatchResult;
import fr.univrennes1.cri.jtacl.lib.ip.PortSpec;
import java.util.ArrayList;

/**
 * Port specification: a list of Port item
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PfPortSpec extends ArrayList<PfPortItem> {

	/**
	 * Checks if this {@link PfPortSpec} matches the port in argument.
	 * @param port port to check.
	 * @return true if this {@link PfPortSpec} matches the port in argument.
	 */
	public MatchResult matches(PortSpec port) {
		int match = 0;
		int all = 0;
		for (PfPortItem item: this) {
			MatchResult res = item.matches(port);
			if (res == MatchResult.ALL)
				all++;
			if (res == MatchResult.MATCH)
				match++;
		}
		if (all > 0)
			return MatchResult.ALL;
		if (match > 0)
			return MatchResult.MATCH;

		return MatchResult.NOT;
	}

}
