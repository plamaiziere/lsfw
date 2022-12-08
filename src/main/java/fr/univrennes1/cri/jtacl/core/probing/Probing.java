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

import java.util.ArrayList;

/**
 * A probing is a collection of probes (owned by a ProbesTracker) which are
 * probing to a destination.
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class Probing extends ArrayList<ProbesTracker> {

	/**
	 * Returns the global FwResult of this probing.
	 * @return the global FWResult of this probing.
	 */
	public FwResult getAclResult() {
		/*
		 * acl counters
		 */
		int accepted = 0;
		int denied = 0;
		int may = 0;
		int match = 0;

		/*
		 * each tracker
		 */
		for (ProbesTracker tracker: this) {
			FwResult aclResult = tracker.getAclResult();
			if (aclResult.hasAccept())
				accepted++;
			if (aclResult.hasDeny())
				denied++;
			if (aclResult.hasMay())
				may++;
			if (aclResult.hasMatch())
				match++;
		}

		/*
		 * Global ACL result
		 */
		FwResult aclResult = new FwResult();
		/*
		 * one result was MAY
		 */
		if (may > 0 || match > 0)
			aclResult.addResult(FwResult.MAY);

		/*
		 * some probes were accepted and some were denied => MAY
		 */
		if (match == 0 && accepted > 0 && denied > 0)
			aclResult.addResult(FwResult.MAY);

		/*
		 * all probes were accepted => ACCEPT
		 */
		if (match == 0 && accepted > 0 && denied == 0)
			aclResult.addResult(FwResult.ACCEPT);

		/*
		 * all probes were denied => DENY
		 */
		if (match == 0 && denied > 0 && accepted == 0) {
			aclResult.addResult(FwResult.DENY);
		}

		/*
		 * some probes were matching => MATCH
		 */
		if (match > 0) {
			aclResult.setResult(FwResult.MATCH);
		}

		return aclResult;
	}
	
	/**
	 * Returns the global RoutingResult of this probing.
	 * @return the global RoutingResult of this probing.
	 */
	public RoutingResult getRoutingResult() {
		/*
		 * routing counters
		 */
		int routed = 0;
		int notrouted = 0;
		int routeunknown =0;

		/*
		 * each tracker
		 */
		for (ProbesTracker tracker: this) {
			switch (tracker.getRoutingResult()) {
				case ROUTED:	routed++;
								break;
				case NOTROUTED:	notrouted++;
								break;
				default:
								routeunknown++;
			}
		}

		/*
		 * Global routing result
		 */
		RoutingResult routingResult = RoutingResult.UNKNOWN;
		/*
		 * one result was UNKNOWN.
		 */
		if (routeunknown > 0) {
			routingResult = RoutingResult.UNKNOWN;
		} else {
			/*
			 * some probes were routed, and some not => UNKNOWN
			 */
			if (routed > 0 && notrouted > 0) {
				routingResult = RoutingResult.UNKNOWN;
			} else {
				/*
				 * all probes were routed => ROUTED
				 */
				if (routed > 0) {
					routingResult = RoutingResult.ROUTED;
				}
				/*
				 * all probes were not routed => NOTROUTED
				 */
				if (notrouted > 0) {
					routingResult = RoutingResult.NOTROUTED;
				}
			}
		}
		return routingResult;
	}

	public boolean checkExpectedResult(ExpectedProbing expect) {
		/*
		 * XXX we need a better logic here.
		 */
		boolean testExpect = false;
		RoutingResult routingResult = getRoutingResult();
		FwResult aclResult = getAclResult();
		
		if (expect.isRouted() &&
				routingResult == RoutingResult.ROUTED)
			testExpect = true;
		if (expect.isNoneRouted() &&
				routingResult == RoutingResult.NOTROUTED)
			testExpect = true;
		if (expect.isUnaccepted() &&
				(routingResult == RoutingResult.NOTROUTED ||
				(aclResult.hasDeny() && !aclResult.hasMay())))
			testExpect = true;
		if (expect.isUnknown() &&
				routingResult == RoutingResult.UNKNOWN)
			testExpect = true;

		if (expect.isAccept() &&
				aclResult.hasAccept() && !aclResult.hasMay())
			testExpect = true;
		if (expect.isDeny() &&
				aclResult.hasDeny() && !aclResult.hasMay())
			testExpect = true;

		if (expect.isMay() &&
				aclResult.hasMay())
				testExpect = true;

		if (expect.isNot())
			testExpect = !testExpect;
		
		return testExpect;
	}

}
