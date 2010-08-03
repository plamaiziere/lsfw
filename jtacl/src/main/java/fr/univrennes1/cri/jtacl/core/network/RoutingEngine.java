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

package fr.univrennes1.cri.jtacl.core.network;

import fr.univrennes1.cri.jtacl.core.monitor.Probe;
import fr.univrennes1.cri.jtacl.core.exceptions.JtaclRoutingException;
import fr.univrennes1.cri.jtacl.lib.ip.IPNet;
import fr.univrennes1.cri.jtacl.lib.ip.IPversion;
import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * The routing engine provides an implementation of routing. It is able to
 * route both IPv4 and IPv6.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class RoutingEngine implements ShowableRoutes {

	class RoutingTableItem implements Comparable {
		IPNet _prefix;
		Routes _routes = new Routes();

		RoutingTableItem(IPNet prefix) {
			_prefix = prefix;
		}

		public int compareTo(Object o) {
			RoutingTableItem item = (RoutingTableItem) o;
			if (equals(item))
				return 0;
			return _prefix.compareTo(item._prefix);
		}

	}

	class RoutingTable extends HashMap<Integer, RoutingTableItem> {

	}

	private RoutingTable _routingTableIPv4;
	private RoutingTable _routingTableIPv6;

	private RoutingTable routingTable(IPversion version) {
		return (version == IPversion.IPV4) ? _routingTableIPv4 : _routingTableIPv6;
	}

	private void showRoute(PrintWriter writer, Route route) {
		writer.print(route.getPrefix().toString("::") + "\t");
		writer.print(route.getNextHop().toString("i::") + "\t");
		writer.print(route.getMetric() + "\t");
		writer.print(route.getLink());
		writer.println();
	}

	/**
	 * Creates a new {@link RoutingEngine} instance.
	 */
	public RoutingEngine() {
		_routingTableIPv4 = new RoutingTable();
		_routingTableIPv6 = new RoutingTable();
	}

	/**
	 * Adds a route into this {@link RoutingEngine} instance.
	 * @param route the {@link Route} to add.
	 */
	public void addRoute(Route route) {
		RoutingTable table = routingTable(route.getPrefix().getIpVersion());
		Integer prefixKey = route.getPrefix().hashCode();
		RoutingTableItem routingItem =  table.get(prefixKey);

		if(routingItem == null) {
			routingItem = new RoutingTableItem(route.getPrefix());
			table.put(prefixKey, routingItem);
		}
		routingItem._routes.add(route);
	}

	/**
	 * Returns the routes that match this destination.
	 * @param destination the {@link IPNet} IP address of the destination.
	 * @return a {@link Routes} list containing the routes. The list could be
	 * empty but not null.
	 * @throws JtaclRoutingException if problem occurs.
	 */
	public Routes getRoutes(IPNet destination) {

		/*
		 * The algorithm used is quite stupid.
		 * We try to find routes starting with the destination prefixlen.
		 * If no routes have been found, try with prefixlen - 1, until all
		 * prefixlen have been tested.
		 */
		IPNet previous = null;
		for (int i = destination.getPrefixLen(); i >= 0; i--) {
			// prefix of the destination
			IPNet prefix;
			try {
				prefix = new IPNet(destination.getIP(), destination.getIpVersion(), i);
				prefix = prefix.networkAddress();
				/*
				 * Do not test again if the network is the same as the
				 * previous test.
				 */
				if (previous != null && prefix.equals(previous))
					continue;
				previous = IPNet.newInstance(prefix);
			} catch (UnknownHostException ex) {
				throw new JtaclRoutingException(ex.getMessage());
			}
			// search the prefix in the routing table
			Integer prefixKey = prefix.hashCode();
			RoutingTable table = routingTable(destination.getIpVersion());
			RoutingTableItem routeItem = table.get(prefixKey);
			if (routeItem != null) {
				Routes routes = new Routes();
				for (Route route: routeItem._routes) {
					/*
					 * If the link is null for a route, use the route matching
					 * the nexthop. TODO: control infinite recursion.
					 */
					if (route.getLink() == null) {
						IPNet nexthop = route.getNextHop();
						Routes resolv = getRoutes(nexthop);
						if (resolv != null)
							routes.addAll(getRoutes(nexthop));
					} else
						routes.add(route);
				}
				/*
				 * Check if we have several routes with the same nexthop.
				 * In this case, return only one route.
				 */
				for (int k = 0; k < routes.size(); k++) {
					Route route = routes.get(k);
					for (int l = k + 1; l < routes.size();) {
						if (routes.get(l).getNextHop().equals(route.getNextHop()))
							routes.remove(l);
						else
							l++;
					}
				}
				return routes;
			}
		}
		return new Routes();
	}

	/**
	 * Returns the routes that match the destination of an {@link Probe} probe.
	 * @param probe a {@link Probe} probe containing the destination.
	 * @return a {@link Routes} list containing the routes. The list could be
	 * empty but not null.
	 * @throws JtaclRoutingException if problem occurs.
	 */
	public Routes getRoutes(Probe probe) {
		return getRoutes(probe.getDestinationAddress());
	}

	public String showRoutes() {
		CharArrayWriter swriter = new CharArrayWriter();
		PrintWriter writer = new PrintWriter(swriter);
		writer.println("IPv4 routes");
		ArrayList<RoutingTableItem> table = new ArrayList<RoutingTableItem>();
		table.addAll(routingTable(IPversion.IPV4).values());
		Collections.sort(table);
		if (table.isEmpty())
			writer.println("(none)");
		for (RoutingTableItem routeItem: table) {
			for (Route route: routeItem._routes) {
				showRoute(writer, route);
			}
		}
		writer.println();
		writer.println("IPv6 routes");
		table.clear();
		table.addAll(routingTable(IPversion.IPV6).values());
		Collections.sort(table);
		if (table.isEmpty())
			writer.println("(none)");
		for (RoutingTableItem routeItem: table) {
			for (Route route: routeItem._routes) {
				showRoute(writer, route);
			}
			writer.println("");
		}
		writer.flush();
		return swriter.toString();
	}

}
