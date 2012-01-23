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

import fr.univrennes1.cri.jtacl.core.probing.Probe;
import fr.univrennes1.cri.jtacl.core.probing.ProbesByUid;
import fr.univrennes1.cri.jtacl.core.probing.ProbesList;
import fr.univrennes1.cri.jtacl.core.probing.ProbesTracker;
import fr.univrennes1.cri.jtacl.core.network.IfaceLink;
import fr.univrennes1.cri.jtacl.core.network.NetworkEquipment;
import fr.univrennes1.cri.jtacl.core.probing.AccessControlList;
import fr.univrennes1.cri.jtacl.core.probing.ProbeResults;
import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.util.List;

/**
 * Report output on probing.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class ShellReport {

	protected ProbesTracker _tracker;
	protected boolean _verbose;
	protected boolean _matching;
	protected boolean _active;

	public ShellReport(ProbesTracker tracker,
			boolean verbose,
			boolean active,
			boolean matching) {

		_tracker = tracker;
		_verbose = verbose;
		_active = active;
		_matching = matching;
	}

	public String showPath(ProbesList probes) {
		CharArrayWriter swriter = new CharArrayWriter();
		PrintWriter writer = new PrintWriter(swriter);

		for (Probe probe: probes) {

			IfaceLink in = probe.getIncomingLink();
			IfaceLink out = probe.getOutgoingLink();
			NetworkEquipment equipment = in.getEquipment();

			writer.println("On: " + equipment.getName() + " (" +
				equipment.getComment() + ")");

			writer.print("    " + in.getIfaceName());
			writer.println(" (" + in.getIface().getComment() + ")");
			writer.print("        interface IP: " + in.getIp().toString("i::"));
			writer.println(" network: " + in.getNetwork().toString("i::"));
			if (out != null) {
				writer.print("    " + out.getIfaceName());
				writer.println(" (" + out.getIface().getComment() + ")");
				writer.print("        interface IP: " + out.getIp().toString("i::"));
				writer.println(" network: " + out.getNetwork().toString("i::"));
				writer.println("        nexthop: " + probe.getNextHop().toString("i::"));
			} else {
				writer.println("    (none)");
			}
		}
		writer.flush();
		return swriter.toString();
	}

	public String showResultingProbes(ProbesByUid probes) {

		CharArrayWriter swriter = new CharArrayWriter();
		PrintWriter writer = new PrintWriter(swriter);

		for (Probe probe: _tracker.getProbesLeaves(probes).values()) {

			ProbesList linkedProbes = probe.getParentProbes();
			linkedProbes.add(probe);

			writer.println("Path: ");
			writer.println(showPath(linkedProbes));

			for (Probe pi: linkedProbes) {
				writer.print(showProbeResult(pi));
			}
			writer.println("-----------------");
		}
		writer.println();
		writer.flush();
		return swriter.toString();
	}

	public String showProbeResult(Probe probe) {
		CharArrayWriter swriter = new CharArrayWriter();
		PrintWriter writer = new PrintWriter(swriter);

		IfaceLink in = probe.getIncomingLink();
		NetworkEquipment equipment = in.getEquipment();

		writer.println("------");
		writer.println(equipment.getName() + " (" + equipment.getComment() + ")");
		if (_verbose) {
			writer.print("Probe" + probe.uidToString());
			writer.println(" path: " + probe.showPath());
			writer.println();
			writer.print("Routing Result: " + probe.getResults().getRoutingResult());
			writer.println(" " + probe.getResults().getRoutingMessage());
			writer.println("ACL Result: " + probe.getResults().getAclResult());
		}

		writer.println(showAclResults(probe.getResults()));

		writer.flush();
		return swriter.toString();

	}

	public String showAclResults(ProbeResults results) {
		CharArrayWriter swriter = new CharArrayWriter();
		PrintWriter writer = new PrintWriter(swriter);

		List<AccessControlList> matchingAclsIn =
			results.getMatchingAclsIn();

		List<AccessControlList> activesAclsIn =
			results.getActivesAclsIn();

		List<AccessControlList> matchingAclsOut =
			results.getMatchingAclsOut();

		List<AccessControlList> activesAclsOut =
			results.getActivesAclsOut();

		String interfaceIn = results.getInterfaceIn();
		String interfaceOut = results.getInterfaceOut();

		if (_matching) {
			writer.println("Matching ACL on input: " + interfaceIn);
			for (AccessControlList acl: matchingAclsIn)
				writer.println("  " + acl.toString());
		}
		if (_active) {
			writer.println("Active ACL on input: " + interfaceIn);
			for (AccessControlList acl: activesAclsIn)
				writer.println("  " + acl.toString());
		}

		if (_matching) {
			writer.println("Matching ACL on output: " + interfaceOut);
			for (AccessControlList acl: matchingAclsOut)
				writer.println("  " + acl.toString());
		}
		if (_active) {
			writer.println("Active ACL on output: " + interfaceOut);
			for (AccessControlList acl: activesAclsOut)
				writer.println("  " + acl.toString());
		}

		writer.flush();
		return swriter.toString();
	}

	public String showResults() {
		CharArrayWriter swriter = new CharArrayWriter();
		PrintWriter writer = new PrintWriter(swriter);

		ProbesByUid finalProbes = _tracker.getFinalProbes();
		ProbesByUid killedProbes = _tracker.getKilledProbes();
		ProbesByUid loopingProbes = _tracker.getLoopingProbes();

		if (!finalProbes.isEmpty()) {

			writer.println("###############################");
			writer.println("-------- Routed probes --------");
			writer.print(showResultingProbes(finalProbes));
		}
		if (!killedProbes.isEmpty()) {
			writer.println("###############################");
			writer.println("------- Killed probes  --------");
			writer.print(showResultingProbes(killedProbes));
		}
		if (!loopingProbes.isEmpty()) {
			writer.println("###############################");
			writer.println("-------- Looping probes -------");
			writer.print(showResultingProbes(loopingProbes));
		}

		writer.flush();
		return swriter.toString();
	}

}
