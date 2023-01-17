/*
 * Copyright (c) 2013 - 2018, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
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

    /**
     * Construct a new checkpoint layer
     * @param name name
     * @param className class name
     * @param comment comment
     * @param uid object's uid
     */
	public CpLayer(String name, String className, String comment, String uid) {

		super(name, className, comment, uid);
	}

    /**
     * Return the list of FW rules in this layer
     * @return the list of FW rules in this layer
     */
    public List<CpFwRule> getRules() { return _rules; };

    /**
     * Return the parent layer of this layer (ie the layer that calls this layer). Could be null.
      * @return the parent layer of this layer (ie the layer that calls this layer)
     */
	public CpLayer getParentLayer() { return _parentLayer; }

    /**
     * Set the parent layer of this layer (ie the layer that calls this layer). Could be null.
     * @param parentLayer layer that calls this layer
     */
    public void setParentLayer(CpLayer parentLayer) {
       _parentLayer = parentLayer;
    }

    /**
     * True if this layer has a parent layer
     * @return True if this layer has a parent layer
     */
    public boolean hasParentLayer() {
	    return _parentLayer != null;
    }

    /**
     * Return the rule number (in the parent layer) where this layer is called
     * @return the rule number (in the parent layer) where this layer is called
     */
    public Integer getLayerCallRuleNumber() {
        return _layerCallRuleNumber;
    }

    /**
     * Set the rule number (in the parent layer) where this layer is called
     * @param ruleNumber the rule number (in the parent layer) where this layer is called
     */
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
