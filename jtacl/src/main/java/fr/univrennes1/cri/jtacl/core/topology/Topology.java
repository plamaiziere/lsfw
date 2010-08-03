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

import fr.univrennes1.cri.jtacl.core.exceptions.JtaclTopologyException;
import fr.univrennes1.cri.jtacl.core.network.Iface;
import fr.univrennes1.cri.jtacl.core.network.IfaceLink;
import fr.univrennes1.cri.jtacl.core.network.IfaceLinksByIp;
import fr.univrennes1.cri.jtacl.core.network.IfacesByName;
import fr.univrennes1.cri.jtacl.core.network.NetworkEquipment;
import fr.univrennes1.cri.jtacl.core.network.NetworkEquipmentsByName;
import fr.univrennes1.cri.jtacl.lib.ip.IPNet;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class describes the topology of the network.
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class Topology {

	/**
	 * All the equipments on the network.
	 */
	protected NetworkEquipmentsByName _equipments = new NetworkEquipmentsByName();

	/**
	 * links between ifacelink and so between equipments.
	 */
	protected NetworkLinks _networkLinks = new NetworkLinks();
	private ArrayList<TopologicalLink> _topologicalLinks = new ArrayList<TopologicalLink>();

	/**
	 * Builds the topology of the network. The topology is describes by some
	 * network links which describe the links beetween ifaces.
	 */
	public void makeTopology() {

		/*
		 * iterate through all the equipments to retrieve their iface links
		 */
		ArrayList<IfaceLink> ifacesLinks = new ArrayList<IfaceLink>();
		// each network equipment
		for (String ename: _equipments.keySet()) {
			NetworkEquipment ne = _equipments.get(ename);
			IfacesByName Ifaces = ne.getIfaces();

			// each interface
			for (Iface iface: Ifaces.values()) {
				IfaceLinksByIp iLinks = iface.getLinks();

				// each interface link
				for (IfaceLink ilink: iLinks.values()) {
					ifacesLinks.add(ilink);
				}
			}
		}

		/*
		 * Connect equipments according to the topological links
		 */
		 ArrayList<TopologicalLink> tlinks = (ArrayList<TopologicalLink>) _topologicalLinks.clone();
		 // each topological links
		 while (!tlinks.isEmpty()) {
			TopologicalLink tlink = tlinks.get(0);
			IPNet network = tlink.getNetwork();
			NetworkLink nlink = new NetworkLink(tlink.isBorderLink(), network);

			// each IfaceLink of the topological link
			for (IfaceLink ilink: tlink.getLinks()) {
				/*
				 * Search if this ifaceLink in the main list of ifaceLink.
				 * If found, connect the ifaceLink to the network link and remove
				 * it from the main list.
				 */
				int index = ifacesLinks.indexOf(ilink);
				if (index > -1) {
					// connect the ifaceLink to the network link
					nlink.addIfaceLink(ilink);
					ilink.setNetworkLink(nlink);
					// and remove this ifaceLink
					ifacesLinks.remove(index);
				}
			}

			/*
			 * be sure that all ifacelink have been connected
			 */
			// each IfaceLink of the topological link
			for (IfaceLink ilink: tlink.getLinks()) {
				if (!nlink.getIfaceLinks().contains(ilink))
					throw new JtaclTopologyException("Can't connect topological link" +
						" equipment: " + ilink.getIface().getEquipment().getName() +
						" link: " + ilink.toString());
			}
			// add the network link to the list and remove it from the known list
			_networkLinks.add(nlink);
			tlinks.remove(0);
		 }

		/*
		 * Try to connect each link to each other according to their network
		 */
		while (!ifacesLinks.isEmpty()) {
			IfaceLink iLink = ifacesLinks.get(0);
			IPNet network = iLink.getNetwork();
			NetworkLinks candidateLinks = getNetworkLinksByNetwork(network);
			NetworkLink nlink = null;
			if (candidateLinks.size() == 0) {
				nlink = new NetworkLink(false, network);
				_networkLinks.add(nlink);
			} else {
				if (candidateLinks.size() == 1) {
					nlink = candidateLinks.get(0);
				} else {
					// we have found more than one network so we don't know
					// which one to choose. User must solve ambiguity
					throw new JtaclTopologyException("More than one network" +
							" was found. Topology is ambiguous." +
							" network: " + iLink.getNetwork().toString() +
							" equipment: " + iLink.getIface().getEquipment().getName() +
							" interface: " + iLink.getIface().getName());
				}
			}
			// connect this iface to this link
			if (nlink != null) {
				nlink.addIfaceLink(iLink);
				iLink.setNetworkLink(nlink);
			}
			ifacesLinks.remove(0);
		}
	}

	/**
	 * Registers an equipment into the topology.
	 * @param equipment the {@link NetworkEquipment} equipment to register.
	 * @throws JtaclTopologyException if the equipment is already registered.
	 */
	public void registerNetworkequipment(NetworkEquipment equipment)
		throws JtaclTopologyException {

		if (isRegistered(equipment))
			throw new JtaclTopologyException("equipment already registered: " +
				equipment.getName());

		_equipments.put(equipment);
	}

	/**
	 * Cheks if a {@link NetworkEquipment} equipment is registered into the
	 * topology.
	 * @param equipment the equipment to test.
	 * @return true if the equipment is registered into the topology.
	 */
	public boolean isRegistered(NetworkEquipment equipment) {
		return getEquipment(equipment.getName()) != null;
	}

	/**
	 * Returns the {@link NetworkEquipment} equipment associated
	 * with the name in argument.
	 * @param name the name of the equipment to retrieve.
	 * @return the equipment associated with this name. Null if the equipment
	 * is unknown.
	 */
	public NetworkEquipment getEquipment(String name) {
		return _equipments.get(name);
	}

	/**
	 * Returns all the {@link NetworkLink} links in the topology.
	 * @return a {@link NetworkLinks} list containing the links.
	 */
	public NetworkLinks getNetworkLinks() {
		return (NetworkLinks) _networkLinks.clone();
	}

	/**
	 * Returns all the {@link NetworkLink} links that match this {@link IPNet} network.
	 * @param network the {@link IPNet} network of the {@link NetworkLink} links.
	 * @return a {@link NetworkLinks} list containing the links.
	 */
	public NetworkLinks getNetworkLinksByNetwork(IPNet network) {
		NetworkLinks links = new NetworkLinks();

		for (NetworkLink link: _networkLinks) {
			if (link.getNetwork().equals(network))
				links.add(link);
		}
		return links;
	}

	/**
	 * Returns all the {@link NetworkLink} links containing this {@link IPNet} IP 
	 * on their subnet.
	 * @param ip the {@link IPNet} IP Address contained by the {@link NetworkLink} links.
	 * @return a {@link NetworkLinks} list containing the links.
	 * @throws JtaclTopologyException if an item can not be expressed as a network.
	 */
	public NetworkLinks getNetworkLinksByIP(IPNet ip) {
		NetworkLinks links = new NetworkLinks();

		for (NetworkLink link: _networkLinks) {
			try {
				if (link.getNetwork().networkContains(ip)) {
					links.add(link);
				}
			} catch (UnknownHostException ex) {
				throw new JtaclTopologyException(ex.getMessage());
			}
		}
		return links;
	}

	/**
	 * Returns all the {@link NetworkLink} links connected to a
	 * {@link NetworkEquipment} equipment.
	 * @param equipment the {@link NetworkEquipment} equipment connected.
	 * @return a {@link NetworkLinks} list containing the links.
	 */
	public NetworkLinks getNetworkLinksByEquipment(NetworkEquipment equipment) {
		NetworkLinks links = new NetworkLinks();

		for (NetworkLink link: _networkLinks) {
			if (link.isConnectedTo(equipment))
				links.add(link);
		}
		return links;
	}

	/**
	 * Creates and adds a {@link TopologicalLink} link into the topology.
	 * @param borderLink set to true if the link to add is a border link.
	 * @param sNetwork the network address of the link.
	 * @param strLinks A textual representaton of the links beetween equipment.
	 * The format is equipment-addressIP, equipement-addressIP, ...
	 * @see TopologicalLink
	 */
	public void addTopologicalLink(boolean borderLink, String sNetwork, String strLinks) {

		/*
		 * parse a link : "equipment|ip, equipmen|ip, ..."
		 */
		IPNet network;
		try {
			network = new IPNet(sNetwork).networkAddress();
		} catch (UnknownHostException ex) {
			throw new JtaclTopologyException("Topological link: network address invalid " + sNetwork);
		}

		TopologicalLink tLink = new TopologicalLink(borderLink, network);

		String [] splits = strLinks.split(",");
		for (String sLink: splits) {
			String s[] = sLink.split("\\|");
			if (s.length != 2)
				throw new JtaclTopologyException("Topological link: invalid " + strLinks);
			String sName = s[0].trim();
			String sIp = s[1].trim();

			NetworkEquipment equipment = _equipments.get(sName);
			if (equipment == null)
				throw new JtaclTopologyException("Topological link: equipment unknown " + strLinks);

			IPNet ip;
			try {
				ip = new IPNet(sIp);
			} catch (UnknownHostException ex) {
				throw new JtaclTopologyException("Topological link: IP address invalid " + strLinks);
			}
			IfaceLink link = equipment.getIfaceLink(ip);
			if (link == null) {
				throw new JtaclTopologyException("Topological link: no link found " + strLinks);
			}
			tLink.getLinks().add(link);
		}
		_topologicalLinks.add(tLink);
	}

	@Override
	public String toString() {
		return showTopology(null);
	}

	public String showTopology(NetworkEquipment equipment) {
		String s = "";
		Iterator<NetworkLink> it = _networkLinks.iterator();
		while (it.hasNext()) {
			NetworkLink nlink = it.next();
			if (equipment == null || nlink.isConnectedTo(equipment)) {
				s = s + nlink.toString();
				if (it.hasNext())
					s = s + "\n";
			}
		}
		return s;
	}
}

