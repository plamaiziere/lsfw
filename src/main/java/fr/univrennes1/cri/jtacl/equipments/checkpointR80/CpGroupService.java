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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Checkpoint group service object
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class CpGroupService extends CpService {

    /* hash of services */
    protected HashMap<String, CpService> _services =
            new HashMap<>();

    boolean _isAny;

    /**
     * Construct a new checkpoint service group
     *
     * @param name      service name
     * @param className class name
     * @param comment   comment
     * @param uid       object's uid
     */
    public CpGroupService(String name, String className, String comment, String uid) {

        super(name, className, comment, uid, CpServiceType.GROUP, null, false);
        if (name.equals("Any"))
            _isAny = true;
    }

    public CpGroupService() {
        super(null, null, null, null, CpServiceType.GROUP, null, false);
        _isAny = false;
    }

    public void addReference(String name, CpService service) {
        _services.put(name, service);
    }

    /**
     * returns a list of the references name included in this group.
     *
     * @return a list of the references name included in this group.
     */
    public List<String> getReferencesName() {
        List<String> list = new LinkedList<>(_services.keySet());
        Collections.sort(list);
        return list;
    }

    public HashMap<String, CpService> getServices() {
        return _services;
    }

    @Override
    public String toString() {
        return _name + ", " + _className + ", " + _comment + ", " + _type
                + ", services=" + getReferencesName();
    }

    public boolean isAny() {
        return _isAny;
    }

    @Override
    public CpServicesMatch matches(ProbeRequest request) {

        CpServicesMatch servicesMatch = new CpServicesMatch();
        int mall = 0;
        int match = 0;
        int unknown = 0;

        for (CpService service : _services.values()) {
            CpServicesMatch sMatch = service.matches(request);
            MatchResult mres = sMatch.getMatchResult();
            if (mres == MatchResult.NOT)
                continue;
            servicesMatch.addAll(sMatch);
            if (mres == MatchResult.ALL)
                mall++;
            if (mres == MatchResult.MATCH)
                match++;
            if (mres == MatchResult.UNKNOWN)
                unknown++;
        }

        if (_isAny)
            mall++;

        if (mall > 0) {
            servicesMatch.setMatchResult(MatchResult.ALL);
            servicesMatch.add(new CpServiceMatch(this, MatchResult.ALL));
            return servicesMatch;
        }
        if (match > 0) {
            servicesMatch.setMatchResult(MatchResult.MATCH);
            servicesMatch.add(new CpServiceMatch(this, MatchResult.MATCH));
            return servicesMatch;
        }

        if (unknown > 0) {
            servicesMatch.setMatchResult(MatchResult.UNKNOWN);
            servicesMatch.add(new CpServiceMatch(this, MatchResult.UNKNOWN));
            return servicesMatch;
        }

        servicesMatch.setMatchResult(MatchResult.NOT);
        servicesMatch.add(new CpServiceMatch(this, MatchResult.NOT));
        return servicesMatch;
    }

}
