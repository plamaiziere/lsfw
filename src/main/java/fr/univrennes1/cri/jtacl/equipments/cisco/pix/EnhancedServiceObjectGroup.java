/*
 * Copyright (c) 2010, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.equipments.cisco.pix;

import fr.univrennes1.cri.jtacl.core.probing.MatchResult;
import fr.univrennes1.cri.jtacl.lib.ip.PortSpec;
import fr.univrennes1.cri.jtacl.lib.ip.ProtocolsSpec;

/**
 * Describes an enhanced service object group.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class EnhancedServiceObjectGroup extends ObjectGroup {

    /**
     * Constructs a new {@link EnhancedServiceObjectGroup} group with the group
     * Id in argument.
     *
     * @param groupId the group Id of this group.
     */
    public EnhancedServiceObjectGroup(String groupId) {
        super(ObjectGroupType.ENHANCED, groupId);
    }

    /**
     * Checks if this at least one item of this group matches the protocols
     * in argument.
     *
     * @param protocols protocols value to check.
     * @return true if at least one item matches the protocols value in argument.
     */
    public boolean matches(ProtocolsSpec protocols) {
        for (ObjectGroupItem item : this) {
            EnhancedServiceObjectGroupItem sitem =
                    (EnhancedServiceObjectGroupItem) item;
            if (sitem.isGroup())
                continue;
            if (sitem.matches(protocols))
                return true;
        }
        return false;
    }

    /**
     * Checks if at least one item of this group matches the service
     * in argument and if this group matches the protocols in argument.
     *
     * @param protocols protocols value to check.
     * @param port      {@link PortSpec} port spec value to check.
     * @return a {@link MatchResult} between this group and the port spec in
     * argument.
     */
    public MatchResult matches(ProtocolsSpec protocols, PortSpec port) {
        int match = 0;
        int all = 0;

        for (ObjectGroupItem item : this) {
            EnhancedServiceObjectGroupItem sitem =
                    (EnhancedServiceObjectGroupItem) item;
            if (sitem.isGroup())
                continue;
            MatchResult res = sitem.matches(protocols, port);
            if (res == MatchResult.ALL)
                all++;
            if (res == MatchResult.MATCH)
                match++;
        }
        if (all > 0)
            return MatchResult.ALL;
        if (match > 0)
            return MatchResult.MATCH;

        return MatchResult.NOT;
    }

    /**
     * Returns an expanded group. Nested object groups
     * are expended recursively.
     *
     * @return a group with nested object groups expanded
     * recursively.
     */
    public EnhancedServiceObjectGroup expand() {
        EnhancedServiceObjectGroup group = new EnhancedServiceObjectGroup(_groupId);
        group.setDescription(_description);

        for (ObjectGroupItem obj : this) {
            if (obj.isGroup())
                group.addAll(((EnhancedServiceObjectGroup) obj.getGroup()).expand());
            else
                group.add(obj);
        }
        return group;
    }

}
