/*
 * Copyright (c) 2013 - 2021, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */
package fr.univrennes1.cri.jtacl.equipments.proxmox;

/**
 * Proxmox options (section OPTIONS)
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PxOptions {
    protected boolean _enable;
    protected PxRuleAction _policyIn;
    protected PxRuleAction _policyOut;

    public static PxOptions ofDefault() {
        PxOptions opt = new PxOptions();
        opt.setEnable(false);
        opt.setPolicyOut(PxRuleAction.ACCEPT);
        opt.setPolicyIn(PxRuleAction.ACCEPT);
        return opt;
    }

    public boolean isEnable() {
        return _enable;
    }

    public void setEnable(boolean enable) {
        _enable = enable;
    }

    public PxRuleAction getPolicyIn() {
        return _policyIn;
    }

    public void setPolicyIn(PxRuleAction policyIn) {
        _policyIn = policyIn;
    }

    public PxRuleAction getPolicyOut() {
        return _policyOut;
    }

    public void setPolicyOut(PxRuleAction policyOut) {
        _policyOut = policyOut;
    }
}
