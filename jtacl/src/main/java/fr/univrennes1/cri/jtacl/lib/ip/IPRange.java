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
		return _ipFirst.toString("i::") + "-" + _ipLast.toString("i::");
	}

	@Override
	public String toString(String format) {
		return _ipFirst.toString(format) + "-" + _ipLast.toString(format);
	}

	@Override
	public String toNetString(String format) {
		IPNet ip;
		String r;
		try {
			ip = toIPNet();
			r = ip.toString("i::");
		} catch (UnknownHostException ex) {
			r = toString("i::");
		}
		return r;
	}

	@Override
	public IPNet toIPNet() throws UnknownHostException {

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
		if (!checkboundary.getIP().equals(_ipFirst.getIP()))
			throw new UnknownHostException(
					"Range is not on a network boundary");
		/*
		 *  make sure the broadcast is the same as the last ip
		 * otherwise it will return /16 for something like:
		 * 192.168.0.0-192.168.191.255
		 */
		checkboundary = new IPNet(ipbase).lastNetworkAddress();
		if (!checkboundary.getIP().equals(_ipLast.getIP()))
			throw new UnknownHostException(
					"Range is not on a network boundary");
		return new IPNet(ipbase);
	}

	@Override
	public boolean isHost() {
		return _ipFirst.getIP().compareTo(_ipLast.getIP()) == 0;
	}

	@Override
	public IPNet nearestNetwork() {

		if (_nearestNetwork != null)
			return _nearestNetwork;

		int len = IP.maxPrefixLen(_ipFirst.getIpVersion());

		for (int l = len; l > 0; l--) {
			IPNet ip = null;
			try {
				ip = new IPNet(_ipFirst.getIP(), _ipFirst.getIpVersion(), l);
			} catch (UnknownHostException ex) {
				// should not happen
			}
			if (ip.contains(_ipFirst) && ip.contains(_ipLast)) {
				_nearestNetwork = ip;
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
}
