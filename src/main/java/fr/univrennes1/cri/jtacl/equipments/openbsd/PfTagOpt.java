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
 * PF tag option.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PfTagOpt {

    private String _tag;

    public PfTagOpt(String tag) {
        _tag = tag;
    }

    public String getTag() {
        return _tag;
    }

    @Override
    public String toString() {
        return _tag;
    }
}
