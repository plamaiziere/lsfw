/*
 * Copyright (c) 2013, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.equipments.checkpoint;

import junit.framework.TestCase;
import org.parboiled.Parboiled;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

/**
 * Tests for CpFwShellParser
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class CpFwShellParserTest extends TestCase {

	CpFwShellParser parser = Parboiled.createParser(CpFwShellParser.class);
	ReportingParseRunner parseRunParse = new ReportingParseRunner(parser.CommandLine());
	ParsingResult<?> result;

	public CpFwShellParserTest(String testName) {
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
	 * Test of command
	 */
	public void testCommandLine() {
		System.out.println("command line");

		String line = "show service";
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		assertEquals("show-service", parser.getCommand());
		assertEquals("", parser.getService());

		line = "show    service    SERVICE   ";
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		assertEquals("show-service", parser.getCommand());
		assertEquals("SERVICE", parser.getService());

		line = "show    network";
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		assertEquals("show-network", parser.getCommand());
		assertEquals("", parser.getNetwork());

		line = "show    network  NETWORK";
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		assertEquals("show-network", parser.getCommand());
		assertEquals("NETWORK", parser.getNetwork());

		line = "show    rules   ";
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		assertEquals("show-rules", parser.getCommand());

	}

}
