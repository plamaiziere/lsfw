/*
 * Copyright (c) 2012, Universite de Rennes 1
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

import junit.framework.TestCase;

/**
 * tests for FwResult
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class FwResultTest extends TestCase {

	public FwResultTest(String testName) {
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
	 * Test of concat method, of class FwResult.
	 */
	public void testConcat() {

		FwResult r;
		FwResult a1;
		FwResult a2;

		/*
		 * DENY + X
		 */
		a1 = new FwResult(FwResult.DENY);
		a2 = new FwResult();
		r = a1.concat(a2);
		assertEquals(new FwResult(FwResult.DENY), r);

		/*
		 * X + DENY
		 */
		r = a2.concat(a1);
		assertEquals(new FwResult(FwResult.DENY), r);

		/*
		 * ACCEPT + ACCEPT
		 */
		a1 = new FwResult(FwResult.ACCEPT);
		a2 = new FwResult(FwResult.ACCEPT);
		r = a1.concat(a2);
		assertEquals(new FwResult(FwResult.ACCEPT), r);

		/*
		 * MAY DENY + X
		 */
		a1 = new FwResult(FwResult.MAY | FwResult.DENY);
		a2 = new FwResult();
		r = a1.concat(a2);
		assertEquals(new FwResult(FwResult.MAY | FwResult.DENY), r);

		/*
		 * X + MAY DENY
		 */
		r = a2.concat(a1);
		assertEquals(new FwResult(FwResult.MAY | FwResult.DENY), r);

		/*
		 * MAY DENY + ACCEPT
		 */
		a1 = new FwResult(FwResult.MAY | FwResult.DENY);
		a2 = new FwResult(FwResult.ACCEPT);
		r = a1.concat(a2);
		assertEquals(new FwResult(FwResult.MAY | FwResult.DENY), r);

		/*
		 * ACCEPT + MAY DENY
		 */
		r = a2.concat(a1);
		assertEquals(new FwResult(FwResult.MAY | FwResult.DENY), r);
	}

	/**
	 * Test of sumPath method, of class FwResult.
	 */
	public void testSumPath() {

		FwResult r;
		FwResult a1;
		FwResult a2;

		/*
		 * DENY + X
		 */
		a1 = new FwResult(FwResult.DENY);
		a2 = new FwResult();
		r = a1.sumPath(a2);
		assertEquals(new FwResult(FwResult.MAY | FwResult.DENY), r);

		/*
		 * X + DENY
		 */
		r = a2.sumPath(a1);
		assertEquals(new FwResult(FwResult.MAY | FwResult.DENY), r);

		/*
		 * ACCEPT + ACCEPT
		 */
		a1 = new FwResult(FwResult.ACCEPT);
		a2 = new FwResult(FwResult.ACCEPT);
		r = a1.sumPath(a2);
		assertEquals(new FwResult(FwResult.ACCEPT), r);

		/*
		 * MAY DENY + X
		 */
		a1 = new FwResult(FwResult.MAY | FwResult.DENY);
		a2 = new FwResult();
		r = a1.sumPath(a2);
		assertEquals(new FwResult(FwResult.MAY | FwResult.DENY), r);

		/*
		 * X + MAY DENY
		 */
		r = a2.sumPath(a1);
		assertEquals(new FwResult(FwResult.MAY | FwResult.DENY), r);

		/*
		 * MAY DENY + ACCEPT
		 */
		a1 = new FwResult(FwResult.MAY | FwResult.DENY);
		a2 = new FwResult(FwResult.ACCEPT);
		r = a1.sumPath(a2);
		assertEquals(new FwResult(FwResult.MAY | FwResult.DENY), r);

		/*
		 * ACCEPT + MAY DENY
		 */
		r = a2.sumPath(a1);
		assertEquals(new FwResult(FwResult.MAY | FwResult.DENY), r);

		/*
		 * ACCEPT + X
		 */
		a1 = new FwResult(FwResult.ACCEPT);
		a2 = new FwResult();
		r = a1.sumPath(a2);
		assertEquals(new FwResult(FwResult.MAY | FwResult.ACCEPT), r);

		/*
		 * X + ACCEPT
		 */
		r = a2.sumPath(a1);
		assertEquals(new FwResult(FwResult.MAY | FwResult.ACCEPT), r);

	}

}
