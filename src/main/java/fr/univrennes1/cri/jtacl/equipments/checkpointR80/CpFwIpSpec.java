/*
 * Copyright (c) 2013 - 2018, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.equipments.checkpointR80;

/**
 * Checkpoint IP specification firewall rule
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class CpFwIpSpec {
    protected CpNetworkGroup _networks = new CpNetworkGroup("", "", "", "");
    protected boolean _notIn;

    public CpNetworkGroup getNetworks() {
        return _networks;
    }

    public void addReference(String name, CpNetworkObject networkObject) {
        _networks.addBaseReference(name, networkObject);
    }

    public void linkTo(CpFwRule fwrule) {
        for (CpNetworkObject nobj : _networks.getBaseObjects().values()) {
            nobj.linkWith(fwrule);
        }
    }

    public void setNotIn(boolean notIn) {
        _notIn = notIn;
    }

    public boolean isNotIn() {
        return _notIn;
    }

    @Override
    public String toString() {
        String s = "";
        if (_notIn)
            s = "!";
        return s + _networks.getBaseReferencesName();
    }
}
