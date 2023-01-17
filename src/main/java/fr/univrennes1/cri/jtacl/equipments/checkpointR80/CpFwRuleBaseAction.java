/*
 * Copyright (c) 2013 - 2018, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.equipments.checkpointR80;

/**
 * Checkpoint object rule base action (accept/drop...)
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class CpFwRuleBaseAction extends CpObject {

    protected CpFwRuleAction _action;

    public CpFwRuleBaseAction(String name, String className, String comment, String uid, CpFwRuleAction action) {
        super(name, className, comment, uid);
        _action = action;
    }

    public CpFwRuleAction getAction() { return _action; }
}
