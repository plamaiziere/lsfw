/*
 * Copyright (c) 2013 - 2018, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.equipments.checkpointR80;

import fr.univrennes1.cri.jtacl.core.probing.MatchResult;
import fr.univrennes1.cri.jtacl.core.probing.ProbeRequest;
import fr.univrennes1.cri.jtacl.lib.ip.ProtocolsSpec;

/**
 * Checkpoint other service object
 * Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class CpOtherService extends CpService {

    /* port */
    protected Integer _protocol;

    /* expression */
    protected String _exp;

    /* included in "Any" service */
    protected boolean _inAny;

    /**
     * Construct a new checkpoint other service
     *
     * @param name     service name
     * @param comment  comment
     * @param uid      Object's uid
     * @param protocol protocol
     * @param exp      expression
     * @param inAny    true if this service is included in any
     */
    public CpOtherService(String name,
                          String comment,
                          String uid,
                          Integer protocol,
                          String exp,
                          String protocolTypeName,
                          boolean inAny) {

        super(name, "service-other", comment, uid, CpServiceType.OTHER,
                protocolTypeName, inAny);
        _protocol = protocol;
        _exp = exp;
    }

    public Integer getProtocol() {
        return _protocol;
    }

    public String getExp() {
        return _exp;
    }

    @Override
    public String toString() {
        return _name + ", " + _className + ", " + _comment + ", " + _type
                + ", protocol=" + _protocol + ", exp=" + _exp
                + ", protocolType= " + _protocolTypeName
                + ", inAny=" + _inAny;
    }

    @Override
    public CpServicesMatch matches(ProbeRequest request) {

        ProtocolsSpec reqProto = request.getProtocols();
        CpServicesMatch servicesMatch = new CpServicesMatch();

        /*
         * protocol
         */
        if (!reqProto.contains(_protocol)) {
            servicesMatch.setMatchResult(MatchResult.NOT);
            return servicesMatch;
        }

        /*
         * we don't handle the "exp" expression
         */
        if (_exp == null) {
            servicesMatch.setMatchResult(MatchResult.ALL);
            servicesMatch.add(new CpServiceMatch(this, MatchResult.ALL));
            return servicesMatch;
        }

        servicesMatch.setMatchResult(MatchResult.UNKNOWN);
        servicesMatch.add(new CpServiceMatch(this, MatchResult.UNKNOWN));
        return servicesMatch;
    }

}
