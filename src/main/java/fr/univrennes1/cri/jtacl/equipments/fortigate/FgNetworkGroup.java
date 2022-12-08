/*
 * Copyright (c) 2013 - 2020, Universite de Rennes 1
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

package fr.univrennes1.cri.jtacl.equipments.fortigate;

import fr.univrennes1.cri.jtacl.core.probing.MatchResult;
import fr.univrennes1.cri.jtacl.lib.ip.IPRangeable;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Fortigate group network object
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class FgNetworkGroup extends FgNetworkObject {

	/* hash of base objects */
	protected HashMap<String, FgNetworkObject> _baseObjects =
            new HashMap<>();

	/* hash of excluded objects */
	protected HashMap<String, FgNetworkObject> _excludedObjects =
            new HashMap<>();


	/**
	 * Construct a new Fortigate network group object
	 * @param name object name name
	 * @param originKey Fortigate orgin key
	 * @param comment comment
	 */
	public FgNetworkGroup(String name, String originKey, String comment, String uid) {

		super(name, originKey, comment, uid, FgNetworkType.GROUP);
	}

	public void addBaseReference(String name, FgNetworkObject networkObject) {
		_baseObjects.put(name, networkObject);
	}

	public void addExcludedReference(String name, FgNetworkObject networkObject) {
		_excludedObjects.put(name, networkObject);
	}

	/**
	 * returns a list of the base references name included in this group.
	 * @return a list of the base references name included in this group.
	 */
	public List<String> getBaseReferencesName() {
		List<String> list = new LinkedList<>(_baseObjects.keySet());
		Collections.sort(list);
		return list;
	}

	/**
	 * returns a list of the excluded references name included in this group.
	 * @return a list of the excluded references name included in this group.
	 */
	public List<String> getExcludedReferencesName() {
		List<String> list = new LinkedList<>(_excludedObjects.keySet());
		Collections.sort(list);
		return list;
	}

	public HashMap<String, FgNetworkObject> getBaseObjects() {
		return _baseObjects;
	}

	public HashMap<String, FgNetworkObject> getExcludedObjects() {
		return _excludedObjects;
	}

	@Override
	public String toString() {
		return _name + ", " + _originKey + ", " + _comment + ", " +  _type
				+ ", base=" + getBaseReferencesName() + ", excluded="
				+ getExcludedReferencesName();
	}

	@Override
	public MatchResult matches(IPRangeable ip) {

    	/*
		 * excluded items
		 */
		int mayexcluded = 0;
		for (FgNetworkObject nobject: _excludedObjects.values()) {
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
		for (FgNetworkObject nobject: _baseObjects.values()) {
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

