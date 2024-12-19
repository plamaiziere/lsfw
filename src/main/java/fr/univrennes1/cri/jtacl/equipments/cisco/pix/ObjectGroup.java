/*
 * Copyright (c) 2010, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.equipments.cisco.pix;

import java.util.ArrayList;

/**
 * Ancestor of object group.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class ObjectGroup extends ArrayList<ObjectGroupItem> {

    protected String _groupId;
    protected String _description;
    protected ObjectGroupType _type;
    protected int _refCount;

    /**
     * Constructs a new {@link ObjectGroup} group with the group Id
     * in argument.
     *
     * @param groupId the group Id of this group.
     */
    public ObjectGroup(ObjectGroupType type, String groupId) {
        super();
        _type = type;
        _groupId = groupId;
    }

    /**
     * Returns the group id of this group.
     *
     * @return the group id of this group.
     */
    public String getGroupId() {
        return _groupId;
    }

    /**
     * Returns the type of this group.
     *
     * @return the type of this group.
     */
    public ObjectGroupType getType() {
        return _type;
    }

    /**
     * Returns the description of this group.
     *
     * @return the description of this group.
     */
    public String getDescription() {
        return _description;
    }

    /**
     * Sets the description of this group.
     *
     * @param description description to set.
     */
    public void setDescription(String description) {
        _description = description;
    }

    public void incRefCount() {
        _refCount++;
    }

    public boolean isUsed() {
        return _refCount > 0;
    }

    public int getRefCount() {
        return _refCount;
    }

}
