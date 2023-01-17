/*
 * Copyright (c) 2013 - 2020, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.equipments.fortigate;

import fr.univrennes1.cri.jtacl.core.probing.Probe;
import fr.univrennes1.cri.jtacl.core.probing.ProbeRequest;

import java.util.LinkedList;
import java.util.List;

public abstract class FgService extends FgObject {

    FgServiceType _type;
    protected List<Object> _linkedTo = new LinkedList<>();

    public FgService(String name, String originKey, FgServiceType type) {
        super(name, originKey);
        _type = type;
    }

	public List<Object> getLinkedTo() {
		return _linkedTo;
	}

	public void linkWith(Object obj) {
		if (!_linkedTo.contains(obj)) {
			_linkedTo.add(obj);
		}
	}

	public FgServiceType getType() {
		return _type;
	}

    /**
	 * Returns the {@link FgServicesMatch} of the given {@link ProbeRequest}.
	 * @param probe the probe to test.
	 * @return the FgServicesMatch of the given ProbeRequest.
	 */
	public abstract FgServicesMatch matches(Probe probe);

}
