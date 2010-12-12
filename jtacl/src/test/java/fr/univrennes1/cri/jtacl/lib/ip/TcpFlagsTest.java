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

import junit.framework.TestCase;

/**
 * Tests for TcpFlags
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class TcpFlagsTest extends TestCase {
    
    public TcpFlagsTest(String testName) {
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
	 * Test of flags.
	 */
	public void testFlags() {
		System.out.println("flags");
		TcpFlags flags = new TcpFlags();
		flags.setFlags(-1);
		assertEquals(-1, flags.getFlags());
		flags.clearAll();
		assertEquals(0, flags.getFlags());
	}

	/**
	 * Test of URG
	 */
	public void testURG() {
		System.out.println("Test URG");
		TcpFlags flags = new TcpFlags();
		assertEquals(false, flags.hasURG());
		flags.setURG();
		assertEquals(true, flags.hasURG());
		flags.clearURG();
		assertEquals(false, flags.hasURG());
	}

	/**
	 * Test of String op
	 */
	public void testString() {
		System.out.println("Test String");
		TcpFlags flags = new TcpFlags("");
		assertEquals(0, flags.getFlags());
		assertEquals("", flags.toString());

		flags = new TcpFlags("WEUAPRSF");
		assertEquals("WEUAPRSF", flags.toString());
	}

	/**
	 * Test testFlagsAll()
	 */
	public void testFlagsAll() {
		System.out.println("Test flags all");
		TcpFlags flags = new TcpFlags("ASRU");

		assertEquals(true, flags.testFlagsAll(""));
		assertEquals(true, flags.testFlagsAll("AS"));
		assertEquals(true, flags.testFlagsAll("ASw"));
		assertEquals(false, flags.testFlagsAll("PU"));

		TcpFlags tflags = new TcpFlags("AU");
		assertEquals(true, flags.testFlagsAll(tflags));
	}

	/**
	 * Test testFlagsAny()
	 */
	public void testFlagsAny() {
		System.out.println("Test flags any");
		TcpFlags flags = new TcpFlags("ASRU");

		assertEquals(true, flags.testFlagsAny("ASRUP"));
		assertEquals(true, flags.testFlagsAny("ASwRUP"));
		assertEquals(false, flags.testFlagsAny("P"));

		TcpFlags tflags = new TcpFlags("A");
		assertEquals(true, flags.testFlagsAny(tflags));
	}


}

