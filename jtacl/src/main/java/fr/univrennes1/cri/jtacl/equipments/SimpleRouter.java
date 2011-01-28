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

package fr.univrennes1.cri.jtacl.equipments;

import fr.univrennes1.cri.jtacl.equipments.Generic.GenericEquipment;
import fr.univrennes1.cri.jtacl.core.exceptions.JtaclConfigurationException;
import fr.univrennes1.cri.jtacl.core.monitor.AccessControlList;
import fr.univrennes1.cri.jtacl.core.monitor.AclResult;
import fr.univrennes1.cri.jtacl.core.monitor.Log;
import fr.univrennes1.cri.jtacl.core.network.Iface;
import fr.univrennes1.cri.jtacl.core.network.IfaceLink;
import fr.univrennes1.cri.jtacl.core.network.Route;
import fr.univrennes1.cri.jtacl.core.network.Routes;
import fr.univrennes1.cri.jtacl.lib.ip.IPNet;
import fr.univrennes1.cri.jtacl.core.monitor.Probe;
import fr.univrennes1.cri.jtacl.core.monitor.Monitor;
import fr.univrennes1.cri.jtacl.core.monitor.ProbeResults;
import fr.univrennes1.cri.jtacl.lib.misc.Direction;
import fr.univrennes1.cri.jtacl.lib.xml.XMLUtils;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Level;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A test case for a simple equipment doing routing and firewalling.
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class SimpleRouter extends GenericEquipment {

	protected ArrayList<String> _acls = new ArrayList<String>();

	/**
	 * Create a new {@link SimpleRouter} with this name and this comment.<br/>
	 * @param monitor the {@link Monitor} monitor associated with this equipment.
	 * @param name the name of the equipment.
	 * @param comment a free comment for this equipment.
	 * @param configurationFileName name of the configuration file to use (may be null).
	 */
	public SimpleRouter(Monitor monitor, String name, String comment, String configurationFileName) {
		super(monitor, name, comment, configurationFileName);
	}

	protected void loadAcl(Document doc) {
		NodeList list = doc.getElementsByTagName("acl");
		for (int i = 0; i < list.getLength(); i++) {
			Element e = (Element) list.item(i);
			String sstring = e.getAttribute("string");

			if (sstring.isEmpty())
				throw new JtaclConfigurationException("Missing acl string");
			_acls.add(sstring);
		}
	}

	protected void loadIfaces(Document doc) {

		NodeList list = doc.getElementsByTagName("iface");
		for (int i = 0; i < list.getLength(); i++) {
			Element e = (Element) list.item(i);
			String name = e.getAttribute("name");
			String comment = e.getAttribute("comment");
			String ifIp = e.getAttribute("ip");
			String ifNetwork = e.getAttribute("network");

			String s = "name: " + name + " comment: " + comment +
					" IP: " + ifIp + " network: " + ifNetwork;

			if (name.isEmpty())
				throw new JtaclConfigurationException("Missing interface name: " + s);

			if (ifIp.isEmpty())
				throw new JtaclConfigurationException("Missing interface IP: " + s);

			IPNet ip;
			try {
				ip = new IPNet(ifIp);
				ip = ip.hostAddress();
			} catch (UnknownHostException ex) {
				throw new JtaclConfigurationException("Invalid interface IP: " + s);
			}

			IPNet network;
			try {
				/*
				 * If network attribute is empty, use the network address of the IP
				 * instead.
				 */
				if (ifNetwork.isEmpty())
					network = new IPNet(ifIp);
				else
					network = new IPNet(ifNetwork);
				network = network.networkAddress();
			} catch (UnknownHostException ex) {
				throw new JtaclConfigurationException("Invalid interface network: " + s);
			}
			
			/*
			 * Append the link to an existing Iface or create a new one.
			 */
			Iface iface = getIface(name);
			if (iface == null) {
				if (comment.isEmpty())
					throw new JtaclConfigurationException("Missing interface comment: " + s);
				iface = addIface(name, comment);
			}
			iface.addLink(ip, network);
		}
	}

	@Override
	public void configure() {

		if (_configurationFileName.isEmpty())
			return;

		Document doc = XMLUtils.getXMLDocument(_configurationFileName);

		loadIfaces(doc);
		routeDirectlyConnectedNetworks();
		loadRoutesFromXML(doc);
		loadAcl(doc);

	}

	@Override
	public void incoming(IfaceLink link, Probe probe) {

		if (Log.debug().isLoggable(Level.INFO))
			Log.debug().info("probe" + probe.uidToString() + " incoming on " + _name);

		probe.decTimeToLive();
		if (!probe.isAlive()) {
			probe.killError("TimeToLive expiration");
			return;
		}

		/*
		 * Filter in the probe
		 */
		packetFilter(link, Direction.IN, probe);

		/*
		 * Check if the destination of the probe is on this equipment.
		 */
		IfaceLink ilink = getIfaceLink(probe.getDestinationAddress());
		if (ilink != null) {
			/*
			 * Set the probe's final position and notify the monitor
			 */
			probe.setOutgoingLink(ilink, probe.getDestinationAddress());
			probe.destinationReached("destination reached");
			return;
		}

		/*
		 * Route the probe.
		 */
		Routes routes = null;
		routes = _routingEngine.getRoutes(probe);
		if (routes.isEmpty()) {
			probe.killNoRoute("No route to " + probe.getDestinationAddress());
			return;
		}
		probe.routed(probe.getDestinationAddress().toString("i::"));

		if (Log.debug().isLoggable(Level.INFO)) {
			for (Route r: routes) {
				Log.debug().info("route: " + r.toString());
			}
		}

		/*
		 * if we have several routes for a destination, we have to probe these
		 * routes too because our goal is to know if a probe is able to
		 * reach a destination, regardless of the route taken.
		 */
		ArrayList<Probe> probes = new ArrayList<Probe>();
		probes.add(probe);

		/*
		 * Create copies of the incoming probe to describe the other routes.
		 */
		for (int i = 1; i < routes.size(); i ++) {
			probes.add(probe.newInstance());
		}

		/*
		 * Set the position of the probes.
		 */
		for (int i = 0; i < routes.size(); i ++) {
			Route<IfaceLink> route = routes.get(i);
			probes.get(i).setOutgoingLink(route.getLink(), route.getNextHop());
		}

		/*
		 * Filter out the probes
		 */
		for (Probe p: probes) {
			packetFilter(p.getOutgoingLink(), Direction.OUT, p);
		}

		/*
		 * Send the probes over the network.
		 */
		for (Probe p: probes) {
			/*
			 * Do not send the probe if the outgoing link is the same as the input.
			 */
			if (!p.getOutgoingLink().equals(link))
				outgoing(p.getOutgoingLink(), p, p.getNextHop());
			else
				probe.killLoop("same incoming and outgoing link");
		}
	}

	protected void packetFilter (IfaceLink link, Direction direction, Probe probe) {
		/*
		 *  Example with a "last rule match" firewall.
		 * 	the acls language of this filter is very simple :
		 *
		 *  <accept|deny> <on|out|in> iface from IP to IP.
		 */

		boolean input = direction == Direction.IN;
		ProbeResults result = probe.getResults();
		AccessControlList lastAcl = null;

		for (int i = 0; i < _acls.size(); i++) {
			try {
				String acl = _acls.get(i);
				String[] trules = acl.split(" ");
				if (trules.length != 7) {
					throw new JtaclConfigurationException("invalid acl + " + acl);
				}
				String raction = trules[0];
				String rdirection = trules[1];
				String riface = trules[2];
				String rfromIP = trules[4];
				String rtoIP = trules[6];
				/*
				 * Compare iface * == any
				 */
				if (!riface.equals("*")) {
					if (!riface.equals(link.getIface().getName())) {
						continue;
					}
				}
				
				/*
				 * Check the direction
				 */
				if (rdirection.equals("in") && !input)
					continue;
				if (rdirection.equals("out") && input)
					continue;

				/*
				 * Compare from and to IP addresses
				 */
				IPNet fromIP = new IPNet(rfromIP);
				IPNet toIP = new IPNet(rtoIP);
				if (!fromIP.networkContains(probe.getSourceAddress())) {
					continue;
				}
				if (!toIP.networkContains(probe.getDestinationAddress())) {
					continue;
				}
				AclResult aclResult = new AclResult(raction.equals("accept") ?
					AclResult.ACCEPT : AclResult.DENY);

				result.addMatchingAcl(input ? Direction.IN : Direction.OUT, acl, aclResult);
				/*
				 * Keep the last acl that matches the probe because this firewall uses
				 * a "last rule winner" logic.
				 */
				lastAcl = new AccessControlList(acl, aclResult);
			} catch (UnknownHostException ex) {
				throw new JtaclConfigurationException(ex.getMessage());
			}
		}
		if (lastAcl == null) {
			// default policy is accept
			result.setAclResult(input ? Direction.IN : Direction.OUT,
					new AclResult(AclResult.ACCEPT));
			return;
		}
		/*
		 * Put the acl as the active acl in the result
		 */
		result.addActiveAcl(input ? Direction.IN : Direction.OUT,
				lastAcl.getAclString(), lastAcl.getResult());
		result.setAclResult(input ? Direction.IN : Direction.OUT, lastAcl.getResult());
	}

}
