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

import java.util.HashMap;

/**
 * IANA IP ranges
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public final class IPIANAranges extends HashMap<String, String> {

	/**
	 * Definition of the Ranges for IPv4 IPs
	 *  this should include www.iana.org/assignments/ipv4-address-space
	 * and www.iana.org/assignments/multicast-addresses
	 */
	public static final HashMap<String, String> ipv4Ranges = new HashMap<String, String>();

	/**
	 * Definition of the Ranges for IPv6 IPs
	 * see also www.iana.org/assignments/ipv6-address-space,
	 * www.iana.org/assignments/ipv6-tla-assignments,
	 * www.iana.org/assignments/ipv6-multicast-addresses,
	 * www.iana.org/assignments/ipv6-anycast-addresses
	 */
	public static final HashMap<String, String> ipv6Ranges = new HashMap<String, String>();

	static {
		/*
		 * ipv4Ranges
		 */
		ipv4Ranges.put("0", "PUBLIC");   // fall back
		ipv4Ranges.put("00000000", "PRIVATE");  // 0/8
		ipv4Ranges.put("00001010", "PRIVATE");  // 10/8
		ipv4Ranges.put("01111111", "PRIVATE");  // 127.0/8
		ipv4Ranges.put("1", "PUBLIC");   // fall back
		ipv4Ranges.put("1010100111111110", "PRIVATE");  // 169.254/16
		ipv4Ranges.put("101011000001", "PRIVATE");  // 172.16/12
		ipv4Ranges.put("1100000010101000", "PRIVATE");  // 192.168/16
		ipv4Ranges.put("11011111", "RESERVED"); // 223/8
		ipv4Ranges.put("111", "RESERVED"); // 224/3

		/*
		 * ipv6Ranges
		 */
		ipv6Ranges.put("00000000", "RESERVED");        // ::/8
		ipv6Ranges.put("00000001", "UNASSIGNED");      // 100::/8
		ipv6Ranges.put("0000001", "NSAP");            // 200::/7
		ipv6Ranges.put("0000010", "IPX");             // 400::/7
		ipv6Ranges.put("0000011", "UNASSIGNED");      // 600::/7
		ipv6Ranges.put("00001", "UNASSIGNED");      // 800::/5
		ipv6Ranges.put("0001", "UNASSIGNED");      // 1000::/4
		ipv6Ranges.put("0010000000000000", "RESERVED");        // 2000::/16 Reserved
		ipv6Ranges.put("0010000000000001", "ASSIGNABLE");      // 2001::/16 Sub-TLA Assignments [RFC2450]
		ipv6Ranges.put("00100000000000010000000", "ASSIGNABLE IANA"); // 2001:0000::/29 - 2001:01F8::/29 IANA
		ipv6Ranges.put("00100000000000010000001", "ASSIGNABLE APNIC");// 2001:0200::/29 - 2001:03F8::/29 APNIC
		ipv6Ranges.put("00100000000000010000010", "ASSIGNABLE ARIN"); // 2001:0400::/29 - 2001:05F8::/29 ARIN
		ipv6Ranges.put("00100000000000010000011", "ASSIGNABLE RIPE"); // 2001:0600::/29 - 2001:07F8::/29 RIPE NCC
		ipv6Ranges.put("0010000000000010", "6TO4");            // 2002::/16 "6to4" [RFC3056]
		ipv6Ranges.put("0011111111111110", "6BONE");           // 3FFE::/16 6bone Testing [RFC2471]
		ipv6Ranges.put("0011111111111111", "RESERVED");        // 3FFF::/16 Reserved
		ipv6Ranges.put("010", "GLOBAL-UNICAST");  // 4000::/3
		ipv6Ranges.put("011", "UNASSIGNED");      // 000::/3
		ipv6Ranges.put("100", "GEO-UNICAST");     // 8000::/3
		ipv6Ranges.put("101", "UNASSIGNED");      // A000::/3
		ipv6Ranges.put("110", "UNASSIGNED");      // C000::/3
		ipv6Ranges.put("1110", "UNASSIGNED");      // E000::/4
		ipv6Ranges.put("11110", "UNASSIGNED");      // F000::/5
		ipv6Ranges.put("111110", "UNASSIGNED");      // F800::/6
		ipv6Ranges.put("1111110", "UNASSIGNED");      // FC00::/7
		ipv6Ranges.put("111111100", "UNASSIGNED");      // FE00::/9
		ipv6Ranges.put("1111111010", "LINKLOCAL");       // FE80::/10
		ipv6Ranges.put("1111111011", "SITELOCAL");       // FEC0::/10
		ipv6Ranges.put("11111111", "MULTICAST");       // FF00::/8
		ipv6Ranges.put(String.format("%96c", '0'), "IPV4COMP");        // ::/96
		ipv6Ranges.put(String.format("%80c%16c", '0', '1'), "IPV4MAP"); // ::FFFF:0:0/96
		ipv6Ranges.put(String.format("%128c", '0'), "UNSPECIFIED");     // ::/128
		ipv6Ranges.put(String.format("%127c1", '0'), "LOOPBACK");       // ::1/128
	}
}
