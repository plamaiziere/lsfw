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

import java.util.LinkedList;
import java.util.List;

/**
 * Fortigate network object
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public abstract class FgNetworkObject extends FgObject {
	protected FgNetworkType _type;
	protected String _comment;
	protected String _uid;

	protected List<Object> _linkedTo =
            new LinkedList<>();

	public FgNetworkObject(String name, String originKey, String comment, String uid, FgNetworkType type) {

	    super(name, originKey);
	    this._comment = comment;
	    this._uid = uid;
		_type = type;
	}

	public List<Object> getLinkedTo() {
		return _linkedTo;
	}

	public void linkWith(Object nobj) {
		if (!_linkedTo.contains(nobj)) {
			_linkedTo.add(nobj);
		}
	}

	public FgNetworkType getType() {
		return _type;
	}

	/**
	 * Returns the {@link MatchResult} of the given IP address.
	 * @param ip IP address to test.
	 * @return the MatchResult of the given IP address.
	 */
	public abstract MatchResult matches(IPRangeable ip);
}
