/*
 * Copyright (c) 2010, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.equipments.cisco.pix;

import fr.univrennes1.cri.jtacl.lib.ip.ProtocolsSpec;

/**
 * Describes a protocol object group.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class ProtocolObjectGroup extends ObjectGroup {

    /**
     * Constructs a new {@link ProtocolObjectGroup} group with the group Id
     * in argument.
     *
     * @param groupId the group Id of this group.
     */
    public ProtocolObjectGroup(String groupId) {
        super(ObjectGroupType.PROTOCOL, groupId);
    }

    /**
     * Checks if at least one item of this group matches the protocols
     * in argument.
     *
     * @param protocols protocols value to check.
     * @return true if an item of this group matches the protocol in argument.
     */
    public boolean matches(ProtocolsSpec protocols) {
        for (ObjectGroupItem item : this) {
            ProtocolObjectGroupItem pitem = (ProtocolObjectGroupItem) item;
            if (pitem.isGroup())
                continue;
            if (pitem.matches(protocols))
                return true;
        }
        return false;
    }

    /**
     * Returns an expanded group. Nested object groups
     * are expended recursively.
     *
     * @return a group with nested object groups expanded
     * recursively.
     */
    public ProtocolObjectGroup expand() {
        ProtocolObjectGroup group = new ProtocolObjectGroup(_groupId);
        group.setDescription(_description);

        for (ObjectGroupItem obj : this) {
            if (obj.isGroup())
                group.addAll(((ProtocolObjectGroup) obj.getGroup()).expand());
            else
                group.add(obj);
        }
        return group;
    }

    /**
     * Returns the protocols contained in this group.
     *
     * @return the protocols contained in this group.
     */
    public ProtocolsSpec getProtocols() {
        ProtocolsSpec protoSpec = new ProtocolsSpec();

        for (ObjectGroupItem obj : this) {
            if (!obj.isGroup()) {
                ProtocolObjectGroupItem item = (ProtocolObjectGroupItem) obj;
                protoSpec.add(item.getProtocol());
            }
        }
        return protoSpec;
    }
}
