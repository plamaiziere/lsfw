/*
 * Copyright (c) 2013 - 2021, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */
package fr.univrennes1.cri.jtacl.equipments.proxmox;

import fr.univrennes1.cri.jtacl.core.probing.MatchResult;
import fr.univrennes1.cri.jtacl.lib.ip.IPRangeable;

/**
 * Proxmox network IP
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PxNetworkIp extends PxNetworkObject {

    protected IPRangeable _ipRange;

    public PxNetworkIp(IPRangeable ipRange) {
        super(ipRange.toNetString("i::"), PxNetworkType.IPRANGE, null);
        _ipRange = ipRange;
    }

    @Override
    public MatchResult matches(IPRangeable ip) {
        if (_ipRange.contains(ip))
            return MatchResult.ALL;
        if (_ipRange.overlaps(ip))
            return MatchResult.MATCH;
        return MatchResult.NOT;
    }

    public IPRangeable getIpRange() {
        return _ipRange;
    }
}
