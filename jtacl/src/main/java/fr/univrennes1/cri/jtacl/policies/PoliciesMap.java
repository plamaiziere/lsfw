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

package fr.univrennes1.cri.jtacl.policies;

import java.util.HashMap;

/**
 * Map of policies keyed by name
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */

public class PoliciesMap extends HashMap <String, Policy> {

	public void put(Policy policy) {
		put(policy.getName(), policy);
	}
}
