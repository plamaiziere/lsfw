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

package fr.univrennes1.cri.jtacl.core.network;

import fr.univrennes1.cri.jtacl.core.monitor.Probe;
import fr.univrennes1.cri.jtacl.core.monitor.Monitor;
import fr.univrennes1.cri.jtacl.core.topology.NetworkLink;
import fr.univrennes1.cri.jtacl.lib.ip.IPNet;

/**
 * Describes a link between an interface {@link Iface} and a network
 * {@link IPNet}.<br/><br/>
 * An IfaceLink has one IP address for the interface {@link IPNet}
 * and one network IP address {@link IPNet}.
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class IfaceLink {

	/**
	 * Iface owner of this link.
	 */
	protected Iface _iface;

	/**
	 * the address IP of this link.
	 */
	protected IPNet	_ip;

	/**
	 * the network address of this link.
	 */
	protected IPNet _network;

	/**
	 * the network link connected to this link.
	 */
	protected NetworkLink _networkLink;

	/**
	 * a reference to the monitor.
	 */
	protected Monitor _monitor;

	/**
	 * Creates a new {@link IfaceLink} link associated with a {@link Iface} interface.
	 * @param monitor the {@link Monitor} monitor associated with this link.
	 * @param iface the {@link Iface} interface to associate.
	 * @param ip the {@link IPNet} IP address of the link.
	 * @param network the {@link IPNet} IP address of the network's link.
	 */
	public IfaceLink(Monitor monitor, Iface iface, IPNet ip, IPNet network) {
		_iface = iface;
		_ip = ip;
		_network = network;
		_monitor = monitor;
	}

	/**
	 * Returns the {@link Iface} interface associated to this {@link IfaceLink} link.
	 * @return the {@link Iface} interface associated to this {@link IfaceLink} link.
	 */
	public Iface getIface() {
		return _iface;
	}

	/**
	 * Returns the {@link IPNet} IP address of this {@link IfaceLink} link.
	 * @return the {@link IPNet} IP address of this {@link IfaceLink} link.
	 */
	public IPNet getIp() {
		return _ip;
	}

	/**
	 * Returns the {@link IPNet} IP network address of this {@link IfaceLink} link.
	 * @return the {@link IPNet} IP network address of this {@link IfaceLink} link.
	 */
	public IPNet getNetwork() {
		return _network;
	}

	/**
	 * Returns the {@link NetworkLink} network link of this {@link IfaceLink} link.
	 * @return the {@link NetworkLink} network link of this {@link IfaceLink} link.
	 */
	public NetworkLink getNetworkLink() {
		return _networkLink;
	}

	/**
	 * Sets the {@link NetworkLink} network link of this {@link IfaceLink} link.
	 * @param networkLink the {@link NetworkLink} network link.
	 */
	public void setNetworkLink(NetworkLink networkLink) {
		_networkLink = networkLink;
	}

	@Override
	public String toString() {
		return _iface.getName() + " - " + _ip.toString("i") + " - " + _network.toString("i");
	}

	/**
	 * This method is called when a {@link Probe} probe is received on this
	 * {@link IfaceLink} link.
	 * @param probe the {@link Probe} received
	 */
	public void incoming(Probe probe) {
		_iface.incoming(this, probe);
	}

	/**
	 * This method is called when a {@link Probe} probe is sent on this
	 * {@link IfaceLink} link.
	 * @param probe the {@link Probe} to send.
	 * @param nextHop the {@link IPNet} IP address of the next hop.
	 */
	public void outgoing(Probe probe, IPNet nextHop) {
		_monitor.receiveProbe(this, probe, nextHop);
	}


}
