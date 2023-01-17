/*
 * Copyright (c) 2013 - 2021, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */
package fr.univrennes1.cri.jtacl.equipments.proxmox;

import fr.univrennes1.cri.jtacl.core.probing.MatchResult;
import fr.univrennes1.cri.jtacl.lib.ip.IPRangeable;
import fr.univrennes1.cri.jtacl.lib.misc.ParseContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Proxmox ipset network object
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PxNetworkIpSet extends PxNetworkObject {

	/* hash of base objects */
	protected HashMap<String, PxNetworkObject> _baseObjects =
            new HashMap<>();

	/**
	 * Construct a new Proxmox IPset network object
	 * @param name object name name
	 */
	public PxNetworkIpSet(String name, ParseContext context) {
		super(name, PxNetworkType.IPSET, context);
	}

	public void addReference(String name, PxNetworkObject networkObject) {
		_baseObjects.put(name, networkObject);
	}

	/**
	 * returns a list of the base references name included in this group.
	 * @return a list of the base references name included in this group.
	 */
	public List<String> getReferencesName() {
		List<String> list = new LinkedList<>(_baseObjects.keySet());
		Collections.sort(list);
		return list;
	}

	public HashMap<String, PxNetworkObject> getBaseObjects() {
		return _baseObjects;
	}

	@Override
	public String toString() {
		return _name + ", " +  _type + ", " + getReferencesName();
	}

	@Override
	public MatchResult matches(IPRangeable ip) {

		/*
		 * base items
		 */
		int mayincluded = 0;
		int all = 0;
		for (PxNetworkObject nobject: _baseObjects.values()) {
			MatchResult mres = nobject.matches(ip);
			if (mres == MatchResult.ALL) {
				all++;
				continue;
			}
			if (mres != MatchResult.NOT) {
				mayincluded++;
				continue;
			}
		}
		if (all > 0) {
			return MatchResult.ALL;
		}
		if (mayincluded == 0)
			return MatchResult.NOT;
		return MatchResult.MATCH;
	}
}
