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

package fr.univrennes1.cri.jtacl.core.probing;

import fr.univrennes1.cri.jtacl.core.monitor.Log;
import fr.univrennes1.cri.jtacl.core.network.IfaceLink;
import java.util.HashMap;

/**
 * This class is reponsible of tracking probes over our virtual network.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class ProbesTracker {

	// the first probe injected
	protected Probe _rootProbe;

	// list of all tracked probes
	protected ProbesByUid _probes;

	// list of 'final' probes: probe with destination reached.
	protected ProbesByUid _finalProbes;

	// list of 'dead' probes: probes killed because of an error.
	protected ProbesByUid _killedProbes;

	// list of 'looping' probes: probes which are looping.
	protected ProbesByUid _loopingProbes;

	public void setRootProbe(Probe rootProbe) {
		_rootProbe = rootProbe;
	}

	/**
	 * Creates a new {@link ProbesTracker} instance.
	 */
	public ProbesTracker() {
		_probes = new ProbesByUid();
		_finalProbes = new ProbesByUid();
		_killedProbes = new ProbesByUid();
		_loopingProbes = new ProbesByUid();
	}

	public Probe getRootProbe() {
		return _rootProbe;
	}

	/**
	 * Add a {@link Probe} probe into the tracker.
	 * @param probe Probe to add.
	 */
	public void trackProbe(Probe probe) {
		_probes.put(probe);

		Log.debug().info("probe: " + probe.getSourceAddress().toString() +
				" - " + probe.getDestinationAddress() +
				" link " + probe.getIncomingLink());
	}

	/**
	 * Untrack (remove) a {@link Probe} probe from the tracker.
	 * @param probe Probe to remove.
	 */
	public void untrackProbe(Probe probe) {
		_probes.remove(ProbesByUid.key(probe));
	}

	/**
	 * Checks if the {@link Probe} probe in argument is tracked.
	 * @param probe Probe to check.
	 * @return true if the probe is tracked
	 */
	public boolean isTracked(Probe probe) {
		return _probes.containsKey(ProbesByUid.key(probe));
	}

	/**
	 * Resets the tracker. All probes tracked are discarded.
	 */
	public void resetTracker() {
		_probes.clear();
		_finalProbes.clear();
		_killedProbes.clear();
		_loopingProbes.clear();
	}

	/**
	 * Returns the probes with incoming position equals to 'link'
	 * @param link
	 * @return a {@link ProbesByUid} map containig the probes.
	 */
	public ProbesByUid probesByIfaceLink(IfaceLink link) {
		ProbesByUid probes = new ProbesByUid();

		for (Integer i: _probes.keySet()) {
			Probe probe =_probes.get(i);
			IfaceLink ilink = probe.getIncomingLink();
			if (ilink != null && ilink == link)
				probes.put(probe);
		}
		return probes;
	}

	/**
	 * Returns the probes with incoming position equals to 'link' and equals
	 * to 'probe'
	 * @param link
	 * @param probe
	 * @return a {@link ProbesByUid} map containig the probes.
	 */
	public ProbesByUid probesByIfaceLinkAndProbe(IfaceLink link,
			Probe probe) {

		ProbesByUid probes = probesByIfaceLink(link);

		for (Probe p: probes.values()) {
			if (!p.getSourceAddress().equals(probe.getSourceAddress()))
				continue;
			if (!p.getDestinationAddress().equals(probe.getDestinationAddress()))
				continue;
			//TODO: compare payload and protocol
			probes.put(p);
		}
		return probes;
	}

	/**
	 * Notifies the tracker that the probe in argument has reached its destination.
	 * @param probe
	 */
	public void probeDestinationReached(Probe probe) {
		_finalProbes.put(probe);
	}

	/**
	 * Notifies the tracker that the probe in argument was killed.
	 * @param probe
	 */
	public void probeKilled(Probe probe) {
		_killedProbes.put(probe);
	}

	/**
	 * Notifies the tracker that the probe in argument was looping.
	 * @param probe
	 */
	public void probeLooping(Probe probe) {
		_loopingProbes.put(probe);
	}

	/**
	 * Returns leaves probes from the {@link ProbesByUid} map in argument.
	 * @return a {@link ProbesByUid} map containing the probes.
	 */
	public ProbesByUid getProbesLeaves(ProbesByUid probes) {
		ProbesByUid result = new ProbesByUid();

		for (Probe p: probes.values()) {
			if (!p.hasChildren())
				result.put(p);
		}
		return result;
	}

	/**
	 * Returns the 'final' probes of this tracker.
	 * @return  a {@link ProbesByUid} map containing the probes.
	 */
	public ProbesByUid getFinalProbes() {
		return _finalProbes;
	}

	/**
	 * Returns the 'killed' probes of this tracker.
	 * @return a {@link ProbesByUid} map containing the probes.
	 */
	public ProbesByUid getKilledProbes() {
		return _killedProbes;
	}

	/**
	 * Returns the 'looping' probes of this tracker.
	 * @return a {@link ProbesByUid} map containing the probes.
	 */
	public ProbesByUid getLoopingProbes() {
		return _loopingProbes;
	}

	/**
	 * Returns all the probes of this tracker.
	 * @return a {@link ProbesByUid} map containing the probes.
	 */
	public ProbesByUid getProbes() {
		return _probes;
	}
	

	/**
	 * Checks if the path taken by of all final probes is unique.
	 *
	 * @return true if the path taken by all final probes is unique.
	 */
	public boolean checkFinalProbePath() {
		HashMap<Integer, String> paths = new HashMap<Integer, String>();

		for (Integer i: _finalProbes.keySet()) {
			Probe probe = _finalProbes.get(i);
			String path = probe.showPath();
			Integer hash = path.hashCode();
			if (paths.containsKey(hash)) {
				Log.notifier().warning("probe" + probe.uidToString() + " dupplicate path: " + path);
				return false;
			}
			paths.put(hash, path);
		}
		return true;
	}

	/**
	 * Returns the routing result of this tracker.
	 * The result is ROUTED if all probes have reached their destination,
	 * NOTROUTED if all probes have not reached their destinationn, and UNKNOWN
	 * otherwise.
	 * @return the {@link RoutingResult} of this tracker.
	 */
	public RoutingResult getRoutingResult() {
		if (!_finalProbes.isEmpty() && _killedProbes.isEmpty())
			return RoutingResult.ROUTED;
		if (_finalProbes.isEmpty() && !_killedProbes.isEmpty())
			return RoutingResult.NOTROUTED;
		return RoutingResult.UNKNOWN;
	}

	/**
	 * Returns the global ACL result of this tracker.<br/>
	 * The global result is ACCEPT if all probes have reached their destination and
	 * all probe's results are ACCEPT.<br/>
	 * If at least one probe's result is DENY, the global result is DENY.<br/>
	 * If at least one probe's result is MAY or the routing result is not ROUTED,
	 * then the global result is MAY.<br/>
	 *
	 * @return the {@link AclResult} of this tracker.
	 */
	public AclResult getAclResult() {

		AclResult result = new AclResult();

		if (getRoutingResult() != RoutingResult.ROUTED)
			result.addResult(AclResult.MAY);

		int accept = 0;
		int deny = 0;
		int match = 0;
		int may = 0;

		for (Probe finalProbe: _finalProbes.values()) {
			ProbesList probes = finalProbe.getParentProbes();
			probes.add(finalProbe);
			for (Probe probe : probes) {
				AclResult probeResult = probe.getResults().getAclResult();
				if (probeResult.hasAccept())
					accept++;
				if (probeResult.hasDeny())
					deny++;
				if (probeResult.hasMatch())
					match++;
				if (probeResult.hasMay())
					may++;
			}
		}
		if (may > 0)
			result.addResult(AclResult.MAY);
		if (match == 0 && accept > 0 && deny == 0)
			result.addResult(AclResult.ACCEPT);
		if (match == 0 && deny > 0)
			result.addResult(AclResult.DENY);
		if (match > 0) {
			result.addResult(AclResult.MATCH);
		}
		return result;
	}

}
