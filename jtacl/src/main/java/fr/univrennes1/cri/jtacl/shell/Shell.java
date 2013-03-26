/*
 * Copyright (c) 2010, Universite de Rennes 1
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

package fr.univrennes1.cri.jtacl.shell;

import fr.univrennes1.cri.jtacl.App;
import fr.univrennes1.cri.jtacl.core.exceptions.JtaclConfigurationException;
import fr.univrennes1.cri.jtacl.core.exceptions.JtaclParameterException;
import fr.univrennes1.cri.jtacl.core.exceptions.JtaclRuntimeException;
import fr.univrennes1.cri.jtacl.core.monitor.Log;
import fr.univrennes1.cri.jtacl.core.monitor.Monitor;
import fr.univrennes1.cri.jtacl.core.network.NetworkEquipment;
import fr.univrennes1.cri.jtacl.core.network.NetworkEquipmentsByName;
import fr.univrennes1.cri.jtacl.core.probing.AclResult;
import fr.univrennes1.cri.jtacl.core.probing.ExpectedProbing;
import fr.univrennes1.cri.jtacl.core.probing.ProbesTracker;
import fr.univrennes1.cri.jtacl.core.probing.Probing;
import fr.univrennes1.cri.jtacl.core.probing.RoutingResult;
import fr.univrennes1.cri.jtacl.core.topology.NetworkLink;
import fr.univrennes1.cri.jtacl.core.topology.NetworkLinks;
import fr.univrennes1.cri.jtacl.lib.ip.IPNet;
import fr.univrennes1.cri.jtacl.lib.ip.IPRange;
import fr.univrennes1.cri.jtacl.lib.ip.IPRangeable;
import fr.univrennes1.cri.jtacl.lib.ip.IPversion;
import fr.univrennes1.cri.jtacl.lib.ip.Protocols;
import fr.univrennes1.cri.jtacl.lib.misc.StringsList;
import fr.univrennes1.cri.jtacl.policies.FlowPolicy;
import fr.univrennes1.cri.jtacl.policies.HostPolicy;
import fr.univrennes1.cri.jtacl.policies.NetworkPolicy;
import fr.univrennes1.cri.jtacl.policies.Policies;
import fr.univrennes1.cri.jtacl.policies.PoliciesMap;
import fr.univrennes1.cri.jtacl.policies.Policy;
import fr.univrennes1.cri.jtacl.policies.PolicyExpect;
import fr.univrennes1.cri.jtacl.policies.PolicyProbe;
import fr.univrennes1.cri.jtacl.policies.ServicePolicy;
import groovy.lang.Binding;
import groovy.ui.Console;
import groovy.util.GroovyScriptEngine;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.UnknownHostException;
import java.util.*;
import java.util.logging.Level;
import org.parboiled.Parboiled;
import org.parboiled.buffers.InputBuffer;
import org.parboiled.errors.ParseError;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

/**
 * lsfw shell
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class Shell {

	protected String _prompt = "lsfw> ";
	protected ShellParser _parser = Parboiled.createParser(ShellParser.class);
	protected ReportingParseRunner _parseRunner =
		new ReportingParseRunner(_parser.CommandLine());
	protected Monitor _monitor = Monitor.getInstance();;
	protected boolean _interactive;
	protected Probing _lastProbing;
	protected boolean _testResult;
	protected PrintStream _outStream = System.out;
	protected PoliciesMap _policies = Policies.getInstance();

	public Shell() {
		_interactive = false;
	}

	public Shell(boolean interactive) {
		_interactive = interactive;
	}

	protected String substitute(String line) {

		String r = line;
		for (String s: _monitor.getDefines().keySet()) {
			r = r.replace("$" + s, _monitor.getDefines().get(s));
		}
		return r;
	}

	protected void autoReload() {

		for (String eqName: _monitor.getEquipments().keySet()) {

			NetworkEquipment equipment =
					_monitor.getEquipments().get(eqName);
			if (equipment.hasChanged()) {
				Log.debug().info("reloading " + eqName);
				try {
					_monitor.reloadEquipment(equipment);
				} catch (JtaclRuntimeException ex) {
					System.err.println("Error: " + ex.getMessage());
				}
			}
		}
	}

	protected LsfwBinding newBinding(String args) {
		return new LsfwBinding(args);
	}

	public void helpCommand(ShellParser command) {
		try {
			String topic = command.getString("HelpTopic");
			if (topic == null) {
				topic = "help";
			}
			InputStream stream = null;
			do {
				stream = this.getClass().getResourceAsStream("/help/" + topic);
				if (stream == null) {
					topic = "help";
				}
			} while (stream == null);
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			for (;;) {
				String line = reader.readLine();
				if (line == null) {
					break;
				}
				_outStream.println(line);
			}
			reader.close();
			/*
			 * system informations
			 */
			if (topic.equals("help")) {
				_outStream.println();
				_outStream.println("--- System informations ---");
				_outStream.println("Java runtime: " +
					System.getProperty("java.runtime.name") + " (" +
					System.getProperty("java.runtime.version") + ")");
				_outStream.println("Java VM: " +
					System.getProperty("java.vm.name") + " (" +
					System.getProperty("java.vm.version") + ")");

				_outStream.println("OS: " +
					System.getProperty("os.name") + " " +
					System.getProperty("os.version") + "/" +
					System.getProperty("os.arch"));
			}
		} catch (IOException ex) {
			_outStream.println("cannot print help");
		}

	}

	public void optionCommand(ShellParser command) {
		String name = command.getString("SetValueName");
		String value = command.getString("SetValueValue");

		if (name != null && value != null)
			try {
				_monitor.getOptions().setOption(name, value);
			} catch (JtaclConfigurationException e) {
				_outStream.println("Error: " + e.getMessage());
			}
		else
			_outStream.println(_monitor.getOptions().getOptionsList());
	}

	public void defineCommand(ShellParser command) {
		String name = command.getString("SetValueName");
		String value = command.getString("SetValueValue");

		if (name != null) {
			String define = _monitor.getDefines().get(name);
			if (define != null)
				_monitor.getDefines().remove(name);
			if (value != null) {
				_monitor.getDefines().put(name, value);
			}
		} else {
			for (String d: _monitor.getDefines().keySet()) {
				_outStream.println(d + "=" + _monitor.getDefines().get(d));
			}
		}

	}

	public void topologyCommand(ShellParser command) {

		String equipmentName = command.getString("Equipments");
		String option = command.getString("TopologyOption");

		/*
		 * Try to parse the value as an IP. If ok, display network links
		 * containing this IP. Overwise this is an equipment.
		 */
		IPNet ip = null;
		NetworkEquipment equipment = null;

		if (equipmentName != null) {
			try {
				ip = new IPNet(equipmentName);
			} catch (UnknownHostException ex) {
				// do nothing
			}
			equipment = null;
			if (ip == null) {
				equipment = _monitor.getEquipments().get(equipmentName);
				if (equipment == null) {
					_outStream.println("No such equipment: " + equipmentName);
					return;
				}
			}
		}
		/*
		 * filter out each link by IP or equipment.
		 */
		NetworkLinks links = _monitor.getTopology().getNetworkLinks();
		Collections.sort(links);

		for (NetworkLink link: links) {
			NetworkLink candidate = null;
			/*
			 * IP
			 */
			if (ip != null) {
				if (link.getNetwork().overlaps(ip))
						candidate = link;
			} else {
				/*
				 * Equipment
				 */
				if (equipmentName != null) {
					if (link.isConnectedTo(equipment))
						candidate = link;
				}
				else {
					candidate = link;
				}
			}
			/*
			 * filter out by connected or !connected.
			 * a NetworkLink is 'connected' if it is associated to a least two
			 * IfaceLink.
			 */
			if (candidate != null && option != null) {
				if (option.equalsIgnoreCase("connected")) {
					if (!candidate.isInterconnection())
						candidate = null;
				}
				if (option.equalsIgnoreCase("!connected")) {
					if (candidate.isInterconnection())
						candidate = null;
				}
			}
			/*
			 * output
			 */
			if (candidate != null)
				_outStream.println(candidate.toString());

		}
	}

	public void routeCommand(ShellParser command) {

		String equipmentName = command.getString("Equipments");
		if (equipmentName != null) {
			if (!_monitor.getEquipments().containsKey(equipmentName)) {
				_outStream.println("No such equipment: " + equipmentName);
				return;
			}
		}
		NetworkEquipmentsByName equipments = _monitor.getEquipments();
		for (NetworkEquipment equipment: equipments.values()) {
			if (equipmentName== null || equipment.getName().equals(equipmentName)) {
				_outStream.println("Routes on " + equipment.getName());
				_outStream.println("-------------------");
				_outStream.println(equipment.getShowableRoutes().showRoutes());
				_outStream.println("-------------------");
				_outStream.println();
			}
		}
	}

	public void equipmentCommand(ShellParser command) {

		String equipmentName = command.getString("Equipments");
		NetworkEquipment equipment = _monitor.getEquipments().get(equipmentName);
		if (equipment == null) {
			_outStream.println("No such equipment: " + equipmentName);
			return;
		}

		equipment.runShell(command.getString("SubCommand"), _outStream);
	}

	public void reloadCommand(ShellParser command) {

		String equipmentName = command.getString("Equipments");

		if (equipmentName == null) {
			_monitor.reload();
			return;
		}

		NetworkEquipment equipment = _monitor.getEquipments().get(equipmentName);
		if (equipment == null) {
			_outStream.println("No such equipment: " + equipmentName);
			return;
		}

		try {
			_monitor.reloadEquipment(equipment);
		} catch (JtaclRuntimeException ex) {
			System.err.println("Error: " + ex.getMessage());
		}
	}

	public boolean probeCommand(String commandLine, ProbeCommandTemplate probeCmd) {

		boolean testMode = probeCmd.getProbeExpect() != null;
		boolean learnMode = probeCmd.getProbeOptLearn();
		boolean silent = testMode || learnMode;

		ProbeCommand cmd = new ProbeCommand();
		try {
			cmd.buildRequest(probeCmd);
		} catch (JtaclParameterException ex) {
			_outStream.println(ex.getMessage());
			return false;
		}

		if (!silent) {
			_outStream.println("probe from: " +
				cmd.getSourceAddress().toNetString("::i") +
				" to: " + cmd.getDestinationAddress().toNetString("::i"));
		}

		/*
		 * probe
		 */
		cmd.runCommand();
		_lastProbing = cmd.getProbing();

		/*
		 * results
		 */

		boolean verbose = probeCmd.getProbeOptVerbose();
		boolean active = probeCmd.getProbeOptActive();
		boolean matching = probeCmd.getProbeOptMatching();

		if (verbose) {
			active = true;
			matching = true;
		}

		if (!active)
			matching = true;
		/*
		 * each tracker
		 */
		for (ProbesTracker tracker: _lastProbing) {
			if (!silent) {
				ShellReport report = new ShellReport(tracker, verbose, active,
					matching);
				_outStream.print(report.showResults());
			}
		}

		/*
		 * Global ACL result
		 */
		AclResult aclResult = _lastProbing.getAclResult();

		/*
		 * Global routing result
		 */
		RoutingResult routingResult = _lastProbing.getRoutingResult();

		if (!silent) {
			_outStream.println("Global ACL result is: " + aclResult);
			_outStream.println("Global routing result is: " + routingResult);
			_outStream.println();
		}

		/*
		 * expect
		 */
		String expect = probeCmd.getProbeExpect();
		if (expect == null) {
			return true;
		}

		ExpectedProbing ep;
		try {
			ep = ShellUtils.parseExpectedProbing(expect);
		} catch (JtaclParameterException ex) {
			_outStream.println(ex.getMessage());
			return false;
		}

		boolean testExpect = _lastProbing.checkExpectedResult(ep);

		if (testMode) {
			if (!testExpect) {
				_outStream.println(commandLine + " [FAILED]");
			} else {
				_outStream.println(commandLine + " [OK]");
			}
		}

		if (learnMode) {
			_outStream.print(commandLine);
			_outStream.print(" [ACL: " + aclResult);
			_outStream.print("; Routing: " + routingResult);
			_outStream.println("]");
		}

		return testExpect;
	}

	public void groovyCommand(ShellParser parser) {

		Binding binding = new Binding();
		LsfwBinding lsfw = newBinding(parser.getString("GroovyArgs"));
		binding.setVariable("lsfw", lsfw);

		GroovyScriptEngine scriptEngine;
		try {
			scriptEngine = new GroovyScriptEngine(parser.getString("GroovyDirectory"));
		} catch (IOException ex) {
			_outStream.println(ex.getMessage());
			return;
		}
		try {
			scriptEngine.run(parser.getString("GroovyScript"), binding);
		} catch (Exception ex) {
			_outStream.println();
			_outStream.println("Error: " + ex.getMessage());
		}
	}

	public void groovyConsoleCommand(ShellParser parser) {
		Binding binding = new Binding();
		LsfwBinding lsfw = newBinding(parser.getString("GroovyArgs"));
		binding.setVariable("lsfw", lsfw);
		Console console = new Console(binding);
		console.run();
	}

	public void hostCommand(ShellParser parser) {

		IPversion ipversion;
		if (parser.getString("Command").equals("host6"))
			ipversion = IPversion.IPV6;
		else
			ipversion = IPversion.IPV4;

		IPNet ip;
		String hostname = parser.getString("AddressArg");
		try {
			ip = IPNet.getByName(hostname, ipversion);
		} catch (UnknownHostException ex) {
			_outStream.println("Cannot resolve " + hostname);
			return;
		}
		_outStream.println(hostname + " has address " + ip.toString("::i"));
	}

	public void ipCommand(ShellParser parser) {

		IPversion ipversion;
		if (parser.getString("Command").equals("ip6"))
			ipversion = IPversion.IPV6;
		else
			ipversion = IPversion.IPV4;

		IPNet ipn = null;
		IPRangeable ip = null;
		String sip = parser.getString("AddressArg");
		try {
			ipn = new IPNet(sip);
		} catch(UnknownHostException ex) {
			try {
				ip = new IPRange(sip);
			} catch (UnknownHostException ex1) {
				try {
					// not an IP try to resolve as a host.
					ipn = IPNet.getByName(sip, ipversion);
				} catch (UnknownHostException ex2) {
					_outStream.println("Error: " + ex2.getMessage());
				}
			}
		}

		/*
		 * IPNet
		 */
		if (ipn != null) {
			_outStream.println("ip      : " + ipn.toString("is"));
			_outStream.println("network : " + ipn.networkAddress().toString("is")
					+ " - " + ipn.lastNetworkAddress().toString("is"));
			if (ipn.isIPv4()) {
				String [] s = ipn.toString("n").split("/");
				_outStream.println("netmask : " + s[1]);
			}
			_outStream.println("prefix  : " + ipn.getPrefixLen());

			_outStream.println("length  : " + ipn.networkLength());
		}

		/*
		 * IPRange
		 */
		if (ip != null) {
			ipn = ip.nearestNetwork();
			_outStream.println("range  : " + ip.toString("is"));
			_outStream.println("length : " + ip.length());
			_outStream.println("nearest network : " + ipn.toString("is")
					+ " - " + ipn.lastNetworkAddress().toString("is"));
			if (ip.isIPv4()) {
				String [] s = ipn.toString("n").split("/");
				_outStream.println("netmask : " + s[1]);
			}
			_outStream.println("prefix : " + ipn.getPrefixLen());
			_outStream.println("network length : " + ipn.networkLength());
		}
	}

	public void policyLoadCommand(ShellParser parser) {

		Policies.clear();
		try {
			Policies.loadPolicies(parser.getString("FileName"));
		} catch (JtaclConfigurationException ex) {
			_outStream.println("Error: " + ex.getMessage());
		}
	}

	public void itStream(int indent) {
		for (int i = 0; i < indent; i++) {
			_outStream.print(" ");
		}
	}

	public void printPolicy(Policy policy, int indent) {
		if (policy instanceof FlowPolicy) {
			FlowPolicy flow = (FlowPolicy) policy;
			String sproto = "any";
			if (flow.getProtocol() == Protocols.TCP)
				sproto = "TCP";
			if (flow.getProtocol() == Protocols.UDP)
				sproto = "UDP";
			itStream(indent); _outStream.println("name: " + flow.getName());
			itStream(indent + 2); _outStream.println("comment: " + flow.getComment());
			itStream(indent + 2); _outStream.println("from: " + flow.getFrom());
			itStream(indent + 2); _outStream.println("to: " + flow.getTo());
			itStream(indent + 2); _outStream.println("proto: " + sproto);
			itStream(indent + 2); _outStream.println("source port: " + flow.getSourcePort());
			itStream(indent + 2); _outStream.println("port: " + flow.getPort());
			itStream(indent + 2); _outStream.println("flags: " + flow.getFlags());
			itStream(indent + 2); _outStream.println("connected: " + flow.isConnected());
		}

		if (policy instanceof NetworkPolicy) {
			NetworkPolicy p = (NetworkPolicy) policy;
			itStream(indent); _outStream.println("name: " + p.getName());
			itStream(indent + 2); _outStream.println("comment: " + p.getComment());
			itStream(indent + 2); _outStream.println("from: " + p.getFrom());
			itStream(indent + 2); _outStream.println("to: " + p.getTo());
			itStream(indent + 2); _outStream.println("expect: " + p.getExpect());
			itStream(indent + 2); _outStream.println("policies: ");
			for (String s: p.getPolicies().getKeysSorted()) {
				printPolicy(_policies.get(s), indent + 4);
			}
		}

		if (policy instanceof ServicePolicy) {
			ServicePolicy p = (ServicePolicy) policy;
			itStream(indent); _outStream.println("name: " + p.getName());
			itStream(indent + 2); _outStream.println("comment: " + p.getComment());
			itStream(indent + 2); _outStream.println("from: " + p.getFrom());
			itStream(indent + 2); _outStream.println("to: " + p.getTo());
			itStream(indent + 2); _outStream.println("policies: ");
			for (String s: p.getPolicies().getKeysSorted()) {
				printPolicy(_policies.get(s), indent + 4);
			}
		}

		if (policy instanceof HostPolicy) {
			HostPolicy p = (HostPolicy) policy;
			itStream(indent); _outStream.println("name: " + p.getName());
			itStream(indent + 2); _outStream.println("comment:" + p.getComment());
			itStream(indent + 2); _outStream.println("address: " + p.getAddress());
			itStream(indent + 2); _outStream.println("policies: ");
			for (String s: p.getPolicies().getKeysSorted()) {
				printPolicy(_policies.get(s), indent + 2);
			}
		}
	}

	public void policyListCommand(ShellParser parser) {

		String pname = parser.getString("PolicyName");
		if (pname != null) {
			Policy p = _policies.get(pname);
			if (p == null) {
				_outStream.println("Error: cannot find policy: " + pname);
			} else {
				printPolicy(p, 0);
			}
			return;
		}
		for (String s: _policies.getKeysSorted()) {
			printPolicy(_policies.get(s), 0);
		}
	}

	protected boolean probeFlow(PolicyProbe policyProbe, FlowPolicy flow,
			boolean state) {

		/*
		 * build the probe command
		 */
		ProbeCommandTemplate ptpt = new ProbeCommandTemplate();
		PolicyExpect expect = policyProbe.getExpect();
		String sexpect = expect == PolicyExpect.ACCEPT ? "accept" : "unaccepted";
		ptpt.setProbeExpect(sexpect);
		ptpt.setPortSource(flow.getSourcePort());
		ptpt.setPortDest(flow.getPort());
		ptpt.setSrcAddress(policyProbe.getFrom().get(0));
		ptpt.setDestAddress(policyProbe.getTo().get(0));
		Integer protocol = flow.getProtocol();
		if (protocol != null) {
			String p = flow.getProtocol() == Protocols.TCP ? "tcp" : "udp";
			ptpt.setProtoSpecification(p);
		}
		if (flow.getFlags() != null) {
			StringsList tcpflags = new StringsList();
			tcpflags.add(flow.getFlags());
			ptpt.setTcpFlags(tcpflags);
		}
		ptpt.setProbeOptQuickDeny(true);
		ptpt.setProbeOptState(state);

		ProbeCommand pc = new ProbeCommand();
		pc.buildRequest(ptpt);

		/*
		 * probe
		 */
		pc.runCommand();

		/*
		 * store the probe and result into the policy probe
		 */
		Probing probing = pc.getProbing();
		policyProbe.setProbing(probing);
		String sprobe = "state " + state
			+ "; proto " + ptpt.getProtoSpecification() + " "
			+ ptpt.getPortSource() + ":"
			+ ptpt.getPortDest() + " "
			+ "flags " + ptpt.getTcpFlags();
		policyProbe.setProbe(sprobe);
		ExpectedProbing ep = new ExpectedProbing(false, sexpect);
		policyProbe.setResult(probing.checkExpectedResult(ep));
		return policyProbe.isResultOk();
	}

	public boolean policyProbe(PolicyProbe policyProbe) {

		Policy policy = policyProbe.getPolicy();

		if (Log.debug().isLoggable(Level.INFO))
			Log.debug().info("probing policy: " + policy.getName());

		/*
		 * ovveride from / destination if unset
		 */
		List<String> nfrom = policyProbe.getFrom();
		if (policy.getFrom() != null)
			nfrom = policy.getFrom();
		List<String> nto = policyProbe.getTo();
		if (policy.getTo() != null)
			nto = policy.getTo();
		PolicyExpect nexpect = policyProbe.getExpect();

		/*
		 * host policy
		 */
		if (policy instanceof HostPolicy) {
			HostPolicy hp = (HostPolicy) policy;

			boolean result = true;
			for (String n: hp.getPolicies().getKeysSorted()) {
				Policy p = hp.getPolicies().get(n);
				PolicyProbe pprobe = new PolicyProbe(p);
				policyProbe.getPolicyProbes().add(pprobe);
				pprobe.setAddress(hp.getAddress());
				pprobe.setFrom(nfrom);
				pprobe.setTo(nto);
				pprobe.setExpect(nexpect);

				if (!policyProbe(pprobe))
					result = false;
			}
			policyProbe.setResult(result);
			return result;
		}

		/*
		 * service policy
		 */
		if (policy instanceof ServicePolicy) {
			ServicePolicy sp = (ServicePolicy) policy;

			boolean result = true;
			for (String n: sp.getPolicies().getKeysSorted()) {
				Policy p = sp.getPolicies().get(n);
				PolicyProbe pprobe = new PolicyProbe(p);
				policyProbe.getPolicyProbes().add(pprobe);
				pprobe.setAddress(policyProbe.getAddress());
				pprobe.setFrom(nfrom);
				pprobe.setTo(nto);
				pprobe.setExpect(nexpect);

				if (!policyProbe(pprobe))
					result = false;
			}
			policyProbe.setResult(result);
			return result;
		}

		/*
		 * network policy
		 */
		if (policy instanceof NetworkPolicy) {
			NetworkPolicy np = (NetworkPolicy) policy;

			if (np.getExpect() != null)
				nexpect = np.getExpect();

			boolean result = true;
			for (String n: np.getPolicies().getKeysSorted()) {
				Policy p = np.getPolicies().get(n);
				PolicyProbe pprobe = new PolicyProbe(p);
				policyProbe.getPolicyProbes().add(pprobe);
				pprobe.setAddress(policyProbe.getAddress());
				pprobe.setFrom(nfrom);
				pprobe.setTo(nto);
				pprobe.setExpect(nexpect);

				if (!policyProbe(pprobe))
					result = false;
			}
			policyProbe.setResult(result);
			return result;
		}

		/*
		 * flow policy
		 */
		if (!(policy instanceof FlowPolicy))
			return false;

		FlowPolicy flow = (FlowPolicy) policy;

		if (nfrom == null) {
			nfrom = policyProbe.getAddress();
		}
		if (nto == null) {
			nto = policyProbe.getAddress();
		}

		/*
		 * probe from each source address to each destination address
		 */
		boolean result = true;
		for (String from: nfrom) {
			for (String to: nto) {
				/*
				 * add a policy probe for each source / destination address
				 */
				PolicyProbe nprobe = new PolicyProbe(flow);
				List<String> lfrom = new ArrayList<String>();
				lfrom.add(from);
				nprobe.setFrom(lfrom);
				List<String> lto = new ArrayList<String>();
				lto.add(to);
				nprobe.setTo(lto);
				nprobe.setExpect(nexpect);
				policyProbe.getPolicyProbes().add(nprobe);

				/*
				 * probe
				 */
				if (!probeFlow(nprobe, flow, false)) {
					result = false;
				}
				/*
				 * check the reply
				 */
				if (flow.isConnected()) {
					String fname = flow.getName();
					String fcomment = flow.getComment();
					FlowPolicy rflow = new FlowPolicy(fname, fcomment);
					rflow.setPort(flow.getSourcePort());
					rflow.setSourcePort(flow.getPort());
					Integer proto = flow.getProtocol();
					rflow.setProtocol(proto);
					if (proto != null && proto == Protocols.TCP) {
						rflow.setFlags("A");
					}
					PolicyProbe rprobe = new PolicyProbe(rflow);
					rprobe.setExpect(PolicyExpect.ACCEPT);
					rprobe.setFrom(nprobe.getTo());
					rprobe.setTo(nprobe.getFrom());
					policyProbe.getPolicyProbes().add(rprobe);
					if (!probeFlow(rprobe, rflow, true)) {
						result = false;
					}
				}
			}
		}
		policyProbe.setResult(result);
		return result;
	}

	public void printPolicyProbe(PolicyProbe pprobe, int indent) {

		String sresult = pprobe.isResultOk() ? "[OK]" : "[FAILED]" ;
		for (int i = 0; i < indent; i++)
			_outStream.print(" ");

		_outStream.print(pprobe.getPolicy().getName());
		_outStream.println("("
				+ "address: " + pprobe.getAddress()
				+ "; from: " + pprobe.getFrom()
				+ "; to: " + pprobe.getTo()
				+ "; probe: " + pprobe.getProbe() + ")"
				+ "; " + sresult);

		for (PolicyProbe p: pprobe.getPolicyProbes()) {
			printPolicyProbe(p, indent + 2);
		}
	}

	public void policyProbeCommand(ShellParser parser) {

		String from = parser.getString("PolicyFrom");
		String to = parser.getString("PolicyTo");
		String pname = parser.getString("PolicyName");

		Policy policy = _policies.get(pname);
		if (policy == null) {
			_outStream.println("Error: cannot find policy: " + pname);
			return;
		}

		PolicyProbe pprobe = new PolicyProbe(policy);
		if (from != null) {
			List<String> flist = new ArrayList<String>();
			flist.add(from);
			pprobe.setFrom(flist);
		}
		if (to != null) {
			List<String> flist = new ArrayList<String>();
			flist.add(to);
			pprobe.setTo(flist);
		}
		policyProbe(pprobe);
		printPolicyProbe(pprobe, 0);
	}

	public void parseShellCommand(String commandLine) {

		if (Log.debug().isLoggable(Level.INFO))
			Log.debug().info(commandLine);

		// comment
		if (commandLine.startsWith("#"))
			return;
		// macro substitution
		String subs = substitute(commandLine);

		// tee redirection
		boolean teeAppend = false;
		String teeFile = null;
		int p = subs.lastIndexOf("|>>");
		if (p != -1) {
			teeAppend = true;
			teeFile = subs.substring(p + 3);
			teeFile = teeFile.trim();
			subs = subs.substring(0, p ).trim();

		} else {
			p = subs.lastIndexOf("|>");
			if (p != -1) {
				teeAppend = false;
				teeFile = subs.substring(p + 2);
				teeFile = teeFile.trim();
				subs = subs.substring(0, p ).trim();
			}
		}

		// empty command
		if (subs.isEmpty())
			return;

		_parseRunner.getParseErrors().clear();
		ParsingResult<?> result = _parseRunner.run(subs);

		if (!result.matched) {
			if (result.hasErrors()) {
				ParseError error = result.parseErrors.get(0);
				InputBuffer buf = error.getInputBuffer();
				_outStream.println("Syntax error: " +
					buf.extract(0, error.getStartIndex()));
			}
		}

		/*
		 * tee stdout
		 */
		if (teeFile != null) {
			try {
				ShellConsole.out().tee(teeFile, teeAppend);
			} catch (FileNotFoundException ex) {
				_outStream.println("Cannot open file: " + teeFile);
				return;
			}
		}

		if (result.matched) {
			String command = _parser.getString("Command");
			if (command.equals("quit")) {
				if (_interactive)
					_outStream.println("Goodbye!");
				System.exit(0);
			}

			if (_interactive && _monitor.getOptions().getAutoReload())
				autoReload();

			if (command.equals("probe") || command.equals("probe6")) {
				boolean test = probeCommand(commandLine, _parser.getProbeCmdTemplate());
				if (_parser.getProbeCmdTemplate().getProbeExpect() != null) {
					if (!test)
						_testResult = false;
				}
			}
			if (command.equals("option"))
				optionCommand(_parser);
			if (command.equals("topology"))
				topologyCommand(_parser);
			if (command.equals("route"))
				routeCommand(_parser);
			if (command.equals("help"))
				helpCommand(_parser);
			if (command.equals("define"))
				defineCommand(_parser);
			if (command.equals("equipment"))
				equipmentCommand(_parser);
			if (command.equals("reload"))
				reloadCommand(_parser);
			if (command.equals("groovy"))
				groovyCommand(_parser);
			if (command.equals("groovyconsole"))
				groovyConsoleCommand(_parser);
			if (command.equals("host") || command.equals("host6"))
				hostCommand(_parser);
			if (command.equals("ip") || command.equals("ip6"))
				ipCommand(_parser);
			if (command.equals("policy-list"))
				policyListCommand(_parser);
			if (command.equals("policy-load"))
				policyLoadCommand(_parser);
			if (command.equals("policy-probe"))
				policyProbeCommand(_parser);
		}

		/*
		 * 'untee' stdout
		 */
		if (teeFile != null) {
			try {
				ShellConsole.out().unTee();
			} catch (IOException ex) {
				_outStream.println("Cannot unTee file: " + teeFile);
			}
		}
	}

	public int runCommand(String commandLine) {
		_testResult = true;
		parseShellCommand(commandLine.trim());
		if (!_testResult)
			return App.EXIT_FAILURE;
		return App.EXIT_SUCCESS;
	}

	public int runFromFile(String fileName) {
		_testResult = true;
		BufferedReader dataIn = null;
		if (fileName == null) {
			 dataIn = new BufferedReader(new InputStreamReader(System.in));
		} else {
			try {
				dataIn = new BufferedReader(new FileReader(fileName));
			} catch (FileNotFoundException ex) {
				throw new JtaclRuntimeException(ex.getMessage());
			}
		}

		for (;;) {
			if (_interactive)
				_outStream.print(_prompt);
			String commandLine;
			try {
				commandLine = dataIn.readLine();
			} catch (IOException ex) {
				throw new JtaclRuntimeException(ex.getMessage());
			}
			if (commandLine == null) {
				break;
			}
			commandLine = commandLine.trim();
			parseShellCommand(commandLine);
		}
		try {
			dataIn.close();
		} catch (IOException ex) {
				throw new JtaclRuntimeException(ex.getMessage());
		}
		if (!_testResult)
			return App.EXIT_FAILURE;
		return App.EXIT_SUCCESS;
	}

	public PrintStream getOutputStream() {
		return _outStream;
	}

	public void setOutputStream(PrintStream outStream) {
		_outStream = outStream;
	}

}