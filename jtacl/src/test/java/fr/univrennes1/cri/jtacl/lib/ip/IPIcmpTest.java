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

package fr.univrennes1.cri.jtacl.lib.ip;

import java.io.IOException;
import java.io.InputStream;
import junit.framework.TestCase;

/**
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class IPIcmpTest extends TestCase {
    
    public IPIcmpTest(String testName) {
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
	 * Test of getIcmpByName method, of class IPIcmp.
	 */
	public void testGetIcmpByName() throws IOException {
		System.out.println("getIcmpByName");

		IPIcmp icmp = IPIcmp4.getInstance();
		InputStream stream = icmp.getClass().getResourceAsStream("/ip/icmp");
		icmp.readIcmp(stream);

		IPIcmpEnt ent = icmp.getIcmpByName("echo-reply");
		assertEquals("echo-reply", ent.getName());
		assertEquals(0, ent.getIcmp());

		ent = icmp.getIcmpByName("mobile-redirect");
		assertEquals("mobile-redirect", ent.getName());
		assertEquals(32, ent.getIcmp());
	}

	/**
	 * Test of getIcmpByNumber method, of class IPIcmp.
	 */
	public void testGetIcmpByNumber() throws IOException {
		System.out.println("getIcmpByNumber");

		IPIcmp icmp = IPIcmp4.getInstance();
		InputStream stream = icmp.getClass().getResourceAsStream("/ip/icmp");
		icmp.readIcmp(stream);

		IPIcmpEnt ent = icmp.getIcmpByNumber(0);
		assertEquals("echo-reply", ent.getName());
		assertEquals(0, ent.getIcmp());

		ent = icmp.getIcmpByNumber(32);
		assertEquals("mobile-redirect", ent.getName());
		assertEquals(32, ent.getIcmp());
	}

}
