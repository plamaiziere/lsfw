/*
 * Copyright (c) 2013 - 2020, Universite de Rennes 1
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
package fr.univrennes1.cri.jtacl.equipments.fortigate;

import fr.univrennes1.cri.jtacl.lib.ip.PortSpec;

public class FgPortsSpec {
    protected PortSpec _sourcePorts;
    protected PortSpec _destPorts;

    public FgPortsSpec(PortSpec sourcePorts, PortSpec destPorts) {
        _sourcePorts = sourcePorts;
        _destPorts = destPorts;
    }

    public PortSpec getSourcePorts() { return _sourcePorts; }
    public PortSpec getDestPorts() { return _destPorts; }
    public boolean hasSourcePortSpec() {return _sourcePorts != null;}
    public boolean hasDestPortSpec() {return _destPorts != null;}

    @Override
    public String toString() {
        String s = "";
        if (hasSourcePortSpec()) s+= "src.ports=" + _sourcePorts + (hasDestPortSpec() ? ", " : "");
        if (hasDestPortSpec()) s+= "dst.ports=" + _destPorts;
        return s;
    }
}
