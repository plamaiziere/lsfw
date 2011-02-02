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

package fr.univrennes1.cri.jtacl.core.monitor;

import junit.framework.TestCase;

/**
 * Test unit for ProbePortSpec
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class ProbePortSpecTest extends TestCase {
    
    public ProbePortSpecTest(String testName) {
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
	 * Test of matches method, of class ProbePortSpec.
	 */ 
	public void testMatches() {
		System.out.println("matches");

		ProbePortSpec p1;
		ProbePortSpec p2;

		p1 = new ProbePortSpec(ProbePortOperator.ANY);
		p2 = new ProbePortSpec(ProbePortOperator.EQ, 1000);
		assertEquals(p1.matches(p2), MatchResult.ALL);

		p1 = new ProbePortSpec(ProbePortOperator.NONE);
		p2 = new ProbePortSpec(ProbePortOperator.EQ, 1000);
		assertEquals(p1.matches(p2), MatchResult.NOT);

		p1 = new ProbePortSpec(ProbePortOperator.EQ, 1000);
		p2 = new ProbePortSpec(ProbePortOperator.EQ, 1000);
		assertEquals(p1.matches(p2), MatchResult.ALL);

		p1 = new ProbePortSpec(ProbePortOperator.NEQ, 1000);
		p2 = new ProbePortSpec(ProbePortOperator.EQ, 1000);
		assertEquals(p1.matches(p2), MatchResult.NOT);

		p1 = new ProbePortSpec(ProbePortOperator.NEQ, 1000);
		p2 = new ProbePortSpec(ProbePortOperator.NEQ, 1000);
		assertEquals(p1.matches(p2), MatchResult.ALL);

		p1 = new ProbePortSpec(ProbePortOperator.NEQ, 1000);
		p2 = new ProbePortSpec(ProbePortOperator.RANGE, 1000, 1001);
		assertEquals(p1.matches(p2), MatchResult.NOT);

		p1 = new ProbePortSpec(ProbePortOperator.NEQ, 1000);
		p2 = new ProbePortSpec(ProbePortOperator.EXCLUDE, 500, 1001);
		assertEquals(p1.matches(p2), MatchResult.MATCH);

		p1 = new ProbePortSpec(ProbePortOperator.RANGE, 500, 1001);
		p2 = new ProbePortSpec(ProbePortOperator.RANGE, 500, 1001);
		assertEquals(p1.matches(p2), MatchResult.ALL);

		p1 = new ProbePortSpec(ProbePortOperator.RANGE, 500, 1001);
		p2 = new ProbePortSpec(ProbePortOperator.RANGE, 600, 1001);
		assertEquals(p1.matches(p2), MatchResult.MATCH);

		p1 = new ProbePortSpec(ProbePortOperator.RANGE, 500, 1001);
		p2 = new ProbePortSpec(ProbePortOperator.RANGE, 500, 700);
		assertEquals(p1.matches(p2), MatchResult.MATCH);

		p1 = new ProbePortSpec(ProbePortOperator.RANGE, 500, 1001);
		p2 = new ProbePortSpec(ProbePortOperator.RANGE, 1, 499);
		assertEquals(p1.matches(p2), MatchResult.NOT);

		p1 = new ProbePortSpec(ProbePortOperator.RANGE, 500, 1001);
		p2 = new ProbePortSpec(ProbePortOperator.GTE, 1002);
		assertEquals(p1.matches(p2), MatchResult.NOT);

		p1 = new ProbePortSpec(ProbePortOperator.RANGE, 500, 1001);
		p2 = new ProbePortSpec(ProbePortOperator.GTE, 1001);
		assertEquals(p1.matches(p2), MatchResult.MATCH);

		p1 = new ProbePortSpec(ProbePortOperator.RANGE, 500, 1001);
		p2 = new ProbePortSpec(ProbePortOperator.LT, 500);
		assertEquals(p1.matches(p2), MatchResult.NOT);

		p1 = new ProbePortSpec(ProbePortOperator.RANGE, 500, 1001);
		p2 = new ProbePortSpec(ProbePortOperator.LTE, 500);
		assertEquals(p1.matches(p2), MatchResult.MATCH);

		p1 = new ProbePortSpec(ProbePortOperator.EXCLUDE, 500, 1001);
		p2 = new ProbePortSpec(ProbePortOperator.EXCLUDE, 500, 1001);
		assertEquals(p1.matches(p2), MatchResult.ALL);

		p1 = new ProbePortSpec(ProbePortOperator.EXCLUDE, 400, 1001);
		p2 = new ProbePortSpec(ProbePortOperator.EXCLUDE, 500, 900);
		assertEquals(p1.matches(p2), MatchResult.ALL);

		p1 = new ProbePortSpec(ProbePortOperator.EXCLUDE, 400, 1001);
		p2 = new ProbePortSpec(ProbePortOperator.EXCLUDE, 300, 1001);
		assertEquals(p1.matches(p2), MatchResult.MATCH);

		p1 = new ProbePortSpec(ProbePortOperator.EXCLUDE, 400, 1001);
		p2 = new ProbePortSpec(ProbePortOperator.RANGE, 400, 1001);
		assertEquals(p1.matches(p2), MatchResult.NOT);


	}

}
