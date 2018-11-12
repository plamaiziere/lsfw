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

import fr.univrennes1.cri.jtacl.core.probing.ProbeRequest;

/**
 * Checkpoint layer object
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public abstract class CpLayer extends CpObject {

	public CpLayer(String name, String className, String comment, String uid) {

		super(name, className, comment, uid);
	}

	/**
	 * Returns the {@link CpServicesMatch} of the given {@link ProbeRequest}.
	 * @param request request to test.
	 * @return the CpServicesMatch of the given ProbeRequest.
	 */
	public abstract CpServicesMatch matches(ProbeRequest request);

}
