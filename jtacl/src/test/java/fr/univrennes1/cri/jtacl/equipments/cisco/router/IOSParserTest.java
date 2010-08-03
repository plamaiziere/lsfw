/*
 * Copyright (c) 2010, Université de Rennes 1
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
import org.parboiled.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

/**
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class IOSParserTest extends TestCase {

	IOSParser parser = Parboiled.createParser(IOSParser.class);
	ParsingResult<?> result;

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
		parser.clear();
		 result = ReportingParseRunner.run(parser.Interface(), line);
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
			parser.clear();
			 result = ReportingParseRunner.run(parser.ExitInterface(), s);
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
		parser.clear();
		result = ReportingParseRunner.run(parser.InInterface(), line);
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
		parser.clear();
		result = ReportingParseRunner.run(parser.InInterface(), line);
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
		parser.clear();
		result = ReportingParseRunner.run(parser.InInterface(), line);
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
		parser.clear();
		result = ReportingParseRunner.run(parser.InInterface(), line);
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
		parser.clear();
		result = ReportingParseRunner.run(parser.Parse(), line);
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
		parser.clear();
		result = ReportingParseRunner.run(parser.Parse(), line);
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

		parser.clear();
		result = ReportingParseRunner.run(parser.Parse(), line);
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

		parser.clear();
		result = ReportingParseRunner.run(parser.Parse(), line);
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

		parser.clear();
		result = ReportingParseRunner.run(parser.Parse(), line);
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

		parser.clear();
		result = ReportingParseRunner.run(parser.Parse(), line);
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

		parser.clear();
		result = ReportingParseRunner.run(parser.Parse(), line);
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

		parser.clear();
		result = ReportingParseRunner.run(parser.Parse(), line);
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

		parser.clear();
		parser.setAclContext(context);
		result = ReportingParseRunner.run(parser.Parse(), line);
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

		parser.clear();
		parser.setAclContext(context);
		result = ReportingParseRunner.run(parser.Parse(), line);
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

		parser.clear();
		parser.setAclContext(context);
		result = ReportingParseRunner.run(parser.Parse(), line);
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
		
		line = "permit tcp any eq SRCPORT any gt DESTPORT";
		context.setAclType(AclType.IPEXT);
		context.setIpVersion(IPversion.IPV4);

		parser.clear();
		parser.setAclContext(context);
		result = ReportingParseRunner.run(parser.Parse(), line);
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

		line = "access-list 100 permit tcp any any";

		parser.clear();
		result = ReportingParseRunner.run(parser.Parse(), line);
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

		parser.clear();
		result = ReportingParseRunner.run(parser.Parse(), line);
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

		parser.clear();
		result = ReportingParseRunner.run(parser.Parse(), line);
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

	
}
