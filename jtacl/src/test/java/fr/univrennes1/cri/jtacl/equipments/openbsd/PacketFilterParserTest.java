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

package fr.univrennes1.cri.jtacl.equipments.openbsd;

import fr.univrennes1.cri.jtacl.lib.misc.StringsList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;
import org.parboiled.Parboiled;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

/**
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PacketFilterParserTest extends TestCase {

	PacketFilterParser parser = Parboiled.createParser(PacketFilterParser.class);
	ReportingParseRunner parseRunParse = new ReportingParseRunner(parser.Parse());
	ParsingResult<?> result;

    public PacketFilterParserTest(String testName) {
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
	 * Test of getRule
	 */
	public void testGetRule() {
		System.out.println("getRule");
		StringBuilder line;
		ExpandedRule exRule;
		Map<String, String> macros = new HashMap<String, String>();
		macros.put("macro1", "ABCD");

		/*
		 * folding in list '{'
		 */
		line = new StringBuilder("abcd {\nitem1 \\\nitem2 \\\n}");
		exRule = parser.getRule(line, macros);
		assertEquals("abcd {\nitem1 \\\nitem2 \\\n}", exRule.expandedToString());

		line = new StringBuilder("abcd\\\nefgh");
		exRule = parser.getRule(line, macros);
		assertEquals("abcd\\\nefgh", exRule.expandedToString());

		line = new StringBuilder("#abcd\n");
		exRule = parser.getRule(line, macros);
		assertEquals("\n", exRule.expandedToString());


		line = new StringBuilder("abcd \\\nefgh");
		exRule = parser.getRule(line, macros);
		assertEquals("abcd \\\nefgh", exRule.expandedToString());

		/*
		 * nb : \n can appear in a STRING
		 */
		line = new StringBuilder("abcd \"STRI \nNG\"\n");
		exRule = parser.getRule(line, macros);
		assertEquals("abcd \"STRI \nNG\"\n", exRule.expandedToString());

	}

	/**
	 * test of rule PfMacro
	 */
	public void testPfMacro() {
		System.out.println("PfMacro");
		String line;

		line = "foobar=\"FOOBAR\"";
		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("foobar", parser.getName());
			assertEquals("FOOBAR", parser.getValue());
			assertEquals("macro", parser.getRuleName());
		}

		line = "foobar = \"FOOBAR \nFOOBAR\"";
		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("foobar", parser.getName());
			assertEquals("FOOBAR FOOBAR", parser.getValue());
			assertEquals("macro", parser.getRuleName());
		}
		
		line = "foobar = \"FOOBAR FOOBAR\"";
		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("foobar", parser.getName());
			assertEquals("FOOBAR FOOBAR", parser.getValue());
			assertEquals("macro", parser.getRuleName());
		}

		line = "foobar = \"FOOBAR\" FOOBAR";
		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("foobar", parser.getName());
			assertEquals("FOOBAR FOOBAR", parser.getValue());
			assertEquals("macro", parser.getRuleName());
		}
	}

	/**
	 * test of rule PfQuotedString
	 */
	public void testPfQuotedString() {
		System.out.println("PfQuotedString");

		ReportingParseRunner parseRunner =
				new ReportingParseRunner(parser.PfQuotedString());
		String line = "\"abc\nde\"";
		
		result = parseRunner.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("abcde", parser.getLastQuotedString());
		}

		line = "'abcde'";
		
		 result = parseRunner.run(line);
		 assertTrue(result.matched);
		if (result.matched) {
			assertEquals("abcde", parser.getLastQuotedString());
		}

		line = "'abc\"de'";
		
		 result = parseRunner.run(line);
		 assertTrue(result.matched);
		if (result.matched) {
			assertEquals("abc\"de", parser.getLastQuotedString());
		}

		line = "'abc\\'de'";
		
		 result = parseRunner.run(line);
		 assertTrue(result.matched);
		if (result.matched) {
			assertEquals("abc'de", parser.getLastQuotedString());
		}

		line = "'abc\\ de'";
		
		 result = parseRunner.run(line);
		 assertTrue(result.matched);
		if (result.matched) {
			assertEquals("abc de", parser.getLastQuotedString());
		}

		line = "'abc\\de'";
		
		 result = parseRunner.run(line);
		 assertTrue(result.matched);
		if (result.matched) {
			assertEquals("abcde", parser.getLastQuotedString());
		}
	}

	/**
	 * test of rule PfInclude
	 */
	public void testInclude() {
		System.out.println("Include");
		String line;

 		line = "include \"FILE NAME\"";
		
		 result = parseRunParse.run(line);
		 assertTrue(result.matched);
		if (result.matched) {
			assertEquals("FILE NAME", parser.getValue());
			assertEquals("include", parser.getRuleName());
		}

		line = "include FILENAME";
		
		 result = parseRunParse.run(line);
		 assertTrue(result.matched);
		if (result.matched) {
			assertEquals("FILENAME", parser.getValue());
			assertEquals("include", parser.getRuleName());
		}

		line = "include \"FILE NAME\"";
		
		 result = parseRunParse.run(line);
		 assertTrue(result.matched);
		if (result.matched) {
			assertEquals("FILE NAME", parser.getValue());
			assertEquals("include", parser.getRuleName());
		}

		line = "include 'FILE\\ NAME'";
		
		 result = parseRunParse.run(line);
		 assertTrue(result.matched);
		if (result.matched) {
			assertEquals("FILE NAME", parser.getValue());
			assertEquals("include", parser.getRuleName());
		}

	}

	/**
	 * test of rule PfRule
	 */
	public void testPfRule() {
		System.out.println("PfRule");
		String line;
		RuleTemplate rule;
		Xhost host;
		List<Xhost> hosts;
		PortItemTemplate port;
		List<PortItemTemplate> ports;
		String flags;
		String flagset;
		List<IcmpItem> icmp;
		IcmpItem icmpItem;
		StringsList states;
		ScrubOptsTemplate scrub;
		RedirSpecTemplate redir;
		PoolOptsTemplate poolOpt;
		RouteOptsTemplate routeOpt;

 		line = "pass in";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("in", rule.getDir());
			assertFalse(rule.isQuick());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass in quick";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("in", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass quick";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals(null, rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals(null, rule.getDir());
			assertFalse(rule.isQuick());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "block out quick";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("block", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("pfrule", parser.getRuleName());
		}

 		line = "block in";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("block", rule.getAction());
			assertEquals("in", rule.getDir());
			assertFalse(rule.isQuick());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "block in quick";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("block", rule.getAction());
			assertEquals("in", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "block quick";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("block", rule.getAction());
			assertEquals(null, rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "block";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("block", rule.getAction());
			assertEquals(null, rule.getDir());
			assertFalse(rule.isQuick());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "block out quick";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("block", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("pfrule", parser.getRuleName());
		}


		line = "match out quick";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("match", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick on IFACE";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("IFACE", rule.getIfList().get(0));
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick on ! IFACE";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("!IFACE", rule.getIfList().get(0));
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick on !IFACE";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("!IFACE", rule.getIfList().get(0));
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick on { IFACE0   , IFACE1 }";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertTrue(rule.getIfList().size() == 2);
			assertEquals("IFACE0", rule.getIfList().get(0));
			assertEquals("IFACE1", rule.getIfList().get(1));
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick on { ! IFACE0   , IFACE1 }";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertTrue(rule.getIfList().size() == 2);
			assertEquals("!IFACE0", rule.getIfList().get(0));
			assertEquals("IFACE1", rule.getIfList().get(1));
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick on {\n! IFACE0   , IFACE1 \n}";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertTrue(rule.getIfList().size() == 2);
			assertEquals("!IFACE0", rule.getIfList().get(0));
			assertEquals("IFACE1", rule.getIfList().get(1));
			assertEquals("pfrule", parser.getRuleName());
		}


		line = "pass out quick inet";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet", rule.getAf());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertTrue(rule.getProtoList().size() == 1);
			assertEquals("PROTO", rule.getProtoList().get(0));
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto { PROTO }";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertTrue(rule.getProtoList().size() == 1);
			assertEquals("PROTO", rule.getProtoList().get(0));
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto { PROTO1, PROTO2 }";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertTrue(rule.getProtoList().size() == 2);
			assertEquals("PROTO1", rule.getProtoList().get(0));
			assertEquals("PROTO2", rule.getProtoList().get(1));
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO all";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			assertTrue(rule.isAll());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO from HOST";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			hosts = rule.getSourceHostList();
			assertTrue(hosts.size() == 1);
			host = hosts.get(0);
			assertEquals("HOST", host.getFirstAddress());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO from <TABLE>";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			hosts = rule.getSourceHostList();
			assertTrue(hosts.size() == 1);
			host = hosts.get(0);
			assertEquals("TABLE", host.getTable());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO from HOST/FOO";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			hosts = rule.getSourceHostList();
			host = hosts.get(0);
			assertEquals("HOST/FOO", host.getFirstAddress());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO from !HOST/FOO";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			hosts = rule.getSourceHostList();
			host = hosts.get(0);
			assertTrue(host.isNot());
			assertEquals("HOST/FOO", host.getFirstAddress());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO from any";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			hosts = rule.getSourceHostList();
			host = hosts.get(0);
			assertTrue(host.isAny());
			assertEquals(null, host.getFirstAddress());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO from " +
				"{ HOST1, HOST2/MASK2, !RANGE1 - RANGE2, <TABLE>}";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			hosts = rule.getSourceHostList();
			assertTrue(hosts.size() == 4);
			host = hosts.get(0);
			assertEquals("HOST1", host.getFirstAddress());
			host = hosts.get(1);
			assertEquals("HOST2/MASK2", host.getFirstAddress());
			host = hosts.get(2);
			assertTrue(host.isNot());
			assertEquals("RANGE1", host.getFirstAddress());
			assertEquals("RANGE2", host.getLastAddress());
			host = hosts.get(3);
			assertFalse(host.isNot());
			assertEquals("TABLE", host.getTable());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick from " +
				"{\n HOST1, HOST2/MASK2, !RANGE1 - RANGE2, <TABLE>\n}";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			hosts = rule.getSourceHostList();
			assertTrue(hosts.size() == 4);
			host = hosts.get(0);
			assertEquals("HOST1", host.getFirstAddress());
			host = hosts.get(1);
			assertEquals("HOST2/MASK2", host.getFirstAddress());
			host = hosts.get(2);
			assertTrue(host.isNot());
			assertEquals("RANGE1", host.getFirstAddress());
			assertEquals("RANGE2", host.getLastAddress());
			host = hosts.get(3);
			assertFalse(host.isNot());
			assertEquals("TABLE", host.getTable());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick from " +
				"{ \"HOST1\", \"HOST2/MASK2\", !\"RANGE1\" - \"RANGE2\", <\"TABLE\">}";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			hosts = rule.getSourceHostList();
			assertTrue(hosts.size() == 4);
			host = hosts.get(0);
			assertEquals("HOST1", host.getFirstAddress());
			host = hosts.get(1);
			assertEquals("HOST2/MASK2", host.getFirstAddress());
			host = hosts.get(2);
			assertTrue(host.isNot());
			assertEquals("RANGE1", host.getFirstAddress());
			assertEquals("RANGE2", host.getLastAddress());
			host = hosts.get(3);
			assertFalse(host.isNot());
			assertEquals("TABLE", host.getTable());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick from " +
				"<TABLE>";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			hosts = rule.getSourceHostList();
			assertTrue(hosts.size() == 1);
			host = hosts.get(0);
			assertFalse(host.isNot());
			assertEquals("TABLE", host.getTable());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO from " +
				"{ HOST1 HOST2/MASK2 !RANGE1 - RANGE2 <TABLE>}";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			hosts = rule.getSourceHostList();
			host = hosts.get(0);
			assertEquals("HOST1", host.getFirstAddress());
			host = hosts.get(1);
			assertEquals("HOST2/MASK2", host.getFirstAddress());
			host = hosts.get(2);
			assertTrue(host.isNot());
			assertEquals("RANGE1", host.getFirstAddress());
			assertEquals("RANGE2", host.getLastAddress());
			host = hosts.get(3);
			assertFalse(host.isNot());
			assertEquals("TABLE", host.getTable());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO from " +
				"{ HOST1, no-route, !no-route}";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			hosts = rule.getSourceHostList();
			host = hosts.get(0);
			assertEquals("HOST1", host.getFirstAddress());
			host = hosts.get(1);
			assertTrue(host.isNoroute());
			host = hosts.get(2);
			assertTrue(host.isNot());
			assertTrue(host.isNoroute());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO from urpf-failed";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			hosts = rule.getSourceHostList();
			host = hosts.get(0);
			assertTrue(host.isUrpffailed());
			assertFalse(host.isNot());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO from !urpf-failed";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			hosts = rule.getSourceHostList();
			host = hosts.get(0);
			assertTrue(host.isUrpffailed());
			assertTrue(host.isNot());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO from (iface0:broadcast)";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			hosts = rule.getSourceHostList();
			host = hosts.get(0);
			assertEquals("iface0:broadcast", host.getDynaddr());
			assertEquals(null, host.getDynaddrMask());
			assertFalse(host.isNot());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO from (iface0:network)/24";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			hosts = rule.getSourceHostList();
			host = hosts.get(0);
			assertEquals("iface0:network", host.getDynaddr());
			assertEquals("24", host.getDynaddrMask());
			assertFalse(host.isNot());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO from any port PORT";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			assertTrue(rule.getSourceHostList().get(0).isAny());
			ports = rule.getSourcePortList();
			assertTrue(ports.size() == 1);
			port = ports.get(0);
			assertEquals("PORT", port.getFirstPort());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO from port PORT";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			ports = rule.getSourcePortList();
			port = ports.get(0);
			assertEquals("PORT", port.getFirstPort());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO from port=PORT";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			ports = rule.getSourcePortList();
			port = ports.get(0);
			assertEquals("PORT", port.getFirstPort());
			assertEquals("=", port.getOperator());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO from port!=PORT";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			ports = rule.getSourcePortList();
			port = ports.get(0);
			assertEquals("PORT", port.getFirstPort());
			assertEquals("!=", port.getOperator());
			assertEquals("pfrule", parser.getRuleName());
		}
		 
		line = "pass out quick inet6 proto PROTO from port<PORT";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			ports = rule.getSourcePortList();
			port = ports.get(0);
			assertEquals("PORT", port.getFirstPort());
			assertEquals("<", port.getOperator());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO from port<=PORT";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			ports = rule.getSourcePortList();
			port = ports.get(0);
			assertEquals("PORT", port.getFirstPort());
			assertEquals("<=", port.getOperator());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO from port>PORT";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			ports = rule.getSourcePortList();
			port = ports.get(0);
			assertEquals("PORT", port.getFirstPort());
			assertEquals(">", port.getOperator());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO from port>=PORT";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			ports = rule.getSourcePortList();
			port = ports.get(0);
			assertEquals("PORT", port.getFirstPort());
			assertEquals(">=", port.getOperator());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO from any port PORT1 <> PORT2 ";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			assertTrue(rule.getSourceHostList().get(0).isAny());
			ports = rule.getSourcePortList();
			assertTrue(ports.size() == 1);
			port = ports.get(0);
			assertEquals("PORT1", port.getFirstPort());
			assertEquals("PORT2", port.getLastPort());
			assertEquals("<>", port.getOperator());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO from any port PORT1:PORT2 ";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			assertTrue(rule.getSourceHostList().get(0).isAny());
			ports = rule.getSourcePortList();
			port = ports.get(0);
			assertEquals("PORT1", port.getFirstPort());
			assertEquals("PORT2", port.getLastPort());
			assertEquals(":", port.getOperator());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO from any port " + 
				"{PORT1:PORT2, =PORT3} ";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			assertTrue(rule.getSourceHostList().get(0).isAny());
			ports = rule.getSourcePortList();
			assertTrue(ports.size() == 2);
			port = ports.get(0);
			assertEquals("PORT1", port.getFirstPort());
			assertEquals("PORT2", port.getLastPort());
			assertEquals(":", port.getOperator());
			port = ports.get(1);
			assertEquals("PORT3", port.getFirstPort());
			assertEquals(null, port.getLastPort());
			assertEquals("=", port.getOperator());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO from any port " +
				"{\nPORT1, PORT2\n} ";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			assertTrue(rule.getSourceHostList().get(0).isAny());
			ports = rule.getSourcePortList();
			assertTrue(ports.size() == 2);
			port = ports.get(0);
			assertEquals("PORT1", port.getFirstPort());
			port = ports.get(1);
			assertEquals("PORT2", port.getFirstPort());
			assertEquals(null, port.getLastPort());
			assertEquals(null, port.getOperator());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO os {OS1,OS2,OS3}";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			assertTrue(rule.getOsList().size() == 3);
			assertEquals("OS1", rule.getOsList().get(0));
			assertEquals("OS2", rule.getOsList().get(1));
			assertEquals("OS3", rule.getOsList().get(2));
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO from any os {\nOS1,OS2,OS3\n}";
		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			assertTrue(rule.getSourceHostList().get(0).isAny());
			assertTrue(rule.getOsList().size() == 3);
			assertEquals("OS1", rule.getOsList().get(0));
			assertEquals("OS2", rule.getOsList().get(1));
			assertEquals("OS3", rule.getOsList().get(2));
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO to HOST";
		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			hosts = rule.getDestHostList();
			assertTrue(hosts.size() == 1);
			host = hosts.get(0);
			assertEquals("HOST", host.getFirstAddress());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO to \"HOST \nFOO\"";
		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			hosts = rule.getDestHostList();
			host = hosts.get(0);
			assertEquals("HOST FOO", host.getFirstAddress());
			assertEquals("pfrule", parser.getRuleName());
		}


		line = "pass out quick inet6 proto PROTO to HOST/FOO";
		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			hosts = rule.getDestHostList();
			host = hosts.get(0);
			assertEquals("HOST/FOO", host.getFirstAddress());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO to !HOST/FOO";
		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			hosts = rule.getDestHostList();
			host = hosts.get(0);
			assertTrue(host.isNot());
			assertEquals("HOST/FOO", host.getFirstAddress());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO from any to any";
		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			hosts = rule.getDestHostList();
			host = hosts.get(0);
			assertTrue(host.isAny());
			assertEquals(null, host.getFirstAddress());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO to " +
				"{ HOST1, HOST2/MASK2, !RANGE1 - RANGE2, <TABLE>}";
		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			hosts = rule.getDestHostList();
			host = hosts.get(0);
			assertEquals("HOST1", host.getFirstAddress());
			host = hosts.get(1);
			assertEquals("HOST2/MASK2", host.getFirstAddress());
			host = hosts.get(2);
			assertTrue(host.isNot());
			assertEquals("RANGE1", host.getFirstAddress());
			assertEquals("RANGE2", host.getLastAddress());
			host = hosts.get(3);
			assertFalse(host.isNot());
			assertEquals("TABLE", host.getTable());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO to {\n HOST1, no-route, !no-route\n}";
		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			hosts = rule.getDestHostList();
			host = hosts.get(0);
			assertEquals("HOST1", host.getFirstAddress());
			host = hosts.get(1);
			assertTrue(host.isNoroute());
			host = hosts.get(2);
			assertTrue(host.isNot());
			assertTrue(host.isNoroute());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO to urpf-failed";
		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			hosts = rule.getDestHostList();
			host = hosts.get(0);
			assertTrue(host.isUrpffailed());
			assertFalse(host.isNot());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO to !urpf-failed";
		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			hosts = rule.getDestHostList();
			host = hosts.get(0);
			assertTrue(host.isUrpffailed());
			assertTrue(host.isNot());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO to (iface0:broadcast)";
		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			hosts = rule.getDestHostList();
			host = hosts.get(0);
			assertEquals("iface0:broadcast", host.getDynaddr());
			assertEquals(null, host.getDynaddrMask());
			assertFalse(host.isNot());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO to (iface0:network)/24";
		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			hosts = rule.getDestHostList();
			host = hosts.get(0);
			assertEquals("iface0:network", host.getDynaddr());
			assertEquals("24", host.getDynaddrMask());
			assertFalse(host.isNot());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO to (iface0:network)/24";
		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			hosts = rule.getDestHostList();
			host = hosts.get(0);
			assertEquals("iface0:network", host.getDynaddr());
			assertEquals("24", host.getDynaddrMask());
			assertFalse(host.isNot());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO from any port PORT1 to any port PORT2";
		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			assertTrue(rule.getSourceHostList().get(0).isAny());
			ports = rule.getSourcePortList();
			port = ports.get(0);
			assertEquals("PORT1", port.getFirstPort());
			assertTrue(rule.getDestHostList().get(0).isAny());
			ports = rule.getDestPortList();
			port = ports.get(0);
			assertEquals("PORT2", port.getFirstPort());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO to port PORT";
		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			ports = rule.getDestPortList();
			port = ports.get(0);
			assertEquals("PORT", port.getFirstPort());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO to port=PORT";
		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			ports = rule.getDestPortList();
			port = ports.get(0);
			assertEquals("PORT", port.getFirstPort());
			assertEquals("=", port.getOperator());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO from any to any port {PORT1:PORT2, =PORT3} ";
		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			assertTrue(rule.getSourceHostList().get(0).isAny());
			assertTrue(rule.getDestHostList().get(0).isAny());
			ports = rule.getDestPortList();
			port = ports.get(0);
			assertEquals("PORT1", port.getFirstPort());
			assertEquals("PORT2", port.getLastPort());
			assertEquals(":", port.getOperator());
			port = ports.get(1);
			assertEquals("PORT3", port.getFirstPort());
			assertEquals(null, port.getLastPort());
			assertEquals("=", port.getOperator());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO from any to any " +
				"port {PORT1:PORT2, =PORT3} " +
				"user 20";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			assertTrue(rule.getSourceHostList().get(0).isAny());
			assertTrue(rule.getDestHostList().get(0).isAny());
			ports = rule.getDestPortList();
			port = ports.get(0);
			assertEquals("PORT1", port.getFirstPort());
			assertEquals("PORT2", port.getLastPort());
			assertEquals(":", port.getOperator());
			port = ports.get(1);
			assertEquals("PORT3", port.getFirstPort());
			assertEquals(null, port.getLastPort());
			assertEquals("=", port.getOperator());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO from any to any " +
				"port {\nPORT1:PORT2, =PORT3\n} " +
				"user { patrick, 20 }";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			assertTrue(rule.getSourceHostList().get(0).isAny());
			assertTrue(rule.getDestHostList().get(0).isAny());
			ports = rule.getDestPortList();
			port = ports.get(0);
			assertEquals("PORT1", port.getFirstPort());
			assertEquals("PORT2", port.getLastPort());
			assertEquals(":", port.getOperator());
			port = ports.get(1);
			assertEquals("PORT3", port.getFirstPort());
			assertEquals(null, port.getLastPort());
			assertEquals("=", port.getOperator());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO from any to any " +
				"port {PORT1:PORT2, =PORT3} " +
				"user {\n patrick, 20 \n} " +
				"group { root, 0 }";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			assertTrue(rule.getSourceHostList().get(0).isAny());
			assertTrue(rule.getDestHostList().get(0).isAny());
			ports = rule.getDestPortList();
			port = ports.get(0);
			assertEquals("PORT1", port.getFirstPort());
			assertEquals("PORT2", port.getLastPort());
			assertEquals(":", port.getOperator());
			port = ports.get(1);
			assertEquals("PORT3", port.getFirstPort());
			assertEquals(null, port.getLastPort());
			assertEquals("=", port.getOperator());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO from any to any " +
				"port {PORT1:PORT2, =PORT3} " +
				"user { patrick, 20 } " +
				"group {\n root, 0 \n}";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			assertTrue(rule.getSourceHostList().get(0).isAny());
			assertTrue(rule.getDestHostList().get(0).isAny());
			ports = rule.getDestPortList();
			port = ports.get(0);
			assertEquals("PORT1", port.getFirstPort());
			assertEquals("PORT2", port.getLastPort());
			assertEquals(":", port.getOperator());
			port = ports.get(1);
			assertEquals("PORT3", port.getFirstPort());
			assertEquals(null, port.getLastPort());
			assertEquals("=", port.getOperator());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO from any to any " +
				"port {PORT1:PORT2, =PORT3} " +
				"user { patrick, 20 } " +
				"group { root, 0 }";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			assertTrue(rule.getSourceHostList().get(0).isAny());
			assertTrue(rule.getDestHostList().get(0).isAny());
			ports = rule.getDestPortList();
			port = ports.get(0);
			assertEquals("PORT1", port.getFirstPort());
			assertEquals("PORT2", port.getLastPort());
			assertEquals(":", port.getOperator());
			port = ports.get(1);
			assertEquals("PORT3", port.getFirstPort());
			assertEquals(null, port.getLastPort());
			assertEquals("=", port.getOperator());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO from any to any " +
				"port {PORT1:PORT2, =PORT3} " +
				"user { patrick, 20 } " +
				"group { root, 0 } " +
				"flags S/SA";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			assertTrue(rule.getSourceHostList().get(0).isAny());
			assertTrue(rule.getDestHostList().get(0).isAny());
			ports = rule.getDestPortList();
			port = ports.get(0);
			assertEquals("PORT1", port.getFirstPort());
			assertEquals("PORT2", port.getLastPort());
			assertEquals(":", port.getOperator());
			port = ports.get(1);
			assertEquals("PORT3", port.getFirstPort());
			assertEquals(null, port.getLastPort());
			assertEquals("=", port.getOperator());
			flags = rule.getFilterOpts().getFlags();
			flagset = rule.getFilterOpts().getFlagset();
			assertEquals("S", flags);
			assertEquals("SA", flagset);
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO from any to any " +
				"port {PORT1:PORT2, =PORT3} " +
				"user { patrick, 20 } " +
				"group { root, 0 } " +
				"flags S/SA " +
				"icmp-type ICMP";


		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			assertTrue(rule.getSourceHostList().get(0).isAny());
			assertTrue(rule.getDestHostList().get(0).isAny());
			ports = rule.getDestPortList();
			port = ports.get(0);
			assertEquals("PORT1", port.getFirstPort());
			assertEquals("PORT2", port.getLastPort());
			assertEquals(":", port.getOperator());
			port = ports.get(1);
			assertEquals("PORT3", port.getFirstPort());
			assertEquals(null, port.getLastPort());
			assertEquals("=", port.getOperator());
			flags = rule.getFilterOpts().getFlags();
			flagset = rule.getFilterOpts().getFlagset();
			assertEquals("S", flags);
			assertEquals("SA", flagset);
			icmp = rule.getFilterOpts().getIcmpspec();
			assertTrue(icmp.size() == 1);
			icmpItem = icmp.get(0);
			assertEquals("ICMP", icmpItem.getIcmpType());
			assertEquals(null, icmpItem.getIcmpCode());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO from any to any " +
				"port {PORT1:PORT2, =PORT3} " +
				"user { patrick, 20 } " +
				"group { root, 0 } " +
				"flags S/SA " +
				"icmp-type ICMP code ICMPCODE";


		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			assertTrue(rule.getSourceHostList().get(0).isAny());
			assertTrue(rule.getDestHostList().get(0).isAny());
			ports = rule.getDestPortList();
			port = ports.get(0);
			assertEquals("PORT1", port.getFirstPort());
			assertEquals("PORT2", port.getLastPort());
			assertEquals(":", port.getOperator());
			port = ports.get(1);
			assertEquals("PORT3", port.getFirstPort());
			assertEquals(null, port.getLastPort());
			assertEquals("=", port.getOperator());
			flags = rule.getFilterOpts().getFlags();
			flagset = rule.getFilterOpts().getFlagset();
			assertEquals("S", flags);
			assertEquals("SA", flagset);
			icmp = rule.getFilterOpts().getIcmpspec();
			assertTrue(icmp.size() == 1);
			icmpItem = icmp.get(0);
			assertEquals("ICMP", icmpItem.getIcmpType());
			assertEquals("ICMPCODE", icmpItem.getIcmpCode());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO from any to any " +
				"port {PORT1:PORT2, =PORT3} " +
				"user { patrick, 20 } " +
				"group { root, 0 } " +
				"flags S/SA " +
				"icmp-type ICMP code ICMPCODE";


		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			assertTrue(rule.getSourceHostList().get(0).isAny());
			assertTrue(rule.getDestHostList().get(0).isAny());
			ports = rule.getDestPortList();
			port = ports.get(0);
			assertEquals("PORT1", port.getFirstPort());
			assertEquals("PORT2", port.getLastPort());
			assertEquals(":", port.getOperator());
			port = ports.get(1);
			assertEquals("PORT3", port.getFirstPort());
			assertEquals(null, port.getLastPort());
			assertEquals("=", port.getOperator());
			flags = rule.getFilterOpts().getFlags();
			flagset = rule.getFilterOpts().getFlagset();
			assertEquals("S", flags);
			assertEquals("SA", flagset);
			icmp = rule.getFilterOpts().getIcmpspec();
			icmpItem = icmp.get(0);
			assertEquals("ICMP", icmpItem.getIcmpType());
			assertEquals("ICMPCODE", icmpItem.getIcmpCode());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO from any to any " +
				"port {PORT1:PORT2, =PORT3} " +
				"user { patrick, 20 } " +
				"group { root, 0 } " +
				"flags S/SA " +
				"icmp-type {ICMP code ICMPCODE}";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			assertTrue(rule.getSourceHostList().get(0).isAny());
			assertTrue(rule.getDestHostList().get(0).isAny());
			ports = rule.getDestPortList();
			port = ports.get(0);
			assertEquals("PORT1", port.getFirstPort());
			assertEquals("PORT2", port.getLastPort());
			assertEquals(":", port.getOperator());
			port = ports.get(1);
			assertEquals("PORT3", port.getFirstPort());
			assertEquals(null, port.getLastPort());
			assertEquals("=", port.getOperator());
			flags = rule.getFilterOpts().getFlags();
			flagset = rule.getFilterOpts().getFlagset();
			assertEquals("S", flags);
			assertEquals("SA", flagset);
			icmp = rule.getFilterOpts().getIcmpspec();
			icmpItem = icmp.get(0);
			assertEquals("ICMP", icmpItem.getIcmpType());
			assertEquals("ICMPCODE", icmpItem.getIcmpCode());
			assertEquals("pfrule", parser.getRuleName());
		}


		line = "pass out quick inet6 proto PROTO from any to any " +
				"port {PORT1:PORT2, =PORT3} " +
				"user { patrick, 20 } " +
				"group { root, 0 } " +
				"flags S/SA " +
				"icmp-type {\nICMP0, ICMP1 code ICMP1CODE \n}";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			assertTrue(rule.getSourceHostList().get(0).isAny());
			assertTrue(rule.getDestHostList().get(0).isAny());
			ports = rule.getDestPortList();
			port = ports.get(0);
			assertEquals("PORT1", port.getFirstPort());
			assertEquals("PORT2", port.getLastPort());
			assertEquals(":", port.getOperator());
			port = ports.get(1);
			assertEquals("PORT3", port.getFirstPort());
			assertEquals(null, port.getLastPort());
			assertEquals("=", port.getOperator());
			flags = rule.getFilterOpts().getFlags();
			flagset = rule.getFilterOpts().getFlagset();
			assertEquals("S", flags);
			assertEquals("SA", flagset);
			icmp = rule.getFilterOpts().getIcmpspec();
			assertTrue(icmp.size() == 2);
			icmpItem = icmp.get(0);
			assertEquals("ICMP0", icmpItem.getIcmpType());
			assertEquals(null, icmpItem.getIcmpCode());
			icmpItem = icmp.get(1);
			assertEquals("ICMP1", icmpItem.getIcmpType());
			assertEquals("ICMP1CODE", icmpItem.getIcmpCode());
			assertEquals("pfrule", parser.getRuleName());
		}
		line = "pass out quick inet6 proto PROTO from any to any " +
				"port {PORT1:PORT2, =PORT3} " +
				"user { patrick, 20 } " +
				"group { root, 0 } " +
				"flags S/SA " +
				"icmp6-type {ICMP code ICMPCODE}";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			assertTrue(rule.getSourceHostList().get(0).isAny());
			assertTrue(rule.getDestHostList().get(0).isAny());
			ports = rule.getDestPortList();
			port = ports.get(0);
			assertEquals("PORT1", port.getFirstPort());
			assertEquals("PORT2", port.getLastPort());
			assertEquals(":", port.getOperator());
			port = ports.get(1);
			assertEquals("PORT3", port.getFirstPort());
			assertEquals(null, port.getLastPort());
			assertEquals("=", port.getOperator());
			flags = rule.getFilterOpts().getFlags();
			flagset = rule.getFilterOpts().getFlagset();
			assertEquals("S", flags);
			assertEquals("SA", flagset);
			icmp = rule.getFilterOpts().getIcmp6spec();
			icmpItem = icmp.get(0);
			assertEquals("ICMP", icmpItem.getIcmpType());
			assertEquals("ICMPCODE", icmpItem.getIcmpCode());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO from any to any " +
				"port {PORT1:PORT2, =PORT3} " +
				"user { patrick, 20 } " +
				"group { root, 0 } " +
				"flags S/SA " +
				"icmp6-type ICMP code ICMPCODE";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			assertTrue(rule.getSourceHostList().get(0).isAny());
			assertTrue(rule.getDestHostList().get(0).isAny());
			ports = rule.getDestPortList();
			port = ports.get(0);
			assertEquals("PORT1", port.getFirstPort());
			assertEquals("PORT2", port.getLastPort());
			assertEquals(":", port.getOperator());
			port = ports.get(1);
			assertEquals("PORT3", port.getFirstPort());
			assertEquals(null, port.getLastPort());
			assertEquals("=", port.getOperator());
			flags = rule.getFilterOpts().getFlags();
			flagset = rule.getFilterOpts().getFlagset();
			assertEquals("S", flags);
			assertEquals("SA", flagset);
			icmp = rule.getFilterOpts().getIcmp6spec();
			icmpItem = icmp.get(0);
			assertEquals("ICMP", icmpItem.getIcmpType());
			assertEquals("ICMPCODE", icmpItem.getIcmpCode());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO from any to any " +
				"port {PORT1:PORT2, =PORT3} " +
				"user { patrick, 20 } " +
				"group { root, 0 } " +
				"flags S/S " +
				"icmp6-type {ICMP0, ICMP1 code ICMP1CODE}";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			assertTrue(rule.getSourceHostList().get(0).isAny());
			assertTrue(rule.getDestHostList().get(0).isAny());
			ports = rule.getDestPortList();
			port = ports.get(0);
			assertEquals("PORT1", port.getFirstPort());
			assertEquals("PORT2", port.getLastPort());
			assertEquals(":", port.getOperator());
			port = ports.get(1);
			assertEquals("PORT3", port.getFirstPort());
			assertEquals(null, port.getLastPort());
			assertEquals("=", port.getOperator());
			flags = rule.getFilterOpts().getFlags();
			flagset = rule.getFilterOpts().getFlagset();
			assertEquals("S", flags);
			assertEquals("S", flagset);
			icmp = rule.getFilterOpts().getIcmp6spec();
			assertTrue(icmp.size() == 2);
			icmpItem = icmp.get(0);
			assertEquals("ICMP0", icmpItem.getIcmpType());
			assertEquals(null, icmpItem.getIcmpCode());
			icmpItem = icmp.get(1);
			assertEquals("ICMP1", icmpItem.getIcmpType());
			assertEquals("ICMP1CODE", icmpItem.getIcmpCode());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO from any to any " +
				"port {PORT1:PORT2, =PORT3} " +
				"user { patrick, 20 } " +
				"group { root, 0 } " +
				"flags /SA " +
				"icmp6-type {ICMP0, ICMP1 code ICMP1CODE} " +
				"tos TOS";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			assertTrue(rule.getSourceHostList().get(0).isAny());
			assertTrue(rule.getDestHostList().get(0).isAny());
			ports = rule.getDestPortList();
			port = ports.get(0);
			assertEquals("PORT1", port.getFirstPort());
			assertEquals("PORT2", port.getLastPort());
			assertEquals(":", port.getOperator());
			port = ports.get(1);
			assertEquals("PORT3", port.getFirstPort());
			assertEquals(null, port.getLastPort());
			assertEquals("=", port.getOperator());
			flags = rule.getFilterOpts().getFlags();
			flagset = rule.getFilterOpts().getFlagset();
			assertEquals(null, flags);
			assertEquals("SA", flagset);
			icmp = rule.getFilterOpts().getIcmp6spec();
			icmpItem = icmp.get(0);
			assertEquals("ICMP0", icmpItem.getIcmpType());
			assertEquals(null, icmpItem.getIcmpCode());
			icmpItem = icmp.get(1);
			assertEquals("ICMP1", icmpItem.getIcmpType());
			assertEquals("ICMP1CODE", icmpItem.getIcmpCode());
			assertEquals("TOS", rule.getFilterOpts().getTos());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO from any to any " +
				"port {PORT1:PORT2, =PORT3} " +
				"user { patrick, 20 } " +
				"group { root, 0 } " +
				"flags any " +
				"icmp6-type {ICMP0, ICMP1 code ICMP1CODE} " +
				"tos TOS " +
				"no state";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			assertTrue(rule.getSourceHostList().get(0).isAny());
			assertTrue(rule.getDestHostList().get(0).isAny());
			ports = rule.getDestPortList();
			port = ports.get(0);
			assertEquals("PORT1", port.getFirstPort());
			assertEquals("PORT2", port.getLastPort());
			assertEquals(":", port.getOperator());
			port = ports.get(1);
			assertEquals("PORT3", port.getFirstPort());
			assertEquals(null, port.getLastPort());
			assertEquals("=", port.getOperator());
			flags = rule.getFilterOpts().getFlags();
			flagset = rule.getFilterOpts().getFlagset();
			assertEquals("any", flags);
			assertEquals(null, flagset);
			icmp = rule.getFilterOpts().getIcmp6spec();
			icmpItem = icmp.get(0);
			assertEquals("ICMP0", icmpItem.getIcmpType());
			assertEquals(null, icmpItem.getIcmpCode());
			icmpItem = icmp.get(1);
			assertEquals("ICMP1", icmpItem.getIcmpType());
			assertEquals("ICMP1CODE", icmpItem.getIcmpCode());
			assertEquals("TOS", rule.getFilterOpts().getTos());
			assertEquals("no-state", rule.getFilterOpts().getAction());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO from any to any " +
				"keep state";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			assertTrue(rule.getSourceHostList().get(0).isAny());
			assertTrue(rule.getDestHostList().get(0).isAny());
			assertEquals("keep-state", rule.getFilterOpts().getAction());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO from any to any " +
				"modulate state";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			assertTrue(rule.getSourceHostList().get(0).isAny());
			assertTrue(rule.getDestHostList().get(0).isAny());
			assertEquals("modulate-state", rule.getFilterOpts().getAction());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick inet6 proto PROTO from any to any " +
				"synproxy state";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			assertTrue(rule.getSourceHostList().get(0).isAny());
			assertTrue(rule.getDestHostList().get(0).isAny());
			assertEquals("synproxy-state", rule.getFilterOpts().getAction());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick " +
				"synproxy state " +
				"(max 00, no-sync,max-src-states 01,max-src-conn 02" +
				",max-src-conn-rate 99/98" +
				",overload <TABLE>" +
				",overload <TABLE> flush" +
				",overload <TABLE> flush global" +
				",max-src-nodes 03" +
				",source-track" +
				",source-track global" +
				",source-track rule" +
				",if-bound" +
				",floating" +
				",sloppy" +
				",pflow" +
				",STRING NUMBER" +
				")";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("synproxy-state", rule.getFilterOpts().getAction());
			states = rule.getFilterOpts().getOptions();
			assertTrue(states.size() == 17);
			assertEquals("MAXIMUM 00", states.get(0));
			assertEquals("NOSYNC", states.get(1));
			assertEquals("MAXSRCSTATES 01", states.get(2));
			assertEquals("MAXSRCCONN 02", states.get(3));
			assertEquals("MAXSRCCONNRATE 99 98", states.get(4));
			assertEquals("OVERLOAD <TABLE>", states.get(5));
			assertEquals("OVERLOAD <TABLE> flush", states.get(6));
			assertEquals("OVERLOAD <TABLE> flush global", states.get(7));
			assertEquals("MAXSRCNODES 03", states.get(8));
			assertEquals("SOURCETRACK", states.get(9));
			assertEquals("SOURCETRACK global", states.get(10));
			assertEquals("SOURCETRACK rule", states.get(11));
			assertEquals("STATELOCK if-bound", states.get(12));
			assertEquals("STATELOCK floating", states.get(13));
			assertEquals("SLOPPY", states.get(14));
			assertEquals("PFLOW", states.get(15));
			assertEquals("STRING STRING NUMBER", states.get(16));
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick fragment";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertTrue(rule.getFilterOpts().isFragment());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick fragment allow-opts";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertTrue(rule.getFilterOpts().isFragment());
			assertTrue(rule.getFilterOpts().isAllowopts());			
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick fragment allow-opts label LABEL";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertTrue(rule.getFilterOpts().isFragment());
			assertTrue(rule.getFilterOpts().isAllowopts());
			assertEquals("LABEL", rule.getFilterOpts().getLabel());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick queue QNAME";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("QNAME", rule.getFilterOpts().getQname());
			assertEquals(null, rule.getFilterOpts().getPQname());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick queue ( QNAME )";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("QNAME", rule.getFilterOpts().getQname());
			assertEquals(null, rule.getFilterOpts().getPQname());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick queue ( QNAME, PQNAME )";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("QNAME", rule.getFilterOpts().getQname());
			assertEquals("PQNAME", rule.getFilterOpts().getPQname());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick tag TAG";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("TAG", rule.getFilterOpts().getTag());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick tagged TAGGED";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("TAGGED", rule.getFilterOpts().getMatchTag());
			assertFalse(rule.getFilterOpts().isMatchTagNot());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick !tagged TAGGED";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("TAGGED", rule.getFilterOpts().getMatchTag());
			assertTrue(rule.getFilterOpts().isMatchTagNot());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick !tagged TAGGED probability PROBABILITY";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("TAGGED", rule.getFilterOpts().getMatchTag());
			assertTrue(rule.getFilterOpts().isMatchTagNot());
			assertEquals("PROBABILITY", rule.getFilterOpts().getProbability());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick probability PROBABILITY rtable RTABLE";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("PROBABILITY", rule.getFilterOpts().getProbability());
			assertEquals("RTABLE", rule.getFilterOpts().getRtableId());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick probability PROBABILITY divert-to HOST port PORT";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("HOST", rule.getFilterOpts().getDivertAddr());
			assertEquals("PORT", rule.getFilterOpts().getDivertPort());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick probability PROBABILITY divert-reply";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals(null, rule.getFilterOpts().getDivertAddr());
			assertEquals("1", rule.getFilterOpts().getDivertPort());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick probability PROBABILITY divert-packet port PORT";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals(null, rule.getFilterOpts().getDivertAddr());
			assertEquals("PORT", rule.getFilterOpts().getDivertPacketPort());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick scrub (" +
				"    no-df" +
				",min-ttl TTL" +
				"  ,   max-mss MSS" +
				", set-tos TOS" +
				", reassemble tcp" +
				", random-id" +
				"   )";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			scrub = rule.getFilterOpts().getScrubOpts();
			assertTrue(scrub.isNodf());
			assertEquals("TTL", scrub.getMinttl());
			assertEquals("MSS", scrub.getMaxmss());
			assertEquals("TOS", scrub.getSettos());
			assertTrue(scrub.isReassemble_tcp());
			assertTrue(scrub.isRandomid());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick " +
				"nat-to HOST";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			redir = rule.getFilterOpts().getNat();
			host = redir.getHosts().get(0);
			assertEquals("HOST", host.getFirstAddress());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick " +
				"nat-to HOST port PORT";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			redir = rule.getFilterOpts().getNat();
			host = redir.getHosts().get(0);
			assertEquals("HOST", host.getFirstAddress());
			assertEquals("PORT", redir.getPortstar());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick " +
				"nat-to { HOST ,  HOST1,   HOST2 } port PORT";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			redir = rule.getFilterOpts().getNat();
			host = redir.getHosts().get(0);
			assertEquals("HOST", host.getFirstAddress());
			host = redir.getHosts().get(1);
			assertEquals("HOST1", host.getFirstAddress());
			host = redir.getHosts().get(2);
			assertEquals("HOST2", host.getFirstAddress());
			assertEquals("PORT", redir.getPortstar());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick " +
				"nat-to { HOST ,  HOST1,   HOST2 } port PORT " +
				"bitmask static-port sticky-address" ;

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			redir = rule.getFilterOpts().getNat();
			host = redir.getHosts().get(0);
			assertEquals("HOST", host.getFirstAddress());
			host = redir.getHosts().get(1);
			assertEquals("HOST1", host.getFirstAddress());
			host = redir.getHosts().get(2);
			assertEquals("HOST2", host.getFirstAddress());
			assertEquals("PORT", redir.getPortstar());
			poolOpt = redir.getPoolOpts();
			assertTrue(poolOpt.getType() == PfConst.PF_POOL_BITMASK);
			assertTrue(poolOpt.isStaticPort());
			assertTrue(poolOpt.getOpts() == PfConst.PF_POOL_STICKYADDR);
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick " +
				"nat-to { HOST ,  HOST1,   HOST2 } port PORT " +
				"random static-port sticky-address" ;

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			redir = rule.getFilterOpts().getNat();
			host = redir.getHosts().get(0);
			assertEquals("HOST", host.getFirstAddress());
			host = redir.getHosts().get(1);
			assertEquals("HOST1", host.getFirstAddress());
			host = redir.getHosts().get(2);
			assertEquals("HOST2", host.getFirstAddress());
			assertEquals("PORT", redir.getPortstar());
			poolOpt = redir.getPoolOpts();
			assertTrue(poolOpt.getType() == PfConst.PF_POOL_RANDOM);
			assertTrue(poolOpt.isStaticPort());
			assertTrue(poolOpt.getOpts() == PfConst.PF_POOL_STICKYADDR);
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick " +
				"nat-to { HOST ,  HOST1,   HOST2 } port PORT " +
				"source-hash static-port sticky-address" ;

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			redir = rule.getFilterOpts().getNat();
			host = redir.getHosts().get(0);
			assertEquals("HOST", host.getFirstAddress());
			host = redir.getHosts().get(1);
			assertEquals("HOST1", host.getFirstAddress());
			host = redir.getHosts().get(2);
			assertEquals("HOST2", host.getFirstAddress());
			assertEquals("PORT", redir.getPortstar());
			poolOpt = redir.getPoolOpts();
			assertTrue(poolOpt.getType() == PfConst.PF_POOL_SRCHASH);
			assertTrue(poolOpt.isStaticPort());
			assertTrue(poolOpt.getOpts() == PfConst.PF_POOL_STICKYADDR);
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick " +
				"nat-to { HOST ,  HOST1 / 24   HOST2 } port PORT " +
				"source-hash     KEY     static-port      sticky-address" ;

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			redir = rule.getFilterOpts().getNat();
			host = redir.getHosts().get(0);
			assertEquals("HOST", host.getFirstAddress());
			host = redir.getHosts().get(1);
			assertEquals("HOST1/24", host.getFirstAddress());
			host = redir.getHosts().get(2);
			assertEquals("HOST2", host.getFirstAddress());
			assertEquals("PORT", redir.getPortstar());
			poolOpt = redir.getPoolOpts();
			assertTrue(poolOpt.getType() == PfConst.PF_POOL_SRCHASH);
			assertEquals("KEY", poolOpt.getKey());
			assertTrue(poolOpt.isStaticPort());
			assertTrue(poolOpt.getOpts() == PfConst.PF_POOL_STICKYADDR);
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick " +
				"nat-to { HOST ,  HOST1,   HOST2 } port PORT " +
				"round-robin     static-port      sticky-address" ;

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			redir = rule.getFilterOpts().getNat();
			host = redir.getHosts().get(0);
			assertEquals("HOST", host.getFirstAddress());
			host = redir.getHosts().get(1);
			assertEquals("HOST1", host.getFirstAddress());
			host = redir.getHosts().get(2);
			assertEquals("HOST2", host.getFirstAddress());
			assertEquals("PORT", redir.getPortstar());
			poolOpt = redir.getPoolOpts();
			assertTrue(poolOpt.getType() == PfConst.PF_POOL_ROUNDROBIN);
			assertTrue(poolOpt.isStaticPort());
			assertTrue(poolOpt.getOpts() == PfConst.PF_POOL_STICKYADDR);
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick " +
				"binat-to { HOST ,  HOST1,   HOST2 } port PORT " +
				"round-robin     static-port      sticky-address" ;

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			redir = rule.getFilterOpts().getNat();
			assertTrue(rule.getFilterOpts().isBinat());
			host = redir.getHosts().get(0);
			assertEquals("HOST", host.getFirstAddress());
			host = redir.getHosts().get(1);
			assertEquals("HOST1", host.getFirstAddress());
			host = redir.getHosts().get(2);
			assertEquals("HOST2", host.getFirstAddress());
			assertEquals("PORT", redir.getPortstar());
			poolOpt = redir.getPoolOpts();
			assertTrue(poolOpt.getType() == PfConst.PF_POOL_ROUNDROBIN);
			assertTrue(poolOpt.isStaticPort());
			assertTrue(poolOpt.getOpts() == PfConst.PF_POOL_STICKYADDR);
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick fastroute";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			routeOpt = rule.getFilterOpts().getRouteOpts();
			assertTrue(routeOpt.getRt() == PfConst.PF_FASTROUTE);
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick " +
				"route-to HOST";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			routeOpt = rule.getFilterOpts().getRouteOpts();
			assertTrue(routeOpt.getRt() == PfConst.PF_ROUTETO);
			host = routeOpt.getHosts().get(0);
			assertEquals("HOST", host.getFirstAddress());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick route-to " +
				"{ HOST1, HOST2/MASK2, <TABLE>, (DYNADDR)/24, (IFNAME HOST)  }";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			routeOpt = rule.getFilterOpts().getRouteOpts();
			assertTrue(routeOpt.getRt() == PfConst.PF_ROUTETO);
			hosts = routeOpt.getHosts();
			host = hosts.get(0);
			assertEquals("HOST1", host.getFirstAddress());
			host = hosts.get(1);
			assertEquals("HOST2/MASK2", host.getFirstAddress());
			host = hosts.get(2);
			assertEquals("TABLE", host.getTable());
			host = hosts.get(3);
			assertEquals("DYNADDR", host.getDynaddr());
			assertEquals("24", host.getDynaddrMask());
			host = hosts.get(4);
			assertEquals("IFNAME", host.getIfName());
			assertEquals("HOST", host.getFirstAddress());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick route-to " +
				"{ HOST1, HOST2/MASK2, <TABLE>, (DYNADDR)/24, (IFNAME HOST)  } " +
				"round-robin     static-port      sticky-address" ;

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			routeOpt = rule.getFilterOpts().getRouteOpts();
			assertTrue(routeOpt.getRt() == PfConst.PF_ROUTETO);
			hosts = routeOpt.getHosts();
			host = hosts.get(0);
			assertEquals("HOST1", host.getFirstAddress());
			host = hosts.get(1);
			assertEquals("HOST2/MASK2", host.getFirstAddress());
			host = hosts.get(2);
			assertEquals("TABLE", host.getTable());
			host = hosts.get(3);
			assertEquals("DYNADDR", host.getDynaddr());
			assertEquals("24", host.getDynaddrMask());
			host = hosts.get(4);
			assertEquals("IFNAME", host.getIfName());
			assertEquals("HOST", host.getFirstAddress());
			poolOpt = routeOpt.getPoolOpts();
			assertTrue(poolOpt.getType() == PfConst.PF_POOL_ROUNDROBIN);
			assertTrue(poolOpt.isStaticPort());
			assertTrue(poolOpt.getOpts() == PfConst.PF_POOL_STICKYADDR);
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick reply-to " +
				"{ HOST1, HOST2/MASK2, <TABLE>, (DYNADDR)/24, (IFNAME HOST)  }";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			routeOpt = rule.getFilterOpts().getRouteOpts();
			assertTrue(routeOpt.getRt() == PfConst.PF_REPLYTO);
			hosts = routeOpt.getHosts();
			host = hosts.get(0);
			assertEquals("HOST1", host.getFirstAddress());
			host = hosts.get(1);
			assertEquals("HOST2/MASK2", host.getFirstAddress());
			host = hosts.get(2);
			assertEquals("TABLE", host.getTable());
			host = hosts.get(3);
			assertEquals("DYNADDR", host.getDynaddr());
			assertEquals("24", host.getDynaddrMask());
			host = hosts.get(4);
			assertEquals("IFNAME", host.getIfName());
			assertEquals("HOST", host.getFirstAddress());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick reply-to " +
				"{ HOST1, HOST2/MASK2, <TABLE>, (DYNADDR)/24, (IFNAME HOST)} " +
				"round-robin     static-port      sticky-address" ;

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			routeOpt = rule.getFilterOpts().getRouteOpts();
			assertTrue(routeOpt.getRt() == PfConst.PF_REPLYTO);
			hosts = routeOpt.getHosts();
			host = hosts.get(0);
			assertEquals("HOST1", host.getFirstAddress());
			host = hosts.get(1);
			assertEquals("HOST2/MASK2", host.getFirstAddress());
			host = hosts.get(2);
			assertEquals("TABLE", host.getTable());
			host = hosts.get(3);
			assertEquals("DYNADDR", host.getDynaddr());
			assertEquals("24", host.getDynaddrMask());
			host = hosts.get(4);
			assertEquals("IFNAME", host.getIfName());
			assertEquals("HOST", host.getFirstAddress());
			poolOpt = routeOpt.getPoolOpts();
			assertTrue(poolOpt.getType() == PfConst.PF_POOL_ROUNDROBIN);
			assertTrue(poolOpt.isStaticPort());
			assertTrue(poolOpt.getOpts() == PfConst.PF_POOL_STICKYADDR);
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick dup-to " +
				"{\n HOST1, HOST2/MASK2, <TABLE>, (DYNADDR)/24, (IFNAME HOST) \n }";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			routeOpt = rule.getFilterOpts().getRouteOpts();
			assertTrue(routeOpt.getRt() == PfConst.PF_DUPTO);
			hosts = routeOpt.getHosts();
			host = hosts.get(0);
			assertEquals("HOST1", host.getFirstAddress());
			host = hosts.get(1);
			assertEquals("HOST2/MASK2", host.getFirstAddress());
			host = hosts.get(2);
			assertEquals("TABLE", host.getTable());
			host = hosts.get(3);
			assertEquals("DYNADDR", host.getDynaddr());
			assertEquals("24", host.getDynaddrMask());
			host = hosts.get(4);
			assertEquals("IFNAME", host.getIfName());
			assertEquals("HOST", host.getFirstAddress());
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick dup-to " +
				"{ HOST1, HOST2/MASK2, <TABLE>, (DYNADDR)/24, (IFNAME HOST)} " +
				"round-robin     static-port      sticky-address" ;

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			routeOpt = rule.getFilterOpts().getRouteOpts();
			assertTrue(routeOpt.getRt() == PfConst.PF_DUPTO);
			hosts = routeOpt.getHosts();
			host = hosts.get(0);
			assertEquals("HOST1", host.getFirstAddress());
			host = hosts.get(1);
			assertEquals("HOST2/MASK2", host.getFirstAddress());
			host = hosts.get(2);
			assertEquals("TABLE", host.getTable());
			host = hosts.get(3);
			assertEquals("DYNADDR", host.getDynaddr());
			assertEquals("24", host.getDynaddrMask());
			host = hosts.get(4);
			assertEquals("IFNAME", host.getIfName());
			assertEquals("HOST", host.getFirstAddress());
			poolOpt = routeOpt.getPoolOpts();
			assertTrue(poolOpt.getType() == PfConst.PF_POOL_ROUNDROBIN);
			assertTrue(poolOpt.isStaticPort());
			assertTrue(poolOpt.getOpts() == PfConst.PF_POOL_STICKYADDR);
			assertEquals("pfrule", parser.getRuleName());
		}

		line = "pass out quick received-on IFNAME";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("pass", rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("IFNAME", rule.getFilterOpts().getRcv());
			assertEquals("pfrule", parser.getRuleName());
		}

	}

	/**
	 * test of rule PfTableDef
	 */
	public void testPfTableDef() {
		System.out.println("PfTableDef");
		String line;
		TableTemplate table;
		List<Xhost> hosts;

		line = "table < TABLE >" ;

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			table = parser.getPfTable();
			assertEquals("TABLE", table.getName());
			assertEquals("tabledef", parser.getRuleName());
		}

		line = "table < TABLE > const counter persist {}" ;

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			table = parser.getPfTable();
			assertEquals("TABLE", table.getName());
			assertEquals("const", table.getOptions().get(0));
			assertEquals("counter", table.getOptions().get(1));
			assertEquals("persist", table.getOptions().get(2));
			assertEquals("tabledef", parser.getRuleName());
		}

		line = "table < TABLE > const counter \\\npersist { HOST1 HOST2 }" ;

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			table = parser.getPfTable();
			hosts = table.getHosts();
			assertEquals("TABLE", table.getName());
			assertEquals("const", table.getOptions().get(0));
			assertEquals("counter", table.getOptions().get(1));
			assertEquals("persist", table.getOptions().get(2));
			assertEquals("HOST1", hosts.get(0).getFirstAddress());
			assertEquals("HOST2", hosts.get(1).getFirstAddress());
			assertEquals("tabledef", parser.getRuleName());
		}

		line = "table < TABLE > const counter persist { \nHOST1, HOST2 \n}" ;

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			table = parser.getPfTable();
			hosts = table.getHosts();
			assertEquals("TABLE", table.getName());
			assertEquals("const", table.getOptions().get(0));
			assertEquals("counter", table.getOptions().get(1));
			assertEquals("persist", table.getOptions().get(2));
			assertEquals("HOST1", hosts.get(0).getFirstAddress());
			assertEquals("HOST2", hosts.get(1).getFirstAddress());
			assertEquals("tabledef", parser.getRuleName());
		}


		line = "table <\"table\"> const counter persist file \"FILE 1\"" +
				" file \"FILE 2\"" ;

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			table = parser.getPfTable();
			assertEquals("table", table.getName());
			assertEquals("const", table.getOptions().get(0));
			assertEquals("counter", table.getOptions().get(1));
			assertEquals("persist", table.getOptions().get(2));
			assertTrue(table.getFileNames().size() == 2);
			assertEquals("FILE 1", table.getFileNames().get(0));
			assertEquals("FILE 2", table.getFileNames().get(1));
			assertEquals("tabledef", parser.getRuleName());
		}

		line = "table < TABLE > const counter persist { \n  \n   HOST1\n\n HOST2 \n\n}" ;

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			table = parser.getPfTable();
			hosts = table.getHosts();
			assertEquals("TABLE", table.getName());
			assertEquals("const", table.getOptions().get(0));
			assertEquals("counter", table.getOptions().get(1));
			assertEquals("persist", table.getOptions().get(2));
			assertEquals("HOST1", hosts.get(0).getFirstAddress());
			assertEquals("HOST2", hosts.get(1).getFirstAddress());
			assertEquals("tabledef", parser.getRuleName());
		}

	}

	/**
	 * test of rule PfLoadRule
	 */
	public void testPfLoadRulef() {
		System.out.println("PfLoadRule");
		String line;

		line = "load anchor ANCHOR from FILENAME";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("ANCHOR", parser.getName());
			assertEquals("FILENAME", parser.getValue());
			assertEquals("loadrule", parser.getRuleName());
		}
	}

	/**
	 * test of rule PfAnchorRule
	 */
	public void testPfAnchorRule() {
		System.out.println("PfAnchrorRule");
		String line;
		RuleTemplate rule;
		Xhost host;
		List<Xhost> hosts;
		PoolOptsTemplate poolOpt;
		RouteOptsTemplate routeOpt;
		AnchorTemplate anchor;

		line = "anchor ANCHORNAME out quick on { ! IFACE0   , IFACE1 }";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			anchor = parser.getPfAnchor();
			assertEquals("ANCHORNAME", anchor.getName());
			rule = anchor.getRule();
			assertEquals(null, rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("!IFACE0", rule.getIfList().get(0));
			assertEquals("IFACE1", rule.getIfList().get(1));
			assertEquals("anchorrule", parser.getRuleName());
		}

		line = "anchor out quick on { ! IFACE0   , IFACE1 } {";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			anchor = parser.getPfAnchor();
			assertTrue(anchor.isInlined());
			rule = anchor.getRule();
			assertEquals(null, rule.getAction());
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("!IFACE0", rule.getIfList().get(0));
			assertEquals("IFACE1", rule.getIfList().get(1));
			assertEquals("anchorrule", parser.getRuleName());
		}

		line = "anchor out quick inet";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfAnchor().getRule();
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			assertEquals("inet", rule.getAf());
			assertEquals("anchorrule", parser.getRuleName());
		}

		line = "anchor in inet6 proto PROTO from HOST";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			anchor = parser.getPfAnchor();
			rule = anchor.getRule();
			assertEquals("in", rule.getDir());
			assertFalse(rule.isQuick());
			assertEquals("inet6", rule.getAf());
			assertEquals("PROTO", rule.getProtoList().get(0));
			hosts = rule.getSourceHostList();
			host = hosts.get(0);
			assertEquals("HOST", host.getFirstAddress());
			assertEquals("anchorrule", parser.getRuleName());
		}

		line = "anchor out quick reply-to " +
				"{ HOST1, HOST2/MASK2, <TABLE>, (DYNADDR)/24, (IFNAME HOST)} " +
				"round-robin     static-port      sticky-address" ;

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			anchor = parser.getPfAnchor();
			rule = anchor.getRule();
			assertEquals("out", rule.getDir());
			assertTrue(rule.isQuick());
			routeOpt = rule.getFilterOpts().getRouteOpts();
			assertTrue(routeOpt.getRt() == PfConst.PF_REPLYTO);
			hosts = routeOpt.getHosts();
			host = hosts.get(0);
			assertEquals("HOST1", host.getFirstAddress());
			host = hosts.get(1);
			assertEquals("HOST2/MASK2", host.getFirstAddress());
			host = hosts.get(2);
			assertEquals("TABLE", host.getTable());
			host = hosts.get(3);
			assertEquals("DYNADDR", host.getDynaddr());
			assertEquals("24", host.getDynaddrMask());
			host = hosts.get(4);
			assertEquals("IFNAME", host.getIfName());
			assertEquals("HOST", host.getFirstAddress());
			poolOpt = routeOpt.getPoolOpts();
			assertTrue(poolOpt.getType() == PfConst.PF_POOL_ROUNDROBIN);
			assertTrue(poolOpt.isStaticPort());
			assertTrue(poolOpt.getOpts() == PfConst.PF_POOL_STICKYADDR);
			assertEquals("anchorrule", parser.getRuleName());
		}

	}

	/**
	 * test of rule PfGenericRule
	 */
	public void testPfGenericRule() {
		System.out.println("PfGenericRule");
		ReportingParseRunner parseRunner =
				new ReportingParseRunner(parser.PfGenericRule());
		String line;

		line = "abcd { \n item , item \n} abcd\nnaaaa";
		
		result = parseRunner.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("abcd { \n item , item \n} abcd\n", parser.getMatchedText());
		}

		line = "{ \n < > = \n}\naaaa";

		result = parseRunner.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("{ \n < > = \n}\n", parser.getMatchedText());
		}

		line = "{ \n < > = }";

		result = parseRunner.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("{ \n < > = }", parser.getMatchedText());
		}

		line = "{ < > = }";

		result = parseRunner.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("{ < > = }", parser.getMatchedText());
		}

		line = "{ }";

		result = parseRunner.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("{ }", parser.getMatchedText());
		}

		line = "abcd +-/ < > = \"ABCD\nEFGH\"\n";

		result = parseRunner.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("abcd +-/ < > = \"ABCD\nEFGH\"\n", parser.getMatchedText());
		}

		line = "abcd ABCD \n";

		result = parseRunner.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("abcd ABCD \n", parser.getMatchedText());
		}

		line = "pass\nPASS";

		result = parseRunner.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("pass\n", parser.getMatchedText());
		}

		line = "pass\n";

		result = parseRunner.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("pass\n", parser.getMatchedText());
		}

		line = "pass";

		result = parseRunner.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("pass", parser.getMatchedText());
		}

		line = "\n";

		result = parseRunner.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("\n", parser.getMatchedText());
		}

	}

	/**
	 * test of rule PfOptionSetSkip
	 */
	public void testPfOptionSetSkip() {
		System.out.println("PfOptionSetSkip");
		String line;
		RuleTemplate rule;

		line = "set skip on IFACE";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("IFACE", rule.getIfList().get(0));
			assertEquals("option set skip", parser.getRuleName());
		}

		line = "set skip on { IFACE0 IFACE1 }";

		
		result = parseRunParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			rule = parser.getPfRule();
			assertEquals("IFACE0", rule.getIfList().get(0));
			assertEquals("IFACE1", rule.getIfList().get(1));
			assertEquals("option set skip", parser.getRuleName());
		}

	}

}
