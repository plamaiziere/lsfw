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
import java.io.CharArrayWriter;
import java.io.PrintWriter;

/**
 * Report output on probing.
 * 
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class ShellReport {

	protected ProbesTracker _tracker;

	public ShellReport(ProbesTracker tracker) {
		_tracker = tracker;
	}

	public String showPath(ProbesList probes) {
		CharArrayWriter swriter = new CharArrayWriter();
		PrintWriter writer = new PrintWriter(swriter);

		for (Probe probe: probes) {

			IfaceLink in = probe.getIncomingLink();
			IfaceLink out = probe.getOutgoingLink();
			NetworkEquipment equipment = in.getIface().getEquipment();

			writer.println("On: " + equipment.getName() + " (" +
				equipment.getComment() + ")");

			writer.print("    " + in.getIface().getName());
			writer.println(" (" + in.getIface().getComment() + ")");
			writer.print("        interface IP: " + in.getIp().toString("i::"));
			writer.println(" network: " + in.getNetwork().toString("i::"));
			if (out != null) {
				writer.print("    " + out.getIface().getName());
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

	public String showResultingProbes(ProbesByUid probes,
			boolean verbose) {

		CharArrayWriter swriter = new CharArrayWriter();
		PrintWriter writer = new PrintWriter(swriter);

		for (Probe probe: _tracker.getProbesLeaves(probes).values()) {

			ProbesList linkedProbes = probe.getParentProbes();
			linkedProbes.add(probe);

			writer.println("Path: ");
			writer.println(showPath(linkedProbes));

			for (Probe pi: linkedProbes) {
				writer.print(showProbeResult(pi, verbose));
			}
			writer.println("-----------------");
		}
		writer.println();
		writer.flush();
		return swriter.toString();
	}

	public String showProbeResult(Probe probe, boolean verbose) {
		CharArrayWriter swriter = new CharArrayWriter();
		PrintWriter writer = new PrintWriter(swriter);

		IfaceLink in = probe.getIncomingLink();
		NetworkEquipment equipment = in.getIface().getEquipment();

		writer.println("------");
		writer.println(equipment.getName() + " (" + equipment.getComment() + ")");
		if (verbose) {
			writer.print("Probe" + probe.uidToString());
			writer.println(" path: " + probe.showPath());
			writer.println();
			writer.print("Routing Result: " + probe.getResults().getRoutingResult());
			writer.println(" " + probe.getResults().getRoutingMessage());
			writer.println("ACL Result: " + probe.getResults().getAclResult());
		}

		writer.println(probe.getResults().showAclResults(verbose));

		writer.flush();
		return swriter.toString();

	}

	public String showResults(boolean verbose) {
		CharArrayWriter swriter = new CharArrayWriter();
		PrintWriter writer = new PrintWriter(swriter);

		ProbesByUid finalProbes = _tracker.getFinalProbes();
		ProbesByUid killedProbes = _tracker.getKilledProbes();
		ProbesByUid loopingProbes = _tracker.getLoopingProbes();

		if (!finalProbes.isEmpty()) {

			writer.println("###############################");
			writer.println("-------- Routed probes --------");
			writer.print(showResultingProbes(finalProbes, verbose));
		}
		if (!killedProbes.isEmpty()) {
			writer.println("###############################");
			writer.println("------- Killed probes  --------");
			writer.print(showResultingProbes(killedProbes, verbose));
		}
		if (!loopingProbes.isEmpty()) {
			writer.println("###############################");
			writer.println("-------- Looping probes -------");
			writer.print(showResultingProbes(loopingProbes, verbose));
		}

		writer.flush();
		return swriter.toString();
	}

}
