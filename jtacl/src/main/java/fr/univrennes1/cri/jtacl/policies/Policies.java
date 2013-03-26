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

 */package fr.univrennes1.cri.jtacl.policies;

import fr.univrennes1.cri.jtacl.core.exceptions.JtaclConfigurationException;
import org.config4j.ConfigurationException;

/**
 * Policies Map (singleton)
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class Policies {

	protected static PoliciesMap _instance = new PoliciesMap();

	public static PoliciesMap getInstance() {
		return _instance;
	}

	public static void clear() {
		getInstance().clear();
	}

	public static void loadPolicies(String filename) {
		PolicyConfig config;
		try {
			config = new PolicyConfig(filename);
		} catch (ConfigurationException ex) {
			throw new JtaclConfigurationException(ex.getMessage());
		}

		PoliciesMap pm;
		pm = config.loadPolicies();
		config.linkPolicies(pm);
		getInstance().putAll(pm);
	}

}
