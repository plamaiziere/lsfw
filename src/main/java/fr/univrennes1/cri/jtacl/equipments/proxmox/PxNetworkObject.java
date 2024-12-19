/*
 * Copyright (c) 2013 - 2020, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */
package fr.univrennes1.cri.jtacl.equipments.proxmox;

import fr.univrennes1.cri.jtacl.core.probing.MatchResult;
import fr.univrennes1.cri.jtacl.lib.ip.IPRangeable;
import fr.univrennes1.cri.jtacl.lib.misc.ParseContext;

/**
 * Proxmox network object
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public abstract class PxNetworkObject extends PxObject {

    protected String _name;
    protected PxNetworkType _type;

    public PxNetworkObject(String name, PxNetworkType type, ParseContext context) {
        super(context);
        _name = name;
        _type = type;
    }

    public String getName() {
        return _name;
    }

    public PxNetworkType getType() {
        return _type;
    }

    /**
     * Returns the {@link MatchResult} of the given IP address.
     *
     * @param ip IP address to test.
     * @return the MatchResult of the given IP address.
     */
    public abstract MatchResult matches(IPRangeable ip);
}
