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

import java.net.UnknownHostException;

/**
 * Comparable interface for IP addresses.
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public interface IPComparable {

	/**
	 * Checks if this instance is between two another
	 * {@link IPNet} objects.<br/>
	 * @param first the first {@link IPNet} object to compare.
	 * @param second the second {@link IPNet} object to compare.
	 * @return true if the {@link BigInteger} IP address of this instance is:
	 * first &lt= IP &lt= second.
	 * We do not take care of the prefix length of the {@link IPNet} objects.
	 */
	boolean isBetweenIP(IPNet first, IPNet second);

	/**
	 * Checks if this instance contains an {@link IPNet} object.<br/>
	 * An {@link IPNet} object contains another {@link IPNet} object if all the
	 * IP addresses designated by the second {@link IPNet} object are included
	 * in the first {@link IPNet} object.
	 * @param ipnet IPNet object to compare.
	 * @return true if all the IP addresses of the {@link IPNet} ipnet object are
	 * included in this instance.
	 */
	boolean contains(IPNet ipnet) throws UnknownHostException;

	/**
	 * Checks if this instance overlaps the IPNet object in
	 * argument. Two IPNet objects overlap if they share at least one IP address.
	 * @param ipnet IPNet object to compare.
	 * @return true if this IPNet instance overlaps the IPNet object in argument.
	 */
	boolean overlaps(IPNet ipnet) throws UnknownHostException;

}
