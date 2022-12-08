/*
 * Copyright (c) 2013 - 2021, Universite de Rennes 1
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
package fr.univrennes1.cri.jtacl.equipments.proxmox;

import fr.univrennes1.cri.jtacl.lib.misc.ParseContext;

import java.util.ArrayList;

/**
 * Group of rules (rule section or Group)
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PxGroupRules extends ArrayList<PxRule> {
	protected String _name;
	protected ParseContext _context;

	public PxGroupRules(String name, ParseContext context) {
		super();
		_name = name;
		_context = context;
	}

	public String getName() { return _name; }

	public ParseContext getContext() { return _context; }
}
