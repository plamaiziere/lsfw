/*
 * Copyright (c) 2010, Université de Rennes 1
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

package fr.univrennes1.cri.jtacl.core.monitor;

import fr.univrennes1.cri.jtacl.core.exceptions.JtaclInternalException;
import fr.univrennes1.cri.jtacl.core.network.IfaceLink;
import fr.univrennes1.cri.jtacl.lib.ip.IPNet;

/**
 * This class describes a probe. A 'probe' is like an IP packet with several
 * informations added to describe what we want to check over the network.<br/>
 * By example, a probe can be a range of ip addresses, a range of ports,
 * a range of protocols.<br/>
 * A probe is also associated to a ProbesTracker which has the responsibility to
 * track probes and has a ProbeResults which stores the result of the probing.
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class Probe {

	/**
	 * the unique identifier of this probe.
	 */
	protected int _uid;

	/**
	 * the next uid that will be generated.
	 */
	protected static int _nextuid = 0;

	/**
	 * the source IP address
	 */
	protected IPNet _sourceAddress;

	/**
	 * the destination IP address
	 */
	protected IPNet _destinationAddress;

	/**
	 * The probe's request
	 */
	protected ProbeRequest _request;

	/**
	 * the parent of this probe.
	 */
	protected Probe _parentProbe;

	/**
	 * the timeToLive
	 */
	protected int _timeToLive = 64;

	/**
	 * the results.
	 */
	protected ProbeResults _probeResult;

	/**
	 * the tracker associated with this probe.
	 */
	protected ProbesTracker _probesTracker;

	/**
	 * the position of the probe.
	 */
	protected ProbePosition _position;

	/**
	 * the children probes of this probe.
	 */
	protected ProbesByUid _children;

	/**
	 * Generates and returns a new unique identifier.
	 */
	synchronized protected void newUid() {
			_uid = _nextuid;
			_nextuid ++;
	}

	/**
	 * Initialization of the probe.
	 */
	protected void initialize() {
		newUid();
		_position = new ProbePosition();
		_children = new ProbesByUid();
		_probeResult = new ProbeResults();
	}

	/**
	 * Creates a new raw {@link Probe} probe.
	 * Equipments must not call this constructor, instead use {@link #newChild()}
	 * or {@link #newInstance()} to get a new probe.
	 */
	protected Probe() {
		initialize();
	}
	/**
	 * Log probe's notifications.
	 * @param message informational message.
	 */
	protected void logNotification(String message) {
		String s = uidToString();
		if (!isNotProbing())
			s += " on: " + getIncomingLink().getIface().getEquipment().getName();
		Log.notifier().finest(s + " " + message);
	}

	/**
	 * Creates a new {@link Probe} root probe. The TimeToLive is set to its default
	 * value (32).
	 * @param probesTracker {@link ProbesTracker} tracker associated with this probe
	 * and its children.
	 * @param sourceAddress the {@link IPNet} source IP address of the probe.
	 * @param destinationAddress the {@link IPNet} destination IP address of the probe.
	 * @param request the {@link ProbeRequest} associated to this probe.
	 */
	public Probe(ProbesTracker probesTracker, IPNet sourceAddress, 
			IPNet destinationAddress, ProbeRequest request) {
		initialize();
		_parentProbe = null;
		_probesTracker = probesTracker;
		_sourceAddress = sourceAddress;
		_destinationAddress = destinationAddress;
		_request = request;
		_timeToLive = 64;
		// track the probe when it is built
		_probesTracker.trackProbe(this);
	}

	/**
	 * Creates a new {@link Probe} root probe.
	 * @param probesTracker a {@link ProbesTracker} tracker to be associated with
	 * this probe and its children.
	 * @param sourceAddress the {@link IPNet} source IP address of the probe.
	 * @param destinationAddress the {@link IPNet} destination IP address of the probe.
	 * @param request the {@link ProbeRequest} associated to this probe.	 *
	 * @param timeToLive the TimeToLive of the probe.
	 */
	public Probe(ProbesTracker probesTracker, IPNet sourceAddress,
			IPNet destinationAddress, ProbeRequest request, int timeToLive) {

		initialize();
		_parentProbe = null;
		_probesTracker = probesTracker;
		_sourceAddress = sourceAddress;
		_destinationAddress = destinationAddress;
		_request = request;
		_timeToLive = timeToLive;
		// track the probe when it is built
		_probesTracker.trackProbe(this);
	}

	/**
	 * Returns the unique identifier number (uid) of this {@link Probe} probe.<br/>
	 * Uid starts at zero and is incremented each time a probe is created.
	 * This is used to identify and track probes.
	 * @return the Uid of this {@link Probe}
	 */
	public int getUid() {
		return _uid;
	}

    /**
     * Returns the {@link IPNet} source address of this {@link Probe} probe.
     * @return the source address.
     */
    public IPNet getSourceAddress() {
		return _sourceAddress;
	}

    /**
     * Sets the {@link IPNet} source address of this {@link Probe} probe.
	 * @param address the {@link IPNet} source address.
	 */
    public void setSourceAddress(IPNet address) {
		_sourceAddress = address;
	}

    /**
     * Returns the {@link IPNet} destination address of this {@link Probe}
	 * probe.
     * @return the destination address.
     */
    public IPNet getDestinationAddress() {
		return _destinationAddress;
	}

    /**
     * Sets the {@link IPNet} destination address of this {@link Probe} probe.
	 * @param address the {@link IPNet} destination address.
	 */
    public void setDestinationAddress(IPNet address) {
		_destinationAddress = address;
	}

	/**
	 * Returns the associated {@link ProbeRequest} associated to this
	 * {@link Probe} probe.
	 * @return the associated {@link ProbeRequest} associated to this probe.
	 */
	public ProbeRequest getRequest() {
		return _request;
	}

    /**
     * Tests if this {@link Probe} probe is an IPv4 probe.
     * @return true if this {@link Probe} probe is an IPv4 probe.
     */
    public boolean isIPv4() {
		return _sourceAddress.isIPv4();
	}

    /**
     * Tests if this {@link Probe} probe is an IPv6 probe.
     * @return true if this {@link Probe} probe is an IPv6 probe.
     */
    public boolean isIPv6() {
		return _sourceAddress.isIPv6();
	}

	/**
	 * Returns the TimeToLive (TTL) of this probe. The TTL is decremented on each
	 * equipment and is used to prevent loop, as the TTL field in an IP packet.
	 * @return the TimeToLive of this probe.
	 */
	public int getTimeToLive() {
		return _timeToLive;
	}

	/**
	 *	Sets the TimeToLive of this probe.
	 * @param ttl TimeToLive.
	 */
	public void setTimeToLive(int ttl) {
		_timeToLive = ttl;
	}

	/**
	 * Checks if this probe is alive.
	 * @return true if the TimeToLive of this probe is greater than 0.
	 */
	public boolean isAlive() {
		return _timeToLive > 0;
	}

	/**
	 * Decrement the TimeToLive of this probe.
	 */
	public void decTimeToLive() {
		if (_timeToLive > 0)
			_timeToLive--;
	}

	/**
	 * Returns the {@link ProbesTracker} associated to this probe.
	 * @return the {@link ProbesTracker} associated to this probe.
	 */
	public ProbesTracker getProbesTracker() {
		return _probesTracker;
	}

	/**
	 * Returns the {@link ProbeResults} results of this probe.
	 * @return the {@link ProbeResults} results of this probe.
	 */
	public ProbeResults getResults() {
		return _probeResult;
	}

	/**
	 * Returns the parent probe of this {@link Probe}.<br/>
	 * The parent probe is the probe which has created this probe.
	 * @return the parent {@link Probe} probe of this probe.
	 */
	public Probe getParentProbe() {
		return _parentProbe;
	}

	/**
	 * Returns the {@link IfaceLink} outgoing link of this probe.
	 * @return the {@link IfaceLink} outgoing link of this probe.
	 */
	public IfaceLink getOutgoingLink() {
		return _position.getOutgoing();
	}

	/**
	 * Sets the outgoing {@link IfaceLink} link of this probe.
	 * @param outgoing the {@link IfaceLink} outgoing link to be associated.
	 * @param nextHop the {@link IPNet} IP address of the nexthop.
	 */
	public void setOutgoingLink(IfaceLink outgoing, IPNet nextHop) {
		_position.setOutgoing(outgoing, nextHop);
	}

	/**
	 * Returns the {@link IPNet} IP address of the nexthop of this probe.
	 * @return the {@link IPNet} IP address of the nexthop of this probe.
	 */
	public IPNet getNextHop() {
		return _position.getNextHop();
	}

	/**
	 * Returns the {@link IfaceLink} incoming link of this probe.
	 * @return the {@link IfaceLink} incoming link of this probe.
	 */
	public IfaceLink getIncomingLink() {
		return _position.getIncoming();
	}

	/**
	 * Sets the {@link IfaceLink} incoming link of this probe.
	 * @param incoming the {@link IfaceLink} incoming link to be associated.
	 */
	public void setIncomingLink(IfaceLink incoming) {
		_position.setIncoming(incoming);
	}

	/**
	 * Checks if this {@link Probe} probe is not currently probing.
	 * @return true if this {@link Probe} probe is not currently probing.
	 */
	public boolean isNotProbing() {
		return _position.getIncoming() == null && _position.getOutgoing() == null;
	}

	/**
	 * Checks if this {@link Probe} probe is currently probing.
	 * @return true if this {@link Probe} probe is currently probing.
	 */
	public boolean isProbing() {
		return _position.getIncoming() != null && _position.getOutgoing() == null;
	}

	/**
	 * Checks if this {@link Probe} probe's probing is done.
	 * @return true if this {@link Probe} probe's probing is done.
	 */
	public boolean isProbingDone() {
		return _position.getIncoming() != null && _position.getOutgoing() != null;
	}

	/**
	 * Returns the {@link ProbePosition} position of this probe.
	 * @return the {@link ProbePosition} position of this probe.
	 */
	public ProbePosition getPosition() {
		return _position;
	}

	/**
	 * Creates a new {@link Probe}] probe, child of this probe.
	 * The tracker and the position are not shared and the newly created probe
	 * is added to the list of the children of this probe.
	 * @return the created probe.
	 */
	public Probe newChild() {
		Probe child = new Probe();
		child._probesTracker = _probesTracker;
		child._parentProbe = this;
		child._sourceAddress = _sourceAddress;
		child._destinationAddress = _destinationAddress;
		child._request = _request.newInstance();
		child._timeToLive = _timeToLive;
		_children.put(child);
		// track the newly probe
		_probesTracker.trackProbe(child);
		return child;
	}

	/**
	 * Creates a new {@link Probe} copy of this probe.
	 * The tracker is not shared and the position is initialized by a copy of the
	 * position of this probe. The list of the children is copied too.
	 * @return the created probe.
	 */
	public Probe newInstance() {
		Probe probe = new Probe();
		probe._probesTracker = _probesTracker;
		probe._parentProbe = _parentProbe;
		probe._sourceAddress = _sourceAddress;
		probe._destinationAddress = _destinationAddress;
		probe._request = _request.newInstance();
		probe._timeToLive = _timeToLive;
		// copy the position
		probe._position = _position.newInstance();
		// copy children
		probe._children.putAll(_children);
		// copy the result
		probe._probeResult = _probeResult.newInstance();
		// track the newly probe
		_probesTracker.trackProbe(probe);

		return probe;
	}

	/**
	 * Checks if this {@link Probe} probe has some children probes.
	 * @return true if this {@link Probe} probe has some children probes.
	 */
	public boolean hasChildren() {
		return !_children.isEmpty();
	}

	/**
	 * Returns a {@link String} representation of the path followed by this
	 * probe and its ancestors
	 * @return a {@link String} representation of the path followed by this
	 * probe and its ancestors.
	 */
	public String path() {
		String str = "";

		if (_parentProbe != null )
			str = _parentProbe.path() + "/";

		str += (_position.getIncoming() != null) ? _position.getIncoming().getIface().getEquipment().getName() : null;
		str += "(";
		str += (_position.getIncoming() != null) ? _position.getIncoming().getIface().getName() : null;
		str += "-";
		str += (_position.getOutgoing() != null) ? _position.getOutgoing().getIface().getName() : null;
		str += ")";
		return str;
	}

	/**
	 * Returns all the parent probes of this {@link Probe} probe. Parents are
	 * returned in the order root parent - nearest parent.
	 * @return a {@link ProbesList} list containing the parent probes. The list
	 * could be empty but not null.
	 */
	public ProbesList getParentProbes() {
		ProbesList list = new ProbesList();

		Probe parent = _parentProbe;
		while (parent != null) {
			list.add(0, parent);
			parent = parent.getParentProbe();
		}
		return list;
	}

	/**
	 * Returns all the positions of this {@link Probe} probe and its ancestors.
	 * @return a {@link ProbePositions} list containing the positions. The list
	 *  could be empty but not null.
	 */
	public ProbePositions getPositions() {
		ProbePositions pos = new ProbePositions();

		if (isProbingDone()) {
			pos.add(_position);
		}

		Probe parent = _parentProbe;
		while (parent != null) {
			ProbePosition p = parent.getPosition();
			if (!pos.contains(p))
				pos.add(0, p);
			parent = parent.getParentProbe();
		}
		return pos;
	}

	/**
	 * Returns all the positions of the ancestors of this {@link Probe} probe.
	 * @return a {@link ProbePositions} list containing the positions. The list
	 *  could be empty but not null.
	 */
	public ProbePositions getParentsPositions() {
		ProbePositions pos = new ProbePositions();

		Probe parent = _parentProbe;
		while (parent != null) {
			ProbePosition p = parent.getPosition();
			if (!pos.contains(p))
				pos.add(0, p);
			parent = parent.getParentProbe();
		}
		return pos;
	}

	/**
	 * Returns a {@link String} representation of {@link #getParentsPositions()}.
	 * @return a {@link String} representation of {@link #getParentsPositions()}.
	 */
	public String showSimplePath() {
		String str = "/";
		ProbePositions pos = getParentsPositions();

		for (ProbePosition p: pos) {
			str += p + "/";
		}
		return str;
	}

	/**
	 * Returns a {@link String} representation of {@link #getPositions()}.
	 * (ie the path followed by the probe.)
	 * @return a {@link String} representation of {@link #getPositions()}.
	 */
	public String showPath() {
		String str = "/";
		ProbePositions pos = getPositions();

		for (ProbePosition p: pos) {
			str += p + "/";
		}
		return str;
	}

	/**
	 * Returns a {@link String} representation of this probe uid and parent probe uid.
	 * @return a {@link String} representation of this probe uid and parent probe uid.
	 */
	public String uidToString() {
		String s = "<#" + _uid + ", ";
		s += (_parentProbe == null) ? "-1" : _parentProbe.getUid();
		s += ">";
		return s;
	}

	/**
	 * Notify this {@link Probe} probe that its destination is reached.
	 * @param message informational message.
	 * @throws JtaclInternalException if the state of the probe is not in probingDone(),
	 * see {@link #isProbingDone()}.
	 */
	public void destinationReached(String message) {
		if (!isProbingDone())
			throw new JtaclInternalException("Probe must be in probingDone state");
		logNotification(message);
		_probeResult.setRoutingResult(RoutingResult.ROUTED, message);
		_probesTracker.probeDestinationReached(this);
	}

	/**
	 * Notify this {@link Probe} probe that it has been routed to its destination.
	 * @param message informational message.
	 */
	public void routed(String message) {
		logNotification(message);
		_probeResult.setRoutingResult(RoutingResult.ROUTED, message);
	}

	/**
	 * Notify this {@link Probe} probe that it has been killed because of an error (TTL)
	 * @param message informational message.
	 */
	public void killError(String message) {
		logNotification(message);
		_probeResult.setRoutingResult(RoutingResult.KILLED, message);
		_probesTracker.probeKilled(this);
	}

	/**
	 * Notify this {@link Probe} probe that it has been killed because no route
	 * was found.
	 * @param message informational message.
	 */
	public void killNoRoute(String message) {
		logNotification(message);
		_probeResult.setRoutingResult(RoutingResult.NOTROUTED, message);
		_probesTracker.probeKilled(this);
	}

	/**
	 * Notify this {@link Probe} probe that it has been killed because it was looping.
	 * @param message informational message.
	 */
	public void killLoop(String message) {
		logNotification(message);
		_probeResult.setRoutingResult(RoutingResult.LOOP, message);
		_probesTracker.probeLooping(this);
	}

}
