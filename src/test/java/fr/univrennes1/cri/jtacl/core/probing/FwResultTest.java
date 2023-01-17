/*
 * Copyright (c) 2012, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */
package fr.univrennes1.cri.jtacl.core.probing;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

	public void testReduceFwResults() {
		List<FwResult> results = new ArrayList<>(
				Arrays.asList(
					// MAY ACCEPT / ACCEPT -> ACCEPT
					new FwResult(FwResult.ACCEPT | FwResult.MAY)
					, new FwResult(FwResult.ACCEPT)
				)
		);

		FwResult r = FwResult.reduceFwResults(results);
		assertTrue(r.isCertainlyAccept());

		results = new ArrayList<>(
					Arrays.asList(
						// MAY DENY / DENY -> DENY
						new FwResult(FwResult.DENY | FwResult.MAY)
						, new FwResult(FwResult.DENY)
					)
		);
		r = FwResult.reduceFwResults(results);
		assertTrue(r.isCertainlyDeny());

		results = new ArrayList<>(
					Arrays.asList(
						// MAY DENY / ACCEPT -> MAY DENY
						new FwResult(FwResult.DENY | FwResult.MAY)
						, new FwResult(FwResult.ACCEPT)
					)
		);
		r = FwResult.reduceFwResults(results);
		assertTrue(r.hasDeny() && !r.isCertainlyDeny());

		results = new ArrayList<>(
					Arrays.asList(
						// MAY DENY / ACCEPT / DENY -> MAY DENY
						new FwResult(FwResult.DENY | FwResult.MAY)
						, new FwResult(FwResult.ACCEPT)
						, new FwResult(FwResult.DENY)
					)
		);
		r = FwResult.reduceFwResults(results);
		assertTrue(r.hasDeny() && !r.isCertainlyDeny());

		results = new ArrayList<>(
					Arrays.asList(
						// MAY ACCEPT / DENY -> MAY ACCEPT
						new FwResult(FwResult.ACCEPT | FwResult.MAY)
						, new FwResult(FwResult.DENY)
					)
		);
		r = FwResult.reduceFwResults(results);
		assertTrue(r.hasAccept() && !r.isCertainlyAccept());

		results = new ArrayList<>(
					Arrays.asList(
						// MAY ACCEPT / DENY / ACCEPT -> MAY ACCEPT
						new FwResult(FwResult.ACCEPT | FwResult.MAY)
						, new FwResult(FwResult.DENY)
						, new FwResult(FwResult.ACCEPT)
					)
		);
		r = FwResult.reduceFwResults(results);
		assertTrue(r.hasAccept() && !r.isCertainlyAccept());

		results = new ArrayList<>(
					Arrays.asList(
						// MAY ACCEPT / MAY ACCEPT / ACCEPT -> ACCEPT
						new FwResult(FwResult.ACCEPT | FwResult.MAY)
						, new FwResult(FwResult.ACCEPT | FwResult.MAY )
						, new FwResult(FwResult.ACCEPT)
					)
		);
		r = FwResult.reduceFwResults(results);
		assertTrue(r.isCertainlyAccept());

		results = new ArrayList<>(
					Arrays.asList(
						// MAY DENY / MAY DENY / DENY / MAY DENY-> DENY
						new FwResult(FwResult.DENY | FwResult.MAY)
						, new FwResult(FwResult.DENY | FwResult.MAY )
						, new FwResult(FwResult.DENY)
						, new FwResult(FwResult.DENY | FwResult.MAY )
					)
		);
		r = FwResult.reduceFwResults(results);
		assertTrue(r.isCertainlyDeny());

		results = new ArrayList<>(
					Arrays.asList(
						// MAY DENY / MATCH / DENY / MAY DENY-> DENY
						new FwResult(FwResult.DENY | FwResult.MAY)
						, new FwResult(FwResult.MATCH)
						, new FwResult(FwResult.DENY)
						, new FwResult(FwResult.DENY | FwResult.MAY )
					)
		);
		r = FwResult.reduceFwResults(results);
		assertTrue(r.isCertainlyDeny());

		results = new ArrayList<>(
					Arrays.asList(
						// MAY DENY / MAY DENY -> MAY DENY
						new FwResult(FwResult.DENY | FwResult.MAY)
						, new FwResult(FwResult.DENY | FwResult.MAY )
					)
		);
		r = FwResult.reduceFwResults(results);
		assertTrue(r.hasDeny() && !r.isCertainlyDeny());

		results = new ArrayList<>(
					Arrays.asList(
						// MAY ACCEPT / MAY ACCEPT -> MAY ACCEPT
						new FwResult(FwResult.ACCEPT | FwResult.MAY)
						, new FwResult(FwResult.ACCEPT | FwResult.MAY )
					)
		);
		r = FwResult.reduceFwResults(results);
		assertTrue(r.hasAccept() && !r.isCertainlyAccept());

	}

}
