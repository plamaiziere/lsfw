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

import fr.univrennes1.cri.jtacl.core.probing.Probe;
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

		@Override
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
	private RoutingTable _sourceRoutingTableIPv4;
	private RoutingTable _sourceRoutingTableIPv6;

	private RoutingTable routingTable(IPversion version) {
		return (version == IPversion.IPV4) ? _routingTableIPv4 : _routingTableIPv6;
	}

	private RoutingTable sourceRoutingTable(IPversion version) {
		return (version == IPversion.IPV4) ? _sourceRoutingTableIPv4 :
			_sourceRoutingTableIPv6;
	}

	private void showRoute(PrintWriter writer, Route route) {
		writer.print(route.getPrefix().toString("") + " ");
		if (route.isNullRoute()) {
			writer.print("null-route");
		} else {
			writer.print(route.getNextHop().toString("i") + " ");
			writer.print(route.getMetric());
			writer.print(" link = " + route.getLink());
		}
		writer.println();
	}

	private void showRoutingTable(PrintWriter writer, RoutingTable table) {
		ArrayList<RoutingTableItem> stable = new ArrayList<RoutingTableItem>();
		stable.addAll(table.values());
		Collections.sort(stable);
		if (stable.isEmpty())
			writer.println("(none)");
		for (RoutingTableItem routeItem: stable) {
			for (Route route: routeItem._routes) {
				showRoute(writer, route);
			}
		}
	}

	private Routes getRoutes(IPNet address, RoutingTable table) {

		/*
		 * The algorithm used is quite stupid.
		 * We try to find routes starting with the address prefixlen.
		 * If no routes has been found, try with prefixlen - 1, until all
		 * prefixlen have been tested.
		 */
		IPNet previous = null;
		for (int i = address.getPrefixLen(); i >= 0; i--) {
			// prefix of the destination
			IPNet prefix;
			try {
				prefix = new IPNet(address.getIP(), address.getIpVersion(), i);
				prefix = prefix.networkAddress();
				/*
				 * Do not test again if the network is the same as the
				 * previous test.
				 */
				if (previous != null && prefix.equals(previous))
					continue;
				previous = prefix;
			} catch (UnknownHostException ex) {
				throw new JtaclRoutingException(ex.getMessage());
			}
			// search the prefix in the routing table
			Integer prefixKey = prefix.hashCode();
			RoutingTableItem routeItem = table.get(prefixKey);
			if (routeItem != null) {
				Routes routes = new Routes();
				for (Route route: routeItem._routes) {
					/*
					 * If the link is null for a route, use the route matching
					 * the nexthop. TODO: control infinite recursion.
					 */
					if (!route.isNullRoute()) {
						if (route.getLink() == null) {
							IPNet nexthop = route.getNextHop();
							/*
							 * XXX: nexthop is a destination
							 */
							Routes resolv = getRoutes(nexthop);
							if (resolv != null)
								routes.addAll(resolv);
						} else
							routes.add(route);
					}
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
	 * Creates a new {@link RoutingEngine} instance.
	 */
	public RoutingEngine() {
		_routingTableIPv4 = new RoutingTable();
		_routingTableIPv6 = new RoutingTable();
		_sourceRoutingTableIPv4 = new RoutingTable();
		_sourceRoutingTableIPv6 = new RoutingTable();
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
	 * Adds a source route into this {@link RoutingEngine} instance.
	 * @param route the {@link Route} to add.
	 */
	public void addSourceRoute(Route route) {
		RoutingTable table = sourceRoutingTable(route.getPrefix().getIpVersion());
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

		RoutingTable table = routingTable(destination.getIpVersion());
		return getRoutes(destination, table);
	}

	/**
	 * Returns the source routes that match this source address.
	 * @param source the {@link IPNet} IP address of the source.
	 * @return a {@link Routes} list containing the routes. The list could be
	 * empty but not null.
	 * @throws JtaclRoutingException if problem occurs.
	 */
	public Routes getSourceRoutes(IPNet source) {

		RoutingTable table = sourceRoutingTable(source.getIpVersion());
		return getRoutes(source, table);
	}

	/**
	 * Returns the source-routes or routes for a {@link Probe} probe
	 * @param probe a {@link Probe} probe containing the source/destination.
	 * @return a {@link Routes} list containing the routes. The list could be
	 * empty but not null.
	 * @throws JtaclRoutingException if problem occurs.
	 */
	public Routes getRoutes(Probe probe) {
		Routes sourceRoutes = getSourceRoutes(probe.getSourceAddress());
		if (!sourceRoutes.isEmpty())
			return sourceRoutes;

		return getRoutes(probe.getDestinationAddress());
	}

	@Override
	public String showRoutes() {
		CharArrayWriter swriter = new CharArrayWriter();
		PrintWriter writer = new PrintWriter(swriter);

		if (!_sourceRoutingTableIPv4.isEmpty()) {
			writer.println("IPv4 source-routes");
			showRoutingTable(writer, _sourceRoutingTableIPv4);
			writer.println();
		}
		if (!_routingTableIPv4.isEmpty()) {
			writer.println("IPv4 routes");
			showRoutingTable(writer, _routingTableIPv4);
			writer.println();
		}
		if (!_sourceRoutingTableIPv6.isEmpty()) {
			writer.println("IPv6 source-routes");
			showRoutingTable(writer, _sourceRoutingTableIPv6);
			writer.println();
		}
		if (!_routingTableIPv6.isEmpty()) {
			writer.println("IPv6 routes");
			showRoutingTable(writer, _routingTableIPv6);
			writer.println();
		}
		writer.flush();
		return swriter.toString();
	}

}
