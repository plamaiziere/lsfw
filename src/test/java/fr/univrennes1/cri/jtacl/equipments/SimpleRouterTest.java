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

import fr.univrennes1.cri.jtacl.core.monitor.Monitor;
import fr.univrennes1.cri.jtacl.core.network.Iface;
import fr.univrennes1.cri.jtacl.core.network.IfaceLink;
import fr.univrennes1.cri.jtacl.lib.ip.IPNet;
import java.net.URL;
import java.net.UnknownHostException;
import junit.framework.TestCase;

/**
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class SimpleRouterTest extends TestCase {


    public SimpleRouterTest(String testName) {
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
	 * Test of configure method, of class SimpleRouter.
	 */
	public void testConfigure() throws UnknownHostException {
		System.out.println("configure");
		URL url = this.getClass().getResource("/TestConfigSimpleRouter1.xml");
		String fileName  = url.getFile();
		Monitor monitor = Monitor.getInstance();
		SimpleRouter router = new SimpleRouter(monitor, "Router1", "a router", fileName);
		router.configure();
		
		// we should have two interfaces eth0, eth1
		Iface iface1 = router.getIface("eth0");
		Iface iface2 = router.getIface("eth1");
		assertTrue(iface1 != null);
		assertTrue(iface2 != null);

		// check links
		IPNet ip = new IPNet("192.168.0.1");
		IPNet network = new IPNet("192.168.0.0/24");
		IfaceLink link = iface1.getLink(ip);
		assertTrue(link != null);
		assertEquals(iface1, link.getIface());
		assertEquals(ip, link.getIp());
		assertEquals(network, link.getNetwork());

		ip = new IPNet("192.168.1.1");
		network = new IPNet("192.168.1.0/24");
		link = iface2.getLink(ip);
		assertTrue(link != null);
		assertEquals(iface2, link.getIface());
		assertEquals(ip, link.getIp());
		assertEquals(network, link.getNetwork());


	}

}
