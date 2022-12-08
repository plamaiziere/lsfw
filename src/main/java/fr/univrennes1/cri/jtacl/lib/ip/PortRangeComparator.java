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

import java.util.Comparator;

/**
 * PortRange comparator.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PortRangeComparator implements Comparator<PortRange> {

	@Override
	public int compare(PortRange port1, PortRange port2) {
		/*
		 * compare first by base, then by range
		 */
		if (port1.getFirstPort() < port2.getFirstPort())
			return -1;
		if (port1.getFirstPort() > port2.getFirstPort())
			return 1;
		if (port1.getLastPort() < port2.getLastPort())
			return -1;
		if (port1.getLastPort() > port2.getLastPort())
			return 1;
		return 0;

	}
}
