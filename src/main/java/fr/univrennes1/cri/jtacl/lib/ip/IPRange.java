/*
 * Copyright (c) 2013, Universite de Rennes 1
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

/**
 * IP range. A range of ip addresses
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class IPRange implements IPRangeable {

	protected IPNet _ipFirst;
	protected IPNet _ipLast;
	protected IPNet _nearestNetwork;
	protected Boolean _isNetwork;
	protected IPNet _toIpNet;

	/**
	 * Constructs a new range using the two IP network addresses in argument
	 * @param ipFirst first IP address
	 * @param ipLast last IP address
	 */
	public IPRange(IPNet ipFirst, IPNet ipLast) {
		_ipFirst = ipFirst.networkAddress();
		_ipLast = ipLast.networkAddress();
	}

	/**
	 * Constructs a new range using the IPNet address in argument.
	 * The new range is between the network ip address of the given ipnet address
	 * and the lastNetworkAddress of the given ipnet address.
	 * @param ipnet ipnet address to use.
	 */
	public IPRange(IPNet ipnet) {
		_ipFirst = ipnet.networkAddress();
		_ipLast = ipnet.lastNetworkAddress();
	}

	/**
	 * Constructs a new range using the IPNet address in argument.
	 * The new range is between the network ip address of the given ipnet address
	 * and the lastNetworkAddress of the given ipnet address. If
	 * includeLastAddress is true, the last network address is included.
	 * @param ipnet ipnet address to use.
	 * @param includeLastAddress set to true to include the last network address.
	 */
	public IPRange(IPNet ipnet, boolean includeLastAddress) {
		_ipFirst = ipnet.networkAddress();
		if (ipnet.isHost()) {
			_ipLast = ipnet;
		} else {
			if (includeLastAddress) {
				_ipLast = ipnet.lastNetworkAddress();
			} else {
				_ipLast = ipnet.lastNetworkAddress();
				BigInteger lip = _ipLast.getIP();
				lip = lip.subtract(BigInteger.ONE);
				try {
					_ipLast = new IPNet(lip, ipnet.getIpVersion());
				} catch (UnknownHostException ex) {
					// could not happen
				}
			}
		}
	}

	/**
	 * Constructs a new range object according to the string data.
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
	 * to compute the prefixlen.<br/>
	 *			ex: IPNet("0.0.0.0-255.255.255.255)
	 * </li>
	 * <li>
	 * Name resolution<br/>
	 *   A data string starting with '@' or '@@' specifies a host name to
	 *   resolve using dns. The number of '@' characters specifies the address
	 *   family to use (IPv4 or IPv6). <br/>
	 *		ex : @localhost/34 (returns 127.0.0.1/24). <br/>
	 *		ex : @@localhost (returns ::1/128). <br/>
	 * </ul>
	 * @param data The {@link String} string to parse.
	 * @throws UnknownHostException if some parameters are invalid or if we
	 * can't parse the string.
	 */
	public IPRange(String data) throws UnknownHostException {

		// splitting of a string into IP and prefixlen et. al.
		String[] split = data.split("-");
        if (split.length > 2)
			throw new UnknownHostException(
				"Only one '-' allowed in IP Address: " + data);

		if (split.length == 2) {
			_ipFirst = new IPNet(split[0].trim());
			_ipLast = new IPNet(split[1].trim());
		} else {
			_ipFirst = new IPNet(data).networkAddress();
			_ipLast = _ipFirst.lastNetworkAddress();
		}

		if (_ipFirst.getIP().compareTo(_ipLast.getIP()) > 0)
			throw new UnknownHostException(
				"last address must be greater than the first: " + data);
	}

	@Override
	public IPNet getIpFirst() {
		return _ipFirst;
	}

	@Override
	public IPNet getIpLast() {
		return _ipLast;
	}

	@Override
	public final boolean contains(IPRangeable iprange) {

		IPNet firstOther = iprange.getIpFirst();
		IPNet lastOther = iprange.getIpLast();

		return firstOther.isBetweenIP(_ipFirst, _ipLast) &&
				lastOther.isBetweenIP(_ipFirst, _ipLast);
	}

	@Override
	public final boolean overlaps(IPRangeable iprange) {
		IPNet firstOther = iprange.getIpFirst();
		IPNet lastOther = iprange.getIpLast();

		return _ipFirst.isBetweenIP(firstOther, lastOther) ||
				_ipLast.isBetweenIP(firstOther, lastOther) ||
				firstOther.isBetweenIP(_ipFirst, _ipLast) ||
				lastOther.isBetweenIP(_ipFirst, _ipLast);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(_ipFirst.toString("i::"))
				.append("-")
				.append(_ipLast.toString("i::"));
		return sb.toString();
	}

	@Override
	public String toString(String format) {
		StringBuilder sb = new StringBuilder(_ipFirst.toString(format))
				.append("-")
				.append(_ipLast.toString(format));
		return sb.toString();
	}

	@Override
	public String toNetString(String format) {
		IPNet ip;
		String r;
		if (isNetwork()) {
			ip = toIPNet();
			r = ip.toString("i::");
		} else {
			r = toString("i::");
		}
		return r;
	}

	@Override
	public IPNet toIPNet() {

		if (_isNetwork != null) {
			return _toIpNet;
		}

		// size = last - first
		BigInteger size = _ipLast.getIP();
		size = size.subtract(_ipFirst.getIP());

		BigInteger ip = _ipFirst.getIP();
		int len = IP.maxPrefixLen(_ipFirst.getIpVersion()) - 1;
		int prefixLen = len - IP.highest1Bits(size);
		IPBase ipbase = new IPBase(ip, prefixLen, _ipFirst.getIpVersion());

		/*
		 *  make sure the network is the same as the first ip
		 * otherwise it will return /24 for something like:
		 * 192.168.0.1-192.168.1.255
		 */
		IPNet checkboundary = new IPNet(ipbase).networkAddress();
		if (!checkboundary.getIP().equals(_ipFirst.getIP())) {
			_isNetwork = false;
			return null;
		}
		/*
		 *  make sure the broadcast is the same as the last ip
		 * otherwise it will return /16 for something like:
		 * 192.168.0.0-192.168.191.255
		 */
		checkboundary = new IPNet(ipbase).lastNetworkAddress();
		if (!checkboundary.getIP().equals(_ipLast.getIP())) {
			_isNetwork = false;
			return null;
		}
		_isNetwork = true;
		_toIpNet = new IPNet(ipbase);
		return _toIpNet;
	}

	@Override
	public final boolean isHost() {
		return _ipFirst.getIP().compareTo(_ipLast.getIP()) == 0;
	}

	@Override
	public final boolean isNetwork() {
		if (_isNetwork == null)
			return toIPNet() != null;
		return _isNetwork;
	}

	@Override
	public IPNet nearestNetwork() {

		if (_nearestNetwork != null)
			return _nearestNetwork;

		int len = IP.maxPrefixLen(_ipFirst.getIpVersion());

		for (int l = len; l >= 0; l--) {
			IPNet ip = null;
			try {
				ip = new IPNet(_ipFirst.getIP(), _ipFirst.getIpVersion(), l);
			} catch (UnknownHostException ex) {
				// should not happen
			}
			if (ip.contains(_ipFirst) && ip.contains(_ipLast)) {
				_nearestNetwork = ip.networkAddress();
				return _nearestNetwork;
			}
		}
		// not reached
		return null;
	}

	@Override
	public IPversion getIpVersion() {
		return _ipFirst.getIpVersion();
	}

	@Override
	public final boolean isIPv4() {
		return _ipFirst.isIPv4();
	}

	@Override
	public final boolean isIPv6() {
		return _ipFirst.isIPv6();
	}

	@Override
	public final boolean sameIPVersion(IPRangeable range) {
		return getIpVersion().equals(range.getIpVersion());
	}

	@Override
	public final BigInteger length() {
		return _ipLast.getIP().subtract(_ipFirst.getIP()) ;
	}
}
