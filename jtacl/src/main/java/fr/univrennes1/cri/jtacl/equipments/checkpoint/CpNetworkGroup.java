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

package fr.univrennes1.cri.jtacl.equipments.checkpoint;

import fr.univrennes1.cri.jtacl.core.probing.MatchResult;
import fr.univrennes1.cri.jtacl.lib.ip.IPRangeable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Checkpoint group network object
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class CpNetworkGroup extends CpNetworkObject {

	/* hash of base objects */
	protected HashMap<String, CpNetworkObject> _baseObjects =
		new HashMap<String, CpNetworkObject>();

	/* hash of excluded objects */
	protected HashMap<String, CpNetworkObject> _excludedObjects =
		new HashMap<String, CpNetworkObject>();


	/**
	 * Construct a new checkpoint IP network group object
	 * @param name object name name
	 * @param className checkpoint class name
	 * @param comment comment
	 */
	public CpNetworkGroup(String name, String className, String comment) {

		super(name, className, comment, CpNetworkType.GROUP);
	}

	public void addBaseReference(String name, CpNetworkObject networkObject) {
		_baseObjects.put(name, networkObject);
	}

	public void addExcludedReference(String name, CpNetworkObject networkObject) {
		_excludedObjects.put(name, networkObject);
	}

	/**
	 * returns a list of the base references name included in this group.
	 * @return a list of the base references name included in this group.
	 */
	public List<String> getBaseReferencesName() {
		List<String> list = new LinkedList<String>(_baseObjects.keySet());
		Collections.sort(list);
		return list;
	}

	/**
	 * returns a list of the excluded references name included in this group.
	 * @return a list of the excluded references name included in this group.
	 */
	public List<String> getExcludedReferencesName() {
		List<String> list = new LinkedList<String>(_excludedObjects.keySet());
		Collections.sort(list);
		return list;
	}

	public HashMap<String, CpNetworkObject> getBaseObjects() {
		return _baseObjects;
	}

	public HashMap<String, CpNetworkObject> getExcludedObjects() {
		return _excludedObjects;
	}

	@Override
	public String toString() {
		return _name + ", " + _className + ", " + _comment + ", " +  _type
				+ ", base=" + getBaseReferencesName() + ", excluded="
				+ getExcludedReferencesName();
	}

	@Override
	public MatchResult matches(IPRangeable ip) {
		/*
		 * excluded items
		 */
		int mayexcluded = 0;
		for (CpNetworkObject nobject: _excludedObjects.values()) {
			MatchResult mres = nobject.matches(ip);
			if (mres == MatchResult.ALL)
				return MatchResult.NOT;
			if (mres != MatchResult.NOT)
				mayexcluded++;
		}
		/*
		 * base items
		 */
		int mayincluded = 0;
		int all = 0;
		for (CpNetworkObject nobject: _baseObjects.values()) {
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
			return mayexcluded == 0 ? MatchResult.ALL : MatchResult.MATCH;
		}
		if (mayincluded == 0)
			return MatchResult.NOT;
		return MatchResult.MATCH;
	}
}
