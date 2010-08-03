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

package fr.univrennes1.cri.jtacl.lib.ip;

import java.math.BigInteger;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * Utility class to deal with IP addresses and networks.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class IP {

	/**
	 *  This class is static and should no be instanciated
	 */
	private IP() {
	}

	/**
	 * Largest value for an IPv6 address: 2^128 -1.
	 */
	public static final BigInteger MAX_IPV6_NUMBER =
			new BigInteger("ffffffffffffffffffffffffffffffff", 16);

	/**
	 * Largest value for an IPv4 address: 2^32 - 1.
	 */
	public static final BigInteger MAX_IPV4_NUMBER =
			new BigInteger("ffffffff", 16);

	/**
	 * Value 0x1000.
	 */
	public static final BigInteger BIG_INT_0x10000 = new BigInteger("10000", 16);

	/**
	 * Value 0xFFFF.
	 */
	public static final BigInteger BIG_INT_0xFFFF = new BigInteger("FFFF", 16);

	/**
	 * Value 0xFF.
	 */
	public static final BigInteger BIG_INT_0xFF = new BigInteger("FF", 16);


	/**
	 * Converts a netmask (IPv4) to a prefix length.
	 * @param netmask the {@link BigInteger} netmask to convert from.
	 * @return the length of the prefix.
	 * @throws UnknownHostException if the netmask can't be expressed as a prefix.
	 */
	public static int netmaskToPrefixLen(BigInteger netmask)
			throws UnknownHostException {

		int netlen = highest0Bits(netmask) + 1;
		int masklen = highest1Bits(netmask) + 1;

		if (!checkNetmask(netmask, masklen))
			throw new UnknownHostException("Netmask can't be expressed as a prefix: " +
				netmask.toString(16));
		return (masklen - netlen);
	}

	/**
	 * Converts a prefix length to a netmask.
	 * @param prefixLen the prefix to convert from.
	 * @param ipVersion the {@link IPversion} version of IP.
	 * @return the {@link BigInteger} netmask.
	 */
	public static BigInteger prefixLenToNetmask(int prefixLen, IPversion ipVersion) {

		BigInteger result = BigInteger.ZERO;
		int numBit = maxPrefixLen(ipVersion) - 1;

		for (int i = 0; i < prefixLen; i++) {
			result = result.setBit(numBit);
			numBit--;
		}
		return result;
	}

	/**
	 * Checks if a netmask can be expressed as a prefix.
	 * @param netmask the {@link BigInteger} netmask to check.
	 * @param masklen the length the mask.
	 * @return true if the netmask can be expressed as a prefix.
	 */
	public static boolean checkNetmask(BigInteger netmask, int masklen) {

		BigInteger num = netmask;
		int bits = masklen;

		// remove zero bits at end
		while (!num.testBit(0) && bits != 0) {
			num = num.shiftRight(1);
			bits--;
		}
		// check if the rest consists only of one
		while (bits > 0) {
			if (!num.testBit(0))
				return false;
			num = num.shiftRight(1);
			bits--;
		}
		return true;
	}

	/**
	 * Checks if a {@link BigInteger} network IP address is compatible with a prefix.
	 * @param netaddr the network address to check.
	 * @param prefixLen the prefx length of the network.
	 * @param ipVersion the {@link IPversion} version of IP.
	 * @return true if the network IP address is compatible.
	 */
	public static boolean checkNetaddrPrefixLen(BigInteger netaddr,
			int prefixLen, IPversion ipVersion) {

		BigInteger net = netaddr;
		net = net.and(prefixLenToNetmask(prefixLen, ipVersion));
		return net.compareTo(netaddr) == 0;
	}

	/**
	 * Returns the {@link BigInteger} length (ie the number of IP addresses) in
	 * a network.
	 * @param prefixLen the prefix length of the network
	 * @param ipVersion the {@link IPversion} version of IP.
	 * @return the {@link BigInteger} number of IP addresses
	 */
	public static BigInteger networkLength(int prefixLen, IPversion ipVersion) {
		int netlen = maxPrefixLen(ipVersion) - prefixLen;
		BigInteger bi = new BigInteger("2");
		bi = bi.pow(netlen);
		return bi;
	}
	/**
	 * Returns the max prefix length according to the IP version.<br/>
	 * <ul>
	 * <li> IPv4: 32.</li>
	 * <li> IPv6: 128.</li>
	 * </ul>
	 * @param ipVersion the {@link IPversion} IP version of the prefix.
	 * @return the max prefix length
	 */
	public static int maxPrefixLen(IPversion ipVersion) {
		return (ipVersion == IPversion.IPV4) ? 32 : 128;
	}

	/**
	 * Checks if a prefix length is valid according to the IP version.
	 * <ul>
	 * <li>IPv4: 0 &lt= prefix &lt= 32.</li>
	 * <li>IPv6: 0 &lt= prefix &lt= 128.</li>
	 * </ul>
	 * @param prefixLen the prefix length to check.
	 * @param ipVersion the {@link IPversion} IP version.
	 * @return true if the length is valid.
	 */
	public static boolean isValidPrefixLen(int prefixLen, IPversion ipVersion) {
		return prefixLen >= 0 && prefixLen <= maxPrefixLen(ipVersion);
	}

	/**
	 * Checks if an IP address is valid according to the IP version.
	 * <ul>
	 * <li>IPv4: 0 &lt= IP &lt 2^32.</li>
	 * <li>IPV6: 0 &lt= IP &lt 2^128.</li>
	 * </ul>
	 * @param ip the {@link BigInteger} IP address to check.
	 * @param ipVersion the {@link IPversion} IP version of the address.
	 * @return true if the IP address is valid.
	 */
	public static boolean isValidIP(BigInteger ip, IPversion ipVersion) {
		if (ip.compareTo(BigInteger.ZERO) < 0)
			return false;
		switch (ipVersion) {
			case IPV4:
				return ip.compareTo(MAX_IPV4_NUMBER) <= 0;
			case IPV6:
				return ip.compareTo(MAX_IPV6_NUMBER) <= 0;
		}
		return false;
	}

	/**
	 * Returns the position of the highest bit set to "1" in a number.
	 * @param number a {@link BigInteger} number.
	 * @return the position of the highest bit set to "1".
	 * Or -1 if there is not bit equal to "1" (number == 0).
	 */
	public static int highest1Bits(BigInteger number) {
		int ret = -1;
		BigInteger n = number;

		int i = 0;
		while (n.compareTo(BigInteger.ZERO) > 0) {
			if (n.testBit(0))
				ret = i;
			n = n.shiftRight(1);
			i++;
		}
		return ret;
	}

	/**
	 * Returns the position of the highest bit set to "0" in a number.
	 * @param number a {@link BigInteger} number.
	 * @return the position of the highest bit set to "0".
	 * Or -1 if there is no bit equal to "0".
	 */
	public static int highest0Bits(BigInteger number) {
		int ret = -1;
		BigInteger n = number;

		int i = 0;
		while (n.compareTo(BigInteger.ZERO) > 0) {
			if (!n.testBit(0))
				ret = i;
			n = n.shiftRight(1);
			i++;
		}
		return ret;
	}


	/**
	 * Converts a {@link BigInteger} IP address into the string representation of
	 * the IP address and prefixlen.
	 * @param ip the {@link BigInteger} IP address to convert from.
	 * @param prefixlen the prefixlen to convert from.
	 * @return An array of two {@link String} strings. The first item String[0]
	 * contains a string representation of the IP address. The second item
	 * String[1] contains a string representation of the prefix.
	 */
	public static String[] ipv4ToStrings(BigInteger ip, int prefixlen) {
		String[] result = new String[2];
		StringBuilder sip = new StringBuilder("");

		BigInteger bi = ip;
		for (int i = 0; i < 4; i++) {
			BigInteger dot = BigInteger.ZERO;
			if (bi.compareTo(BigInteger.ZERO) > 0) {
				dot = bi.and(BIG_INT_0xFF);
				bi = bi.shiftRight(8);
			}
			sip.insert(0, dot.toString());
			if (i < 3)
				sip.insert(0, '.');
		}
		result[0] = sip.toString();
		result[1] = Integer.toString(prefixlen);
		return result;
	}

	/**
	 * Converts a {@link BigInteger} IP address into the string representation of
	 * the IP address and prefixlen.
	 * @param ip the {@link BigInteger} IP address to convert from.
	 * @param prefixLen the prefixlen to convert from.
	 * @param compress if true, the IP address representation try to use the
	 * short IPv6 notation '::'.
 	 * @return An array of two {@link String} strings. The first item String[0]
	 * contains a string representation of the IP address. The second item
	 * String[1] contains a string representation of the prefix.
	 */
	public static String[] ipv6ToStrings(BigInteger ip, int prefixLen, boolean compress) {
		String[] result = new String[2];
		StringBuilder sip = new StringBuilder("");
		ArrayList<BigInteger> hextets = new ArrayList<BigInteger>();
		int p1 = -1;
		int p2 = -1;

		/*
		 * slit the ip into 8 hextets
		 */
		BigInteger bi = ip;
		for (int i = 0; i < 8; i++) {
			BigInteger hextet = BigInteger.ZERO;
			if (bi.compareTo(BigInteger.ZERO) > 0) {
				hextet = bi.and(BIG_INT_0xFFFF);
				bi = bi.shiftRight(16);
			}
			hextets.add(hextet);
		}

		/*
		 * Iterate to find consecutive hextets equal to 0
		 * result between [p1, p2]
		 */
		if (compress) {
			boolean compressed = false;
			for (int i = 7; i >= 0; i--) {
				BigInteger hextet = hextets.get(i);
				if (hextet.compareTo(BigInteger.ZERO) == 0 && !compressed) {
					if (p1 == -1)
						p1 = i;
					else
						p2 = i;
				} else {
					if (p2 >= 0)
						compressed = true;
					else
						p1 = -1;
				}
			}
		}

		/*
		 * Output hextets to a string
		 */
		for (int i = 0; i < 8; i++) {
			if (p2 >= 0 && p1 > p2 && compress) {
				if (i == p1) {
					if (sip.length() > 0)
						sip.insert(0, ":");
					else
						sip.insert(0, "::");
					continue;
				}
				if (i >= p2 && i <= p1)
					continue;
			}
			sip.insert(0, hextets.get(i).toString(16));
			if (i < 7)
				sip.insert(0, ':');
		}
		if ("::".contentEquals(sip))
			sip.append('0');
		result[0] = sip.toString();
		result[1] = Integer.toString(prefixLen);
		return result;
	}

}
