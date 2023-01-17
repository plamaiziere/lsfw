/*
 * Copyright (c) 2010, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.equipments.cisco.pix;

/**
 * Types of Object Group.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public enum ObjectGroupType {
	/**
	 * network object-group
	 */
	NETWORK,
	/**
	 * service object-group
	 */
	SERVICE,
	/**
	 * enhanced service object-group
	 */
	ENHANCED,
	/**
	 * protocol object-group
	 */
	PROTOCOL,
	/**
	 * icmp object-group
	 */
	ICMP
}
