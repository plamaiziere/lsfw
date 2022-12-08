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

/**
 * Checkpoint network object left unhandled by lsfw
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class CpUnhandledNetwork extends CpNetworkObject {

	/**
	 * Construct a new unhandled service
	 * @param name service name
	 * @param className class name
	 * @param comment comment
	 */
	public CpUnhandledNetwork(String name, String className, String comment, String uid) {

		super(name, className, comment, uid, CpNetworkType.UNHANDLED);
	}

	@Override
	public String toString() {
		return _name + ", " + _className + ", " + _comment + ", " +  _type;
	}

	@Override
	public MatchResult matches(IPRangeable ip) {
		return MatchResult.UNKNOWN;
	}

}
