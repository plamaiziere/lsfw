/*
 * Copyright (c) 2010, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.equipments.cisco.pix;

import fr.univrennes1.cri.jtacl.core.probing.MatchResult;
import fr.univrennes1.cri.jtacl.lib.ip.IPNet;
import fr.univrennes1.cri.jtacl.lib.ip.IPRangeable;
import fr.univrennes1.cri.jtacl.lib.misc.ParseContext;

/**
 * An item of a network object group.
 * <p>
 * An item can describe a group or an IP address.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class NetworkObjectGroupItem extends ObjectGroupItem {

    /**
     * IP address if this item is not a group, null otherwise
     */
    protected IPNet _ipAddress;

    /**
     * Returns the IP address. Valid only isGroup() returns false.
     *
     * @return the IP address. Valid only isGroup() returns false.
     */
    public IPNet getIpAddress() {
        return _ipAddress;
    }

    /**
     * Constructs a new network object item of type "IP address".
     */
    public NetworkObjectGroupItem(ObjectGroup owner, ParseContext parseContext,
                                  IPNet ipAddress) {
        _owner = owner;
        _parseContext = parseContext;
        _ipAddress = ipAddress;
    }

    /**
     * Constructs a new network object item of type "group".
     */
    public NetworkObjectGroupItem(ObjectGroup owner, ParseContext parseContext,
                                  ObjectGroup group) {
        _owner = owner;
        _parseContext = parseContext;
        _group = group;
    }

    /**
     * Checks if this item matches the IP range addresses in argument.
     *
     * @param ip IP range addresses to check.
     * @return a {@link MatchResult} according to the test.
     */
    public MatchResult matches(IPRangeable ip) {
        if (_ipAddress.contains(ip))
            return MatchResult.ALL;
        if (_ipAddress.overlaps(ip))
            return MatchResult.MATCH;
        return MatchResult.NOT;
    }

}
