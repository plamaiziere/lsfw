/*
 * Copyright (c) 2010, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.equipments.openbsd;

import fr.univrennes1.cri.jtacl.core.network.Iface;
import fr.univrennes1.cri.jtacl.core.network.IfaceLink;
import fr.univrennes1.cri.jtacl.lib.misc.StringsList;

/**
 * Describes a PF interface
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PfIface {

    protected Iface _iface;
    protected StringsList _groups = new StringsList();
    protected IfaceLink _firstIpV4;
    protected IfaceLink _firstIpV6;

    public PfIface(Iface iface) {
        _iface = iface;
    }

    public StringsList getGroups() {
        return _groups;
    }

    public Iface getIface() {
        return _iface;
    }

    public IfaceLink getFirstIpV4() {
        return _firstIpV4;
    }

    public void setFirstIpV4(IfaceLink firstIpV4) {
        _firstIpV4 = firstIpV4;
    }

    public IfaceLink getFirstIpV6() {
        return _firstIpV6;
    }

    public void setFirstIpV6(IfaceLink firstIpV6) {
        _firstIpV6 = firstIpV6;
    }

}
