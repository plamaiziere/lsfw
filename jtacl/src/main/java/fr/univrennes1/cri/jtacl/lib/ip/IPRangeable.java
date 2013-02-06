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

}
