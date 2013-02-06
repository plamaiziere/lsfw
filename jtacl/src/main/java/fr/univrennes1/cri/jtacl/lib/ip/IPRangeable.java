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
 * interface for IP range. A range of ip addresses
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public interface IPRangeable {

	/**
	 * Checks if this range instance contains an {@link IPNet} object.<br/>
	 * A range object contains another {@link IPNet} object if all the
	 * IP addresses designated by the second {@link IPNet} object are included
	 * in this range.
	 * @param ipnet IPNet object to compare.
	 * @return true if all the IP addresses of the {@link IPNet} ipnet object are
	 * included in this instance.
	 */
	boolean contains(IPNet ipnet);

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
	 * Checks if this range instance overlaps the IPNet object in
	 * argument. The range overlaps if they share at least one IP address.
	 * @param ipnet IPNet object to compare.
	 * @return true if this range instance overlaps the IPNet object in argument.
	 */
	boolean overlaps(IPNet ipnet);

}
