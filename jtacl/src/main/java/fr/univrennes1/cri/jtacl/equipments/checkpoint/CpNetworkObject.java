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
import fr.univrennes1.cri.jtacl.lib.ip.IPNet;
import java.util.LinkedList;
import java.util.List;

/**
 * Checkpoint network object
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public abstract class CpNetworkObject {
	protected String _name;
	protected String _className;
	protected String _comment;
	protected CpNetworkType _type;
	protected List<Object> _linkedTo =
		new LinkedList<Object>();

	public CpNetworkObject(String name, String className, String comment,
			CpNetworkType type) {

		_name = name;
		_className = className;
		_comment = comment;
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

	public String getName() {
		return _name;
	}

	public String getClassName() {
		return _className;
	}

	public String getComment() {
		return _comment;
	}

	public CpNetworkType getType() {
		return _type;
	}

	/**
	 * Returns the {@link MatchResult} of the given IP address.
	 * @param ip IP address to test.
	 * @return the MatchResult of the given IP address.
	 */
	public abstract MatchResult matches(IPNet ip);
}
