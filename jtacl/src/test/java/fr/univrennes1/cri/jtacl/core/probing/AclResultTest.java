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
 * tests for AclResult
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class AclResultTest extends TestCase {

	public AclResultTest(String testName) {
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
	 * Test of concat method, of class AclResult.
	 */
	public void testConcat() {

		AclResult r;
		AclResult a1;
		AclResult a2;

		/*
		 * DENY + X
		 */
		a1 = new AclResult(AclResult.DENY);
		a2 = new AclResult();
		r = a1.concat(a2);
		assertEquals(new AclResult(AclResult.DENY), r);

		/*
		 * X + DENY
		 */
		r = a2.concat(a1);
		assertEquals(new AclResult(AclResult.DENY), r);

		/*
		 * ACCEPT + ACCEPT
		 */
		a1 = new AclResult(AclResult.ACCEPT);
		a2 = new AclResult(AclResult.ACCEPT);
		r = a1.concat(a2);
		assertEquals(new AclResult(AclResult.ACCEPT), r);

		/*
		 * MAY DENY + X
		 */
		a1 = new AclResult(AclResult.MAY | AclResult.DENY);
		a2 = new AclResult();
		r = a1.concat(a2);
		assertEquals(new AclResult(AclResult.MAY | AclResult.DENY), r);

		/*
		 * X + MAY DENY
		 */
		r = a2.concat(a1);
		assertEquals(new AclResult(AclResult.MAY | AclResult.DENY), r);

		/*
		 * MAY DENY + ACCEPT
		 */
		a1 = new AclResult(AclResult.MAY | AclResult.DENY);
		a2 = new AclResult(AclResult.ACCEPT);
		r = a1.concat(a2);
		assertEquals(new AclResult(AclResult.MAY | AclResult.DENY), r);

		/*
		 * ACCEPT + MAY DENY
		 */
		r = a2.concat(a1);
		assertEquals(new AclResult(AclResult.MAY | AclResult.DENY), r);
	}

	/**
	 * Test of sumPath method, of class AclResult.
	 */
	public void testSumPath() {

		AclResult r;
		AclResult a1;
		AclResult a2;

		/*
		 * DENY + X
		 */
		a1 = new AclResult(AclResult.DENY);
		a2 = new AclResult();
		r = a1.sumPath(a2);
		assertEquals(new AclResult(AclResult.MAY | AclResult.DENY), r);

		/*
		 * X + DENY
		 */
		r = a2.sumPath(a1);
		assertEquals(new AclResult(AclResult.MAY | AclResult.DENY), r);

		/*
		 * ACCEPT + ACCEPT
		 */
		a1 = new AclResult(AclResult.ACCEPT);
		a2 = new AclResult(AclResult.ACCEPT);
		r = a1.sumPath(a2);
		assertEquals(new AclResult(AclResult.ACCEPT), r);

		/*
		 * MAY DENY + X
		 */
		a1 = new AclResult(AclResult.MAY | AclResult.DENY);
		a2 = new AclResult();
		r = a1.sumPath(a2);
		assertEquals(new AclResult(AclResult.MAY | AclResult.DENY), r);

		/*
		 * X + MAY DENY
		 */
		r = a2.sumPath(a1);
		assertEquals(new AclResult(AclResult.MAY | AclResult.DENY), r);

		/*
		 * MAY DENY + ACCEPT
		 */
		a1 = new AclResult(AclResult.MAY | AclResult.DENY);
		a2 = new AclResult(AclResult.ACCEPT);
		r = a1.sumPath(a2);
		assertEquals(new AclResult(AclResult.MAY | AclResult.DENY), r);

		/*
		 * ACCEPT + MAY DENY
		 */
		r = a2.sumPath(a1);
		assertEquals(new AclResult(AclResult.MAY | AclResult.DENY), r);

		/*
		 * ACCEPT + X
		 */
		a1 = new AclResult(AclResult.ACCEPT);
		a2 = new AclResult();
		r = a1.sumPath(a2);
		assertEquals(new AclResult(AclResult.MAY | AclResult.ACCEPT), r);

		/*
		 * X + ACCEPT
		 */
		r = a2.sumPath(a1);
		assertEquals(new AclResult(AclResult.MAY | AclResult.ACCEPT), r);

	}

}
