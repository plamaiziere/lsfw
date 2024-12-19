/*
 * Copyright (c) 2013, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.lib.ip;

import fr.univrennes1.cri.jtacl.core.probing.MatchResult;

import java.util.ArrayList;

/**
 * Protocols specification : a list of protocol number.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class ProtocolsSpec extends ArrayList<Integer> {

    /**
     * Returns true if a given list of protocols matches this specification.
     * IP matches any protocol.
     *
     * @param protocols protocols list to check.
     * @return true if a given list of protocols matches this specification.
     */
    public MatchResult matches(ProtocolsSpec protocols) {

        for (Integer proto : protocols) {
            if (this.contains(proto))
                return MatchResult.ALL;
        }
        return MatchResult.NOT;
    }

    /**
     * Returns true if a given protocol matches this specification.
     * IP matches any protocol.
     *
     * @param protocol protocol to check.
     * @return true if a given protocol matches this specification.
     */
    public MatchResult matches(Integer protocol) {

        if (this.contains(protocol))
            return MatchResult.ALL;

        return MatchResult.NOT;
    }

}
