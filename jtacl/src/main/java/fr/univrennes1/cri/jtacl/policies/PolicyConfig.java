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

import fr.univrennes1.cri.jtacl.core.exceptions.JtaclConfigurationException;
import fr.univrennes1.cri.jtacl.core.monitor.Log;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.config4j.Configuration;
import org.config4j.ConfigurationException;

/**
 * flow configuration
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PolicyConfig {

	protected String _defaultTcpFlags;
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

	protected List<String> lookupStringOrList(String scope, String key) {

		List<String> list = new ArrayList<String>();
		String s = lookupString(scope, key);
		if (s != null) {
			list.add(s);
		} else {
			list = lookupList(scope, key);
		}
		return list;
	}

	public PolicyConfig(String filename) {
		_config.parse(filename);
	}

	/**
	 * Gets a PolicyFlow from the configuration
	 * @param scope configuration scope.
	 * @param name name of the PolicyFlow
	 * @return a PolicyFlow
	 * @throws JtaclConfigurationException if the configuration is invalid.
	 */
	public PolicyFlow getFlow(String scope, String name) {
		// TODO: sanity checks

		/*
		 * flow's scope
		 */
		String pscope = scope + "." + name;

		String comment = lookupString(pscope, "comment");
		if (comment == null)
			comment = "";

		PolicyFlow flow = new PolicyFlow(name, comment);

		List<String> from = lookupStringOrList(pscope, "from");
		flow.setFrom(from);
		List<String> to = lookupStringOrList(pscope, "to");
		flow.setTo(to);

		String proto = lookupString(pscope, "proto");
		if (proto.equalsIgnoreCase("udp") || proto.equalsIgnoreCase("tcp") ||
				proto.equalsIgnoreCase("ip")) {
			flow.setProtocol(proto);
		} else {
			throw new JtaclConfigurationException("Policy: " + name
				+ ", invalid protocol: " + proto);
		}

		String sport = lookupString(pscope, "source_port");
		flow.setSourcePort(sport);

		String port = lookupString(pscope, "port");
		flow.setPort(port);

		String flags = lookupString(pscope, "flags");
		flow.setFlags(flags);

		String connected = lookupString(pscope, "connected");
		flow.setConnected(Boolean.parseBoolean(connected));

		return flow;
	}

	public NetworkPolicy getNetworkPolicy(String scope, String name) {

		// TODO: sanity checks

		/*
		 * flow's scope
		 */
		String pscope = scope + "." + name;

		String comment = lookupString(pscope, "comment");
		if (comment == null)
			comment = "";

		NetworkPolicy policy = new NetworkPolicy(name, comment);

		List<String> from = lookupStringOrList(pscope, "from");
		policy.setFrom(from);
		List<String> to = lookupStringOrList(pscope, "to");
		policy.setTo(to);

		/*
		 * expect default to ACCEPT
		 */
		String sexpect = lookupString(pscope, "expect");
		PolicyExpect expect = null;
		if (sexpect == null) {
			sexpect = "ACCEPT";
		} else {
			if (sexpect.equalsIgnoreCase("ACCEPT"))
				expect = PolicyExpect.ACCEPT;
			if (sexpect.equalsIgnoreCase("DENY"))
				expect = PolicyExpect.DENY;
		}
		if (expect == null)
			throw new JtaclConfigurationException("Policy: " + name
				+ ", invalid expect " + sexpect);
		policy.setExpect(expect);

		List<String> include = lookupStringOrList(pscope, "policies");
		if (include != null) {
			for (String s: include) {
				/*
				 * XXX reference are resolved later
				 */
				policy.getPolicies().put(s, null);
			}
		} else {
			throw new JtaclConfigurationException("Policy: " + name +
				" does not specify any policy");
		}
		System.out.println(policy);
		Log.debug().info("policy: " + policy);
		return policy;
	}

	public ServicePolicy getServicePolicy(String scope, String name) {

		// TODO: sanity checks

		/*
		 * scope
		 */
		String pscope = scope + "." + name;

		String comment = lookupString(pscope, "comment");
		if (comment == null)
			comment = "";

		ServicePolicy policy = new ServicePolicy(name, comment);

		List<String> from = lookupStringOrList(pscope, "from");
		policy.setFrom(from);
		List<String> to = lookupStringOrList(pscope, "to");
		policy.setTo(to);

		List<String> include = lookupStringOrList(pscope, "policies");
		if (include != null) {
			for (String s: include) {
				/*
				 * XXX reference are resolved later
				 */
				policy.getPolicies().put(s, null);
			}
		} else {
			throw new JtaclConfigurationException("Policy: " + name +
				" does not specify any policy");
		}

		System.out.println(policy);
		Log.debug().info("policy: " + policy);
		return policy;
	}

	public HostPolicy getHostPolicy(String scope, String name) {

		// TODO: sanity checks

		/*
		 * scope
		 */
		String pscope = scope + "." + name;

		String comment = lookupString(pscope, "comment");
		if (comment == null)
			comment = "";

		HostPolicy policy = new HostPolicy(name, comment);

		List<String> address = lookupStringOrList(pscope, "address");
		policy.setAddress(address);

		List<String> include = lookupStringOrList(pscope, "policies");
		if (include != null) {
			for (String s: include) {
				/*
				 * XXX reference are resolved later
				 */
				policy.getPolicies().put(s, null);
			}
		} else {
			throw new JtaclConfigurationException("Policy: " + name +
				" does not specify any policy");
		}

		System.out.println(policy);
		Log.debug().info("policy: " + policy);
		return policy;
	}

	public void loadFlows(PoliciesMap policies) {

		String[] ls = _config.listLocallyScopedNames("flows", "",
			Configuration.CFG_SCOPE_AND_VARS, false);

		for (String s: ls) {
			System.out.println(s);
			PolicyFlow nflow = getFlow("flows", s);
			if (nflow != null)
				policies.put(nflow);
		}
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

	public void loadHostPolicies(PoliciesMap policies) {

		String[] ls = _config.listLocallyScopedNames("hosts", "",
			Configuration.CFG_SCOPE_AND_VARS, false);

		for (String s: ls) {
			System.out.println(s);
			HostPolicy hpolicy = getHostPolicy("hosts", s);
			if (hpolicy != null)
				policies.put(hpolicy);
		}
	}

	public PoliciesMap loadPolicies() {

		PoliciesMap policies = new PoliciesMap();

		_defaultTcpFlags = lookupString("", "default_tcp_flags");
		loadFlows(policies);
		loadNetworkPolicies(policies);
		loadServicePolicies(policies);
		loadHostPolicies(policies);
		return policies;
	}

	protected void linkPolicyRef(PoliciesMap globalPolicies,
			PoliciesMap localPolicies) {

		for (String pname: localPolicies.keySet()) {
			Policy ref = globalPolicies.get(pname);
			if (ref == null) {
				/*
				 * auto-create a FlowPolicy if the name starts with UDP/
				 * or TCP/
				 */
				String[] ss = pname.split("/");
				if (ss.length == 2) {
					if (ss[0].equalsIgnoreCase("tcp")
							|| ss[0].equalsIgnoreCase("udp")) {
						PolicyFlow flow = new PolicyFlow(pname,
							pname);
						flow.setProtocol(ss[0].toLowerCase());
						flow.setPort(ss[1].toLowerCase());
						if (flow.getProtocol().equals("tcp"))
							flow.setFlags(_defaultTcpFlags);
						flow.setConnected(true);
						globalPolicies.put(flow);
						ref = flow;
					}
				}
			}
			if (ref == null)
				throw new JtaclConfigurationException(
						"Cannot find policy: " + pname);
			localPolicies.put(ref);
		}
	}

	public void linkPolicies(PoliciesMap policies) {
		/*
		 * link policies
		 */
		List<String> policiesNames = new ArrayList<String>();
		policiesNames.addAll(policies.keySet());
		for (String n: policiesNames) {
			Policy p = policies.get(n);
			if (p instanceof ServicePolicy) {
				ServicePolicy sp = (ServicePolicy) p;
				PoliciesMap lpolicies = sp.getPolicies();
				linkPolicyRef(policies, lpolicies);
			}
			if (p instanceof NetworkPolicy) {
				NetworkPolicy np = (NetworkPolicy) p;
				PoliciesMap lpolicies = np.getPolicies();
				linkPolicyRef(policies, lpolicies);
			}
			if (p instanceof HostPolicy) {
				HostPolicy hp = (HostPolicy) p;
				PoliciesMap lpolicies = hp.getPolicies();
				linkPolicyRef(policies, lpolicies);
			}
		}
	}

}
