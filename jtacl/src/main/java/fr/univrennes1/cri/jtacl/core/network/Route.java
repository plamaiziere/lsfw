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

import fr.univrennes1.cri.jtacl.core.exceptions.JtaclRoutingException;
import fr.univrennes1.cri.jtacl.lib.ip.IPNet;
import java.net.UnknownHostException;

/**
 * This class describes a route.<br/>
 * A route is composed by a {@link IPNet} IP prefix, the {@link IPNet} IP
 * address of the next hop, and a metric that could be use to sort routes.
 *
 * A route is associated to a link describing the outgoing link for this route.
 *
 * @param <T> Type of the link.
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class Route<T> {

	/**
	 * the IP prefix of this route.
	 */
	protected IPNet _prefix;

	/**
	 * the IP address of the nexthop.
	 */
	protected IPNet _nextHop;

	/**
	 * metric of this route.
	 */
	protected int _metric;

	/**
	 * the outgoing link associated to this route.
	 */
	protected T _link;

	/**
	 * Creates a new {@link Route} route.
	 * @param prefix the {@link IPNet} prefix of the route.
	 * @param nextHop the {@link IPNet} IP address of the nextHop.
	 * @param metric the metric of this route.
	 * @param link the outgoing link associated to this route. Could be null.
	 */
	public Route(IPNet prefix, IPNet nextHop, int metric, T link) {
		try {
			/*
			 * we assert that prefix is really a network prefix.
			 */
			if (!prefix.networkAddress().equals(prefix)) {
				throw new JtaclRoutingException("route with not a network address prefix");
			}
		} catch (UnknownHostException ex) {
				throw new JtaclRoutingException("route with invalid prefix: " +
					ex.getMessage());
		}
		_prefix = prefix;
		_nextHop = nextHop;
		_metric = metric;
		_link = link;
	}

	/**
	 * Retuns the metric of this route.
	 * @return the metric of this route.
	 */
	public int getMetric() {
		return _metric;
	}

	/**
	 * Returns the {@link IPNet} IP address of this route.
	 * @return the {@link IPNet} IP address of this route.
	 */
	public IPNet getNextHop() {
		return _nextHop;
	}

	/**
	 * Returns the {@link IPNet} prefix of this route.
	 * @return the {@link IPNet} prefix of this route.
	 */
	public IPNet getPrefix() {
		return _prefix;
	}

	/**
	 * Returns the link associated to this route. Could be null.
	 * @return the link associated to this route.
	 */
	public T getLink() {
		return _link;
	}

	@Override
	public String toString() {
		String slink = (_link == null) ? "none": _link.toString();
		return _prefix.toString("::") + " -> " + _nextHop.toString("i::") +
				" (" + _metric + ") link: " + slink;
	}

}
