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

package fr.univrennes1.cri.jtacl.lib.ip;

import java.math.BigInteger;
import java.net.UnknownHostException;
import junit.framework.TestCase;

/**
 * Test class for {@link IPNet}.
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class IPNetTest extends TestCase {
    
	/**
	 *
	 * @param testName
	 */
	public IPNetTest(String testName) {
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
	 * (helper) Test of the constructor of class IPNet.
	 */
	protected void testConstructorNetIP(String par, String ip,
			int prefixlen, IPversion version) throws UnknownHostException {

		System.out.println("Test NetIP() : " + par);
		IPNet ipnet  = new IPNet(par);
		BigInteger bi = new BigInteger(ip);
		if (bi.compareTo(BigInteger.ZERO) != 0) {
	 		boolean comp = bi.compareTo(ipnet.getIP()) == 0;
			assertEquals(true, comp);
		}
		if (prefixlen != 0)
			assertEquals(prefixlen, ipnet.getPrefixLen());
		assertEquals(version, ipnet.getIpVersion());
	}


	private void testDataNetipOk(String [][] dt) throws UnknownHostException {

		for (int i = 0; i < dt.length; i++) {
			int prefix = Integer.parseInt(dt[i][2]);
			IPversion version = Integer.parseInt(dt[i][3]) == 4 ? IPversion.IPV4 : IPversion.IPV6;

			testConstructorNetIP(dt[i][0], dt[i][1], prefix, version);
		}
	}

	private void testDataNetipNOk(String [][] dt) {
		for (int i = 0; i < dt.length; i++) {
			boolean f = false;
			int prefix = Integer.parseInt(dt[i][2]);
			IPversion version = Integer.parseInt(dt[i][3]) == 4 ? IPversion.IPV4 : IPversion.IPV6;

			try {
				testConstructorNetIP(dt[i][0], dt[i][1], prefix, version);
			} catch (UnknownHostException e) {
				System.out.println("check : " + e.getMessage());
				f = true;
			}
			assertTrue(f);
		}
	}

	/**
	 * Test of the constructor of class IPNet.
	 */
	public void testNetIP() throws UnknownHostException {

		// min / max ipV4 and ipV6

		String [][] dtmn = {
		// param, ip, ipversion
		{"0x0000",		"0",			"32", "4"},
		{"0xffffffff",	"4294967295",	"32", "4"},
		{"0x100000000",	"4294967296",	"128", "6"},
		{"0x0123456789abcdef", "81985529216486895", "128", "6"},
		{"0xffffffffffffffffffffffffffffffff", "340282366920938463463374607431768211455", "128", "6"},
		};
		testDataNetipOk(dtmn);


		boolean f = false;
		try {
			// max ipv6 + 1 => exception expected
			testConstructorNetIP(
				"0x100000000000000000000000000000000",
				"340282366920938463463374607431768211456",
				128,
				IPversion.IPV6
			);
		} catch (UnknownHostException e) {
			System.out.println("check : " + e.getMessage());
			f = true;
		}
		assertEquals(true, f);

		// some valid addresses
		// IPV4
		String [][] dtaddrv4 = {
		{"0.0.0.0",			"0",			"32", "4"},
		{"123.123",			"2071658496",	"32", "4"},
		{"123.123.123.123",	"2071690107",	"32", "4"},
		{"255.255.255.255",	"4294967295",	"32", "4"},
		{"1",				"16777216",		"32", "4"},
		{"255",				"4278190080",	"32", "4"},
		{"256",				"256",			"32", "4"},
		};
		testDataNetipOk(dtaddrv4);

		// some invalid addresses
		// IPV4
		String [][] dtaddrv4NOK = {
		{"255.255.255.255.255",			"0", "32", "4"},
		{"255.255.256.255",				"0", "32", "4"},
		{"255.a12.255.255",				"0", "32", "4"},
		{"0xhelloworld",				"0", "32", "4"},
		};
		testDataNetipNOk(dtaddrv4NOK);

		// some valid addresses
		// IPV6
		String [][] dtaddrv6 = {
		{"0x0123456789abcdef", "81985529216486895", "128", "6"},
		{"FEDC:BA98:7654:3210:FEDC:BA98:7654:3210", "338770000845734292534325025077361652240", "128", "6"},
		{"FEDCBA9876543210FEDCBA9876543210", "338770000845734292534325025077361652240", "128", "6"},
		{"0xFEDCBA9876543210FEDCBA9876543210", "338770000845734292534325025077361652240", "128", "6"},
		{"1080:0000:0000:0000:0008:0800:200C:417A",	"21932261930451111902915077091070067066", "128", "6"},
		{"1080:0:0:0:8:800:200C:417A", "21932261930451111902915077091070067066", "128", "6"},
		{"1080:0::8:800:200C:417A",	"21932261930451111902915077091070067066", "128", "6"},
		{"1080::8:800:200C:417A", "21932261930451111902915077091070067066", "128", "6"},
		{"FF01:0:0:0:0:0:0:43", "338958331222012082418099330867817087043", "128", "6"},
		{"FF01:0:0::0:0:0:43", "338958331222012082418099330867817087043", "128", "6"},
		{"FF01::43", "338958331222012082418099330867817087043", "128", "6"},
		{"0:0:0:0:0:0:0:1", "1", "128", "6"},
		{"0:0:0::0:0:0:1", "1", "128", "6"},
		{"::1", "1", "128", "6"},
		{"0:0:0:0:0:0:0:0", "0", "128", "6"},
		{"0:0:0::0:0:0:0", "0", "128", "6"},
		{"::0", "0", "128", "6"},
		{"::", "0", "128", "6"},
		{"1:2:3:4:5:6::", "5192455318486707404433266432802816", "128", "6"},
		{"0:0:0:0:0:0:13.1.68.3", "218186755", "128", "6"},
		{"::13.1.68.3",	"218186755", "128", "6"},
		};
		testDataNetipOk(dtaddrv6);

		// some invalid addresses
		// IPV6
		String [][] dtaddrv6NOK = {
		{"0:0:0:0:0:0:0:0:0", "0", "128", "6"},
		{"0:10000:0:0:0:0:0:0", "0", "128", "6"},
		{"0::0:0:0::0:0:0:0", "0", "128", "6"},
		{"0:127.0.0.1:0::0:0", "0", "128", "6"},
		{"0:hello:0:0:world:0:0:0", "0", "128", "6"},
		};
		testDataNetipNOk(dtaddrv6NOK);

		 // range and netmask

		// netmask, prefixlen
		String [][] dtrn = {
			{"255.255.255.255", "32"},
			{"255.255.255.254", "31"},
			{"255.255.255.252", "30"},
			{"255.255.255.248", "29"},
			{"255.255.255.240", "28"},
			{"255.255.255.224", "27"},
			{"255.255.255.192", "26"},
			{"255.255.255.128", "25"},
			{"255.255.255.0",   "24"},
			{"255.255.254.0",   "23"},
			{"255.255.252.0",   "22"},
			{"255.255.248.0",   "21"},
			{"255.255.240.0",   "20"},
			{"255.255.224.0",   "19"},
			{"255.255.192.0",   "18"},
			{"255.255.128.0",   "17"},
			{"255.255.0.0",		"16"},
			{"255.254.0.0",		"15"},
			{"255.252.0.0",		"14"},
			{"255.248.0.0",		"13"},
			{"255.240.0.0",		"12"},
			{"255.224.0.0",		"11"},
			{"255.192.0.0",		"10"},
			{"255.128.0.0",		"9"},
			{"255.0.0.0",		"8"},
			{"254.0.0.0",		"7"},
			{"252.0.0.0",		"6"},
			{"248.0.0.0",		"5"},
			{"240.0.0.0",		"4"},
			{"224.0.0.0",		"3"},
			{"192.0.0.0",		"2"},
			{"128.0.0.0",		"1"},
			{"0.0.0.0",			"0"},
		};

		for (int i = 0 ; i < dtrn.length; i++) {
			testConstructorNetIP(
				"192.168.0.1/" + dtrn[i][0],
				"0",
				Integer.parseInt(dtrn[i][1]),
				IPversion.IPV4
			);
		}

		// test /
		for (int i = 0; i < dtrn.length; i++) {
			testConstructorNetIP(
				"192.168.0.1/" + dtrn[i][1],
				"0",
				Integer.parseInt(dtrn[i][1]),
				IPversion.IPV4
			);
		}

		// invalid
		f = false;
		try {
			testConstructorNetIP(
				"192.168.0.1/256.255.255.0",
				"0",
				32,
				IPversion.IPV4
			);
		} catch (UnknownHostException e) {
			f = true;
			System.out.println("check : " + e.getMessage());
		}
		assertTrue(f);

		// invalid
		f = false;
		try {
			testConstructorNetIP(
				"192.168.0.1/33",
				"0",
				33,
				IPversion.IPV4
			);
		} catch (UnknownHostException e) {
			f = true;
			System.out.println("check : " + e.getMessage());
		}
		assertTrue(f);

		// invalid
		f = false;
		try {
			testConstructorNetIP(
				"192.168.0.1/abcd",
				"0",
				0,
				IPversion.IPV4
			);
		} catch (UnknownHostException e) {
			f = true;
			System.out.println("check : " + e.getMessage());
		}
		assertTrue(f);

		// invalid
		f = false;
		try {
			testConstructorNetIP(
				"0.0.0.255/0.0.0.255",
				"0",
				0,
				IPversion.IPV4
			);
		} catch (UnknownHostException e) {
			f = true;
			System.out.println("check : " + e.getMessage());
		}
		assertTrue(f);

		// test / ipv6
		for (int i = 0; i <= 128; i++) {
			testConstructorNetIP(
				"::1/" + i,
				"0",
				i,
				IPversion.IPV6
			);
		}

		// invalid
		f = false;
		try {
			testConstructorNetIP(
				"::1/129",
				"0",
				0,
				IPversion.IPV6
			);
		} catch (UnknownHostException e) {
			f = true;
			System.out.println("check : " + e.getMessage());
		}
		assertTrue(f);

		// test range
		testConstructorNetIP(
			"0.0.0.0-255.255.255.255",
			"0",
			0,
			IPversion.IPV4
		);

		// test range
		testConstructorNetIP(
			"192.168.0.0-192.168.0.255",
			"0",
			24,
			IPversion.IPV4
		);

	}

	/**
	 * Test of isIPv4 method, of class IPNet.
	 */
	public void testIsIPv4() throws UnknownHostException {
		System.out.println("isIPv4");

		IPNet ip;
		ip = new IPNet("::1");
		assertEquals(false, ip.isIPv4());

		ip = new IPNet("127.0.0.1");
		assertEquals(true, ip.isIPv4());
	}

	/**
	 * Test of isIPv6 method, of class IPNet.
	 */
	public void testIsIPv6() throws UnknownHostException {
		System.out.println("isIPv6");

		IPNet ip;
		ip = new IPNet("::1");
		assertEquals(true, ip.isIPv6());

		ip = new IPNet("127.0.0.1");
		assertEquals(false, ip.isIPv6());
	}

	/**
	 * Test of equals method, of class IPNet.
	 */
	public void testEquals() throws UnknownHostException  {
		System.out.println("equals");

		assertEquals(new IPNet("127.0.0.1"), new IPNet("127.0.0.1"));

		IPNet ip1 = new IPNet("127.0.0.1/7");
		IPNet ip2 = new IPNet("127.0.0.1/8");

		assertFalse(ip1.equals(ip2));
	}

	/**
	 * Test of networkLength, of class IPNet.
	 */
	public void testNetworksLength() throws UnknownHostException  {
		System.out.println("networkLength");

		IPNet ip = new IPNet("127.0.0.1");
		assertEquals(ip.networkLength(), new BigInteger("1"));

		ip = new IPNet("::1");
		assertEquals(ip.networkLength(), new BigInteger("1"));

		ip = new IPNet("127.0.0.1/24");
		assertEquals(ip.networkLength(), new BigInteger("256"));

		ip = new IPNet("::1/120");
		assertEquals(ip.networkLength(), new BigInteger("256"));

		ip = new IPNet("0.0.0.0/0");
		assertEquals(ip.networkLength(), IP.MAX_IPV4_NUMBER.add(BigInteger.ONE));

		ip = new IPNet("::0/0");
		assertEquals(ip.networkLength(), IP.MAX_IPV6_NUMBER.add(BigInteger.ONE));
	}

	/**
	 * Test of networkContains, of class IPNet.
	 */
	public void testNetworkContains() throws UnknownHostException  {
		System.out.println("networkContains");

		IPNet ip1 = new IPNet("127.0.0.1");
		IPNet ip2 = new IPNet("127.0.0.1");
		assertTrue(ip1.networkContains(ip2));

		ip1 = new IPNet("127.0.0.1");
		ip2 = new IPNet("127.0.0.2");
		assertFalse(ip1.networkContains(ip2));

		ip1 = new IPNet("::1");
		ip2 = new IPNet("::1");
		assertTrue(ip1.networkContains(ip2));

		ip1 = new IPNet("::1");
		ip2 = new IPNet("::2");
		assertFalse(ip1.networkContains(ip2));

		ip1 = new IPNet("127.0.0.1/16");
		ip2 = new IPNet("127.0.0.2/24");
		assertTrue(ip1.networkContains(ip2));

		ip1 = new IPNet("::1/16");
		ip2 = new IPNet("::1/24");
		assertTrue(ip1.networkContains(ip2));

		ip1 = new IPNet("127.0.0.1/24");
		ip2 = new IPNet("127.0.0.1/16");
		assertFalse(ip1.networkContains(ip2));

		ip1 = new IPNet("::1/24");
		ip2 = new IPNet("::1/16");
		assertFalse(ip1.networkContains(ip2));
	}


	/**
	 * Test of broadcastAddress
	 * @throws UnknownHostException
	 */
	public void testBroadcastAddress() throws UnknownHostException {
		System.out.println("broadcastAddress");

		IPNet ip1 = new IPNet("127.0.0.0/24");
		IPNet ip2 = ip1.broadcastAddress();
		IPNet eq = new IPNet("127.0.0.255/24");
		assertEquals(eq, ip2);

		ip1 = new IPNet("127.0.0.0/16");
		ip2 = ip1.broadcastAddress();
		eq = new IPNet("127.0.255.255/16");
		assertEquals(eq, ip2);

		ip1 = new IPNet("127.0.0.0/8");
		ip2 = ip1.broadcastAddress();
		eq = new IPNet("127.255.255.255/8");
		assertEquals(eq, ip2);

		ip1 = new IPNet("0.0.0.0/8");
		ip2 = ip1.broadcastAddress();
		eq = new IPNet("0.255.255.255/8");
		assertEquals(eq, ip2);

		ip1 = new IPNet("0.0.0.0/0");
		ip2 = ip1.broadcastAddress();
		eq = new IPNet("255.255.255.255/0");
		assertEquals(eq, ip2);

		ip1 = new IPNet("::0/0");
		ip2 = ip1.broadcastAddress();
		eq = new IPNet("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff/0");
		assertEquals(eq, ip2);

	}

	/**
	 * Test of overlaps, of class IPNet.
	 */
	public void testOverlaps() throws UnknownHostException  {
		System.out.println("overlaps");

		IPNet ip1;
		IPNet ip2;
		
		ip1 = new IPNet("127.0.0.1");
		ip2 = new IPNet("127.0.0.1");
		assertTrue(ip1.overlaps(ip2));
		assertTrue(ip2.overlaps(ip1));

		ip1 = new IPNet("127.0.0.1");
		ip2 = new IPNet("127.0.0.2");
		assertFalse(ip1.overlaps(ip2));
		assertFalse(ip2.overlaps(ip1));


		ip1 = new IPNet("127.0.0.1/31");
		ip2 = new IPNet("127.0.0.2");
		assertFalse(ip1.overlaps(ip2));
		assertFalse(ip2.overlaps(ip1));

		ip1 = new IPNet("127.0.0.1/30");
		ip2 = new IPNet("127.0.0.2/31");
		assertTrue(ip1.overlaps(ip2));
		assertTrue(ip2.overlaps(ip1));

		ip1 = new IPNet("127.0.0.1/32");
		ip2 = new IPNet("0/0");
		assertTrue(ip1.overlaps(ip2));
		assertTrue(ip2.overlaps(ip1));

		ip1 = new IPNet("::1/120");
		ip2 = new IPNet("::0/0");
		assertTrue(ip1.overlaps(ip2));
		assertTrue(ip2.overlaps(ip1));

	}


	/**
	 * Test of toString(), of class IPNet.
	 */
	public void testToString() throws UnknownHostException {
		System.out.println("toString");

		IPNet ip = new IPNet("127.128.129.130");

		assertEquals("127.128.129.130/32", ip.toString());

		assertEquals("127.128.129.130", ip.toString("i"));
		assertEquals("127.128.129.130", ip.toString("s"));
		assertEquals("127.128.129.130/255.255.255.255", ip.toString("n"));


		ip = new IPNet("127.0.0.1/15");
		assertEquals("127.0.0.1/15", ip.toString());

		ip = new IPNet("1:2:3:4:5:6:7:8");
		assertEquals("1:2:3:4:5:6:7:8/128", ip.toString());

		ip = new IPNet("1:2:3:4:5:6:7:8");
		assertEquals("1:2:3:4:5:6:7:8", ip.toString("s"));

		ip = new IPNet("::1");
		assertEquals("0:0:0:0:0:0:0:1/128", ip.toString());

		ip = new IPNet("::1/100");
		assertEquals("0:0:0:0:0:0:0:1/100", ip.toString());

		ip = new IPNet("::0");
		assertEquals("::0", ip.toString("s::"));

		ip = new IPNet("1::1/128");
		assertEquals("1::1", ip.toString("s::"));

		ip = new IPNet("1:2:3:4:5:6:7:0");
		assertEquals("1:2:3:4:5:6:7:0", ip.toString("s::"));

		ip = new IPNet("1:0:3:0:5:0:7:0");
		assertEquals("1:0:3:0:5:0:7:0", ip.toString("s::"));

		ip = new IPNet("1:0:0:0:0:0:0:0");
		assertEquals("1::", ip.toString("s::"));

		ip = new IPNet("1:0:0:0:1:0:0:0");
		assertEquals("1::1:0:0:0", ip.toString("s::"));

	}

	/**
	 * Test of isHost(), of class IPNet.
	 */
	public void testIsHost() throws UnknownHostException {
		System.out.println("isHost");

		IPNet ip;
		ip = new IPNet("127.128.129.130");
		assertTrue(ip.isHost());
		ip = new IPNet("::1");
		assertTrue(ip.isHost());

		ip = new IPNet("127.128.129.130/31");
		assertFalse(ip.isHost());
		ip = new IPNet("::1/127");
		assertFalse(ip.isHost());
	}

}
