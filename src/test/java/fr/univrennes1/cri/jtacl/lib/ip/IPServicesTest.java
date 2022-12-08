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

package fr.univrennes1.cri.jtacl.lib.ip;

import java.io.IOException;
import java.io.InputStream;
import junit.framework.TestCase;

/**
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class IPServicesTest extends TestCase {
    
    public IPServicesTest(String testName) {
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
	 * Test of getServByName method, of class IPServices.
	 */
	public void testGetServByName() throws IOException {
		System.out.println("getServByName");
		IPServices services = IPServices.getInstance();
		InputStream stream = services.getClass().getResourceAsStream("/ip/services");
		services.readServices(stream);

		IPServEnt ent = services.getServByName("http", "tcp");
		assertEquals("http", ent.getName());
		assertEquals("www", ent.getAliases().get(0));
		assertEquals("www-http", ent.getAliases().get(1));
		assertEquals(80, ent.getPort());
		assertEquals("tcp", ent.getProto());

		ent = services.getServByName("www-http", "tcp");
		assertEquals("http", ent.getName());
		assertEquals("www", ent.getAliases().get(0));
		assertEquals("www-http", ent.getAliases().get(1));
		assertEquals(80, ent.getPort());
		assertEquals("tcp", ent.getProto());
	}

	/**
	 * Test of getServByPort method, of class IPServices.
	 */
	public void testGetServByPort() throws IOException {
		System.out.println("getServByPort");
		IPServices services = IPServices.getInstance();
		InputStream stream = services.getClass().getResourceAsStream("/ip/services");
		services.readServices(stream);

		IPServEnt ent = services.getServByPort(80, "tcp");
		assertEquals("http", ent.getName());
		assertEquals(80, ent.getPort());
		assertEquals("tcp", ent.getProto());

	}

}
