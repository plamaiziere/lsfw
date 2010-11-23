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

}
