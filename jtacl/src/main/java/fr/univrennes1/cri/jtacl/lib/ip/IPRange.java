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

	/**
	 * Constructs a new range using the two IP addresses in argument
	 * @param ipFirst first IP address
	 * @param ipLast last IP address
	 */
	public IPRange(IPNet ipFirst, IPNet ipLast) {
		_ipFirst = ipFirst.networkAddress();
		_ipLast = ipLast.networkAddress();
	}

	/**
	 * Constructs a new range using the IPNet address in argument.
	 * The new range is between the host ip address of the given ipnet address
	 * and the lastNetworkAddress of the given ipnet address.
	 * @param ipnet ipnet address to use.
	 */
	public IPRange(IPNet ipnet) {
		_ipFirst = ipnet.networkAddress();
		_ipLast = ipnet.lastNetworkAddress();
	}

	/**
	 * Constructs a new range using the IPNet address in argument.
	 * The new range is between the host ip address of the given ipnet address
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

}
