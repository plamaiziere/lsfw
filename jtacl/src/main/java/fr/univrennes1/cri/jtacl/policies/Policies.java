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

/**
 * Policies Map (singleton)
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class Policies {

	protected static PoliciesMap _instance = new PoliciesMap();

	public static PoliciesMap getInstance() {
		return _instance;
	}
}
