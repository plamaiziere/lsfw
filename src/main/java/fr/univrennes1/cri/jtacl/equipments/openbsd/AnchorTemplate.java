/*
 * Copyright (c) 2010, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.equipments.openbsd;

/**
 * Template to build anchor rule. This class is used at parsing time
 * as an intermediate storage.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 * @see PfAnchorRule
 */
public class AnchorTemplate {

    private String _name;
    private RuleTemplate _rule;
    private boolean _inlined;

    public boolean isInlined() {
        return _inlined;
    }

    public boolean setInlined(boolean inlined) {
        _inlined = inlined;
        return true;
    }

    public String getName() {
        return _name;
    }

    public boolean setName(String name) {
        _name = name;
        return true;
    }

    public RuleTemplate getRule() {
        return _rule;
    }

    public boolean setRule(RuleTemplate rule) {
        _rule = rule;
        return true;
    }


}
