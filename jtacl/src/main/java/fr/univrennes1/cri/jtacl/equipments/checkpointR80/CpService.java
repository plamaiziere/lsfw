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

import java.util.LinkedList;
import java.util.List;

/**
 * Checkpoint service object
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public abstract class CpService extends CpObject {
	protected CpServiceType _type;
	protected String _protocolTypeName;
    /* included in "Any" service */
    protected boolean _inAny;
	protected List<Object> _linkedTo = new LinkedList<>();

	public CpService(String name, String className, String comment, String uid,
                     CpServiceType type, String protocolTypeName, boolean inAny) {

		super(name, className, comment, uid);
		_type = type;
		_protocolTypeName = protocolTypeName;
		_inAny = inAny;
	}

	public List<Object> getLinkedTo() {
		return _linkedTo;
	}

	public void linkWith(Object obj) {
		if (!_linkedTo.contains(obj)) {
			_linkedTo.add(obj);
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

	public CpServiceType getType() {
		return _type;
	}

	public String getProtocolTypeName() {
		return _protocolTypeName;
	}

	public boolean hasProtocolType() {
		return _protocolTypeName != null;
	}

    public boolean isInAny() {
        return _inAny;
    }

	/**
	 * Returns the {@link CpServicesMatch} of the given {@link ProbeRequest}.
	 * @param request request to test.
	 * @return the CpServicesMatch of the given ProbeRequest.
	 */
	public abstract CpServicesMatch matches(ProbeRequest request);

}
