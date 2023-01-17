/*
 * Copyright (c) 2021, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */
package fr.univrennes1.cri.jtacl.equipments.proxmox;

import junit.framework.TestCase;
import org.parboiled.Parboiled;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

import java.util.List;

public class PxVeParserTest extends TestCase {
	PxVeParser parser = Parboiled.createParser(PxVeParser.class);
	// ReportingParseRunner parseRunParse = new ReportingParseRunner(parser.Parse());
	ParsingResult<?> result;

    public PxVeParserTest(String testName) {
        super(testName);
    }

	 /**
	 * Test of Source IP
	 */
	public void testSourceIp() {
		System.out.println("testSourceIpRule");
		String line;
		List<String> ipSpec;

		line = "--source   127.0.0.1/24,::0,alias,+ipset ";
		parser.newRuleTpl();
		result = new ReportingParseRunner(parser.RSourceIp()).run(line);
		assertTrue(result.matched);
		if (result.matched) {
			ipSpec = parser.getRuleTpl().getSourceIpSpec();
			assertEquals(ipSpec.get(0), "127.0.0.1/24");
			assertEquals(ipSpec.get(1), "::0");
			assertEquals(ipSpec.get(2), "alias");
			assertEquals(ipSpec.get(3), "+ipset");
		}
	}

	 /**
	 * Test of Dest IP
	 */
	public void testDestIp() {
		System.out.println("testDestIpRule");
		String line;
		List<String> ipSpec;

		line = "--dest   alias,  127.0.0.1/24,::0,+ipset ";
		parser.newRuleTpl();
		result = new ReportingParseRunner(parser.RDestIp()).run(line);
		assertTrue(result.matched);
		if (result.matched) {
			ipSpec = parser.getRuleTpl().getDestIpSpec();
			assertEquals(ipSpec.get(0), "alias");
			assertEquals(ipSpec.get(1), "127.0.0.1/24");
			assertEquals(ipSpec.get(2), "::0");
			assertEquals(ipSpec.get(3), "+ipset");
		}
	}

	/**
	 * Test of Source Port
	 */
	public void testSourcePort() {
		System.out.println("testSourcePortRule");
		String line;
		List<String> portSpec;

		line = "--sport   80, 80:85";
		parser.newRuleTpl();
		result = new ReportingParseRunner(parser.RSourcePort()).run(line);
		assertTrue(result.matched);
		if (result.matched) {
			portSpec = parser.ruleTpl.getSourcePortSpec();
			assertEquals(portSpec.get(0), "80");
			assertEquals(portSpec.get(1), "80:85");
		}
	}

	/**
	 * Test of Dest Port
	 */
	public void testDestPort() {
		System.out.println("testDestPortRule");
		String line;
		List<String> portSpec;

		line = "--dport   80, 80:85";
		parser.newRuleTpl();
		result = new ReportingParseRunner(parser.RDestPort()).run(line);
		assertTrue(result.matched);
		if (result.matched) {
			portSpec = parser.ruleTpl.getDestPortSpec();;
			assertEquals(portSpec.get(0), "80");
			assertEquals(portSpec.get(1), "80:85");
		}
	}

	/**
	 * Test of icmp-type
	 */
	public void testIcmpType() {
		System.out.println("testIcmpTypeRule");
		String line;
		String icmpType;

		line = "--icmp-type type";
		parser.newRuleTpl();
		result = new ReportingParseRunner(parser.RIcmpType()).run(line);
		assertTrue(result.matched);
		if (result.matched) {
			icmpType = parser.getRuleTpl().getIcmpType();
			assertEquals(icmpType, "type");
		}
	}

	/**
	 * Test of proto
	 */
	public void testProto() {
		System.out.println("testProtoRule");
		String line;
		String proto;

		line = "--proto udp/tcp";
		parser.newRuleTpl();
		result = new ReportingParseRunner(parser.RProto()).run(line);
		assertTrue(result.matched);
		if (result.matched) {
			proto = parser.getRuleTpl().getProto();
			assertEquals(proto, "udp/tcp");
		}
	}

	/**
	 * Test of iface
	 */
	public void testIface() {
		System.out.println("testIfaceRule");
		String line;
		String iface;

		line = "--iface en0";
		parser.newRuleTpl();
		result = new ReportingParseRunner(parser.RIface()).run(line);
		assertTrue(result.matched);
		if (result.matched) {
			iface = parser.getRuleTpl().getIface();
			assertEquals(iface, "en0");
		}
	}

	/**
	 * Test of rule
	 */
	public void testRule() {
		System.out.println("testRuleRule");
		String line;
		PxRuleTemplate ruleTpl;

		line = "IN SSH(ACCEPT)";
		result = new ReportingParseRunner(parser.RveRule()).run(line);
		assertTrue(result.matched);
		if (result.matched) {
			ruleTpl = parser.getRuleTpl();
			assertEquals(ruleTpl.getDirection(), "IN");
			assertEquals(ruleTpl.getMacro(), "SSH");
			assertEquals(ruleTpl.getAction(), "ACCEPT");
		}

		line = "OUT     SSH(DROP)     ";
		result = new ReportingParseRunner(parser.RveRule()).run(line);
		assertTrue(result.matched);
		if (result.matched) {
			ruleTpl = parser.getRuleTpl();
			assertEquals(ruleTpl.getDirection(), "OUT");
			assertEquals(ruleTpl.getMacro(), "SSH");
			assertEquals(ruleTpl.getAction(), "DROP");
		}

		line = "OUT     DROP     ";
		result = new ReportingParseRunner(parser.RveRule()).run(line);
		assertTrue(result.matched);
		if (result.matched) {
			ruleTpl = parser.getRuleTpl();
			assertEquals(ruleTpl.getDirection(), "OUT");
			assertEquals(ruleTpl.getMacro(), null);
			assertEquals(ruleTpl.getAction(), "DROP");
		}

		line = "|OUT   REJECT";
		result = new ReportingParseRunner(parser.RveRule()).run(line);
		assertTrue(result.matched);
		if (result.matched) {
			ruleTpl = parser.getRuleTpl();
			assertTrue(ruleTpl.isDisabled());
			assertEquals(ruleTpl.getDirection(), "OUT");
			assertEquals(ruleTpl.getMacro(), null);
			assertEquals(ruleTpl.getAction(), "REJECT");
		}

		line = "|  OUT   REJECT";
		result = new ReportingParseRunner(parser.RveRule()).run(line);
		assertTrue(result.matched);
		if (result.matched) {
			ruleTpl = parser.getRuleTpl();
			assertTrue(ruleTpl.isDisabled());
			assertEquals(ruleTpl.getDirection(), "OUT");
			assertEquals(ruleTpl.getMacro(), null);
			assertEquals(ruleTpl.getAction(), "REJECT");
		}

		line = "OUT     DROP   --source 129.20/16    --dest     alias    --icmp-type    type    --proto    icmp --dport    80:80 --sport   81:81 --log   alert";
		result = new ReportingParseRunner(parser.RveRule()).run(line);
		assertTrue(result.matched);
		if (result.matched) {
			ruleTpl = parser.getRuleTpl();
			assertEquals(ruleTpl.getDirection(), "OUT");
			assertEquals(ruleTpl.getMacro(), null);
			assertEquals(ruleTpl.getAction(), "DROP");
			assertEquals(ruleTpl.getSourceIpSpec().get(0), "129.20/16");
			assertEquals(ruleTpl.getDestIpSpec().get(0), "alias");
			assertEquals(ruleTpl.getIcmpType(), "type");
			assertEquals(ruleTpl.getProto(), "icmp");
			assertEquals(ruleTpl.getDestPortSpec().get(0), "80:80");
			assertEquals(ruleTpl.getSourcePortSpec().get(0), "81:81");
		}

		line = "OUT ACCEPT   --source 129.20/16    --dest     alias    --icmp-type    type    --proto    icmp --dport    80:80 --sport   81:81 --log   alert";
		result = new ReportingParseRunner(parser.RveRule()).run(line);
		assertTrue(result.matched);
		if (result.matched) {
			ruleTpl = parser.getRuleTpl();
			assertEquals(ruleTpl.getDirection(), "OUT");
			assertEquals(ruleTpl.getMacro(), null);
			assertEquals(ruleTpl.getAction(), "ACCEPT");
			assertEquals(ruleTpl.getSourceIpSpec().get(0), "129.20/16");
			assertEquals(ruleTpl.getDestIpSpec().get(0), "alias");
			assertEquals(ruleTpl.getIcmpType(), "type");
			assertEquals(ruleTpl.getProto(), "icmp");
			assertEquals(ruleTpl.getDestPortSpec().get(0), "80:80");
			assertEquals(ruleTpl.getSourcePortSpec().get(0), "81:81");
		}

		line = "OUT ACCEPT -p icmp -log nolog";
		result = new ReportingParseRunner(parser.RveRule()).run(line);
		assertTrue(result.matched);
		if (result.matched) {
			ruleTpl = parser.getRuleTpl();
			assertEquals(ruleTpl.getDirection(), "OUT");
			assertEquals(ruleTpl.getMacro(), null);
			assertEquals(ruleTpl.getAction(), "ACCEPT");
			assertEquals(ruleTpl.getProto(), "icmp");
		}



		line = "  GROUp     groupName    ";
		result = new ReportingParseRunner(parser.RveRule()).run(line);
		assertTrue(result.matched);
		if (result.matched) {
			ruleTpl = parser.getRuleTpl();
			assertEquals(ruleTpl.getGroupName(), "groupName");
		}

		line = " | GROUp   groupName";
		result = new ReportingParseRunner(parser.RveRule()).run(line);
		assertTrue(result.matched);
		if (result.matched) {
			ruleTpl = parser.getRuleTpl();
			assertTrue(ruleTpl.isDisabled());
			assertEquals(ruleTpl.getGroupName(), "groupName");
		}
	}

	/**
	 * Test of section
	 */
	public void testSection() {
		System.out.println("testSectionRule");
		String line;
		PxSectionTemplate section;

		line = "   [   section  ] nnnnn nnnn";
		result = new ReportingParseRunner(parser.RSection()).run(line);
		assertFalse(result.matched);

    	line = "   [   section  ]    ";
		result = new ReportingParseRunner(parser.RSection()).run(line);
		assertTrue(result.matched);
		if (result.matched) {
			section = parser.getSectionTpl();
			assertEquals(section.getSectionName(), "section");
			assertEquals(section.getName(), null);
		}

    	line = "   [   section NAME ]    ";
		result = new ReportingParseRunner(parser.RSection()).run(line);
		assertTrue(result.matched);
		if (result.matched) {
			section = parser.getSectionTpl();
			assertEquals(section.getSectionName(), "section");
			assertEquals(section.getName(), "NAME");
		}

	}

	/**
	 * Test of alias
	 */
	public void testAlias() {
		System.out.println("testAliasRule");
		String line;
		PxAliasTemplate alias;

		line = "ident     ip   ";
		result = new ReportingParseRunner(parser.RAlias()).run(line);
		assertTrue(result.matched);
		if (result.matched) {
			alias = parser.getAliasTpl();
			assertEquals(alias.getName(), "ident");
			assertEquals(alias.getIpSpec().get(0), "ip");
		}

		// not valid / just in case
		line = "ident     ip,ip2";
		result = new ReportingParseRunner(parser.RAlias()).run(line);
		assertTrue(result.matched);
		if (result.matched) {
			alias = parser.getAliasTpl();
			assertEquals(alias.getName(), "ident");
			assertEquals(alias.getIpSpec().get(0), "ip");
			assertEquals(alias.getIpSpec().get(1), "ip2");
		}
	}
}
