/*
 * Copyright (c) 2013, Universite de Rennes 1
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
import java.net.UnknownHostException;
import junit.framework.TestCase;

/**
 * Tests for IPRange.
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class IPRangeTest extends TestCase {

	public IPRangeTest(String testName) {
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
	 * Test of contains, of class IPRange.
	 */
	public void testContains() throws UnknownHostException  {
		System.out.println("contains");

		IPNet ip1 = new IPNet("127.0.0.1");
		IPNet ip2 = new IPNet("127.0.0.1");
		IPRange range = new IPRange(ip1, ip2);
		assertTrue(range.contains(ip2));

		ip2 = new IPNet("127.0.0.2");
		assertFalse(range.contains(ip2));

		ip1 = new IPNet("::1");
		ip2 = new IPNet("::1");
		range = new IPRange(ip1, ip2);
		assertTrue(range.contains(ip2));

		ip2 = new IPNet("::2");
		assertFalse(range.contains(ip2));

		ip1 = new IPNet("127.0.0.1/16");
		range = new IPRange(ip1);
		ip2 = new IPNet("127.0.0.2/24");
		assertTrue(range.contains(ip2));

		ip1 = new IPNet("::1/16");
		range = new IPRange(ip1);
		ip2 = new IPNet("::1/24");
		assertTrue(range.contains(ip2));

		ip1 = new IPNet("127.0.0.1/24");
		range = new IPRange(ip1);
		ip2 = new IPNet("127.0.0.1/16");
		assertFalse(range.contains(ip2));

		ip1 = new IPNet("::1/24");
		range = new IPRange(ip1);
		ip2 = new IPNet("::1/16");
		assertFalse(range.contains(ip2));

		ip1 = new IPNet("127.0.0.0/24");
		ip2 = new IPNet("127.0.0.255");
		range = new IPRange(ip1, true);
		assertTrue(range.contains(ip2));

		range = new IPRange(ip1, false);
		assertFalse(range.contains(ip2));

		ip2 = new IPNet("127.0.0.254");
		assertTrue(range.contains(ip2));
	}

	/**
	 * Test of overlaps, of class IPRange.
	 */
	public void testOverlaps() throws UnknownHostException, IOException  {
		System.out.println("overlaps");

		IPNet ip1;
		IPNet ip2;

		ip1 = new IPNet("127.0.0.1");
		IPRange range = new IPRange(ip1);
		ip2 = new IPNet("127.0.0.1");
		assertTrue(range.overlaps(ip2));

		ip1 = new IPNet("127.0.0.1");
		range = new IPRange(ip1);
		ip2 = new IPNet("127.0.0.2");
		assertFalse(range.overlaps(ip2));

		ip1 = new IPNet("127.0.0.1/31");
		range = new IPRange(ip1);
		ip2 = new IPNet("127.0.0.2");
		assertFalse(range.overlaps(ip2));

		ip1 = new IPNet("127.0.0.1/30");
		range = new IPRange(ip1);
		assertTrue(range.overlaps(ip2));

		ip1 = new IPNet("127.0.0.1/32");
		range = new IPRange(ip1);
		ip2 = new IPNet("0/0");
		assertTrue(range.overlaps(ip2));

		ip1 = new IPNet("::1/120");
		range = new IPRange(ip1);
		ip2 = new IPNet("::0/0");
		assertTrue(range.overlaps(ip2));

		ip1 = new IPNet("127.0.0.0/24");
		ip2 = new IPNet("127.0.0.255");
		range = new IPRange(ip1, true);
		assertTrue(range.overlaps(ip2));

		range = new IPRange(ip1, false);
		assertFalse(range.overlaps(ip2));
 	}

	/**
	 * Test of toIPNet()
	 */
	public void testToIPNet() throws UnknownHostException, IOException  {
		System.out.println("toIPNet");

		IPNet ip1;
		IPNet ip2;

		ip1 = new IPNet("127.0.0.0");
		ip2 = new IPNet("127.0.0.255");

		IPRange range = new IPRange(ip1, ip2);
		IPNet rip = range.toIPNet();
		assertEquals("127.0.0.0/24", rip.toString());

		ip1 = new IPNet("0.0.0.0/0");
		range = new IPRange(ip1);
		rip = range.toIPNet();
		assertEquals("0.0.0.0/0", rip.toString());

		ip1 = new IPNet("::0");
		ip2 = new IPNet("::ffff");
		range = new IPRange(ip1, ip2);
		rip = range.toIPNet();
		assertEquals("::0/112", rip.toString("::"));

		ip1 = new IPNet("127.0.0.1");
		ip2 = new IPNet("127.0.0.255");
		range = new IPRange(ip1, ip2);
		rip = range.toIPNet();
		assertEquals(null, rip);

		ip1 = new IPNet("::1");
		ip2 = new IPNet("::ffff");
		range = new IPRange(ip1, ip2);
		rip = range.toIPNet();
		assertEquals(null, rip);
	}

	/**
	 * Test of nearestNework()
	 */
	public void testNearestNetwork() throws UnknownHostException, IOException  {
		System.out.println("nearestNetwork");

		IPNet ip1;
		IPNet ip2;
		IPRange range;
		IPNet net;

		ip1 = new IPNet("127.0.0.1");
		ip2 = new IPNet("127.0.0.1");
		range = new IPRange(ip1, ip2);

		net = range.nearestNetwork();
		assertEquals("127.0.0.1/32", net.toString("::"));

		ip1 = new IPNet("127.0.0.1");
		ip2 = new IPNet("127.0.0.252");
		range = new IPRange(ip1, ip2);

		net = range.nearestNetwork();
		assertEquals("127.0.0.1/24", net.toString("::"));


		ip1 = new IPNet("::1");
		ip2 = new IPNet("::1");
		range = new IPRange(ip1, ip2);

		net = range.nearestNetwork();
		assertEquals("::1/128", net.toString("::"));

		ip1 = new IPNet("::1");
		ip2 = new IPNet("::ff");
		range = new IPRange(ip1, ip2);

		net = range.nearestNetwork();
		assertEquals("::1/120", net.toString("::"));

	}
}
