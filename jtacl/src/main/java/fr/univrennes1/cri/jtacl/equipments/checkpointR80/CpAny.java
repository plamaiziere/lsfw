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
 * Checkpoint "Any" object
 * This object can be use in ip specification or service specification.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class CpAny extends CpObject {

    protected CpGroupService _anyService;
    protected CpNetworkGroup _anyNetwork;

	public CpAny(String name, String className, String comment, String uid) {

		super(name, className, comment, uid);
		_anyService = new CpGroupService(name, comment, uid);
		_anyNetwork = new CpNetworkGroup(name, className, comment, uid);
	}

    public CpGroupService getAnyService() {
        return _anyService;
    }

    public CpNetworkGroup getAnyNetwork() {
        return _anyNetwork;
    }

    /**
	 * Returns the {@link CpServicesMatch} of the given {@link ProbeRequest}.
	 * @param request request to test.
	 * @return the CpServicesMatch of the given ProbeRequest.
	 */
	// public CpServicesMatch matches(ProbeRequest request);

}
