/*
 * Copyright (c) 2013 - 2018, Universite de Rennes 1
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

package fr.univrennes1.cri.jtacl.equipments.checkpointR80;

import fr.univrennes1.cri.jtacl.core.probing.MatchResult;
import fr.univrennes1.cri.jtacl.lib.ip.IPRangeable;

import java.util.LinkedList;
import java.util.List;

/**
 * Checkpoint network object
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public abstract class CpNetworkObject extends CpObject {
	protected CpNetworkType _type;
	protected List<Object> _linkedTo =
            new LinkedList<>();

	public CpNetworkObject(String name, String className, String comment, String uid, CpNetworkType type) {

	    super(name, className, comment, uid);
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

	public CpNetworkType getType() {
		return _type;
	}

	/**
	 * Returns the {@link MatchResult} of the given IP address.
	 * @param ip IP address to test.
	 * @return the MatchResult of the given IP address.
	 */
	public abstract MatchResult matches(IPRangeable ip);
}
