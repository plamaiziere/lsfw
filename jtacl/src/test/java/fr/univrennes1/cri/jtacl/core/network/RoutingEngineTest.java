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

import fr.univrennes1.cri.jtacl.lib.ip.IPNet;
import java.net.UnknownHostException;
import junit.framework.TestCase;

/**
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class RoutingEngineTest extends TestCase {
    
    public RoutingEngineTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

	/**
	 * Test of addRoute method, of class RoutingEngine.
	 */
	public void testAddRoute() throws UnknownHostException {
		System.out.println("addRoute");
		RoutingEngine rn = new RoutingEngine();

		IPNet prefix = new IPNet("192.168.0.0/24");
		IPNet nextHop = new IPNet("192.168.1.0");
		Route<Iface> route = new Route<>(prefix, nextHop, 1, null);
		rn.addRoute(route);
	}

	/**
	 * Test of addSourceRoute method, of class RoutingEngine.
	 */
	public void testAddSourceRoute() throws UnknownHostException {
		System.out.println("addSourceRoute");
		RoutingEngine rn = new RoutingEngine();

		IPNet prefix = new IPNet("192.168.0.0/24");
		IPNet nextHop = new IPNet("192.168.1.0");
		Route<Iface> route = new Route<>(prefix, nextHop, 1, null);
		rn.addSourceRoute(route);
	}
	

	/**
	 * Test of getRoutes method, of class RoutingEngine.
	 */
	public void testGetRoutes() throws Exception {
		System.out.println("getRoutes");
		RoutingEngine rn = new RoutingEngine();

		IPNet prefix = new IPNet("192.168.0.0/24");
		IPNet nextHop = new IPNet("192.168.1.1");
		Route<RoutingEngineTest> route = new Route<>(prefix, nextHop, 1, this);
		rn.addRoute(route);

		IPNet destination = new IPNet("192.168.0.1");
		Routes result = rn.getRoutes(destination);
		assertTrue(result.size() == 1);
		assertEquals(route, result.get(0));

		destination = new IPNet("192.168.1.1");
		result = rn.getRoutes(destination);
		assertTrue(result.size() == 0);

		prefix = new IPNet("192.168.0.0/24");
		nextHop = new IPNet("192.168.1.2");
		route = new Route<>(prefix, nextHop, 1, this);
		rn.addRoute(route);

		destination = new IPNet("192.168.0.1");
		result = rn.getRoutes(destination);
		assertTrue(result.size() == 2);

		destination = new IPNet("192.168.0.0/24");
		result = rn.getRoutes(destination);
		assertTrue(result.size() == 2);

		prefix = new IPNet("127.0.0.1");
		route = new Route<>(prefix);
		rn.addRoute(route);
		destination = new IPNet("127.0.0.1");		
		result = rn.getRoutes(destination);
		assertTrue(result.size() == 0);
		
	}

	/**
	 * Test of getSourceRoutes method, of class RoutingEngine.
	 */
	public void testSourceGetRoutes() throws Exception {
		System.out.println("getSourceRoutes");
		RoutingEngine rn = new RoutingEngine();

		IPNet prefix = new IPNet("192.168.0.0/24");
		IPNet nextHop = new IPNet("192.168.1.1");
		Route<RoutingEngineTest> route = new Route<>(prefix, nextHop, 1, this);
		rn.addSourceRoute(route);

		IPNet address = new IPNet("192.168.0.1");
		Routes result = rn.getSourceRoutes(address);
		assertTrue(result.size() == 1);
		assertEquals(route, result.get(0));
	}

}
