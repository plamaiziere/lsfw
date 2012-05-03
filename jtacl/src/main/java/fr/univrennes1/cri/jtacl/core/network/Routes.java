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

package fr.univrennes1.cri.jtacl.core.network;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * An {@link ArrayList} list containing {@link Route} items.
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class Routes extends ArrayList<Route> {

		static class PrefixComparator implements Comparator<Route> {

			@Override
			public int compare(Route r1, Route r2) {
				int lenr1 = r1.getPrefix().getPrefixLen();
				int lenr2 = r2.getPrefix().getPrefixLen();

				if (lenr1 < lenr2)
					return 1;
				if (lenr1 > lenr2)
					return -1;

				BigInteger ip1 = r1.getPrefix().getIP();
				BigInteger ip2 = r2.getPrefix().getIP();

				return ip1.compareTo(ip2);
			}
		}

		static class MetricComparator implements Comparator<Route> {

			@Override
			public int compare(Route r1, Route r2) {
				int metricr1 = r1.getMetric();
				int metricr2 = r2.getMetric();

				if (metricr1 < metricr2)
					return -1;
				if (metricr1 > metricr2)
					return 1;

				return 0;
			}
		}

	private static PrefixComparator _prefixCompare = new PrefixComparator();
	private static MetricComparator _metricCompare = new MetricComparator();

	/**
	 * Sorts this list by prefix order.
	 */
	public void sortByPrefix() {
		Collections.sort(this, _prefixCompare);
	}

	/**
	 * Sorts this list by metric order.
	 */
	public void sortByMetric() {
		Collections.sort(this, _metricCompare);
	}

}
