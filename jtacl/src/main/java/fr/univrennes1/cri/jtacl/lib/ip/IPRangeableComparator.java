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
 * IPRangeable comparator.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class IPRangeableComparator implements Comparator<IPRangeable> {

	@Override
	public int compare(IPRangeable ip1, IPRangeable ip2) {
		/*
		 * compare first by base ip, then by range
		 */
		int comp = ip1.getIpFirst().compareTo(ip2.getIpFirst());
		if (comp != 0)
			return comp;
		return ip1.getIpLast().compareTo(ip2.getIpLast());
	}
}
