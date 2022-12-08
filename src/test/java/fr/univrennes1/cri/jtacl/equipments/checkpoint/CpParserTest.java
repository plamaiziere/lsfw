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
package fr.univrennes1.cri.jtacl.equipments.checkpoint;

import junit.framework.TestCase;
import org.parboiled.Parboiled;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

/**
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class CpParserTest extends TestCase {

	CpParser parser = Parboiled.createParser(CpParser.class);
	ParsingResult<?> result;

	public CpParserTest(String testName) {
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
	 * Test of CpPortItem method, of class CpParser.
	 */
	public void testCpPortItem() {
		System.out.println("CpPortItem");

		String line = ">1023";
		result = new ReportingParseRunner(parser.CpPortItem()).run(line);
		assertTrue(result.matched);
		PortItemTemplate portItem = parser.getPortItem();
		assertEquals(">", portItem.getOperator());
		assertEquals("1023", portItem.getFirstPort());

		line = "1023-1024";
		result = new ReportingParseRunner(parser.CpPortItem()).run(line);
		assertTrue(result.matched);
		portItem = parser.getPortItem();
		assertEquals("-", portItem.getOperator());
		assertEquals("1023", portItem.getFirstPort());
		assertEquals("1024", portItem.getLastPort());

		line = "1023";
		result = new ReportingParseRunner(parser.CpPortItem()).run(line);
		assertTrue(result.matched);
		portItem = parser.getPortItem();
		assertEquals(null, portItem.getOperator());
		assertEquals("1023", portItem.getFirstPort());
	}

}
