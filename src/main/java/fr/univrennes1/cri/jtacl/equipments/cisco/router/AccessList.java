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

import java.util.ArrayList;

/**
 * An Access List is a collection of access list elements (ACE).
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class AccessList extends ArrayList<AccessListElement> {

    /**
     * number of this acl (numbered access-list).
     */
    private Integer _number;
    /**
     * name of this acl (named access-list).
     * For numbered acl, name == number.
     */
    private String _name;

    /**
     * type of this acl.
     */
    private AclType _type;

    /**
     * IP version of this acl.
     */
    private IPversion _ipVersion;

    /**
     * Returns the number of this acl.<br/>
     * Null if this acl is a named acl
     *
     * @return the number of this acl.
     * Null if this acl is a named acl.
     */
    public Integer getNumber() {
        return _number;
    }

    /**
     * Set the number of this acl.
     *
     * @param number number to set.
     */
    public void setNumber(Integer number) {
        _number = number;
    }

    /**
     * Returns the name of this acl.<br/>
     * If the acl is a numbered acl, name is equal to the number.
     *
     * @return the name of this acl.
     */
    public String getName() {
        return _name;
    }

    /**
     * Sets the name of this acl.
     *
     * @param name the name to set.
     */
    public void setName(String name) {
        _name = name;
    }

    /**
     * Sets the type of this acl.
     *
     * @param type type to set.
     */
    public void setAclType(AclType type) {
        _type = type;
    }

    /**
     * Returns the type of this acl.
     *
     * @return the type of this acl.
     */
    public AclType getAclType() {
        return _type;
    }

    /**
     * Returns the IP version of this acl.
     *
     * @return the IP version of this acl.
     */
    public IPversion getIpVersion() {
        return _ipVersion;
    }

    /**
     * Sets the IP version of this acl.
     *
     * @param ipVersion IP version to set.
     */
    public void setIpVersion(IPversion ipVersion) {
        _ipVersion = ipVersion;
    }

}
