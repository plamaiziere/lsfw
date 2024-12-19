/*
 * Copyright (c) 2012, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.equipments.openbsd;

/**
 * PF tagged option.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PfTaggedOpt {

    private boolean _not;
    private String _tag;

    public PfTaggedOpt(boolean not, String tag) {
        _not = not;
        _tag = tag;
    }

    public String getTag() {
        return _tag;
    }

    public boolean isNot() {
        return _not;
    }

    @Override
    public String toString() {
        String s = "";
        if (_not)
            s += "! ";
        s += _tag;
        return s;
    }
}
