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
import fr.univrennes1.cri.jtacl.lib.misc.ParseContext;

/**
 * An item of an enhanced service object group.
 * <p>
 * An item can be a group or a service-object.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class EnhancedServiceObjectGroupItem extends ObjectGroupItem {

    /**
     * ServiceObject if this item is a service object, null otherwise
     */
    protected ServiceObject _serviceObject;

    /**
     * Returns the Service Object. Valid only isGroup() returns false.
     *
     * @return the Service Object. Valid only isGroup() returns false.
     */
    public ServiceObject getServiceObject() {
        return _serviceObject;
    }

    /**
     * Constructs a new service object item of type service object.
     */
    public EnhancedServiceObjectGroupItem(ObjectGroup owner,
                                          ParseContext parseContext,
                                          ServiceObject serviceObject) {
        _owner = owner;
        _parseContext = parseContext;
        _serviceObject = serviceObject;
    }

    /**
     * Constructs a new service object item of type "group".
     */
    public EnhancedServiceObjectGroupItem(ObjectGroup owner,
                                          ParseContext parseContext,
                                          ObjectGroup group) {
        _owner = owner;
        _parseContext = parseContext;
        _group = group;
    }

    /**
     * Checks if this item matches the protocols in argument.
     *
     * @param protocols protocols value to check.
     * @return true if this item matches any of the protocols value in argument.
     */
    public boolean matches(ProtocolsSpec protocols) {
        if (protocols.matches(_serviceObject.getProtocols()) == MatchResult.ALL)
            return true;

        return false;
    }


    /**
     * Checks if this item matches the protocols and service in argument.
     *
     * @param protocols protocols value to check.
     * @param port      {@link PortSpec} port spec value to check.
     * @return a {@link MatchResult} between this group and the port spec in
     * argument.
     */
    public MatchResult matches(ProtocolsSpec protocols, PortSpec port) {
        PortObject pobject = _serviceObject.getPortObject();

        if (!matches(protocols))
            return MatchResult.NOT;
        if (pobject != null)
            return (pobject.matches(port));
        return MatchResult.NOT;
    }

}
