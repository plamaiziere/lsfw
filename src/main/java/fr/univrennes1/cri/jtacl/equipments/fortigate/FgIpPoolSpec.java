/*
 * Copyright (c) 2022, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.equipments.fortigate;

import java.util.LinkedList;
import java.util.List;

/**
 * Fortigate IP pool specification SNAT rule
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class FgIpPoolSpec {
    protected List<FgNetworkIpPool> _ippools = new LinkedList<>();

    public List<FgNetworkIpPool> getIpPools() {
        return _ippools;
    }

    public void linkTo(FgObject snatRule) {
        for (FgNetworkObject nobj : _ippools) {
            nobj.linkWith(snatRule);
        }
    }

    @Override
    public String toString() {
        return _ippools.toString();
    }

    public String toText() {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (FgNetworkIpPool nobj : _ippools) {
            if (!first) sb.append(", ");
            sb.append(nobj.getName());
            first = false;
        }
        sb.append("]");
        return sb.toString();
    }
}
