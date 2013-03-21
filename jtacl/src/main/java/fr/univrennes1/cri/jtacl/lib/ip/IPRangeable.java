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

/**
 * interface for IP range. A range of ip addresses
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public interface IPRangeable {

	/**
	 * Checks if this range instance contains a range.<br/>
	 * A range object contains another range object if all the
	 * IP addresses designated by the second range object are included
	 * in this range.
	 * @param iprange Range object to compare.
	 * @return true if all the IP addresses of the range object are
	 * included in this instance.
	 */
	boolean contains(IPRangeable iprange);

	/**
	 * Returns the first IP address of this range.
	 * @return the first IP address of this range.
	 */
	IPNet getIpFirst();

	/**
	 * Returns the last IP address of this range.
	 * @return the last IP address of this range.
	 */
	IPNet getIpLast();

	/**
	 * Checks if this range instance overlaps the range in argument.
	 * The range overlaps if they share at least one IP address.
	 * @param iprange IPRangeable object to compare.
	 * @return true if this range instance overlaps the range in argument.
	 */
	boolean overlaps(IPRangeable iprange);

	/**
	 * Returns an IPNet representation of this range or null if the range
	 * is not on a network boundary.
	 * @return an IPNet representation of this range.
	 */
	IPNet toIPNet();

	/**
	 * Returns a {@link String} representation of this range
	 * according to the {@link String} format.<br/><br/>
	 * Formats:<ul>
	 * <li>'i': (ip) do not output the prefix length if this a host.</li>
	 * <li>'n': (netmask use a netmask representation to output the prefix
	 * length (IPv4 only).</li>
	 * <li>'s': (short) never output the prefix length.</li>
	 * <li>'::' (compress) compress the output for IPv6 address.</li>
	 * </ul>
	 * @param format Format string
	 * @return a String representation of this range.
	 */
	public String toString(String format);

	/**
	 * Tests if this instance is a single host IP address.
	 * @return true if this instance is a single host IP address.
	 */
	public boolean isHost();

	/**
	 * Tests if this instance can be expressed as a network.
	 * @return if this instance can be expressed as a network.
	 */
	public boolean isNetwork();

	/**
	 * Returns a String representation of this range according
	 * to the String format. If this range is a network, displays it as a
	 * network.
	 * @return a String representation of this range according
	 * to the String format.
	 */
	public String toNetString(String format);

	/**
	 * Returns the nearest network including this range.
	 * to the String format. If this range is a network, displays it as a
	 * network.
	 * @return the nearest network including this range.
	 */
	public IPNet nearestNetwork();

	/**
	 * Returns the IP version of this range.
	 * @return the IP version of this range.
	 */
	public IPversion getIpVersion();

	/**
	 * Checks if the {@link IPversion} IP version of this range is IPv4.
	 * @return true if this range is an IPv6 range.
	 */
	public boolean isIPv4();

	/**
	 * Checks if the {@link IPversion} IP version of this range is IPv6.
	 * @return true if this range is an IPv6 range.
	 */
	public boolean isIPv6();

	/**
	 * Compares the {@link IPversion} IP version of this instance to another
	 * range.
	 * @param range the range object to compare.
	 * @return true if the {@link IPversion} IP versions are equal.
	 */
	public boolean sameIPVersion(IPRangeable range);

	/**
	 * Returns the length of this range.
	 * @return the length of this range.
	 */
	public BigInteger length();
}
