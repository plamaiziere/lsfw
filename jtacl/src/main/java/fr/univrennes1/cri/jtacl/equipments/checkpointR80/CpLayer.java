/*
 * Copyright (c) 2013 - 2018, Universite de Rennes 1
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

package fr.univrennes1.cri.jtacl.equipments.checkpointR80;

import fr.univrennes1.cri.jtacl.core.probing.ProbeRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Checkpoint layer object
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class CpLayer extends CpObject {

    List<CpFwRule> _rules = new ArrayList<>();
    protected CpLayer _parentLayer;
    protected Integer _layerCallRuleNumber;

	public CpLayer(String name, String className, String comment, String uid) {

		super(name, className, comment, uid);
	}

    public List<CpFwRule> getRules() { return _rules; };
	public CpLayer getParentLayer() { return _parentLayer; }

    public void setParentLayer(CpLayer parentLayer) {
       _parentLayer = parentLayer;
    }

    public boolean hasParentLayer() {
	    return _parentLayer != null;
    }

    public Integer getLayerCallRuleNumber() {
        return _layerCallRuleNumber;
    }

    public void setLayerCallRuleNumber(Integer ruleNumber) {
        _layerCallRuleNumber = ruleNumber;
    }

    /**
	 * Returns the {@link CpServicesMatch} of the given {@link ProbeRequest}.
	 * @param request request to test.
	 * @return the CpServicesMatch of the given ProbeRequest.
	 */
	// public CpServicesMatch matches(ProbeRequest request);

}
