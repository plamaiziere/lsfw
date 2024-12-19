/*
 * Copyright (c) 2010, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.equipments.cisco.router;

import fr.univrennes1.cri.jtacl.lib.ip.IPversion;

/**
 * Template to build Access List. This class is used at parsing time as an
 * intermediate storage.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 * @see AccessList
 */
public class AclTemplate {
    private Integer _number;
    private String _name;
    private AclType _type;
    private IPversion _ipVersion;

    public Integer getNumber() {
        return _number;
    }

    public boolean setNumber(Integer number) {
        _number = number;
        return true;
    }

    public String getName() {
        return _name;
    }

    public boolean setName(String name) {
        _name = name;
        return true;
    }

    public boolean setAclType(AclType type) {
        _type = type;
        return true;
    }

    public AclType getAclType() {
        return _type;
    }

    public IPversion getIpVersion() {
        return _ipVersion;
    }

    public boolean setIpVersion(IPversion ipVersion) {
        _ipVersion = ipVersion;
        return true;
    }

}
