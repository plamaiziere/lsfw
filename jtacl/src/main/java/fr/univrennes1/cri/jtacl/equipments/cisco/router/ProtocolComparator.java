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

package fr.univrennes1.cri.jtacl.equipments.cisco.router;

import fr.univrennes1.cri.jtacl.lib.ip.Protocols;
import java.util.List;

/**
 * Comparator of protocols.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class ProtocolComparator {

	/**
	 * Returns true if the protocol matches the list of protocols in argument.
	 * IP matches any protocol.
	 * @param protocols list.
	 * @param protocol protocol to check.
	 * @return true if the protocol matches the list of protocols in argument.
	 */
	public static boolean matches(List<Integer> protocols, int protocol) {
		if (protocol == Protocols.IP)
			return true;

		return protocols.contains(protocol);
	}


}
