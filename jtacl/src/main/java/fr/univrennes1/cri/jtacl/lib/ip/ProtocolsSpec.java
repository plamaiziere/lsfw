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

package fr.univrennes1.cri.jtacl.lib.ip;

import fr.univrennes1.cri.jtacl.core.probing.MatchResult;
import java.util.ArrayList;

/**
 * Protocols specification : a list of protocol number.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class ProtocolsSpec extends ArrayList<Integer> {

	/**
	 * Returns true if the protocol matches the list of protocols in argument.
	 * IP matches any protocol.
	 * @param protocols list.
	 * @param protocol protocol to check.
	 * @return true if the protocol matches the list of protocols in argument.
	 */
	public MatchResult matches(ProtocolsSpec protocols) {

		for (Integer proto: protocols) {
			if (this.contains(proto))
				return MatchResult.ALL;
		}
		return MatchResult.NOT;
	}
}
