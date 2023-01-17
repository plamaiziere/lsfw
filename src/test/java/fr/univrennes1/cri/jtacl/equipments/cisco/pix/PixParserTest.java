/*
 * Copyright (c) 2010, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.equipments.cisco.pix;

import fr.univrennes1.cri.jtacl.lib.ip.IPIcmp;
import fr.univrennes1.cri.jtacl.lib.ip.IPIcmp4;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import junit.framework.TestCase;
import org.parboiled.Parboiled;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

/**
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PixParserTest extends TestCase implements GroupTypeSearchable {

	PixParser parser = Parboiled.createParser(PixParser.class);
	ParsingResult<?> result;
	ReportingParseRunner parseRunerParse =
		new ReportingParseRunner(parser.Parse());
	HashMap<String, ObjectGroupType> groups =
            new HashMap<>();

    public PixParserTest(String testName) {
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
	 * Test of stripComment method, of class PixParser.
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
	 * Test of Interface method, of class PixParser.
	 */
	public void testInterface() {
		System.out.println("Interface");
		String line = "interface foo bar";
		result = new ReportingParseRunner(parser.Interface()).run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("foo bar", parser.getName());
			assertEquals(null, parser.getShutdown());
			assertEquals("interface", parser.getRuleName());
		}

		line = "interface foo bar shutdown blabla";
		result = new ReportingParseRunner(parser.Interface()).run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("foo bar", parser.getName());
			assertEquals("shutdown", parser.getShutdown());
			assertEquals("interface", parser.getRuleName());
		}
	}

	/**
	 * Test of ExitInterface method, of class PixParser.
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
	 * Test of IfAddress, of class PixParser.
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
	 * Test of IfIpv6Address, of class PixParser.
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

		line = "ipv6     address      IPADDRESS something";
		result = new ReportingParseRunner(parser.InInterface()).run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("IPADDRESS", parser.getIpAddress());
			assertEquals("ipv6 address", parser.getRuleName());
		}
	}

	/**
	 * Test of IfName method, of class PixParser.
	 */
	public void testIfName() {
		System.out.println("IfName");
		String line = "nameif       outside";
		result = new ReportingParseRunner(parser.InInterface()).run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("outside", parser.getName());
			assertEquals("nameif", parser.getRuleName());
		}
	}

	/**
	 * Test of PixName method, of class PixParser.
	 */
	public void testPixName() {
		System.out.println("PixName");
		String line = "name     IPADDRESS     NAME    A DESCRIPTION ";
		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("IPADDRESS", parser.getIpAddress());
			assertEquals("NAME", parser.getName());
			assertEquals("name", parser.getRuleName());
		}

	}

	/**
	 * Test of PixRoute method, of class PixParser.
	 */
	public void testPixRoute() {
		System.out.println("PixRoute");
		String line = "route    INTERFACE    IPADDRESS   NETMASK    NEXTHOP some option";
		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("INTERFACE", parser.getInterface());
			assertEquals("IPADDRESS", parser.getIpAddress());
			assertEquals("NETMASK", parser.getIpNetmask());
			assertEquals("NEXTHOP", parser.getNexthop());
			assertEquals("route", parser.getRuleName());
		}
	}

	/**
	 * Test of Ipv6Route method, of class PixParser.
	 */
	public void testIpv6Route() {
		System.out.println("Ipv6Route");
		String line = "ipv6 route   INTERFACE  IPADDRESS   NEXTHOP something";
		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("INTERFACE", parser.getInterface());
			assertEquals("IPADDRESS", parser.getIpAddress());
			assertEquals("NEXTHOP", parser.getNexthop());
			assertEquals("ipv6 route", parser.getRuleName());
		}
	}

	/**
	 * Test of ObjectGroupNetwork, of class PixParser.
	 */
	public void testObjectGroupNetwork() {
		System.out.println("ObjectGroupNetwork");
		String line = "object-group      network      GROUPID";
		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("GROUPID", parser.getGroupId());
			assertEquals("object-group network", parser.getRuleName());
		}
	}

	/**
	 * Test of ObjectGroupService, of class PixParser.
	 */
	public void testObjectGroupService() {
		System.out.println("ObjectGroupService");
		String line = "object-group      service      GROUPID tcp";
		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("GROUPID", parser.getGroupId());
			assertEquals("tcp", parser.getProtocol());
			assertEquals("object-group service", parser.getRuleName());
		}

		line = "object-group      service      GROUPID udp";
		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("GROUPID", parser.getGroupId());
			assertEquals("udp", parser.getProtocol());
			assertEquals("object-group service", parser.getRuleName());
		}

		line = "object-group      service      GROUPID tcp-udp";
		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("GROUPID", parser.getGroupId());
			assertEquals("tcp-udp", parser.getProtocol());
			assertEquals("object-group service", parser.getRuleName());
		}

		line = "object-group      service      GROUPID";
		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("GROUPID", parser.getGroupId());
			assertEquals(null, parser.getProtocol());
			assertEquals("object-group service", parser.getRuleName());
		}

	}

	/**
	 * Test of ObjectGroupProtocol, of class PixParser.
	 */
	public void testObjectGroupProtocol() {
		System.out.println("ObjectGroupProtocol");
		String line = "object-group      protocol      GROUPID";
		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("GROUPID", parser.getGroupId());
			assertEquals("object-group protocol", parser.getRuleName());
		}
	}

	/**
	 * Test of ObjectGroupIcmp, of class PixParser.
	 */
	public void testObjectGroupIcmp() {
		System.out.println("ObjectGroupIcmp");
		String line = "object-group      icmp-type      GROUPID";
		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("GROUPID", parser.getGroupId());
			assertEquals("object-group icmp-type", parser.getRuleName());
		}
	}

	/**
	 * Test of NetworkObject, of class PixParser.
	 */
	public void testNetworkObject() {
		System.out.println("NetworkObject");
		String line = "network-object     host       IPADDRESS";
		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("IPADDRESS", parser.getIpAddress());
			assertEquals("network-object", parser.getRuleName());
		}

		line = "network-object     IPADDRESS     NETMASK";
		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("IPADDRESS", parser.getIpAddress());
			assertEquals("NETMASK", parser.getIpNetmask());
			assertEquals("network-object", parser.getRuleName());
		}

		line = "network-object     IPADDRESS";
		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("IPADDRESS", parser.getIpAddress());
			assertEquals(null, parser.getIpNetmask());
			assertEquals("network-object", parser.getRuleName());
		}
	}

	/**
	 * Test of ProtocolObject, of class PixParser.
	 */
	public void testProtocolObject() {
		System.out.println("ProtocolObject");
		String line = "protocol-object     PROTO";
		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("PROTO", parser.getProtocol());
			assertEquals("protocol-object", parser.getRuleName());
		}
	}

	/**
	 * Test of IcmpObject, of class PixParser.
	 */
	public void tesIcmpObject() {
		System.out.println("IcmpObject");
		String line = "icmp-object     PROTO";
		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("PROTO", parser.getProtocol());
			assertEquals("icmp-object", parser.getRuleName());
		}
	}

	/**
	 * Test of GroupObject, of class PixParser.
	 */
	public void testGroupObject() {
		System.out.println("GroupObject");
		String line = "group-object     GROUPID";
		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("GROUPID", parser.getGroupId());
			assertEquals("group-object", parser.getRuleName());
		}

		line = "network-object     IPADDRESS     NETMASK";
		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("IPADDRESS", parser.getIpAddress());
			assertEquals("NETMASK", parser.getIpNetmask());
			assertEquals("network-object", parser.getRuleName());
		}

		line = "network-object     IPADDRESS";
		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("IPADDRESS", parser.getIpAddress());
			assertEquals(null, parser.getIpNetmask());
			assertEquals("network-object", parser.getRuleName());
		}
	}

	/**
	 * Test of ServiceObject, of class PixParser.
	 */
	public void testServiceObject() {
		System.out.println("service-object");
		String line = "service-object PROTO  eq   PORT";
		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("PROTO", parser.getProtocol());
			assertEquals("eq", parser.getPortOperator());
			assertEquals("PORT", parser.getFirstPort());
			assertEquals("service-object", parser.getRuleName());
		}

		line = "service-object PROTO  range   PORT1 PORT2";
		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("PROTO", parser.getProtocol());
			assertEquals("range", parser.getPortOperator());
			assertEquals("PORT1", parser.getFirstPort());
			assertEquals("PORT2", parser.getLastPort());
			assertEquals("service-object", parser.getRuleName());
		}

		line = "service-object PROTO";
		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("PROTO", parser.getProtocol());
			assertEquals(null, parser.getPortOperator());
			assertEquals("service-object", parser.getRuleName());
		}

	}

	/**
	 * Test of PortObject, of class PixParser.
	 */
	public void testPortObject() {
		System.out.println("port-object");
		String line = "port-object  eq   PORT";
		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("eq", parser.getPortOperator());
			assertEquals("PORT", parser.getFirstPort());
			assertEquals("port-object", parser.getRuleName());
		}
		line = "port-object  neq   PORT";
		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("neq", parser.getPortOperator());
			assertEquals("PORT", parser.getFirstPort());
			assertEquals("port-object", parser.getRuleName());
		}
		line = "port-object  lt   PORT";
		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("lt", parser.getPortOperator());
			assertEquals("PORT", parser.getFirstPort());
			assertEquals("port-object", parser.getRuleName());
		}
		line = "port-object  gt   PORT";
		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("gt", parser.getPortOperator());
			assertEquals("PORT", parser.getFirstPort());
			assertEquals("port-object", parser.getRuleName());
		}
		line = "port-object  range   PORT1 PORT2";
		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("range", parser.getPortOperator());
			assertEquals("PORT1", parser.getFirstPort());
			assertEquals("PORT2", parser.getLastPort());
			assertEquals("port-object", parser.getRuleName());
		}
	}

	/**
	 * Test of AccessGroup, of class PixParser.
	 */
	public void testAccessGroup() {
		System.out.println("AccessGroup");
		String line = "access-group GROUP    in   interface    INTERFACE  something";
		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("GROUP", parser.getName());
			assertEquals("in", parser.getDirection());
			assertEquals("INTERFACE", parser.getInterface());
			assertEquals("access-group", parser.getRuleName());
		}

		line = "access-group GROUP    out  interface    INTERFACE  something";
		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("GROUP", parser.getName());
			assertEquals("out", parser.getDirection());
			assertEquals("INTERFACE", parser.getInterface());
			assertEquals("access-group", parser.getRuleName());
		}
	}

	/**
	 * Test of AccessListRemark, of class PixParser.
	 */
	public void testAccessListRemark() {
		System.out.println("AccessListRemark");
		String line = "access-list ID remark something something";
		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("access-list remark", parser.getRuleName());
		}

		line = "access-list ID remark something something something something";
		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("access-list remark", parser.getRuleName());
		}

	}

	/**
	 * Test of Description, of class PixParser.
	 */
	public void testDescription() {
		System.out.println("Description");
		String line = "description something something";
		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			assertEquals("description", parser.getRuleName());
			assertEquals("something something", parser.getName());
		}
	}

	/**
	 * Test of AccessListAcl, of class PixParser.
	 */
	public void testAccessListAcl() throws IOException {
		System.out.println("AccessListAcl");

		AclTemplate acl;
		String line;
		parser.setGroupTypeSearch(this);
		groups.put("GPROTO", ObjectGroupType.PROTOCOL);
		groups.put("GSRCNETWORK", ObjectGroupType.NETWORK);
		groups.put("GDSTNETWORK", ObjectGroupType.NETWORK);
		groups.put("GSRCSERV", ObjectGroupType.SERVICE);
		groups.put("GDSTSERV", ObjectGroupType.SERVICE);
		groups.put("GENHANCED", ObjectGroupType.ENHANCED);
		groups.put("GICMP", ObjectGroupType.ICMP);

		line = "access-list ID  line 40  extended  permit" +
				" PROTO  any eq SERVSRC" +
				" host  IP   eq   SERVDST";

		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			acl = parser.getAcl();
			assertEquals("access-list acl", parser.getRuleName());
			assertEquals("ID", acl.getAccessListId());
			assertEquals("permit", acl.getAction());
			assertEquals("PROTO", acl.getProtocol());
			assertEquals("any", acl.getSrcIp());
			assertEquals("eq", acl.getSrcPortOperator());
			assertEquals("SERVSRC", acl.getSrcFirstPort());
			assertEquals("IP", acl.getDstIp());
			assertEquals("eq", acl.getDstPortOperator());
			assertEquals("SERVDST", acl.getDstFirstPort());
		}

		line = "access-list ID  line 40  extended  permit" +
				" PROTO  any eq SERVSRC" +
				" host  IP   eq   SERVDST inactive";

		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			acl = parser.getAcl();
			assertEquals("access-list acl", parser.getRuleName());
			assertEquals("ID", acl.getAccessListId());
			assertEquals("permit", acl.getAction());
			assertEquals("PROTO", acl.getProtocol());
			assertEquals("any", acl.getSrcIp());
			assertEquals("eq", acl.getSrcPortOperator());
			assertEquals("SERVSRC", acl.getSrcFirstPort());
			assertEquals("IP", acl.getDstIp());
			assertEquals("eq", acl.getDstPortOperator());
			assertEquals("SERVDST", acl.getDstFirstPort());
			assertEquals(true, acl.getInactive());
		}

		line = "access-list ID  line 40  extended  permit" +
				" PROTO any any";

		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			acl = parser.getAcl();
			assertEquals("access-list acl", parser.getRuleName());
			assertEquals("ID", acl.getAccessListId());
			assertEquals("permit", acl.getAction());
			assertEquals("PROTO", acl.getProtocol());
			assertEquals("any", acl.getSrcIp());
			assertEquals("any", acl.getDstIp());
		}

		line = "access-list ID  line 40  extended  permit" +
				" PROTO any any inactive";

		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			acl = parser.getAcl();
			assertEquals("access-list acl", parser.getRuleName());
			assertEquals("ID", acl.getAccessListId());
			assertEquals("permit", acl.getAction());
			assertEquals("PROTO", acl.getProtocol());
			assertEquals("any", acl.getSrcIp());
			assertEquals("any", acl.getDstIp());
			assertEquals(true, acl.getInactive());
		}


		line = "access-list ID permit" +
				" PROTO  any eq SERVSRC" +
				" host  IP   eq   SERVDST";

		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			acl = parser.getAcl();
			assertEquals("access-list acl", parser.getRuleName());
			assertEquals("ID", acl.getAccessListId());
			assertEquals("permit", acl.getAction());
			assertEquals("PROTO", acl.getProtocol());
			assertEquals("any", acl.getSrcIp());
			assertEquals("eq", acl.getSrcPortOperator());
			assertEquals("SERVSRC", acl.getSrcFirstPort());
			assertEquals("IP", acl.getDstIp());
			assertEquals("eq", acl.getDstPortOperator());
			assertEquals("SERVDST", acl.getDstFirstPort());
		}

		line = "access-list ID deny" +
				" PROTO  any eq SERVSRC" +
				" host  IP   eq   SERVDST";

		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			acl = parser.getAcl();
			assertEquals("access-list acl", parser.getRuleName());
			assertEquals("ID", acl.getAccessListId());
			assertEquals("deny", acl.getAction());
			assertEquals("PROTO", acl.getProtocol());
			assertEquals("any", acl.getSrcIp());
			assertEquals("eq", acl.getSrcPortOperator());
			assertEquals("SERVSRC", acl.getSrcFirstPort());
			assertEquals("IP", acl.getDstIp());
			assertEquals("eq", acl.getDstPortOperator());
			assertEquals("SERVDST", acl.getDstFirstPort());
		}

		line = "access-list ID deny" +
				" object-group GPROTO  any eq SERVSRC" +
				" host  IP   eq   SERVDST";

		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			acl = parser.getAcl();
			assertEquals("access-list acl", parser.getRuleName());
			assertEquals("ID", acl.getAccessListId());
			assertEquals("deny", acl.getAction());
			assertEquals("GPROTO", acl.getProtocolGroupId());
			assertEquals("any", acl.getSrcIp());
			assertEquals("eq", acl.getSrcPortOperator());
			assertEquals("SERVSRC", acl.getSrcFirstPort());
			assertEquals("IP", acl.getDstIp());
			assertEquals("eq", acl.getDstPortOperator());
			assertEquals("SERVDST", acl.getDstFirstPort());
		}

		line = "access-list ID deny" +
				" PROTO  IPSRC MASKSRC eq SERVSRC" +
				" host  IP   eq   SERVDST";

		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			acl = parser.getAcl();
			assertEquals("access-list acl", parser.getRuleName());
			assertEquals("ID", acl.getAccessListId());
			assertEquals("deny", acl.getAction());
			assertEquals("PROTO", acl.getProtocol());
			assertEquals("IPSRC", acl.getSrcIp());
			assertEquals("MASKSRC", acl.getSrcIpMask());
			assertEquals("eq", acl.getSrcPortOperator());
			assertEquals("SERVSRC", acl.getSrcFirstPort());
			assertEquals("IP", acl.getDstIp());
			assertEquals("eq", acl.getDstPortOperator());
			assertEquals("SERVDST", acl.getDstFirstPort());
		}

		line = "access-list ID deny" +
				" PROTO  host IPSRC  eq SERVSRC" +
				" host  IP   eq   SERVDST";

		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			acl = parser.getAcl();
			assertEquals("access-list acl", parser.getRuleName());
			assertEquals("ID", acl.getAccessListId());
			assertEquals("deny", acl.getAction());
			assertEquals("PROTO", acl.getProtocol());
			assertEquals("IPSRC", acl.getSrcIp());
			assertEquals("eq", acl.getSrcPortOperator());
			assertEquals("SERVSRC", acl.getSrcFirstPort());
			assertEquals("IP", acl.getDstIp());
			assertEquals("eq", acl.getDstPortOperator());
			assertEquals("SERVDST", acl.getDstFirstPort());
		}

		line = "access-list ID deny" +
				" PROTO  object-group GSRCNETWORK  eq SERVSRC" +
				" host  IP   eq   SERVDST";

		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			acl = parser.getAcl();
			assertEquals("access-list acl", parser.getRuleName());
			assertEquals("ID", acl.getAccessListId());
			assertEquals("deny", acl.getAction());
			assertEquals("PROTO", acl.getProtocol());
			assertEquals("GSRCNETWORK", acl.getSrcNetworkGroup());
			assertEquals("eq", acl.getSrcPortOperator());
			assertEquals("SERVSRC", acl.getSrcFirstPort());
			assertEquals("IP", acl.getDstIp());
			assertEquals("eq", acl.getDstPortOperator());
			assertEquals("SERVDST", acl.getDstFirstPort());
		}

		line = "access-list ID deny" +
				" PROTO  any lt SERVSRC" +
				" host  IP   eq   SERVDST";

		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			acl = parser.getAcl();
			assertEquals("access-list acl", parser.getRuleName());
			assertEquals("ID", acl.getAccessListId());
			assertEquals("deny", acl.getAction());
			assertEquals("PROTO", acl.getProtocol());
			assertEquals("any", acl.getSrcIp());
			assertEquals("lt", acl.getSrcPortOperator());
			assertEquals("SERVSRC", acl.getSrcFirstPort());
			assertEquals("IP", acl.getDstIp());
			assertEquals("eq", acl.getDstPortOperator());
			assertEquals("SERVDST", acl.getDstFirstPort());
		}

		line = "access-list ID deny" +
				" PROTO  any gt SERVSRC" +
				" host  IP   eq   SERVDST";

		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			acl = parser.getAcl();
			assertEquals("access-list acl", parser.getRuleName());
			assertEquals("ID", acl.getAccessListId());
			assertEquals("deny", acl.getAction());
			assertEquals("PROTO", acl.getProtocol());
			assertEquals("any", acl.getSrcIp());
			assertEquals("gt", acl.getSrcPortOperator());
			assertEquals("SERVSRC", acl.getSrcFirstPort());
			assertEquals("IP", acl.getDstIp());
			assertEquals("eq", acl.getDstPortOperator());
			assertEquals("SERVDST", acl.getDstFirstPort());
		}

		line = "access-list ID deny" +
				" PROTO  any neq SERVSRC" +
				" host  IP   eq   SERVDST";

		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			acl = parser.getAcl();
			assertEquals("ID", acl.getAccessListId());
			assertEquals("access-list acl", parser.getRuleName());
			assertEquals("deny", acl.getAction());
			assertEquals("PROTO", acl.getProtocol());
			assertEquals("any", acl.getSrcIp());
			assertEquals("neq", acl.getSrcPortOperator());
			assertEquals("SERVSRC", acl.getSrcFirstPort());
			assertEquals("IP", acl.getDstIp());
			assertEquals("eq", acl.getDstPortOperator());
			assertEquals("SERVDST", acl.getDstFirstPort());
		}

		line = "access-list ID deny" +
				" PROTO  any range SERV1SRC SERV2SRC" +
				" host  IP   eq   SERVDST";

		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			acl = parser.getAcl();
			assertEquals("access-list acl", parser.getRuleName());
			assertEquals("ID", acl.getAccessListId());
			assertEquals("deny", acl.getAction());
			assertEquals("PROTO", acl.getProtocol());
			assertEquals("any", acl.getSrcIp());
			assertEquals("range", acl.getSrcPortOperator());
			assertEquals("SERV1SRC", acl.getSrcFirstPort());
			assertEquals("SERV2SRC", acl.getSrcLastPort());
			assertEquals("IP", acl.getDstIp());
			assertEquals("eq", acl.getDstPortOperator());
			assertEquals("SERVDST", acl.getDstFirstPort());
		}

		line = "access-list ID deny" +
				" PROTO  any object-group GSRCSERV" +
				" host  IP   eq   SERVDST";

		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			acl = parser.getAcl();
			assertEquals("access-list acl", parser.getRuleName());
			assertEquals("ID", acl.getAccessListId());
			assertEquals("deny", acl.getAction());
			assertEquals("PROTO", acl.getProtocol());
			assertEquals("any", acl.getSrcIp());
			assertEquals("GSRCSERV", acl.getSrcServiceGroup());
			assertEquals("IP", acl.getDstIp());
			assertEquals("eq", acl.getDstPortOperator());
			assertEquals("SERVDST", acl.getDstFirstPort());
		}

		line = "access-list ID deny" +
				" PROTO  any object-group GSRCSERV" +
				" IPDST MASKDST   eq   SERVDST";

		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			acl = parser.getAcl();
			assertEquals("access-list acl", parser.getRuleName());
			assertEquals("ID", acl.getAccessListId());
			assertEquals("deny", acl.getAction());
			assertEquals("PROTO", acl.getProtocol());
			assertEquals("any", acl.getSrcIp());
			assertEquals("GSRCSERV", acl.getSrcServiceGroup());
			assertEquals("IPDST", acl.getDstIp());
			assertEquals("MASKDST", acl.getDstIpMask());
			assertEquals("eq", acl.getDstPortOperator());
			assertEquals("SERVDST", acl.getDstFirstPort());
		}

		line = "access-list ID deny" +
				" PROTO  any object-group GSRCSERV" +
				" object-group GDSTNETWORK   eq   SERVDST";

		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			acl = parser.getAcl();
			assertEquals("access-list acl", parser.getRuleName());
			assertEquals("ID", acl.getAccessListId());
			assertEquals("deny", acl.getAction());
			assertEquals("PROTO", acl.getProtocol());
			assertEquals("any", acl.getSrcIp());
			assertEquals("GSRCSERV", acl.getSrcServiceGroup());
			assertEquals("GDSTNETWORK", acl.getDstNetworkGroup());
			assertEquals("eq", acl.getDstPortOperator());
			assertEquals("SERVDST", acl.getDstFirstPort());
		}

		line = "access-list ID deny" +
				" PROTO  any object-group GSRCSERV" +
				" object-group GDSTNETWORK";

		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			acl = parser.getAcl();
			assertEquals("access-list acl", parser.getRuleName());
			assertEquals("ID", acl.getAccessListId());
			assertEquals("deny", acl.getAction());
			assertEquals("PROTO", acl.getProtocol());
			assertEquals("any", acl.getSrcIp());
			assertEquals("GSRCSERV", acl.getSrcServiceGroup());
			assertEquals("GDSTNETWORK", acl.getDstNetworkGroup());
			assertEquals(null, acl.getDstPortOperator());
			assertEquals(null, acl.getDstFirstPort());
		}

		line = "access-list ID deny" +
				" PROTO  interface SRCIFACE" +
				" interface DSTIFACE";

		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			acl = parser.getAcl();
			assertEquals("access-list acl", parser.getRuleName());
			assertEquals("ID", acl.getAccessListId());
			assertEquals("deny", acl.getAction());
			assertEquals("PROTO", acl.getProtocol());
			assertEquals("SRCIFACE", acl.getSrcIfName());
			assertEquals("DSTIFACE", acl.getDstIfName());
			assertEquals(false, acl.getInactive());
		}

		line = "access-list ID deny" +
				" PROTO  interface SRCIFACE" +
				" interface DSTIFACE" +
				" something something inactive something";

		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			acl = parser.getAcl();
			assertEquals("access-list acl", parser.getRuleName());
			assertEquals("ID", acl.getAccessListId());
			assertEquals("deny", acl.getAction());
			assertEquals("PROTO", acl.getProtocol());
			assertEquals("SRCIFACE", acl.getSrcIfName());
			assertEquals("DSTIFACE", acl.getDstIfName());
			assertEquals(true, acl.getInactive());
		}

		line = "access-list inside_access_in " +
				"extended deny tcp any object-group GDSTNETWORK";

		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			acl = parser.getAcl();
			assertEquals("access-list acl", parser.getRuleName());
			assertEquals("inside_access_in", acl.getAccessListId());
			assertEquals("deny", acl.getAction());
			assertEquals("tcp", acl.getProtocol());
			assertEquals("any", acl.getSrcIp());
			assertEquals(null, acl.getSrcServiceGroup());
			assertEquals("GDSTNETWORK", acl.getDstNetworkGroup());
		}

		line = "access-list ID " +
				"extended deny object-group GENHANCED any object-group GDSTNETWORK";

		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			acl = parser.getAcl();
			assertEquals("access-list acl", parser.getRuleName());
			assertEquals("ID", acl.getAccessListId());
			assertEquals("deny", acl.getAction());
			assertEquals(null, acl.getProtocol());
			assertEquals(null, acl.getProtocolGroupId());
			assertEquals("GENHANCED", acl.getDstEnhancedServiceGroup());
			assertEquals("any", acl.getSrcIp());
			assertEquals(null, acl.getSrcNetworkGroup());
			assertEquals(null, acl.getSrcServiceGroup());
			assertEquals("GDSTNETWORK", acl.getDstNetworkGroup());
		}

		line = "access-list inside_access_in " +
				"extended deny ICMP any object-group GDSTNETWORK" +
				" object-group GICMP";

		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			acl = parser.getAcl();
			assertEquals("access-list acl", parser.getRuleName());
			assertEquals("inside_access_in", acl.getAccessListId());
			assertEquals("deny", acl.getAction());
			assertEquals("ICMP", acl.getProtocol());
			assertEquals("any", acl.getSrcIp());
			assertEquals(null, acl.getSrcServiceGroup());
			assertEquals("GDSTNETWORK", acl.getDstNetworkGroup());
			assertEquals("GICMP", acl.getIcmpGroup());
		}

		line = "access-list inside_access_in " +
				"extended deny ICMP object-group GSRCNETWORK object-group GDSTNETWORK" +
				" object-group GICMP inactive";

		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			acl = parser.getAcl();
			assertEquals("access-list acl", parser.getRuleName());
			assertEquals("inside_access_in", acl.getAccessListId());
			assertEquals("deny", acl.getAction());
			assertEquals("ICMP", acl.getProtocol());
			assertEquals(null, acl.getSrcIp());
			assertEquals("GSRCNETWORK", acl.getSrcNetworkGroup());
			assertEquals("GDSTNETWORK", acl.getDstNetworkGroup());
			assertEquals("GICMP", acl.getIcmpGroup());
			assertEquals(true, acl.getInactive());
		}

		IPIcmp icmp = IPIcmp4.getInstance();
		InputStream stream = icmp.getClass().getResourceAsStream("/ip/icmp");
		icmp.readIcmp(stream);

		line = "access-list ID " +
				"extended deny ICMP object-group GSRCNETWORK" +
				" object-group GDSTNETWORK" +
				" echo";

		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			acl = parser.getAcl();
			assertEquals("access-list acl", parser.getRuleName());
			assertEquals("ID", acl.getAccessListId());
			assertEquals("deny", acl.getAction());
			assertEquals("ICMP", acl.getProtocol());
			assertEquals(null, acl.getSrcIp());
			assertEquals("GSRCNETWORK", acl.getSrcNetworkGroup());
			assertEquals("GDSTNETWORK", acl.getDstNetworkGroup());
			assertEquals("echo", acl.getIcmp());
		}

		line = "access-list ID " +
				"extended deny object-group GPROTO object-group GSRCNETWORK" +
				" object-group GDSTNETWORK object-group GDSTSERV";

		result = parseRunerParse.run(line);
		assertTrue(result.matched);
		if (result.matched) {
			acl = parser.getAcl();
			assertEquals("access-list acl", parser.getRuleName());
			assertEquals("ID", acl.getAccessListId());
			assertEquals("deny", acl.getAction());
			assertEquals("GPROTO", acl.getProtocolGroupId());
			assertEquals(null, acl.getSrcIp());
			assertEquals("GSRCNETWORK", acl.getSrcNetworkGroup());
			assertEquals("GDSTNETWORK", acl.getDstNetworkGroup());
			assertEquals("GDSTSERV", acl.getDstServiceGroup());
		}

	}

	public ObjectGroupType getGroupType(String groupId) {
		return groups.get(groupId);
	}

}
