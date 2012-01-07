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

package fr.univrennes1.cri.jtacl.shell;

import junit.framework.TestCase;
import org.parboiled.Parboiled;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

/**
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class ShellParserTest extends TestCase {

	ShellParser parser = Parboiled.createParser(ShellParser.class);
	ParsingResult<?> result;

    public ShellParserTest(String testName) {
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

	public void testDefine() {
		System.out.println("define");
		String line = "define";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("define", parser.getCommand());

		line = "define     NAME     =    VALUE";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("define", parser.getCommand());
		assertEquals("NAME", parser.getSetValueName());
		assertEquals("VALUE", parser.getSetValueValue());

		line = "d     NAME     =    VALUE";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("define", parser.getCommand());
		assertEquals("NAME", parser.getSetValueName());
		assertEquals("VALUE", parser.getSetValueValue());
	}

	public void testEquipment() {
		System.out.println("equiment");
		String line = "equipment NAME ABCD EFGH";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("equipment", parser.getCommand());
		assertEquals("NAME", parser.getEquipments());
		assertEquals("ABCD EFGH", parser.getSubCommand());

		line = "eq NAME ABCD EFGH";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("equipment", parser.getCommand());
		assertEquals("NAME", parser.getEquipments());
		assertEquals("ABCD EFGH", parser.getSubCommand());
	}

	public void testExit() {
		System.out.println("exit");
		String line = "exit";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("quit", parser.getCommand());

		line = "e";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("quit", parser.getCommand());

		line = "quit";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("quit", parser.getCommand());

		line = "q";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("quit", parser.getCommand());
	}

	public void testHelp() {
		System.out.println("help");
		String line = "help";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("help", parser.getCommand());

		line = "help     TOPIC";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("help", parser.getCommand());
		assertEquals("TOPIC", parser.getHelpTopic());
	}

	public void testOption() {
		System.out.println("option");
		String line = "option";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("option", parser.getCommand());

		line = "option     NAME     =    VALUE";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("option", parser.getCommand());
		assertEquals("NAME", parser.getSetValueName());
		assertEquals("VALUE", parser.getSetValueValue());

		line = "o     NAME     =    VALUE";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("option", parser.getCommand());
		assertEquals("NAME", parser.getSetValueName());
		assertEquals("VALUE", parser.getSetValueValue());
	}

	public void testProbe() {
		System.out.println("probe");

		String line = "probe       SOURCE     DEST";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("probe", parser.getCommand());
		assertEquals("SOURCE", parser.getSrcAddress());
		assertEquals("DEST", parser.getDestAddress());

		line = "p       SOURCE     DEST";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("probe", parser.getCommand());
		assertEquals("SOURCE", parser.getSrcAddress());
		assertEquals("DEST", parser.getDestAddress());

		line = "probe6       SOURCE     DEST";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("probe6", parser.getCommand());
		assertEquals("SOURCE", parser.getSrcAddress());
		assertEquals("DEST", parser.getDestAddress());

		line = "p6       SOURCE     DEST";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("probe6", parser.getCommand());
		assertEquals("SOURCE", parser.getSrcAddress());
		assertEquals("DEST", parser.getDestAddress());

		line = "p     expect    EXPECT     SOURCE     DEST";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("probe", parser.getCommand());
		assertEquals("SOURCE", parser.getSrcAddress());
		assertEquals("DEST", parser.getDestAddress());
		assertEquals("EXPECT", parser.getProbeExpect());

		line = "p     expect    EXPECT     on    ON   SOURCE     DEST";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("probe", parser.getCommand());
		assertEquals("SOURCE", parser.getSrcAddress());
		assertEquals("DEST", parser.getDestAddress());
		assertEquals("EXPECT", parser.getProbeExpect());
		assertEquals("ON", parser.getEquipments());

		line = "p    on    ON   expect    EXPECT        SOURCE     DEST";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("probe", parser.getCommand());
		assertEquals("SOURCE", parser.getSrcAddress());
		assertEquals("DEST", parser.getDestAddress());
		assertEquals("EXPECT", parser.getProbeExpect());
		assertEquals("ON", parser.getEquipments());
		assertFalse(parser.getProbeOptNoAction());

		line = "p    on    ON   expect    EXPECT   no-action       SOURCE     DEST";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("probe", parser.getCommand());
		assertEquals("SOURCE", parser.getSrcAddress());
		assertEquals("DEST", parser.getDestAddress());
		assertEquals("EXPECT", parser.getProbeExpect());
		assertEquals("ON", parser.getEquipments());
		assertTrue(parser.getProbeOptNoAction());

		line = "p    on    ON   na expect    EXPECT     SOURCE     DEST";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("probe", parser.getCommand());
		assertEquals("SOURCE", parser.getSrcAddress());
		assertEquals("DEST", parser.getDestAddress());
		assertEquals("EXPECT", parser.getProbeExpect());
		assertEquals("ON", parser.getEquipments());
		assertTrue(parser.getProbeOptNoAction());

		line = "p    on    ON   verbose  na expect    EXPECT     SOURCE     DEST";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("probe", parser.getCommand());
		assertEquals("SOURCE", parser.getSrcAddress());
		assertEquals("DEST", parser.getDestAddress());
		assertEquals("EXPECT", parser.getProbeExpect());
		assertEquals("ON", parser.getEquipments());
		assertTrue(parser.getProbeOptNoAction());
		assertTrue(parser.getProbeOptVerbose());

		line = "p   SOURCE     DEST   tcp";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("probe", parser.getCommand());
		assertEquals("SOURCE", parser.getSrcAddress());
		assertEquals("DEST", parser.getDestAddress());
		assertEquals(null, parser.getProbeExpect());
		assertEquals(null, parser.getEquipments());
		assertEquals("tcp", parser.getProtoSpecification());
		assertFalse(parser.getProbeOptNoAction());
		assertFalse(parser.getProbeOptVerbose());

		line = "p   SOURCE     DEST   tcp      PORT";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("probe", parser.getCommand());
		assertEquals("SOURCE", parser.getSrcAddress());
		assertEquals("DEST", parser.getDestAddress());
		assertEquals("tcp", parser.getProtoSpecification());
		assertEquals(null, parser.getPortSource());
		assertEquals("PORT", parser.getPortDest());

		line = "p   SOURCE     DEST   tcp    PORT1:";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("probe", parser.getCommand());
		assertEquals("SOURCE", parser.getSrcAddress());
		assertEquals("DEST", parser.getDestAddress());
		assertEquals("tcp", parser.getProtoSpecification());
		assertEquals("PORT1", parser.getPortSource());
		assertEquals(null, parser.getPortDest());

		line = "p   SOURCE     DEST   tcp    PORT1:PORT2";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("probe", parser.getCommand());
		assertEquals("SOURCE", parser.getSrcAddress());
		assertEquals("DEST", parser.getDestAddress());
		assertEquals("tcp", parser.getProtoSpecification());
		assertEquals("PORT1", parser.getPortSource());
		assertEquals("PORT2", parser.getPortDest());

		line = "p   SOURCE     DEST   udp    PORT1:PORT2";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("probe", parser.getCommand());
		assertEquals("SOURCE", parser.getSrcAddress());
		assertEquals("DEST", parser.getDestAddress());
		assertEquals("udp", parser.getProtoSpecification());
		assertEquals("PORT1", parser.getPortSource());
		assertEquals("PORT2", parser.getPortDest());

		line = "p   SOURCE     DEST   tcp    flags S SA";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("probe", parser.getCommand());
		assertEquals("SOURCE", parser.getSrcAddress());
		assertEquals("DEST", parser.getDestAddress());
		assertEquals("tcp", parser.getProtoSpecification());
		assertEquals(null, parser.getPortSource());
		assertEquals(null, parser.getPortDest());
		assertEquals(2, parser.getTcpFlags().size());
		assertEquals("S", parser.getTcpFlags().get(0));
		assertEquals("SA", parser.getTcpFlags().get(1));

		line = "p   SOURCE     DEST   tcp  PORT  flags S SA";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("probe", parser.getCommand());
		assertEquals("SOURCE", parser.getSrcAddress());
		assertEquals("DEST", parser.getDestAddress());
		assertEquals("tcp", parser.getProtoSpecification());
		assertEquals(null, parser.getPortSource());
		assertEquals("PORT", parser.getPortDest());
		assertEquals(2, parser.getTcpFlags().size());
		assertEquals("S", parser.getTcpFlags().get(0));
		assertEquals("SA", parser.getTcpFlags().get(1));
	}

	public void testReload() {
		System.out.println("reload");

		String line = "reload";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("reload", parser.getCommand());

		line = "reload     EQ";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("reload", parser.getCommand());
		assertEquals("EQ", parser.getEquipments());
	}

	public void testRoute() {
		System.out.println("route");

		String line = "route";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("route", parser.getCommand());

		line = "route     EQ";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("route", parser.getCommand());
		assertEquals("EQ", parser.getEquipments());
	}

	public void testTopology() {
		System.out.println("topology");

		String line = "topology";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("topology", parser.getCommand());

		line = "topology   EQ";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("topology", parser.getCommand());
		assertEquals("EQ", parser.getEquipments());

		line = "topology   connected  EQ";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("topology", parser.getCommand());
		assertEquals("EQ", parser.getEquipments());
		assertEquals("connected", parser.getTopologyOption());

		line = "topology   !connected";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("topology", parser.getCommand());
		assertEquals(null, parser.getEquipments());
		assertEquals("!connected", parser.getTopologyOption());
	}

}
