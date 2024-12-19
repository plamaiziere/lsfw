/*
 * Copyright (c) 2013, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.equipments.checkpoint;

import fr.univrennes1.cri.jtacl.core.probing.MatchResult;
import fr.univrennes1.cri.jtacl.core.probing.ProbeRequest;

import java.util.LinkedList;
import java.util.List;

/**
 * Checkpoint service object
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public abstract class CpService {
    protected String _name;
    protected String _className;
    protected String _comment;
    protected CpServiceType _type;
    protected String _protocolTypeName;
    protected List<Object> _linkedTo = new LinkedList<>();

    public CpService(String name, String className, String comment,
                     CpServiceType type, String protocolTypeName) {

        _name = name;
        _className = className;
        _comment = comment;
        _type = type;
        _protocolTypeName = protocolTypeName;
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

    /**
     * Returns the {@link CpServicesMatch} of the given {@link ProbeRequest}.
     *
     * @param request request to test.
     * @return the CpServicesMatch of the given ProbeRequest.
     */
    public abstract CpServicesMatch matches(ProbeRequest request);

}
