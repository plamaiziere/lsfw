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

package fr.univrennes1.cri.jtacl.core.monitor;

import java.util.ArrayList;

/**
 * A {@link ArrayList} list of {@link ProbePosition} items.
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class ProbePositions extends ArrayList<ProbePosition> {

	/**
	 * Checks if the {@link ProbePositions} positions in argument contains all
	 * items found in this instance, regardless of the order.
	 *
	 * @param positions {@link ProbePositions} positions to compare.
	 * @return true if all items are contained by this instance.
	 */
	public boolean sameAs(ProbePositions positions) {

		if (size() != positions.size())
			return false;
		for (ProbePosition p: this) {
			if (!positions.contains(p))
				return false;
		}
		return true;
	}
}
