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
public class IPRange {

	protected IPNet _ipFirst;
	protected IPNet _ipLast;

	/**
	 * Constructs a new range using the two IP address in argument
	 * @param ipFirst first IP address
	 * @param ipLast last IP address
	 * @throws UnknownHostException  if some parameters are invalid.
	 */
	public IPRange(IPNet ipFirst, IPNet ipLast) throws UnknownHostException {
		_ipFirst = ipFirst.networkAddress();
		_ipLast = ipLast.networkAddress();
	}

	/**
	 * Constructs a new range using the the IPNet address in argument.
	 * The new range is between the host ip address of the given ipnet address
	 * and the lastNetworkAddress of the given ipnet address.
	 * @param ipnet ipnet address to use.
	 * @throws UnknownHostException if some parameters are invalid.
	 */
	public IPRange(IPNet ipnet) throws UnknownHostException {
		_ipFirst = ipnet.networkAddress();
		_ipLast = ipnet.lastNetworkAddress();
	}

	/**
	 * Constructs a new range using the the IPNet address in argument.
	 * The new range is between the host ip address of the given ipnet address
	 * and the lastNetworkAddress of the given ipnet address. If
	 * includeLastAddress is true, the last network address is included.
	 * @param ipnet ipnet address to use.
	 * @param includeLastAddress set to true to include the last network address.
	 * @throws UnknownHostException if some parameters are invalid.
	 */
	public IPRange(IPNet ipnet, boolean includeLastAddress) throws UnknownHostException {
		_ipFirst = ipnet.networkAddress();
		if (includeLastAddress) {
			_ipLast = ipnet.lastNetworkAddress();
		} else {
			_ipLast = ipnet.lastNetworkAddress();
			BigInteger lip = _ipLast.getIP();
			lip = lip.subtract(BigInteger.ONE);
			_ipLast = new IPNet(lip, ipnet.getIpVersion());
		}
	}

	public IPNet getIpFirst() {
		return _ipFirst;
	}

	public IPNet getIpLast() {
		return _ipLast;
	}

/**
	 * Checks if this range instance contains an {@link IPNet} object.<br/>
	 * A range object contains another {@link IPNet} object if all the
	 * IP addresses designated by the second {@link IPNet} object are included
	 * in this range.
	 * @param ipnet IPNet object to compare.
	 * @return true if all the IP addresses of the {@link IPNet} ipnet object are
	 * included in this instance.
	 */
	public final boolean contains(IPNet ipnet)
			throws UnknownHostException {

		IPNet firstOther = ipnet.networkAddress();
		IPNet lastOther = ipnet.lastNetworkAddress();

		return firstOther.isBetweenIP(_ipFirst, _ipLast) &&
				lastOther.isBetweenIP(_ipFirst, _ipLast);
	}

	/**
	 * Checks if this range instance overlaps the IPNet object in
	 * argument. The range overlaps if they share at least one IP address.
	 * @param ipnet IPNet object to compare.
	 * @return true if this range instance overlaps the IPNet object in argument.
	 */
	public final boolean overlaps(IPNet ipnet) throws UnknownHostException {
		IPNet firstOther = ipnet.networkAddress();
		IPNet lastOther = ipnet.lastNetworkAddress();

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
