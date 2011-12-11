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

package fr.univrennes1.cri.jtacl.equipments.cisco.router;

import fr.univrennes1.cri.jtacl.lib.ip.IPversion;
import junit.framework.TestCase;
import org.parboiled.Parboiled;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

/**
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class IOSParserTest extends TestCase {

	IOSParser parser = Parboiled.createParser(IOSParser.class);
	ParsingResult<?> result;
	ReportingParseRunner parseRunerParse =
		new ReportingParseRunner(parser.Parse());

    public IOSParserTest(String testName) {
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
	 * Test of stripComment method, of class IOSParser.
	 */
	public void testStripComment() {
		System.out.println("stripComment");
		String str = "";
		String expResult = "";
		String res = parser.stripComment(str);
		assertEquals(expResult, res);

		str = "!";
		expResult = "";
		res = parser.stripComment(str);
		assertEquals(expResult, res);

		str = "test ! coment ! comment";
		expResult = "test ";
		res = parser.stripComment(str);
		assertEquals(expResult, res);

		str = "test # coment ! comment";
		expResult = "test ";
		res = parser.stripComment(str);
		assertEquals(expResult, res);

		str = "test ! coment # comment";
		expResult = "test ";
		res = parser.stripComment(str);
		assertEquals(expResult, res);
	}

	/**
	 * Test of Interface method, of class IOSParser.
	 */
	public void testInterface() {
		System.out.println("Interface");
		String line = "interface foo bar";
		 result = new ReportingParseRunner(parser.Interface()).run(line);
		 assertTrue(result.matched);
		if (result.matched) {
			assertEquals("foo bar", parser.getName());
			assertEquals("interface", parser.getRuleName());
		}
	}

	/**
	 * Test of ExitInterface method, of class IOSParser.
	 */
	public void testExitInterface() {
		System.out.println("ExitInterface");
		String [] dt = {
			"interface foo bar",
			"router",
			"ip access-list foo bar",
			"boot",
			"ftp",
			"dns",
			"clock",
			"passwd",
			"access-list"
		};
		for (String s: dt) {
			 result = new ReportingParseRunner(parser.ExitInterface()).run(s);
			 System.out.println("test: " + s);
			 assertTrue(result.matched);
		}
	}

	/**
	 * Test of IfDescription method, of class IOSParser.
	 */
	public void testIfDescription() {
		System.out.println("IfDescription");
		String line = "description an interface";
		result = new ReportingParseRunner(parser.InInterface()).run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("an interface", parser.getDescription());
			assertEquals("description", parser.getRuleName());
		}
	}

	/**
	 * Test of IfShutdown method, of class IOSParser.
	 */
	public void testIfShutdown() {
		System.out.println("IfShutdown");
		String line = "shutdown";
		result = new ReportingParseRunner(parser.InInterface()).run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("shutdown", parser.getRuleName());
		}
	}

	/**
	 * Test of IfAddress, of class IOSParser.
	 */
	public void testIfAddress() {
		System.out.println("IfAddress");
		String line = "ip  address    IPADDRESS    NETMASK   secondary or something else";
		result = new ReportingParseRunner(parser.InInterface()).run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("IPADDRESS", parser.getIpAddress());
			assertEquals("NETMASK", parser.getIpNetmask());
			assertEquals("ip address", parser.getRuleName());
		}
	}

	/**
	 * Test of IfIpv6Address, of class IOSParser.
	 */
	public void testIfIpv6Address() {
		System.out.println("IfIpv6Address");
		String line = "ipv6     address      IPADDRESS";
		result = new ReportingParseRunner(parser.InInterface()).run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("IPADDRESS", parser.getIpAddress());
			assertEquals("ipv6 address", parser.getRuleName());
		}
	}

	/**
	 * Test of IpRoute method, of class IOSParser.
	 */
	public void testIpRoute() {
		System.out.println("IpRoute");
		String line = "ip route    IPADDRESS   NETMASK    NEXTHOP";
	
		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("IPADDRESS", parser.getIpAddress());
			assertEquals("NETMASK", parser.getIpNetmask());
			assertEquals("NEXTHOP", parser.getNexthop());
			assertEquals("ip route", parser.getRuleName());
		}
	}

	/**
	 * Test of Ipv6Route method, of class IOSParser.
	 */
	public void testIpv6Route() {
		System.out.println("Ipv6Route");
		String line = "ipv6 route    IPADDRESS     NEXTHOP";
		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("IPADDRESS", parser.getIpAddress());
			assertEquals("NEXTHOP", parser.getNexthop());
			assertEquals("ipv6 route", parser.getRuleName());
		}
	}

	/**
	 * Test of AccessListStandard, of class IOSParser.
	 */
	public void testAccessListStandard() {
		System.out.println("AccessListStandard");
		String line;
		AclTemplate acl;
		AceTemplate ace;

		line = "access-list 1 permit any log";

		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			acl = parser.getAclTemplate();
			ace = parser.getAceTemplate();
			assertEquals(1, acl.getNumber().intValue());
			assertEquals("permit", ace.getAction());
			assertEquals(null, ace.getSrcIp());
			assertEquals(null, ace.getSrcIpMask());
			assertEquals("access-list", parser.getRuleName());
		}

		line = "access-list 99 deny any";

		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			acl = parser.getAclTemplate();
			ace = parser.getAceTemplate();
			assertEquals(99, acl.getNumber().intValue());
			assertEquals("deny", ace.getAction());
			assertEquals(null, ace.getSrcIp());
			assertEquals(null, ace.getSrcIpMask());
			assertEquals("access-list", parser.getRuleName());
		}

		line = "access-list 1399 deny 1.2.3.4 log";

		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			acl = parser.getAclTemplate();
			ace = parser.getAceTemplate();
			assertEquals(1399, acl.getNumber().intValue());
			assertEquals("deny", ace.getAction());
			assertEquals("1.2.3.4", ace.getSrcIp());
			assertEquals(null, ace.getSrcIpMask());
			assertEquals("access-list", parser.getRuleName());
		}

		line = "access-list 1999 deny 1.2.3.4 5.6.7.8 log";

		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			acl = parser.getAclTemplate();
			ace = parser.getAceTemplate();
			assertEquals(1999, acl.getNumber().intValue());
			assertEquals("deny", ace.getAction());
			assertEquals("1.2.3.4", ace.getSrcIp());
			assertEquals("5.6.7.8", ace.getSrcIpMask());
			assertEquals("access-list", parser.getRuleName());
		}

		line = "access-list 15 deny 1.2.3.4 5.6.7.8";

		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			acl = parser.getAclTemplate();
			ace = parser.getAceTemplate();
			assertEquals(15, acl.getNumber().intValue());
			assertEquals("deny", ace.getAction());
			assertEquals("1.2.3.4", ace.getSrcIp());
			assertEquals("5.6.7.8", ace.getSrcIpMask());
			assertEquals("access-list", parser.getRuleName());
		}


		line = "access-list 90 permit host 127.0.0.1";

		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			acl = parser.getAclTemplate();
			ace = parser.getAceTemplate();
			assertEquals(90, acl.getNumber().intValue());
			assertEquals("permit", ace.getAction());
			assertEquals("127.0.0.1", ace.getSrcIp());
			assertEquals(null, ace.getSrcIpMask());
			assertEquals("access-list", parser.getRuleName());
		}

		AccessList context = new AccessList();
		context.setAclType(AclType.IPSTD);
		context.setIpVersion(IPversion.IPV4);
		line = "deny 1.2.3.4 5.6.7.8";

		parser.setAclContext(context);
		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			acl = parser.getAclTemplate();
			ace = parser.getAceTemplate();
			assertEquals(null, acl);
			assertEquals("deny", ace.getAction());
			assertEquals("1.2.3.4", ace.getSrcIp());
			assertEquals("5.6.7.8", ace.getSrcIpMask());
			assertEquals("ace standard", parser.getRuleName());
		}

	}

	/**
	 * Test of AccessListExtended, of class IOSParser.
	 */
	public void testAccessListExtended() {
		System.out.println("AccessListExtended");
		String line;
		AccessList context = new AccessList();
		AclTemplate acl;
		AceTemplate ace;

		line = "permit udp any any gt PORT";
		context.setAclType(AclType.IPEXT);
		context.setIpVersion(IPversion.IPV4);

		parser.setAclContext(context);
		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			acl = parser.getAclTemplate();
			ace = parser.getAceTemplate();
			assertEquals(null, acl);
			assertEquals("permit", ace.getAction());
			assertEquals("udp", ace.getProtocol());
			assertEquals("any", ace.getSrcIp());
			assertEquals(null, ace.getSrcIpMask());
			assertEquals("any", ace.getDstIp());
			assertEquals(null, ace.getDstIpMask());
			assertEquals("gt", ace.getDstPortOperator());
			assertEquals("PORT", ace.getDstFirstPort());
			assertEquals("ace extended", parser.getRuleName());
		}

		line = "permit tcp SRCIP SRCMASK DESTIP DESTMASK eq PORT";
		context.setAclType(AclType.IPEXT);
		context.setIpVersion(IPversion.IPV4);

		parser.setAclContext(context);
		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			acl = parser.getAclTemplate();
			ace = parser.getAceTemplate();
			assertEquals(null, acl);
			assertEquals("permit", ace.getAction());
			assertEquals("tcp", ace.getProtocol());
			assertEquals("SRCIP", ace.getSrcIp());
			assertEquals("SRCMASK", ace.getSrcIpMask());
			assertEquals("DESTIP", ace.getDstIp());
			assertEquals("DESTMASK", ace.getDstIpMask());
			assertEquals("eq", ace.getDstPortOperator());
			assertEquals("PORT", ace.getDstFirstPort());
			assertEquals("ace extended", parser.getRuleName());
		}

		line = "permit tcp SRCIP SRCMASK DESTIP DESTMASK eq PORT established";
		context.setAclType(AclType.IPEXT);
		context.setIpVersion(IPversion.IPV4);

		parser.setAclContext(context);
		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			acl = parser.getAclTemplate();
			ace = parser.getAceTemplate();
			assertEquals(null, acl);
			assertEquals("permit", ace.getAction());
			assertEquals("tcp", ace.getProtocol());
			assertEquals("SRCIP", ace.getSrcIp());
			assertEquals("SRCMASK", ace.getSrcIpMask());
			assertEquals("DESTIP", ace.getDstIp());
			assertEquals("DESTMASK", ace.getDstIpMask());
			assertEquals("eq", ace.getDstPortOperator());
			assertEquals("PORT", ace.getDstFirstPort());
			assertEquals("match-any", ace.getTcpKeyword());
			assertEquals("+ack", ace.getTcpFlags().get(0));
			assertEquals("+rst", ace.getTcpFlags().get(1));
			assertEquals("ace extended", parser.getRuleName());
		}

		
		line = "permit tcp any eq SRCPORT any gt DESTPORT";
		context.setAclType(AclType.IPEXT);
		context.setIpVersion(IPversion.IPV4);

		parser.setAclContext(context);
		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			acl = parser.getAclTemplate();
			ace = parser.getAceTemplate();
			assertEquals(null, acl);
			assertEquals("permit", ace.getAction());
			assertEquals("tcp", ace.getProtocol());
			assertEquals("any", ace.getSrcIp());
			assertEquals(null, ace.getSrcIpMask());
			assertEquals("eq", ace.getSrcPortOperator());
			assertEquals("SRCPORT", ace.getSrcFirstPort());
			assertEquals("any", ace.getDstIp());
			assertEquals(null, ace.getDstIpMask());
			assertEquals("gt", ace.getDstPortOperator());
			assertEquals("DESTPORT", ace.getDstFirstPort());
			assertEquals("ace extended", parser.getRuleName());
		}

		line = "permit tcp any eq SRCPORT any gt DESTPORT established";
		context.setAclType(AclType.IPEXT);
		context.setIpVersion(IPversion.IPV4);

		parser.setAclContext(context);
		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			acl = parser.getAclTemplate();
			ace = parser.getAceTemplate();
			assertEquals(null, acl);
			assertEquals("permit", ace.getAction());
			assertEquals("tcp", ace.getProtocol());
			assertEquals("any", ace.getSrcIp());
			assertEquals(null, ace.getSrcIpMask());
			assertEquals("eq", ace.getSrcPortOperator());
			assertEquals("SRCPORT", ace.getSrcFirstPort());
			assertEquals("any", ace.getDstIp());
			assertEquals(null, ace.getDstIpMask());
			assertEquals("gt", ace.getDstPortOperator());
			assertEquals("DESTPORT", ace.getDstFirstPort());
			assertEquals("match-any", ace.getTcpKeyword());
			assertEquals("+ack", ace.getTcpFlags().get(0));
			assertEquals("+rst", ace.getTcpFlags().get(1));
			assertEquals("ace extended", parser.getRuleName());
		}


		line = "access-list 100 permit tcp any any";

		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			acl = parser.getAclTemplate();
			ace = parser.getAceTemplate();
			assertEquals(100, acl.getNumber().intValue());
			assertEquals("permit", ace.getAction());
			assertEquals("tcp", ace.getProtocol());
			assertEquals("any", ace.getSrcIp());
			assertEquals(null, ace.getSrcIpMask());
			assertEquals("any", ace.getDstIp());
			assertEquals(null, ace.getDstIpMask());
			assertEquals("access-list", parser.getRuleName());
		}

		line = "access-list 100 permit tcp any any established";

		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			acl = parser.getAclTemplate();
			ace = parser.getAceTemplate();
			assertEquals(100, acl.getNumber().intValue());
			assertEquals("permit", ace.getAction());
			assertEquals("tcp", ace.getProtocol());
			assertEquals("any", ace.getSrcIp());
			assertEquals(null, ace.getSrcIpMask());
			assertEquals("any", ace.getDstIp());
			assertEquals(null, ace.getDstIpMask());
			assertEquals("match-any", ace.getTcpKeyword());
			assertEquals("+ack", ace.getTcpFlags().get(0));
			assertEquals("+rst", ace.getTcpFlags().get(1));
			assertEquals("access-list", parser.getRuleName());
		}

		line = "access-list 100 permit tcp any any ack rst psh fin syn urg";

		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			acl = parser.getAclTemplate();
			ace = parser.getAceTemplate();
			assertEquals(100, acl.getNumber().intValue());
			assertEquals("permit", ace.getAction());
			assertEquals("tcp", ace.getProtocol());
			assertEquals("any", ace.getSrcIp());
			assertEquals(null, ace.getSrcIpMask());
			assertEquals("any", ace.getDstIp());
			assertEquals(null, ace.getDstIpMask());
			assertEquals("match-any", ace.getTcpKeyword());
			assertEquals("+ack", ace.getTcpFlags().get(0));
			assertEquals("+rst", ace.getTcpFlags().get(1));
			assertEquals("+psh", ace.getTcpFlags().get(2));
			assertEquals("+fin", ace.getTcpFlags().get(3));
			assertEquals("+syn", ace.getTcpFlags().get(4));
			assertEquals("+urg", ace.getTcpFlags().get(5));
			assertEquals("access-list", parser.getRuleName());
		}

		line = "access-list 100 permit tcp any any match-any +ack +rst +psh +fin +syn +urg";

		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			acl = parser.getAclTemplate();
			ace = parser.getAceTemplate();
			assertEquals(100, acl.getNumber().intValue());
			assertEquals("permit", ace.getAction());
			assertEquals("tcp", ace.getProtocol());
			assertEquals("any", ace.getSrcIp());
			assertEquals(null, ace.getSrcIpMask());
			assertEquals("any", ace.getDstIp());
			assertEquals(null, ace.getDstIpMask());
			assertEquals("match-any", ace.getTcpKeyword());
			assertEquals("+ack", ace.getTcpFlags().get(0));
			assertEquals("+rst", ace.getTcpFlags().get(1));
			assertEquals("+psh", ace.getTcpFlags().get(2));
			assertEquals("+fin", ace.getTcpFlags().get(3));
			assertEquals("+syn", ace.getTcpFlags().get(4));
			assertEquals("+urg", ace.getTcpFlags().get(5));
			assertEquals("access-list", parser.getRuleName());
		}

		line = "access-list 100 permit tcp any any match-all +ack +rst +psh +fin +syn +urg";

		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			acl = parser.getAclTemplate();
			ace = parser.getAceTemplate();
			assertEquals(100, acl.getNumber().intValue());
			assertEquals("permit", ace.getAction());
			assertEquals("tcp", ace.getProtocol());
			assertEquals("any", ace.getSrcIp());
			assertEquals(null, ace.getSrcIpMask());
			assertEquals("any", ace.getDstIp());
			assertEquals(null, ace.getDstIpMask());
			assertEquals("match-all", ace.getTcpKeyword());
			assertEquals("+ack", ace.getTcpFlags().get(0));
			assertEquals("+rst", ace.getTcpFlags().get(1));
			assertEquals("+psh", ace.getTcpFlags().get(2));
			assertEquals("+fin", ace.getTcpFlags().get(3));
			assertEquals("+syn", ace.getTcpFlags().get(4));
			assertEquals("+urg", ace.getTcpFlags().get(5));
			assertEquals("access-list", parser.getRuleName());
		}

		line = "access-list 100 permit tcp any any match-all -ack -rst -psh -fin -syn -urg";

		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			acl = parser.getAclTemplate();
			ace = parser.getAceTemplate();
			assertEquals(100, acl.getNumber().intValue());
			assertEquals("permit", ace.getAction());
			assertEquals("tcp", ace.getProtocol());
			assertEquals("any", ace.getSrcIp());
			assertEquals(null, ace.getSrcIpMask());
			assertEquals("any", ace.getDstIp());
			assertEquals(null, ace.getDstIpMask());
			assertEquals("match-all", ace.getTcpKeyword());
			assertEquals("-ack", ace.getTcpFlags().get(0));
			assertEquals("-rst", ace.getTcpFlags().get(1));
			assertEquals("-psh", ace.getTcpFlags().get(2));
			assertEquals("-fin", ace.getTcpFlags().get(3));
			assertEquals("-syn", ace.getTcpFlags().get(4));
			assertEquals("-urg", ace.getTcpFlags().get(5));
			assertEquals("access-list", parser.getRuleName());
		}
	}

	/**
	 * Test of IpAccessListNamed, of class IOSParser.
	 */
	public void testIpAccessListNamed() {
		System.out.println("IpAccessListNamed");
		String line;
		AclTemplate acl;
		AceTemplate ace;

		line = "ip access-list standard NAME";

		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			acl = parser.getAclTemplate();
			ace = parser.getAceTemplate();
			assertEquals(null, acl.getNumber());
			assertTrue(acl.getAclType().equals(AclType.IPSTD));
			assertEquals("NAME", acl.getName());
			assertEquals("ip access-list named", parser.getRuleName());
		}

		line = "ip access-list extended NAME";

		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			acl = parser.getAclTemplate();
			ace = parser.getAceTemplate();
			assertEquals(null, acl.getNumber());
			assertTrue(acl.getAclType().equals(AclType.IPEXT));
			assertEquals("NAME", acl.getName());
			assertEquals("ip access-list named", parser.getRuleName());
		}

	}

	/**
	 * Test of InAclContext, of class IOSParser.
	 */
	public void testInAclContext() {
		System.out.println("InAclContext");
		String line;
		AceTemplate ace;
		ReportingParseRunner parseRunner =
				new ReportingParseRunner(parser.InAclContext());

		line = "permit   ";
		result = parseRunner.run(line);
		assertTrue(result.matched);

		line = "deny   ";
		result = parseRunner.run(line);
		assertTrue(result.matched);

		line = "no   permit   ";
		result = parseRunner.run(line);
		assertTrue(result.matched);
		
		line = "no   deny   ";
		result = parseRunner.run(line);
		assertTrue(result.matched);

		line = "remark   ";
		result = parseRunner.run(line);
		assertTrue(result.matched);

		line = "no   remark   ";
		result = parseRunner.run(line);
		assertTrue(result.matched);

		line = "evaluate  ";
		result = parseRunner.run(line);
		assertTrue(result.matched);

		line = "no evaluate  ";
		result = parseRunner.run(line);
		assertTrue(result.matched);
	}

}
