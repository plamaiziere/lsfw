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

import fr.univrennes1.cri.jtacl.core.monitor.MatchResult;
import junit.framework.TestCase;

/**
 * Test unit for PortSpec
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PortSpecTest extends TestCase {
    
    public PortSpecTest(String testName) {
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
	 * Test of matches method, of class PortSpec.
	 */ 
	public void testMatches() {
		System.out.println("matches");

		PortSpec p1;
		PortSpec p2;

		p1 = new PortSpec(PortOperator.ANY);
		p2 = new PortSpec(PortOperator.EQ, 1000);
		assertEquals(p1.matches(p2), MatchResult.ALL);

		p1 = new PortSpec(PortOperator.NONE);
		p2 = new PortSpec(PortOperator.EQ, 1000);
		assertEquals(p1.matches(p2), MatchResult.NOT);

		p1 = new PortSpec(PortOperator.EQ, 1000);
		p2 = new PortSpec(PortOperator.EQ, 1000);
		assertEquals(p1.matches(p2), MatchResult.ALL);

		p1 = new PortSpec(PortOperator.NEQ, 1000);
		p2 = new PortSpec(PortOperator.EQ, 1000);
		assertEquals(p1.matches(p2), MatchResult.NOT);

		p1 = new PortSpec(PortOperator.NEQ, 1000);
		p2 = new PortSpec(PortOperator.NEQ, 1000);
		assertEquals(p1.matches(p2), MatchResult.ALL);

		p1 = new PortSpec(PortOperator.NEQ, 1000);
		p2 = new PortSpec(PortOperator.RANGE, 1000, 1001);
		assertEquals(p1.matches(p2), MatchResult.MATCH);

		p1 = new PortSpec(PortOperator.NEQ, 1000);
		p2 = new PortSpec(PortOperator.EXCLUDE, 500, 1001);
		assertEquals(p1.matches(p2), MatchResult.ALL);

		p1 = new PortSpec(PortOperator.RANGE, 500, 1001);
		p2 = new PortSpec(PortOperator.RANGE, 500, 1001);
		assertEquals(p1.matches(p2), MatchResult.ALL);

		p1 = new PortSpec(PortOperator.RANGE, 500, 1001);
		p2 = new PortSpec(PortOperator.RANGE, 600, 1001);
		assertEquals(p1.matches(p2), MatchResult.ALL);

		p1 = new PortSpec(PortOperator.RANGE, 500, 1001);
		p2 = new PortSpec(PortOperator.RANGE, 500, 700);
		assertEquals(p1.matches(p2), MatchResult.ALL);

		p1 = new PortSpec(PortOperator.RANGE, 500, 1001);
		p2 = new PortSpec(PortOperator.RANGE, 1, 499);
		assertEquals(p1.matches(p2), MatchResult.NOT);

		p1 = new PortSpec(PortOperator.RANGE, 500, 1001);
		p2 = new PortSpec(PortOperator.GTE, 1002);
		assertEquals(p1.matches(p2), MatchResult.NOT);

		p1 = new PortSpec(PortOperator.RANGE, 500, 1001);
		p2 = new PortSpec(PortOperator.GTE, 1001);
		assertEquals(p1.matches(p2), MatchResult.MATCH);

		p1 = new PortSpec(PortOperator.RANGE, 500, 1001);
		p2 = new PortSpec(PortOperator.LT, 500);
		assertEquals(p1.matches(p2), MatchResult.NOT);

		p1 = new PortSpec(PortOperator.RANGE, 500, 1001);
		p2 = new PortSpec(PortOperator.LTE, 500);
		assertEquals(p1.matches(p2), MatchResult.MATCH);

		p1 = new PortSpec(PortOperator.EXCLUDE, 500, 1001);
		p2 = new PortSpec(PortOperator.EXCLUDE, 500, 1001);
		assertEquals(p1.matches(p2), MatchResult.ALL);

		p1 = new PortSpec(PortOperator.EXCLUDE, 400, 1001);
		p2 = new PortSpec(PortOperator.EXCLUDE, 500, 900);
		assertEquals(p1.matches(p2), MatchResult.MATCH);

		p1 = new PortSpec(PortOperator.EXCLUDE, 400, 1001);
		p2 = new PortSpec(PortOperator.EXCLUDE, 300, 1001);
		assertEquals(p1.matches(p2), MatchResult.ALL);

		p1 = new PortSpec(PortOperator.EXCLUDE, 400, 1001);
		p2 = new PortSpec(PortOperator.RANGE, 400, 1001);
		assertEquals(p1.matches(p2), MatchResult.NOT);


	}

}
