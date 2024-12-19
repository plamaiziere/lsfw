/*
 * Copyright (c) 2013 - 2021, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */
package fr.univrennes1.cri.jtacl.equipments.proxmox;

import java.util.List;

/**
 * Template to build alias. This class is used at parsing time
 * as an intermediate storage.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 * @see PxVeParser
 */
public class PxAliasTemplate {
    protected String _name;
    protected List<String> _ipspec;

    public String getName() {
        return _name;
    }

    public boolean setName(String _name) {
        this._name = _name;
        return true;
    }

    public List<String> getIpSpec() {
        return _ipspec;
    }

    public boolean setIpSpec(List<String> _ipspec) {
        this._ipspec = _ipspec;
        return true;
    }
}
