/*
 * Copyright (c) 2011, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */
package fr.univrennes1.cri.jtacl.equipments.openbsd;

import fr.univrennes1.cri.jtacl.core.exceptions.JtaclInternalException;
import fr.univrennes1.cri.jtacl.core.network.IfaceLink;
import fr.univrennes1.cri.jtacl.lib.ip.IPNet;

/**
 * Route options in rule.<br/>
 * XXX: only ROUTETO options are handled.
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */

public class PfRouteOpts {
	protected PfRouteOptsType _type;
	protected String _ifName;
	protected IPNet _nextHop;
	protected IfaceLink _link;

	/**
	 * Creates a new PfRouteOpts of type PF_ROUTETO.
	 * @return a new PfRouteOpts of type PF_ROUTETO.
	 */
	public static PfRouteOpts newRouteOptsRouteTo() {
		PfRouteOpts routeOpts = new PfRouteOpts();
		routeOpts._type  = PfRouteOptsType.PF_ROUTETO;
		return routeOpts;
	}

	/**
	 * Set the route.
	 * @param ifName name of the interface.
	 * @param nextHop IP address of the nexthop.
	 * @param link IfaceLink on the interface.
	 * @throws JtaclInternalException if the type of this instance is not of
	 * type ROUTETO
	 * 
	 */
	public void setRoute(String ifName, IPNet nextHop, IfaceLink link) {
		if (!isRouteTo())
			throw new JtaclInternalException("invalid route opt type");
		_ifName = ifName;
		_nextHop = nextHop;
		_link = link;
	}

	/**
	 * Returns the interface name.
	 * @return the interface name.
	 * @throws JtaclInternalException if the type of this instance is not of
	 * type ROUTETO 
	 */
	public String getIfName() {
		if (!isRouteTo())
			throw new JtaclInternalException("invalid route opt type");
		return _ifName;
	}
	
	/**
	 * Returns the nexthop.
	 * @return the nexthop.
	 * @throws JtaclInternalException if the type of this instance is not of
	 * type ROUTETO 
	 */
	public IPNet getNextHop() {
		if (!isRouteTo())
			throw new JtaclInternalException("invalid route opt type");
		return _nextHop;
	}

	/**
	 * Returns the link.
	 * @return the link.
	 * @throws JtaclInternalException if the type of this instance is not of
	 * type ROUTETO 
	 */
	public IfaceLink getLink() {
		if (!isRouteTo())
			throw new JtaclInternalException("invalid route opt type");
		return _link;		
	}

	/**
	 * Checks that this instance is of type ROUTETO.
	 * @return true if this instance is of type ROUTETO.
	 */
	public boolean isRouteTo() {
		return _type == PfRouteOptsType.PF_ROUTETO;
	}
	
	/**
	 * Returns the type of this instance.
	 * @return the type of this instance.
	 */
	public PfRouteOptsType getType() {
		return _type;
	}

	@Override
	public String toString() {
		String s = _type.toString();
		if (isRouteTo()) {
			s = s + " " + _ifName + "," + _nextHop.toString("::i");
		}
		return s;
	}

}
