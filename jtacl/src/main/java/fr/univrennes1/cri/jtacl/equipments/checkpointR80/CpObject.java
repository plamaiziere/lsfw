/*
 * Copyright (c) 2018, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the ESUP-Portail license as published by the
 * ESUP-Portail consortium.
 *
 * Alternatively, this software may be distributed under the terms of BSD
 * license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.equipments.checkpointR80;

/**
 * Checkpoint base object
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class CpObject {
    protected String _name;
    protected String _className;
    protected String _comment;
    protected String _uid;

    public CpObject(String name, String className, String comment, String uid) {
        this._name = name;
        this._className = className;
        this._comment = comment;
        this._uid = uid;
    }

    public String getName() {
        return _name;
    }

    public String getClassName() {
        return _className;
    }

    public String getUid() {
        return _uid;
    }

    public String getComment() { return _comment; }
}
