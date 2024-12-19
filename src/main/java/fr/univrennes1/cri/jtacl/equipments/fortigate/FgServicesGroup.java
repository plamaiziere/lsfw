package fr.univrennes1.cri.jtacl.equipments.fortigate;

/*
 * Copyright (c) 2013 - 2020, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

import fr.univrennes1.cri.jtacl.core.probing.MatchResult;
import fr.univrennes1.cri.jtacl.core.probing.Probe;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Fortigate group service object
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class FgServicesGroup extends FgService {

    protected String _comment;

    /* hash of services */
    protected HashMap<String, FgService> _services =
            new HashMap<>();

    /**
     * Construct a new fortigate service group
     *
     * @param name      service name
     * @param originKey fortigate origin_key
     * @param comment   comment
     */
    public FgServicesGroup(String name, String originKey, String comment) {

        super(name, originKey, FgServiceType.GROUP);
        _comment = comment;
    }

    public FgServicesGroup() {
        super(null, null, FgServiceType.GROUP);
    }

    public void addReference(String name, FgService service) {
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

    public HashMap<String, FgService> getServices() {
        return _services;
    }

    @Override
    public String toString() {
        return _name + ", " + _originKey + ", " + _comment + ", " + _type
                + ", services=" + getReferencesName();
    }

    @Override
    public FgServicesMatch matches(Probe probe) {

        FgServicesMatch servicesMatch = new FgServicesMatch();
        int mall = 0;
        int match = 0;
        int unknown = 0;

        for (FgService service : _services.values()) {
            FgServicesMatch sMatch = service.matches(probe);
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

        if (mall > 0) {
            servicesMatch.setMatchResult(MatchResult.ALL);
            servicesMatch.add(new FgServiceMatch(this, MatchResult.ALL));
            return servicesMatch;
        }
        if (match > 0) {
            servicesMatch.setMatchResult(MatchResult.MATCH);
            servicesMatch.add(new FgServiceMatch(this, MatchResult.MATCH));
            return servicesMatch;
        }

        if (unknown > 0) {
            servicesMatch.setMatchResult(MatchResult.UNKNOWN);
            servicesMatch.add(new FgServiceMatch(this, MatchResult.UNKNOWN));
            return servicesMatch;
        }

        servicesMatch.setMatchResult(MatchResult.NOT);
        servicesMatch.add(new FgServiceMatch(this, MatchResult.NOT));
        return servicesMatch;
    }
}
