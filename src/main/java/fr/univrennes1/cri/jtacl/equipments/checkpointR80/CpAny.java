/*
 * Copyright (c) 2013 - 2018, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
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
		_anyService = new CpGroupService(name, className, comment, uid);
		_anyNetwork = new CpNetworkGroup(name, className, comment, uid);
	}

    public CpGroupService getAnyService() {
        return _anyService;
    }

    public CpNetworkGroup getAnyNetwork() {
        return _anyNetwork;
    }
}
