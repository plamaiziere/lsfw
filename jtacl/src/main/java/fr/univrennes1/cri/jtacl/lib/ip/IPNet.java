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
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Class and tools to deal with IPv4 and IPv6 addresses and networks.<br/>
 * An IP address is a {@link BigInteger} number between 0 and 2^128, and could
 * have a prefix length to specify network mask.<br/><br/>
 * Mostly taken from IPy http://pypi.python.org/pypi/IPy
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class IPNet implements Comparable {

	protected HashMap<String, String> _ipRange;

	protected int _prefixLen;
	protected BigInteger _ip;
	protected IPversion _ipVersion;


	protected static String[] ipV4ToHextet(String addr) throws UnknownHostException {
		String [] result = new String[2];
		/*
		 * parse the IPv4 address and return it as two hextets
		 */
		BigInteger ipInt = parseAddressIPv4(addr);
		BigInteger bi = ipInt.shiftRight(16);
		result[0] = bi.toString(16);
		bi = ipInt.and(IP.BIG_INT_0xFFFF);
		result[1] = bi.toString(16);

		return result;
	}

	protected static BigInteger parseAddressIPv4(String addr) throws UnknownHostException {

			// assume IPv4  ('127' gets interpreted as '127.0.0.0')
			String[] sbytes = addr.split("\\.");
			if (sbytes.length > 4)
				throw new UnknownHostException("IPv4 Address with more than 4 bytes: " + addr);
			int[] ipInts = new int[4];
			for (int i = 0; i < 4; i++) {
				if (i < sbytes.length)
					try {
						ipInts[i] = Integer.parseInt(sbytes[i]);
					} catch (NumberFormatException e) {
						throw new UnknownHostException("IP Address with not a number: " + addr);
					}
				else
					ipInts[i] = 0;
				if (ipInts[i] < 0 || ipInts[i] > 255)
					throw new UnknownHostException("IPv4 Address with ! 0 <= bytes <= 255: " + addr);
			}

			BigInteger ipInt = BigInteger.valueOf(0);
			BigInteger bi;

			// ipInts[0] << 24
			bi = BigInteger.valueOf(ipInts[0]);
			bi = bi.shiftLeft(24);
			ipInt = ipInt.add(bi);

			// ipInts[1] << 16
			bi = BigInteger.valueOf(ipInts[1]);
			bi = bi.shiftLeft(16);
			ipInt = ipInt.add(bi);

			// ipInts[2] << 8
			bi = BigInteger.valueOf(ipInts[2]);
			bi = bi.shiftLeft(8);
			ipInt = ipInt.add(bi);

			// ipInts[3]
			bi = BigInteger.valueOf(ipInts[3]);
			ipInt = ipInt.add(bi);

			return ipInt;
		}

	protected static BigInteger parseAddressIPv6(String addr) throws UnknownHostException {

		/*
		 * Split the address into two lists, one for items on the left of the '::'
		 * the other for the items on the right
		 *
		 *  example: '1080:200C::417A'
		 *		=> leftItems = "1080", "200C"
		 *		=> rightItems = 417A
		 */

		String left = null;
		String right = null;
		String[] saddr = addr.split("::");
		int count = saddr.length;

		if (count > 2)
			// Invalid IPv6, eg. '1::2::'
			throw new UnknownHostException("Invalid IPv6 address: more than one '::' : " + addr);

		if (count == 0) {
			// addr == "::"
			return BigInteger.ZERO;
		}

		if (count == 1) {
			/*
			 * we have to test where is the "::" to know which string is
			 * the left one
			 */
			if (addr.startsWith("::")) {
				left = null;
				right = saddr[0];
			} else {
				left = saddr[0];
				right = null;
			}
		} else {
			left = saddr[0];
			right = saddr[1];
		}

		ArrayList<String> rightItems = new ArrayList<String>();
		ArrayList<String> items = new ArrayList<String>();

		String[] lItems	= null;
		String[] rItems	= null;


		if (left != null)
			lItems = left.split(":");

		if (right != null)
			rItems = right.split(":");

		for (int i = 0; rItems != null && i < rItems.length; i++) {
			String s = rItems[i];
			int p1 = s.indexOf('.');
			if (p1 >= 0)  {
				// IPv6 ending with IPv4 like '::ffff:192.168.0.1'
				if (i != rItems.length - 1)
					// Invalid IPv6: 'ffff:192.168.0.1::'
					throw new UnknownHostException("Invalid IPv6 address: IPv4 only allowed at the tail: " + addr);
				String [] hextets = ipV4ToHextet(s);
				rightItems.add(hextets[0]);
				rightItems.add(hextets[1]);
			} else
				rightItems.add(s);
		}

		for (int i = 0; lItems != null && i < lItems.length; i++) {
			String s = lItems[i];
			if (s != null && !s.isEmpty()) {
				int p1 = s.indexOf('.');
				if (p1 >= 0) {
					// IPv6 ending with IPv4 like '::ffff:192.168.0.1'
					if ((i != lItems.length - 1) || rItems != null)
						// Invalid IPv6: 'ffff:192.168.0.1::'
						throw new UnknownHostException("Invalid IPv6 address: IPv4 only allowed at the tail: " + addr);
					String [] hextets = ipV4ToHextet(s);
					items.add(hextets[0]);
					items.add(hextets[1]);
				} else
					items.add(s);
			}
		}

		// pad with "0" between left and right
		while ((items.size() + rightItems.size()) < 8)
			items.add("0");

		// add right items
		for (String s: rightItems)
			items.add(s);

		/*
		 * Here we have a list of 8 hextets
		 */
		if (items.size() != 8)
			// Invalid IPv6, eg. '1:2:3'
			throw new UnknownHostException("Invalid IPv6 address: should have 8 hextets: " + addr);

		 /*
		  * Convert hextets to BigInteger
		  */
		BigInteger value = BigInteger.valueOf(0);

		for (String item: items)  {
			BigInteger hexlet;
			try {
				hexlet = new BigInteger(item, 16);
			} catch (NumberFormatException e) {
				throw new UnknownHostException("Invalid IPv6 address: invalid hextet: " + addr);
			}
			if ((hexlet.compareTo(BigInteger.ZERO) < 0) || (hexlet.compareTo(IP.BIG_INT_0xFFFF) > 0))
				throw new UnknownHostException("Invalid IPv6 address: invalid hextet: " + addr);
			value = value.multiply(IP.BIG_INT_0x10000);
			value = value.add(hexlet);
		}

		return value;
	}

	private static IPNetParseResult parseAddress(String addr) throws UnknownHostException {
		IPNetParseResult result = new IPNetParseResult();

		try {
			if (addr.startsWith("0x")) {
				BigInteger bi = new BigInteger(addr.substring(2), 16);

				if (!IP.isValidIP(bi, IPversion.IPV6))
					throw new UnknownHostException("IP Address must be 0 <= IP < 2^128: " + addr);

				if (bi.compareTo(IP.MAX_IPV4_NUMBER) <= 0) {
					result.ipInt = bi;
					result.ipVersion = IPversion.IPV4;
					return result;
				} else {
					result.ipInt = bi;
					result.ipVersion = IPversion.IPV6;
					return result;
				}
			}
			if (addr.contains(":")) {
				// IPv6 notation
				result.ipInt = parseAddressIPv6(addr);
				result.ipVersion = IPversion.IPV6;
				return result;
			}
			/*
			 * XXX: not sure if this is a good idea
			 */
			if (addr.length() == 32) {
				// assume IPv6 in pure hexadecimal notation
				result.ipInt = new BigInteger(addr, 16);
				result.ipVersion = IPversion.IPV6;
				return result;
			}

			if (addr.contains(".") || (addr.length() < 4) && Integer.parseInt(addr) < 256) {
				result.ipInt = parseAddressIPv4(addr);
				result.ipVersion = IPversion.IPV4;
				return result;
			}

			/*
			 * we try to interprete it as a decimal digit -
			 * this only works for numbers > 255 ... others
			 * will be interpreted as IPv4 first byte
			 */
			BigInteger bi = new BigInteger(addr);

			if (!IP.isValidIP(bi, IPversion.IPV6))
				throw new UnknownHostException("IP Address must be 0 <= IP < 2^128: " + addr);
			
			if (bi.compareTo(IP.MAX_IPV4_NUMBER) <= 0) {
				result.ipInt = bi;
				result.ipVersion = IPversion.IPV4;
				return result;
			} else {
				result.ipInt = bi;
				result.ipVersion = IPversion.IPV6;
				return result;
			}

		} catch (NumberFormatException e) {
			throw new UnknownHostException("IP Address must contain numbers: " + addr);
		}
	}

	protected void makeIP(BigInteger ip, IPversion ipVersion, int prefixLen) throws UnknownHostException {

		if (!IP.isValidIP(ip, ipVersion))
			throw new UnknownHostException("Invalid IP address 0 <= IP < " +
					((ipVersion == IPversion.IPV4) ? "2^32 :" : "2^128 :") + ip);

		if (!IP.isValidPrefixLen(prefixLen, ipVersion))
			throw new UnknownHostException("Invalid prefix length 0 <= prefix <= " +
					IP.maxPrefixLen(ipVersion) + " :" + ip);

		_ip = ip;
		_prefixLen = prefixLen;
		_ipVersion = ipVersion;
	}

	protected void makeFromIP(String data, String sip)
			throws UnknownHostException {

		IPNetParseResult result = parseAddress(sip);
		makeIP(result.ipInt, result.ipVersion, IP.maxPrefixLen(result.ipVersion));
	}

	protected void makeFromRange(String data, String sfirst, String slast)
			throws UnknownHostException {

		IPNetParseResult first;
		IPNetParseResult last;

		first = parseAddress(sfirst);
		last = parseAddress(slast);
		if (first.ipVersion != IPversion.IPV4)
			throw new UnknownHostException("First-last notation only allowed for IPv4: " + data);
		if (last.ipVersion != IPversion.IPV4)
			throw new UnknownHostException("Last address must be IPv4: " + data);
		if (first.ipInt.compareTo(last.ipInt) > 0)
			throw new UnknownHostException("Last address must be greater than first: " + data);

		// size = last - first
		BigInteger size = new BigInteger(last.ipInt.toByteArray());
		size = size.subtract(first.ipInt);

		// IPV4 only
		_ipVersion = IPversion.IPV4;
		_ip = first.ipInt;
		_prefixLen = 31 - IP.highest1Bits(size);
		/*
		 *  make sure the broadcast is the same as the last ip
		 * otherwise it will return /16 for something like:
		 * 192.168.0.0-192.168.191.255
		 */
		/*
		 * TODO
		 if IP('%s/%s' % (ip, 32-netbits)).broadcast().int() != last:
				raise ValueError, \
					"the range %s is not on a network boundary." % data
		*/
	}

	protected static int getPrefixFromNetmask(String smask, IPversion ipVersion)
			throws UnknownHostException {

		int prefix;
		/*
		 * check if the netmask is like a.b.c.d/255.255.255.0
		 */
		int pos = smask.indexOf('.');
		if (pos >= 0) {
			IPNetParseResult netmask = parseAddress(smask);
			if (netmask.ipVersion != IPversion.IPV4)
				throw new UnknownHostException("Netmask must be IPv4: " + smask);
			if (ipVersion != IPversion.IPV4)
				throw new UnknownHostException("Dot netmask with not an IPv4 address: " + smask);
			prefix = IP.netmaskToPrefixLen(netmask.ipInt);
		} else {
			// cidr notation /n
			try {
				prefix = Integer.parseInt(smask);
			} catch (NumberFormatException e) {
				throw new UnknownHostException("Netmask must contain numbers: " + smask);
			}
			if (!IP.isValidPrefixLen(prefix, ipVersion))
				throw new UnknownHostException("Invalid prefix 0 <= prefix <= " +
					IP.maxPrefixLen(ipVersion) + " :" + smask);
		}
		return prefix;
	}

	protected void makeFromNetMask(String data, String sip, String smask)
			throws UnknownHostException {

		IPNetParseResult result = parseAddress(sip);
		int prefix = getPrefixFromNetmask(smask, result.ipVersion);
		makeIP(result.ipInt, result.ipVersion, prefix);
	}


	/**
	 * Constructs a new {@link IPNet} IP address.
	 * @param ip the {@link BigInteger} IP address as a number
	 * @param ipVersion the {@link IPversion} IP version of this address.
	 * @param prefixLen the prefixlen for this IP address.
	 * @throws UnknownHostException if some parameters are invalid
	 */
	public IPNet(BigInteger ip, IPversion ipVersion, int prefixLen) throws UnknownHostException {

		makeIP(new BigInteger(ip.toByteArray()), ipVersion, prefixLen);
	}

	/**
	 * Constructs a new {@link IPNet} object as a single IP with a prefixlen set
	 * to 32 or 128 according to the IP version.
	 * @param ip the {@link BigInteger} IP adress as a number.
	 * @param ipVersion the {@link IPversion} IP version of this address.
	 * @throws UnknownHostException if some parameters are invalid
	 */
	public IPNet(BigInteger ip, IPversion ipVersion) throws UnknownHostException {

		makeIP(new BigInteger(ip.toByteArray()), ipVersion, IP.maxPrefixLen(ipVersion));
	}

	/**
	 * Constructs a new {@link IPNet} object according to the string data.
	 * <br/><br/>
	 * Several notations are supported:
	 * <ul>
	 *	<li>Decimal: IPNet("n")<br/>
	 *		n &lt= 255 -> assumed to be the IPv4 address n.0.0.0<br/>
	 *			example 127 -> 127.0.0.0</li>
	 * <li> n &gt= 256 -> assumed to be the address 'n'.<br/>
	 *		If n is less than 2^32 theIPversion is set to IPv4, else to IPv6.</li>
	 * <li>
	 *	Hexadecimal: IPNet("0xn")<br/>
	 *		the number n is converted to the address ip as a number<br/></li>
	 *<li>
	 *  Dot notation (IPv4) : 'IPNet("x.y.w.z")<br/>
	 *		if less than 4 bytes are specified, it is padded with some "0"<br/>
	 *			ex: 127.0 -> 127.0.0.0</li>
	 *<li>
	 * Ipv6:
	 *		IPv6 notation is supported, as well as an IPv4 address specified<br/>
	 *		at the tail of the string<br/>
	 *			ex: IPNet("::1:127.0.0.1")</li>
	 * <li>
	 *  Netmask and prefix<br/>
	 *		IPv4: /n or /w.x.y.z<br/>
	 *		IPv6: /n only</li>
	 * <li>
	 *	Range<br/>
	 *		a range between two addresses can be specified, the first IP address
	 * is used as the address of the {@link IPNet} ip and the second is used
	 * to compute the prefixlen. This is only supported for IPv4.<br/>
	 *			ex: IPNet("0.0.0.0-255.255.255.255)
	 * </li></ul>
	 * @param data The {@link String} string to parse.
	 * @throws UnknownHostException if some parameters are invalid or if we
	 * can't parse the string.
	 */
	public IPNet(String data) throws UnknownHostException {

		// splitting of a string into IP and prefixlen et. al.
		String[] split = data.split("-");
        if (split.length > 2)
			throw new UnknownHostException("Only one '-' allowed in IP Address: " + data);

		if (split.length == 2) {
			// a.b.c.0-a.b.c.255 specification ?
			makeFromRange(data, split[0], split[1]);
			return;
		}
		if (split.length == 1) {
			split = data.split("/");
			// netmask specification ?
			if (split.length > 2)
				throw new UnknownHostException("Only one '/' allowed in IP Address: " + data);

			if (split.length == 1) {
				// no prefix given, use defaults
				makeFromIP(data, split[0]);
				return;
			} else {
				makeFromNetMask(data, split[0], split[1]);
				return;
			}
		}
		throw new UnknownHostException("Can't parse IP Address: " + data);
	}

	/**
	 * Given the name of a host, returns a list of its IP addresses
	 * matching the IP version in argument.
	 * If the name of the host contains a mask, the mask is applied to the
	 * resulting ip addresses.
	 * @param hostname the name of the host
	 * @param ipVersion the IP version of the addresses to return.
	 * @return a list of all the IP addresses for a given host name.
	 * @throws UnknownHostException if no IP address for the host could be
	 *  found, or if a scope_id was specified for a global IPv6 address.
	 * @throws SecurityException if a security manager exists and its
	 * checkConnect method doesn't allow the operation.
	 * @see InetAddress
	 */
	public static List<IPNet> getAllByName(String hostname, IPversion ipVersion)
			throws UnknownHostException {

		String split [] = hostname.split("/");
		int prefix = -1;

		// netmask specification ?
		if (split.length > 2)
			throw new UnknownHostException("Only one '/' allowed in IP Address: " +
				hostname);

		if (split.length == 2)
			prefix = getPrefixFromNetmask(split[1], ipVersion);

		InetAddress inet [] = InetAddress.getAllByName(split[0]);
		ArrayList<IPNet> addresses = new ArrayList<IPNet>();
		for (InetAddress addr: inet) {
			if ((addr instanceof Inet4Address && ipVersion == IPversion.IPV4) ||
				(addr instanceof Inet6Address && ipVersion == IPversion.IPV6)) {
				IPNet address = new IPNet(addr.getHostAddress());
				if (prefix != -1)
					address = address.setMask(prefix);
				addresses.add(address);
			}
		}
		if (addresses.isEmpty())
			throw new UnknownHostException(split[0]);
		return addresses;
	}

	/**
	 * Given the name of a host, returns its IP address matching the IP
	 * version in argument.
	 * If the name of the host contains a mask, the mask is applied to the
	 * resulting ip address.
	 * @param hostname the name of the host
	 * @param ipVersion the IP version of the address to return.
	 * @return the IP address for a given host name.
	 * @throws UnknownHostException if no IP address for the host could be
	 *  found, or if a scope_id was specified for a global IPv6 address.
	 * @throws SecurityException if a security manager exists and its
	 * checkConnect method doesn't allow the operation.
	 * @see InetAddress
	 */
	public static IPNet getByName(String hostname, IPversion ipVersion)
			throws UnknownHostException {

		return getAllByName(hostname, ipVersion).get(0);
	}

	/**
	 * Returns a copy of an {@link IPNet} instance.
	 * @param ipnet the {@link IPNet} instance to copy
	 * @return a copy of the {@link IPNet} ipnet object.
	 * @throws UnknownHostException
	 */
	public static IPNet newInstance(IPNet ipnet) throws UnknownHostException {
		return new IPNet(ipnet.getIP(), ipnet.getIpVersion(), ipnet.getPrefixLen());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final IPNet other = (IPNet) obj;
		if (_prefixLen != other._prefixLen) {
			return false;
		}
		if (_ip != other._ip && (_ip == null || !_ip.equals(other._ip))) {
			return false;
		}
		if (_ipVersion != other._ipVersion && (_ipVersion == null || !_ipVersion.equals(other._ipVersion))) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 97 * hash + _prefixLen;
		hash = 97 * hash + (_ip != null ? _ip.hashCode() : 0);
		hash = 97 * hash + (_ipVersion != null ? _ipVersion.hashCode() : 0);
		return hash;
	}

	/**
	 * Compares the {@link IPversion} IP version of this instance to another
	 * {@link IPNet} object.
	 * @param ipnet the {@link IPNet} object to compare.
	 * @return true if the {@link IPversion} IP versions are equal.
	 */
	public boolean sameIPVersion(IPNet ipnet) {
		return _ipVersion.equals(ipnet.getIpVersion());
	}

	/**
	 * Checks if this {@link IPNet} instance contains another {@link IPNet} object.<br/>
	 * An {@link IPNet} object contains another {@link IPNet} object if all the
	 * IP addresses designated by the second {@link IPNet} object are included
	 * in the first {@link IPNet} object.
	 * @param ipnet IPNet object to compare.
	 * @return true if all the IP addresses of the {@link IPNet} ipnet object are
	 * included in this instance.
	 */
	public boolean networkContains(IPNet ipnet) throws UnknownHostException {
		IPNet first = networkAddress();
		IPNet last  = broadcastAddress();
		IPNet firstOther = ipnet.networkAddress();
		IPNet lastOther = ipnet.broadcastAddress();

		return firstOther.isBetweenIP(first, last) && lastOther.isBetweenIP(first, last);
	}

	/**
	 * Checks if this {@link IPNet} instance overlaps the IPNet object in
	 * argument. Two IPNet objects overlap if they share at least one IP address.
	 * @param ipnet IPNet object to compare.
	 * @return true if this IPNet instance overlaps the IPNet object in argument.
	 */
	public boolean overlaps(IPNet ipnet) throws UnknownHostException {
		IPNet first = networkAddress();
		IPNet last  = broadcastAddress();
		IPNet firstOther = ipnet.networkAddress();
		IPNet lastOther = ipnet.broadcastAddress();

		return first.isBetweenIP(firstOther, lastOther) || 
				last.isBetweenIP(firstOther, lastOther) || 
				firstOther.isBetweenIP(first, last) ||
				lastOther.isBetweenIP(first, last);
	}

	/**
	 * Checks if this {@link IPNet} instance is between two another
	 * {@link IPNet} objects.<br/>
	 * @param first the first {@link IPNet} object to compare.
	 * @param second the second {@link IPNet} object to compare.
	 * @return true if the {@link BigInteger} IP address of this instance is:
	 * first &lt= IP &lt= second.
	 * We do not take care of the prefix length of the {@link IPNet} objects.
	 */
	public boolean isBetweenIP(IPNet first, IPNet second) {
		if (_ip.compareTo(first.getIP()) < 0)
			return false;
		if (_ip.compareTo(second.getIP()) > 0)
			return false;
		return true;
	}

	/**
	 * Returns the network IP address of this {@link IPNet} instance.
	 * @return the {@link IPNet} network IP address.
	 * @throws UnknownHostException if this instance can not be expressed as a
	 * network IP address.
	 */
	public IPNet networkAddress() throws UnknownHostException {
		BigInteger bi = IP.prefixLenToNetmask(_prefixLen, _ipVersion);
		bi = _ip.and(bi);
		return new IPNet(bi, _ipVersion, _prefixLen);
	}

	/**
	 * Returns the host IP address of this {@link IPNet} instance.
	 * The host address is expressed by setting the prefixlen to 32 or 128
	 * according to the IP version of the instance.
	 * @return the {@link IPNet} host address.
	 * @throws UnknownHostException
	 */
	public IPNet hostAddress() throws UnknownHostException {
		return new IPNet(_ip, _ipVersion, IP.maxPrefixLen(_ipVersion));
	}

	/**
	 * Returns the broadcast IP address of this {@link IPNet} instance.
	 * @return the {@link IPNet} broadcast IP address of the network associated
	 * to this instance.
	 * @throws UnknownHostException if this instance can not be expressed as a
	 * network.
	 */
	public IPNet broadcastAddress() throws UnknownHostException {
		IPNet net = networkAddress();
		BigInteger bi = net.getIP().add(IP.networkLength(_prefixLen, _ipVersion));
		bi = bi.subtract(BigInteger.ONE);
		return new IPNet(bi, _ipVersion, _prefixLen);
	}

	/**
	 * Returns a new IP address of this {@link IPNet} instance with the mask
	 * set to the value in argument.
	 * @param mask mask to set in cidr notation.
	 * @return a new {@link IPNet} instance.
	 * @throws UnknownHostException
	 */
	public IPNet setMask(int mask) throws UnknownHostException {
		return new IPNet(_ip, _ipVersion, mask);
	}

	/**
	 * Tests if this {@link IPNet} instance is a single host IP address.
	 * @return true if this {@link IPNet} instance is a single host IP address.
	 * @throws UnknownHostException
	 */
	public boolean isHost() throws UnknownHostException {
		return _prefixLen == IP.maxPrefixLen(_ipVersion);
	}

	/**
	 * The "null" network for IPv4 (0.0.0.0/0)
	 */
	public static IPNet NULL_IPV4 = null;

	/**
	 * The "null" network for IPv6 (::0/0)
	 */
	public static IPNet NULL_IPV6 = null;

	static {
		try {
			NULL_IPV4 = new IPNet(BigInteger.ZERO, IPversion.IPV4, 0);
			NULL_IPV6 = new IPNet(BigInteger.ZERO, IPversion.IPV6, 0);
		} catch (UnknownHostException ex) {
			// should not happen
		}
	}

	/**
	 * Checks if the {@link IPversion} IP version of this {@link IPNet} instance is IPv4.
	 * @return true if this {@link IPNet} instance is an IPv4 address.
	 */
	public boolean isIPv4() {
		return _ipVersion == IPversion.IPV4;
	}

	/**
	 * Checks if this {@link IPNet} instance designates the "null" network.<br/>
	 * The "null" network is 0.0.0.0/0 for IPv4 and ::0/0 for IPv6
	 * @return true if this {@link IPNet} instance designates the "null" network.
	 */
	public boolean isNullNetwork() {
		return (_ipVersion == IPversion.IPV4) ? equals(NULL_IPV4) : equals(NULL_IPV6);
	}

	/**
	 * Checks if the {@link IPversion} IP version of this {@link IPNet} instance is IPv6.
	 * @return true if this {@link IPNet} instance is an IPv6 address.
	 */
	public boolean isIPv6() {
		return _ipVersion == IPversion.IPV6;
	}

	/**
	 * Returns the {@link IPversion} IP version of this {@link IPNet} instance.
	 * @return the {@link IPversion} IP version of this {@link IPNet} instance.
	 */
	public IPversion getIpVersion() {
		return _ipVersion;
	}

	/**
	 * Returns the prefix length of this {@link IPNet} instance.
	 * @return the prefix length of this {@link IPNet} instance.
	 */
	public int getPrefixLen() {
		return _prefixLen;
	}

	/**
	 * Returns the {@link BigInteger} IP address of this {@link IPNet} instance
	 * as a number.
	 * @return the {@link BigInteger} IP address of this {@link IPNet} instance.
	 */
	public BigInteger getIP() {
		BigInteger bi = new BigInteger(_ip.toByteArray());
		return bi;
	}

	/**
	 * Returns the length of this {@link IPNet} instance. The length is the
	 * number of IP addresses associates to the network designated by the
	 * {@link IPNet} instance.<br/><br/>
	 * examples:<br/>
	 * IPNet("192.0.0.1/24") =&gt 256 addresses.<br/>
	 * IPNet("192.0.0.1/32") =&gt 1 address.<br/>
	 * @return the {@link BigInteger} number of IP addresses associates to the
	 * network designated by the {@link IPNet} instance.
	 */
	public BigInteger networkLength() {
		return IP.networkLength(_prefixLen, _ipVersion);
	}

	@Override
	public String toString() {
		return toString("");
	}

	/**
	 * Returns a {@link String} representation of this {@link IPNet} instance
	 * according to the {@link String} format.<br/><br/>
	 * Formats:<ul>
	 * <li>'i': (ip) do not output the prefix length.</li>
	 * <li>'n': (netmask use a netmask representation to output the prefix
	 * length (IPv4 only).</li>
	 * <li>'s': (short) never output the prefix length.</li>
	 * <li>'::' (compress) compress the output for IPv6 address.</li>
	 * </ul>
	 * @param format Format string
	 * @return a {@link String} representation of this {@link IPNet} instance.
	 */
	public String toString(String format) {
		boolean fshort = format.contains("s");
		boolean fcompress = format.contains("::");
		boolean fnetmask = format.contains("n");
		boolean fip = format.contains("i");

		String[] s = null;
		switch (_ipVersion) {
			case IPV4:
				s = IP.ipv4ToStrings(_ip, _prefixLen);
				if (fnetmask) {
					BigInteger netmask = IP.prefixLenToNetmask(_prefixLen, _ipVersion);
					String [] ss = IP.ipv4ToStrings(netmask, 32);
					s[1] = ss[0];
				}
				break;
			case IPV6:
				s = IP.ipv6ToStrings(_ip, _prefixLen, fcompress);
				break;
		}
		String result = s[0];
		if (!(fshort || (fip && _prefixLen == IP.maxPrefixLen(_ipVersion))))
			result = result + "/" + s[1];
		return result;
	}

	public int compareTo(Object o) {
		IPNet obj = (IPNet) o;
		if (equals(obj))
			return 0;
		return getIP().compareTo(obj.getIP());
	}
}

class IPNetParseResult {
	BigInteger ipInt;
	IPversion ipVersion;
}
