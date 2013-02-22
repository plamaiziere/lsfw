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

import fr.univrennes1.cri.jtacl.core.monitor.Log;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.config4j.Configuration;
import org.config4j.ConfigurationException;
import sun.util.logging.resources.logging;

/**
 * policy configuration
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PolicyConfig {

	protected Configuration _config = Configuration.create();

	protected List<String> lookupList(String scope, String key) {
		String[] ls;
		try {
			ls = _config.lookupList(scope, key);
		} catch (ConfigurationException ex) {
			return null;
		}
		return new LinkedList<String>(Arrays.asList(ls));
	}

	protected String lookupString(String scope, String key) {
		String s = null;
		try {
			s = _config.lookupString(scope, key);
		} catch (ConfigurationException ex) {
			//
		}
		return s;
	}

	public PolicyConfig(String filename) {
		_config.parse(filename);
	}

	public NetworkPolicy getNetworkPolicy(String scope, String name) {

		// TODO: sanity checks

		/*
		 * policy's scope
		 */
		String pscope = scope + "." + name;

		String comment = lookupString(pscope, "comment");
		if (comment == null)
			comment = "";

		NetworkPolicy policy = new NetworkPolicy(name, comment);

		String from = lookupString(pscope, "from");
		policy.setFrom(from);
		String to = lookupString(pscope, "to");
		policy.setTo(to);

		String proto = lookupString(pscope, "proto");
		policy.setProtocol(proto);

		String sport = lookupString(pscope, "source_port");
		policy.setSourcePort(sport);

		String port = lookupString(pscope, "port");
		policy.setPort(port);

		String flags = lookupString(pscope, "flags");
		policy.setFlags(flags);

		String action = lookupString(pscope, "action");
		policy.setAction(action);

		System.out.println(policy);
		Log.debug().info("policy: " + policy);
		return policy;
	}

	public ServicePolicy getServicePolicy(String scope, String name) {

		// TODO: sanity checks

		/*
		 * policy's scope
		 */
		String pscope = scope + "." + name;

		String comment = lookupString(pscope, "comment");
		if (comment == null)
			comment = "";

		ServicePolicy policy = new ServicePolicy(name, comment);

		String from = lookupString(pscope, "from");
		policy.setFrom(from);
		String to = lookupString(pscope, "to");
		policy.setTo(to);

		List<String> include = lookupList(pscope, "policies");
		for (String s: include) {
			/*
			 * XXX reference are resolved later
			 */
			policy.getPolicies().put(s, null);
		}

		System.out.println(policy);
		Log.debug().info("policy: " + policy);
		return policy;
	}

	public void loadNetworkPolicies(PoliciesMap policies) {

		String[] ls = _config.listLocallyScopedNames("networks", "",
			Configuration.CFG_SCOPE_AND_VARS, false);

		for (String s: ls) {
			System.out.println(s);
			NetworkPolicy npolicy = getNetworkPolicy("networks", s);
			if (npolicy != null)
				policies.put(npolicy);
		}
	}

	public void loadServicePolicies(PoliciesMap policies) {

		String[] ls = _config.listLocallyScopedNames("services", "",
			Configuration.CFG_SCOPE_AND_VARS, false);

		for (String s: ls) {
			System.out.println(s);
			ServicePolicy spolicy = getServicePolicy("services", s);
			if (spolicy != null)
				policies.put(spolicy);
		}
	}

	public PoliciesMap loadPolicies() {

		PoliciesMap policies = new PoliciesMap();

		loadNetworkPolicies(policies);
		loadServicePolicies(policies);
		return policies;
	}
}
