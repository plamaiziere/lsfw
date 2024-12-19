/*
 * Copyright (c) 2023, Universite de Rennes
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.equipments.fortigate;

import fr.univrennes1.cri.jtacl.core.probing.MatchResult;
import fr.univrennes1.cri.jtacl.lib.ip.IPNet;
import fr.univrennes1.cri.jtacl.lib.ip.IPRangeable;
import fr.univrennes1.cri.jtacl.lib.ip.IPversion;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Fortigate FQDN network object
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class FgNetworkFQDN extends FgNetworkObject {

    protected String _fqdn;

    /**
     * Construct a new Fortigate network FQDN object
     *
     * @param name      object name
     * @param originKey Fortigate orgin key
     * @param comment   comment
     * @param uid       object uuid
     * @param fqdn      FQDN
     */
    public FgNetworkFQDN(String name, String originKey, String comment, String uid, String fqdn) {

        super(name, originKey, comment, uid, FgNetworkType.FQDN);
        this._fqdn = fqdn;
    }

    public String getFqdn() {
        return _fqdn;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String s = _name + ", " + _originKey + ", " + _comment + ", " + _type + ", " + _fqdn + ", [";
        sb.append(s);
        sb.append(IPRangeable.formatCollection(resolveFQDN(_fqdn), "::i"));
        sb.append("]");
        return sb.toString();
    }

    @Override
    public MatchResult matches(IPRangeable ip) {
        int mAll = 0;
        int mMay = 0;
        for (IPRangeable i : resolveFQDN(_fqdn)) {
            if (ip.sameIPVersion(i)) {
                if (i.contains(ip))
                    mAll++;
                if (i.overlaps(ip))
                    mMay++;
            }
        }
        if (mAll > 0) return MatchResult.ALL;
        if (mMay > 0) return MatchResult.MATCH;
        return MatchResult.NOT;
    }

    public static List<IPRangeable> resolveFQDN(String fqdn) {
        List<IPRangeable> ip4 = new ArrayList<>();
        List<IPRangeable> ip6 = new ArrayList<>();

        try {
            ip4.addAll(IPNet.getAllByName(fqdn, IPversion.IPV4));
        } catch (UnknownHostException e) {
            // nothing
        }
        try {
            ip6.addAll(IPNet.getAllByName(fqdn, IPversion.IPV6));
        } catch (UnknownHostException e) {
            // nothing
        }
        var l = new ArrayList<>(ip4);
        l.addAll(ip6);
        return l;
    }
}
