/*
 * Copyright (c) 2013, Universite de Rennes 1
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

package fr.univrennes1.cri.jtacl.policies;

import fr.univrennes1.cri.jtacl.core.probing.Probing;
import java.util.LinkedList;
import java.util.List;

/**
 * policy probe
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PolicyProbe {

	protected Policy _policy;
	protected String _probe;
	protected Probing _probing;
	protected boolean _result;
	protected List<String> _from;
	protected List<String> _to;
	protected String _action;
	protected List<String> _address;
	protected List<PolicyProbe> _policyProbes = new LinkedList<PolicyProbe>();

	public PolicyProbe(Policy policy) {
		_policy = policy;
	}

	public Policy getPolicy() {
		return _policy;
	}

	public void setPolicy(Policy policy) {
		_policy = policy;
	}

	public String getProbe() {
		return _probe;
	}

	public void setProbe(String probe) {
		_probe = probe;
	}

	public Probing getProbing() {
		return _probing;
	}

	public void setProbing(Probing probing) {
		_probing = probing;
	}

	public boolean isResultOk() {
		return _result;
	}

	public void setResult(boolean result) {
		_result = result;
	}

	public List<PolicyProbe> getPolicyProbes() {
		return _policyProbes;
	}

	public List<String> getFrom() {
		return _from;
	}

	public void setFrom(List<String> from) {
		_from = from;
	}

	public List<String> getTo() {
		return _to;
	}

	public void setTo(List<String> to) {
		_to = to;
	}

	public String getAction() {
		return _action;
	}

	public void setAction(String action) {
		_action = action;
	}

	public List<String> getAddress() {
		return _address;
	}

	public void setAddress(List<String> address) {
		_address = address;
	}

}
