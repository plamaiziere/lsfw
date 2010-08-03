/*
 * Copyright (c) 2010, Université de Rennes 1
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
	public boolean matches(int port) {
		for (PfPortItem item: this) {
			if (item.matches(port))
				return true;
		}
		return false;
	}

}
