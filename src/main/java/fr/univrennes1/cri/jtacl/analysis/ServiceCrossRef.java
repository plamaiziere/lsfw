/*
 * Copyright (c) 2013, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.analysis;

import fr.univrennes1.cri.jtacl.lib.ip.PortRange;

import java.util.ArrayList;
import java.util.List;

/**
 * Cross reference of service (port).
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class ServiceCrossRef {
    protected PortRange _portRange;

    protected List<ServiceCrossRefContext> _contexts;

    public ServiceCrossRef(PortRange portRange) {
        _portRange = portRange;
        _contexts = new ArrayList<>();
    }

    public List<ServiceCrossRefContext> getContexts() {
        return _contexts;
    }

    public void addContext(ServiceCrossRefContext context) {
        _contexts.add(context);
    }

    public PortRange getPortRange() {
        return _portRange;
    }

    @Override
    public String toString() {
        return "ServiceCrossRef [_portRange=" + _portRange + ", _contexts=" + _contexts + "]";
    }

}
