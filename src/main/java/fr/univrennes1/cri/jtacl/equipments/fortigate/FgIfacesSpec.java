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

public class FgIfacesSpec extends LinkedList<String> {
    protected boolean _negate = false;

    public boolean isAny() {
        return contains("any");
    }

    public FgIfacesSpec() {
        super();
    }

    public FgIfacesSpec(boolean negate) {
        super();
        this._negate = negate;
    }

    public boolean hasNegate() {
        return _negate;
    }

    @Override
    public String toString() {
        return _negate ? "!" + super.toString() : super.toString();
    }
}
