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
		assertEquals("define", parser.getString("Command"));

		line = "define     NAME     =    VALUE";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("define", parser.getString("Command"));
		assertEquals("NAME", parser.getString("SetValueName"));
		assertEquals("VALUE", parser.getString("SetValueValue"));

		line = "d     NAME     =    VALUE";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("define", parser.getString("Command"));
		assertEquals("NAME", parser.getString("SetValueName"));
		assertEquals("VALUE", parser.getString("SetValueValue"));
	}

	public void testEquipment() {
		System.out.println("equiment");
		String line = "equipment NAME ABCD EFGH";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("equipment", parser.getString("Command"));
		assertEquals("NAME", parser.getString("Equipments"));
		assertEquals("ABCD EFGH", parser.getString("SubCommand"));

		line = "eq NAME ABCD EFGH";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("equipment", parser.getString("Command"));
		assertEquals("NAME", parser.getString("Equipments"));
		assertEquals("ABCD EFGH", parser.getString("SubCommand"));
	}

	public void testExit() {
		System.out.println("exit");
		String line = "exit";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("quit", parser.getString("Command"));

		line = "e";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("quit", parser.getString("Command"));

		line = "quit";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("quit", parser.getString("Command"));

		line = "q";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("quit", parser.getString("Command"));
	}

	public void testHelp() {
		System.out.println("help");
		String line = "help";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("help", parser.getString("Command"));

		line = "help     TOPIC";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("help", parser.getString("Command"));
		assertEquals("TOPIC", parser.getString("HelpTopic"));
	}

	public void testOption() {
		System.out.println("option");
		String line = "option";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("option", parser.getString("Command"));

		line = "option     NAME     =    VALUE";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("option", parser.getString("Command"));
		assertEquals("NAME", parser.getString("SetValueName"));
		assertEquals("VALUE", parser.getString("SetValueValue"));

		line = "o     NAME     =    VALUE";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("option", parser.getString("Command"));
		assertEquals("NAME", parser.getString("SetValueName"));
		assertEquals("VALUE", parser.getString("SetValueValue"));
	}

	public void testProbe() {
		System.out.println("probe");

		String line = "probe       SOURCE     DEST";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("probe", parser.getString("Command"));
		assertEquals("SOURCE", parser.getProbeCmdTemplate().getSrcAddress());
		assertEquals("DEST", parser.getProbeCmdTemplate().getDestAddress());

		line = "p       SOURCE     DEST";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("probe", parser.getString("Command"));
		assertEquals("SOURCE", parser.getProbeCmdTemplate().getSrcAddress());
		assertEquals("DEST", parser.getProbeCmdTemplate().getDestAddress());

		line = "probe6       SOURCE     DEST";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("probe", parser.getString("Command"));
		assertTrue(parser.getProbeCmdTemplate().getProbe6flag());
		assertEquals("SOURCE", parser.getProbeCmdTemplate().getSrcAddress());
		assertEquals("DEST", parser.getProbeCmdTemplate().getDestAddress());

		line = "p6       SOURCE     DEST";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("probe", parser.getString("Command"));
		assertTrue(parser.getProbeCmdTemplate().getProbe6flag());
		assertEquals("SOURCE", parser.getProbeCmdTemplate().getSrcAddress());
		assertEquals("DEST", parser.getProbeCmdTemplate().getDestAddress());

		line = "p     expect    EXPECT     SOURCE     DEST";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("probe", parser.getString("Command"));
		assertFalse(parser.getProbeCmdTemplate().getProbe6flag());
		assertEquals("SOURCE", parser.getProbeCmdTemplate().getSrcAddress());
		assertEquals("DEST", parser.getProbeCmdTemplate().getDestAddress());
		assertEquals("EXPECT", parser.getProbeCmdTemplate().getProbeExpect());

		line = "p     expect    EXPECT     on    ON   SOURCE     DEST";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("probe", parser.getString("Command"));
		assertEquals("SOURCE", parser.getProbeCmdTemplate().getSrcAddress());
		assertEquals("DEST", parser.getProbeCmdTemplate().getDestAddress());
		assertEquals("EXPECT", parser.getProbeCmdTemplate().getProbeExpect());
		assertEquals("ON", parser.getProbeCmdTemplate().getEquipments());

		line = "p    on    ON   expect    EXPECT        SOURCE     DEST";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("probe", parser.getString("Command"));
		assertEquals("SOURCE", parser.getProbeCmdTemplate().getSrcAddress());
		assertEquals("DEST", parser.getProbeCmdTemplate().getDestAddress());
		assertEquals("EXPECT", parser.getProbeCmdTemplate().getProbeExpect());
		assertEquals("ON", parser.getProbeCmdTemplate().getEquipments());
		assertFalse(parser.getProbeCmdTemplate().getProbeOptNoAction());

		line = "p    on    ON   expect    EXPECT   no-action       SOURCE     DEST";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("probe", parser.getString("Command"));
		assertEquals("SOURCE", parser.getProbeCmdTemplate().getSrcAddress());
		assertEquals("DEST", parser.getProbeCmdTemplate().getDestAddress());
		assertEquals("EXPECT", parser.getProbeCmdTemplate().getProbeExpect());
		assertEquals("ON", parser.getProbeCmdTemplate().getEquipments());
		assertTrue(parser.getProbeCmdTemplate().getProbeOptNoAction());

		line = "p    on    ON   na expect    EXPECT     SOURCE     DEST";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("probe", parser.getString("Command"));
		assertEquals("SOURCE", parser.getProbeCmdTemplate().getSrcAddress());
		assertEquals("DEST", parser.getProbeCmdTemplate().getDestAddress());
		assertEquals("EXPECT", parser.getProbeCmdTemplate().getProbeExpect());
		assertEquals("ON", parser.getProbeCmdTemplate().getEquipments());
		assertTrue(parser.getProbeCmdTemplate().getProbeOptNoAction());

		line = "p    on    ON   verbose  na expect    EXPECT     SOURCE     DEST";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("probe", parser.getString("Command"));
		assertEquals("SOURCE", parser.getProbeCmdTemplate().getSrcAddress());
		assertEquals("DEST", parser.getProbeCmdTemplate().getDestAddress());
		assertEquals("EXPECT", parser.getProbeCmdTemplate().getProbeExpect());
		assertEquals("ON", parser.getProbeCmdTemplate().getEquipments());
		assertTrue(parser.getProbeCmdTemplate().getProbeOptNoAction());
		assertTrue(parser.getProbeCmdTemplate().getProbeOptVerbose());

		line = "p    on    ON   verbose  na act expect    EXPECT     SOURCE     DEST";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("probe", parser.getString("Command"));
		assertEquals("SOURCE", parser.getProbeCmdTemplate().getSrcAddress());
		assertEquals("DEST", parser.getProbeCmdTemplate().getDestAddress());
		assertEquals("EXPECT", parser.getProbeCmdTemplate().getProbeExpect());
		assertEquals("ON", parser.getProbeCmdTemplate().getEquipments());
		assertTrue(parser.getProbeCmdTemplate().getProbeOptNoAction());
		assertTrue(parser.getProbeCmdTemplate().getProbeOptVerbose());
		assertTrue(parser.getProbeCmdTemplate().getProbeOptActive());

		line = "p    active on    ON  verbose  na expect    EXPECT     SOURCE     DEST";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("probe", parser.getString("Command"));
		assertEquals("SOURCE", parser.getProbeCmdTemplate().getSrcAddress());
		assertEquals("DEST", parser.getProbeCmdTemplate().getDestAddress());
		assertEquals("EXPECT", parser.getProbeCmdTemplate().getProbeExpect());
		assertEquals("ON", parser.getProbeCmdTemplate().getEquipments());
		assertTrue(parser.getProbeCmdTemplate().getProbeOptNoAction());
		assertTrue(parser.getProbeCmdTemplate().getProbeOptVerbose());
		assertTrue(parser.getProbeCmdTemplate().getProbeOptActive());

		line = "p    active on    ON  match verbose  na expect    EXPECT     SOURCE     DEST";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("probe", parser.getString("Command"));
		assertEquals("SOURCE", parser.getProbeCmdTemplate().getSrcAddress());
		assertEquals("DEST", parser.getProbeCmdTemplate().getDestAddress());
		assertEquals("EXPECT", parser.getProbeCmdTemplate().getProbeExpect());
		assertEquals("ON", parser.getProbeCmdTemplate().getEquipments());
		assertTrue(parser.getProbeCmdTemplate().getProbeOptNoAction());
		assertTrue(parser.getProbeCmdTemplate().getProbeOptVerbose());
		assertTrue(parser.getProbeCmdTemplate().getProbeOptActive());
		assertTrue(parser.getProbeCmdTemplate().getProbeOptMatching());

		line = "p    active  learn on    ON  match verbose  na expect    EXPECT     SOURCE     DEST";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("probe", parser.getString("Command"));
		assertEquals("SOURCE", parser.getProbeCmdTemplate().getSrcAddress());
		assertEquals("DEST", parser.getProbeCmdTemplate().getDestAddress());
		assertEquals("EXPECT", parser.getProbeCmdTemplate().getProbeExpect());
		assertEquals("ON", parser.getProbeCmdTemplate().getEquipments());
		assertTrue(parser.getProbeCmdTemplate().getProbeOptNoAction());
		assertTrue(parser.getProbeCmdTemplate().getProbeOptVerbose());
		assertTrue(parser.getProbeCmdTemplate().getProbeOptActive());
		assertTrue(parser.getProbeCmdTemplate().getProbeOptMatching());
		assertTrue(parser.getProbeCmdTemplate().getProbeOptLearn());

		line = "p active learn on ON match verbose na expect EXPECT   qd   SOURCE DEST";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("probe", parser.getString("Command"));
		assertEquals("SOURCE", parser.getProbeCmdTemplate().getSrcAddress());
		assertEquals("DEST", parser.getProbeCmdTemplate().getDestAddress());
		assertEquals("EXPECT", parser.getProbeCmdTemplate().getProbeExpect());
		assertEquals("ON", parser.getProbeCmdTemplate().getEquipments());
		assertTrue(parser.getProbeCmdTemplate().getProbeOptNoAction());
		assertTrue(parser.getProbeCmdTemplate().getProbeOptVerbose());
		assertTrue(parser.getProbeCmdTemplate().getProbeOptActive());
		assertTrue(parser.getProbeCmdTemplate().getProbeOptMatching());
		assertTrue(parser.getProbeCmdTemplate().getProbeOptLearn());
		assertTrue(parser.getProbeCmdTemplate().getProbeOptQuickDeny());

		line = "p active learn on ON match verbose na expect EXPECT   quick-deny   SOURCE DEST";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("probe", parser.getString("Command"));
		assertEquals("SOURCE", parser.getProbeCmdTemplate().getSrcAddress());
		assertEquals("DEST", parser.getProbeCmdTemplate().getDestAddress());
		assertEquals("EXPECT", parser.getProbeCmdTemplate().getProbeExpect());
		assertEquals("ON", parser.getProbeCmdTemplate().getEquipments());
		assertTrue(parser.getProbeCmdTemplate().getProbeOptNoAction());
		assertTrue(parser.getProbeCmdTemplate().getProbeOptVerbose());
		assertTrue(parser.getProbeCmdTemplate().getProbeOptActive());
		assertTrue(parser.getProbeCmdTemplate().getProbeOptMatching());
		assertTrue(parser.getProbeCmdTemplate().getProbeOptLearn());
		assertTrue(parser.getProbeCmdTemplate().getProbeOptQuickDeny());

		line = "p   SOURCE     DEST   tcp";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("probe", parser.getString("Command"));
		assertEquals("SOURCE", parser.getProbeCmdTemplate().getSrcAddress());
		assertEquals("DEST", parser.getProbeCmdTemplate().getDestAddress());
		assertEquals(null, parser.getProbeCmdTemplate().getProbeExpect());
		assertEquals(null, parser.getProbeCmdTemplate().getEquipments());
		assertEquals("tcp", parser.getProbeCmdTemplate().getProtoSpecification());
		assertFalse(parser.getProbeCmdTemplate().getProbeOptNoAction());
		assertFalse(parser.getProbeCmdTemplate().getProbeOptVerbose());
		assertFalse(parser.getProbeCmdTemplate().getProbeOptActive());
		assertFalse(parser.getProbeCmdTemplate().getProbeOptMatching());
		assertFalse(parser.getProbeCmdTemplate().getProbeOptLearn());

		line = "p   SOURCE     DEST   tcp      PORT";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("probe", parser.getString("Command"));
		assertEquals("SOURCE", parser.getProbeCmdTemplate().getSrcAddress());
		assertEquals("DEST", parser.getProbeCmdTemplate().getDestAddress());
		assertEquals("tcp", parser.getProbeCmdTemplate().getProtoSpecification());
		assertEquals(null, parser.getProbeCmdTemplate().getPortSource());
		assertEquals("PORT", parser.getProbeCmdTemplate().getPortDest());

		line = "p   SOURCE     DEST   tcp    PORT1:";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("probe", parser.getString("Command"));
		assertEquals("SOURCE", parser.getProbeCmdTemplate().getSrcAddress());
		assertEquals("DEST", parser.getProbeCmdTemplate().getDestAddress());
		assertEquals("tcp", parser.getProbeCmdTemplate().getProtoSpecification());
		assertEquals("PORT1", parser.getProbeCmdTemplate().getPortSource());
		assertEquals(null, parser.getProbeCmdTemplate().getPortDest());

		line = "p   SOURCE     DEST   tcp    PORT1:PORT2";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("probe", parser.getString("Command"));
		assertEquals("SOURCE", parser.getProbeCmdTemplate().getSrcAddress());
		assertEquals("DEST", parser.getProbeCmdTemplate().getDestAddress());
		assertEquals("tcp", parser.getProbeCmdTemplate().getProtoSpecification());
		assertEquals("PORT1", parser.getProbeCmdTemplate().getPortSource());
		assertEquals("PORT2", parser.getProbeCmdTemplate().getPortDest());

		line = "p   SOURCE     DEST   udp    PORT1:PORT2";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("probe", parser.getString("Command"));
		assertEquals("SOURCE", parser.getProbeCmdTemplate().getSrcAddress());
		assertEquals("DEST", parser.getProbeCmdTemplate().getDestAddress());
		assertEquals("udp", parser.getProbeCmdTemplate().getProtoSpecification());
		assertEquals("PORT1", parser.getProbeCmdTemplate().getPortSource());
		assertEquals("PORT2", parser.getProbeCmdTemplate().getPortDest());

		line = "p   SOURCE     DEST   udp    (PORT1,PORT1)";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("probe", parser.getString("Command"));
		assertEquals("SOURCE", parser.getProbeCmdTemplate().getSrcAddress());
		assertEquals("DEST", parser.getProbeCmdTemplate().getDestAddress());
		assertEquals("udp", parser.getProbeCmdTemplate().getProtoSpecification());
		assertEquals(null, parser.getProbeCmdTemplate().getPortSource());
		assertEquals("(PORT1,PORT1)", parser.getProbeCmdTemplate().getPortDest());

		line = "p   SOURCE     DEST   udp    (PORT1,PORT1):PORT2";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("probe", parser.getString("Command"));
		assertEquals("SOURCE", parser.getProbeCmdTemplate().getSrcAddress());
		assertEquals("DEST", parser.getProbeCmdTemplate().getDestAddress());
		assertEquals("udp", parser.getProbeCmdTemplate().getProtoSpecification());
		assertEquals("(PORT1,PORT1)", parser.getProbeCmdTemplate().getPortSource());
		assertEquals("PORT2", parser.getProbeCmdTemplate().getPortDest());

		line = "p   SOURCE     DEST   udp   (PORT1,PORT1):(PORT2,PORT2)";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("probe", parser.getString("Command"));
		assertEquals("SOURCE", parser.getProbeCmdTemplate().getSrcAddress());
		assertEquals("DEST", parser.getProbeCmdTemplate().getDestAddress());
		assertEquals("udp", parser.getProbeCmdTemplate().getProtoSpecification());
		assertEquals("(PORT1,PORT1)", parser.getProbeCmdTemplate().getPortSource());
		assertEquals("(PORT2,PORT2)", parser.getProbeCmdTemplate().getPortDest());

		line = "p   SOURCE     DEST   tcp    flags S SA";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("probe", parser.getString("Command"));
		assertEquals("SOURCE", parser.getProbeCmdTemplate().getSrcAddress());
		assertEquals("DEST", parser.getProbeCmdTemplate().getDestAddress());
		assertEquals("tcp", parser.getProbeCmdTemplate().getProtoSpecification());
		assertEquals(null, parser.getProbeCmdTemplate().getPortSource());
		assertEquals(null, parser.getProbeCmdTemplate().getPortDest());
		assertEquals(2, parser.getProbeCmdTemplate().getTcpFlags().size());
		assertEquals("S", parser.getProbeCmdTemplate().getTcpFlags().get(0));
		assertEquals("SA", parser.getProbeCmdTemplate().getTcpFlags().get(1));

		line = "p   SOURCE     DEST   tcp  PORT  flags S SA";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("probe", parser.getString("Command"));
		assertEquals("SOURCE", parser.getProbeCmdTemplate().getSrcAddress());
		assertEquals("DEST", parser.getProbeCmdTemplate().getDestAddress());
		assertEquals("tcp", parser.getProbeCmdTemplate().getProtoSpecification());
		assertEquals(null, parser.getProbeCmdTemplate().getPortSource());
		assertEquals("PORT", parser.getProbeCmdTemplate().getPortDest());
		assertEquals(2, parser.getProbeCmdTemplate().getTcpFlags().size());
		assertEquals("S", parser.getProbeCmdTemplate().getTcpFlags().get(0));
		assertEquals("SA", parser.getProbeCmdTemplate().getTcpFlags().get(1));

		line = "p   SOURCE     DEST   tcp  (PORT1,PORT1)  flags S SA";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("probe", parser.getString("Command"));
		assertEquals("SOURCE", parser.getProbeCmdTemplate().getSrcAddress());
		assertEquals("DEST", parser.getProbeCmdTemplate().getDestAddress());
		assertEquals("tcp", parser.getProbeCmdTemplate().getProtoSpecification());
		assertEquals(null, parser.getProbeCmdTemplate().getPortSource());
		assertEquals("(PORT1,PORT1)", parser.getProbeCmdTemplate().getPortDest());
		assertEquals(2, parser.getProbeCmdTemplate().getTcpFlags().size());
		assertEquals("S", parser.getProbeCmdTemplate().getTcpFlags().get(0));
		assertEquals("SA", parser.getProbeCmdTemplate().getTcpFlags().get(1));

		line = "p    active  learn   state   on    ON  match verbose  na expect    EXPECT     SOURCE     DEST";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("probe", parser.getString("Command"));
		assertEquals("SOURCE", parser.getProbeCmdTemplate().getSrcAddress());
		assertEquals("DEST", parser.getProbeCmdTemplate().getDestAddress());
		assertEquals("EXPECT", parser.getProbeCmdTemplate().getProbeExpect());
		assertEquals("ON", parser.getProbeCmdTemplate().getEquipments());
		assertTrue(parser.getProbeCmdTemplate().getProbeOptNoAction());
		assertTrue(parser.getProbeCmdTemplate().getProbeOptVerbose());
		assertTrue(parser.getProbeCmdTemplate().getProbeOptActive());
		assertTrue(parser.getProbeCmdTemplate().getProbeOptMatching());
		assertTrue(parser.getProbeCmdTemplate().getProbeOptLearn());
		assertTrue(parser.getProbeCmdTemplate().getProbeOptState());

		line = "p  \"SOURCE\"     \"DEST\"";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("probe", parser.getString("Command"));
		assertEquals("SOURCE", parser.getProbeCmdTemplate().getSrcAddress());
		assertEquals("DEST", parser.getProbeCmdTemplate().getDestAddress());

		line = "p  \"SOURCE - SOURCE\"     \"DEST - DEST\"";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("probe", parser.getString("Command"));
		assertEquals("SOURCE - SOURCE", parser.getProbeCmdTemplate().getSrcAddress());
		assertEquals("DEST - DEST", parser.getProbeCmdTemplate().getDestAddress());
	}

	public void testReload() {
		System.out.println("reload");

		String line = "reload";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("reload", parser.getString("Command"));

		line = "reload     EQ";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("reload", parser.getString("Command"));
		assertEquals("EQ", parser.getString("Equipments"));
	}

	public void testRoute() {
		System.out.println("route");

		String line = "route";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("route", parser.getString("Command"));

		line = "route     EQ";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("route", parser.getString("Command"));
		assertEquals("EQ", parser.getString("Equipments"));
	}

	public void testTopology() {
		System.out.println("topology");

		String line = "topology";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("topology", parser.getString("Command"));

		line = "topology   EQ";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("topology", parser.getString("Command"));
		assertEquals("EQ", parser.getString("Equipments"));

		line = "topology   connected  EQ";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("topology", parser.getString("Command"));
		assertEquals("EQ", parser.getString("Equipments"));
		assertEquals("connected", parser.getString("TopologyOption"));

		line = "topology   !connected";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("topology", parser.getString("Command"));
		assertEquals(null, parser.getString("Equipments"));
		assertEquals("!connected", parser.getString("TopologyOption"));
	}

	public void testHost() {
		System.out.println("host");

		String line = "host HOSTNAME HOSTNAME";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("host", parser.getString("Command"));
		assertEquals("HOSTNAME HOSTNAME", parser.getString("AddressArg"));
	}

	public void testHost6() {
		System.out.println("host6");

		String line = "host6 HOSTNAME HOSTNAME";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("host6", parser.getString("Command"));
		assertEquals("HOSTNAME HOSTNAME", parser.getString("AddressArg"));
	}

	public void testGroovy() {
		System.out.println("groovy");

		String line = "groovy DIRECTORY FILENAME";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("groovy", parser.getString("Command"));
		assertEquals("DIRECTORY", parser.getString("GroovyDirectory"));
		assertEquals("FILENAME", parser.getString("GroovyScript"));

		line = "g \"D I R E C T O R Y\"   \"F I L E N A M E\"";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("groovy", parser.getString("Command"));
		assertEquals("D I R E C T O R Y", parser.getString("GroovyDirectory"));
		assertEquals("F I L E N A M E", parser.getString("GroovyScript"));
	}

	public void testPolicyLoad() {
		System.out.println("policy-load");

		String line = "policy    load     FILENAME";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("policy-load", parser.getString("Command"));
		assertEquals("FILENAME", parser.getString("FileName"));

		line = "policy    load     \"c:         FILENAME   \"";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("policy-load", parser.getString("Command"));
		assertEquals("c:         FILENAME   ", parser.getString("FileName"));
	}

	public void testPolicyProbe() {
		System.out.println("policy-probe");

		String line = "policy    probe    POLICYNAME";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("policy-probe", parser.getString("Command"));
		assertEquals("POLICYNAME", parser.getString("PolicyName"));

		line = "policy    probe    POLICYNAME  to    TO";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("policy-probe", parser.getString("Command"));
		assertEquals("POLICYNAME", parser.getString("PolicyName"));
		assertEquals("TO", parser.getString("PolicyTo"));

		line = "policy    probe    POLICYNAME  from    FROM";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("policy-probe", parser.getString("Command"));
		assertEquals("POLICYNAME", parser.getString("PolicyName"));
		assertEquals("FROM", parser.getString("PolicyFrom"));

		line = "policy    probe    POLICYNAME  from    FROM   to    TO";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("policy-probe", parser.getString("Command"));
		assertEquals("POLICYNAME", parser.getString("PolicyName"));
		assertEquals("FROM", parser.getString("PolicyFrom"));
		assertEquals("TO", parser.getString("PolicyTo"));
	}

	public void testIp() {
		System.out.println("ip");

		String line = "ip ADDRESS1 ADDRESS2";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("ip", parser.getString("Command"));
		assertEquals("ADDRESS1 ADDRESS2", parser.getString("AddressArg"));

		line = "ip6 ADDRESS1 ADDRESS2";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("ip6", parser.getString("Command"));
		assertEquals("ADDRESS1 ADDRESS2", parser.getString("AddressArg"));
	}

	public void testPolicyList() {
		System.out.println("policy-list");

		String line = "policy list";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("policy-list", parser.getString("Command"));
		assertEquals(null, parser.getString("PolicyName"));

		line = "policy list POLICYNAME";
		result = new ReportingParseRunner(parser.CommandLine()).run(line);
		assertEquals("policy-list", parser.getString("Command"));
		assertEquals("POLICYNAME", parser.getString("PolicyName"));

	}

}
