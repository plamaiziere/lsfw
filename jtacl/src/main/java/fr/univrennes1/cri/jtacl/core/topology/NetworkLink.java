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

package fr.univrennes1.cri.jtacl.core.topology;

import fr.univrennes1.cri.jtacl.core.network.IfaceLink;
import fr.univrennes1.cri.jtacl.core.network.IfaceLinks;
import fr.univrennes1.cri.jtacl.core.network.NetworkEquipment;
import fr.univrennes1.cri.jtacl.core.network.NetworkEquipmentsByName;
import fr.univrennes1.cri.jtacl.lib.ip.IPNet;

/**
 * Describes a link between one network and several interfaces link.
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class NetworkLink implements Comparable {

	/**
	 * the netork IP address.
	 */
	protected IPNet _network;

	/**
	 * the ifaceLinks attached to this link.
	 */
	protected IfaceLinks _ifaceLinks = new IfaceLinks();

	/**
	 * True if this link is a border link. A border link always accepts a probe
	 * regardless of the nexthop of the probe.
	 */
	protected boolean _borderLink;

	/**
	 * the equipments connected by this link.
	 */
	protected NetworkEquipmentsByName _equipments = new NetworkEquipmentsByName();

	/**
	 * Creates a new {@link NetworkLink} link with this {@link IPNet} network
	 * IP address.
	 * @param borderLink set to true if this link will be a 'border link'.
	 * @param network the {@link IPNet} IP address of this {@link NetworkLink} link.
	 */
	public NetworkLink(boolean borderLink, IPNet network) {
		_borderLink = borderLink;
		_network = network;
	}

	/**
	 * Returns the {@link IPNet} IP address of this {@link NetworkLink} link.
	 * @return the {@link IPNet} IP address of this {@link NetworkLink} link.
	 */
	public IPNet getNetwork() {
		return _network;
	}

	/**
	 *  Returns the {@link NetworkEquipment} equipments associated to this
	 * {@link NetworkLink} link.
	 * @return a {@link NetworkEquipmentsByName} map containing the
	 * {@link NetworkEquipment} equipments associated to this
	 * {@link NetworkLink} link.
	 */
	public NetworkEquipmentsByName getEquipments() {
		return _equipments;
	}

	/**
	 *  Returns the {@link IfaceLink} links associated to this
	 * {@link NetworkLink} link.
	 * @return a {@link IfaceLinks} listmap containing the
	 * {@link IfaceLink} links associated to this
	 * {@link NetworkLink} link.
	 */
	public IfaceLinks getIfaceLinks() {
		return _ifaceLinks;
	}

	/**
	 *  Returns the {@link IfaceLink} link with the {@link IPNet} IP address ip
	 * associated to this {@link NetworkLink} link
	 * @param ip {@link IPNet} IP address of the {@link IfaceLink} link to find.
	 * @return the {@link IfaceLink} link associated to this
	 * {@link NetworkLink} link.
	 */
	public IfaceLink getIfaceLink(IPNet ip) {
		for (IfaceLink link: _ifaceLinks) {
			if (link.getIp().equals(ip))
				return link;
		}
		return null;
	}


	/**
	 * Add an {@link IfaceLink} link to this {@link NetworkLink} link.
	 * @param ifaceLink The {@link IfaceLink} link to add.
	 */
	public void addIfaceLink(IfaceLink ifaceLink) {
		NetworkEquipment ne = ifaceLink.getIface().getEquipment();

		_ifaceLinks.add(ifaceLink);
		if (!isConnectedTo(ne))
			_equipments.put(ne);
	}

	/**
	 * Checks if a {@link NetworkEquipment} equipment is connected to this
	 * {@link NetworkLink} link.
	 * @param equipment the {@link NetworkEquipment} to test.
	 * @return true if the {@link NetworkEquipment} equipment is connected to this
	 * {@link NetworkLink} link.

	 */
	public boolean isConnectedTo(NetworkEquipment equipment) {
		return _equipments.get(equipment.getName()) != null;
	}

	/**
	 * Returns all the {@link IfaceLink} links connected to a {@link NetworkEquipment}
	 * in this {@link NetworkLink}.
	 * @param equipment the {@link NetworkEquipment} to test.
	 * @return A {@link IfaceLinks} list containing the {@link IfaceLink}.
	 * The returned list can be empty but not null.
	 */
	public IfaceLinks getIfaceLinksConnectedTo(NetworkEquipment equipment) {
		IfaceLinks links = new IfaceLinks();

		// don't waste time if equipment is unknown
		if (!isConnectedTo(equipment))
			return links;

		for (IfaceLink link: _ifaceLinks) {
			if (link.getIface().getEquipment().equals(equipment))
				links.add(link);
		}
		return links;
	}

	/**
	 * Checks if this {@link NetworkLink} link is a 'border link'.
	 * @return if this {@link NetworkLink} link is a 'border link'.
	 */
	public boolean isBorderLink() {
		return _borderLink;
	}

	@Override
	public String toString() {
		String net = _network.toString();
		String r = "";
		for (IfaceLink link: _ifaceLinks) {
			String equipmentName = link.getIface().getEquipment().getName();
			String eq = equipmentName + "(" + link.getIface().getName() + " - " +
					link.getIp().toString("i::");
			r = r + eq + "), ";
		}
	if (r.length() > 2 )
		r = r.substring(0, r.length() - 2);
	return net + " {" + r + "}";
	}

	public int compareTo(Object o) {
		NetworkLink obj = (NetworkLink) o;
		if (equals(obj))
			return 0;
		return getNetwork().compareTo(obj.getNetwork());
	}
}
