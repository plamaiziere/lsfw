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
import fr.univrennes1.cri.jtacl.lib.ip.ProtocolsSpec;
import fr.univrennes1.cri.jtacl.lib.misc.ParseContext;

/**
 * An item of a protocol object group.
 * <p>
 * An item can be a group or a protocol number.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class ProtocolObjectGroupItem extends ObjectGroupItem {

    /**
     * protocol if this item is not a group, -1 otherwise
     */
    protected int _protocol = -1;

    /**
     * Returns the int value of the protocol.. Valid only if isGroup() returns false.
     *
     * @return the int value of the protocol. Valid only isGroup() returns false.
     */
    public int getProtocol() {
        return _protocol;
    }

    /**
     * Constructs a new protocol object item of type "protocol".
     */
    public ProtocolObjectGroupItem(ObjectGroup owner, ParseContext parseContext,
                                   int protocol) {
        _owner = owner;
        _parseContext = parseContext;
        _protocol = protocol;
    }

    /**
     * Constructs a new protocol object item of type "group".
     */
    public ProtocolObjectGroupItem(ObjectGroup owner, ParseContext parseContext,
                                   ObjectGroup group) {
        _owner = owner;
        _parseContext = parseContext;
        _group = group;
        _protocol = -1;
    }

    /**
     * Checks if this item matches the protocols in argument.
     *
     * @param protocols protocols value to check.
     * @return true if this item matches any of the protocols value in argument.
     */
    public boolean matches(ProtocolsSpec protocols) {
        return protocols.matches(_protocol) == MatchResult.ALL;
    }

}
