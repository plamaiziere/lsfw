/*
 * Copyright (c) 2012, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.core.probing;

import fr.univrennes1.cri.jtacl.core.exceptions.JtaclInternalException;

import java.util.Arrays;
import java.util.List;

/**
 * The expected result of a probing.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class ExpectedProbing {

    protected boolean _not;
    static protected final List<String> _expectStrings = Arrays.asList(
            "ROUTED", "NONE-ROUTED", "UNKNOWN", "ACCEPT", "DENY", "MAY",
            "UNACCEPTED");

    protected String _expect;

    public ExpectedProbing(boolean not, String expect) {
        expect = expect.toUpperCase();

        if (!_expectStrings.contains(expect))
            throw new JtaclInternalException("invalid expect probing");
        _not = not;
        _expect = expect;
    }

    public boolean isNot() {
        return _not;
    }

    public boolean isRouted() {
        return _expect.equals("ROUTED");
    }

    public boolean isNoneRouted() {
        return _expect.equals("NONE-ROUTED");
    }

    public boolean isUnknown() {
        return _expect.equals("UNKNOWN");
    }

    public boolean isAccept() {
        return _expect.equals("ACCEPT");
    }

    public boolean isDeny() {
        return _expect.equals("DENY");
    }

    public boolean isMay() {
        return _expect.equals("MAY");
    }

    public boolean isUnaccepted() {
        return _expect.equals("UNACCEPTED");
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + (this._not ? 1 : 0);
        hash = 11 * hash + (this._expect != null ? this._expect.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ExpectedProbing other = (ExpectedProbing) obj;
        if (this._not != other._not) {
            return false;
        }
        if ((this._expect == null) ? (other._expect != null) :
                !this._expect.equals(other._expect)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ExpectedProbing{" + "_not=" + _not + ", _expect=" + _expect + '}';
    }

}
