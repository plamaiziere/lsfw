/*
 * Copyright (c) 2010, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.lib.ip;

/**
 * Port operators.
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public enum PortOperator {
	/**
	 * Operator "ANY"
	 */
	ANY,

	/**
	 * Operator "NONE"
	 */
	NONE,

	/**
	 * Operator equal
	 */
	EQ,

	/**
	 * Operator not equal
	 */
	NEQ,

	/**
	 * Operator less than
	 */
	LT,

	/**
	 * Operator less than or equal
	 */
	LTE,

	/**
	 * Operator greater than
	 */
	GT,

	/**
	 * Operator greater than or equal
	 */
	GTE,

	/**
	 * Operator range including boundaries
	 */
	RANGE,

	/**
	 * Operator range, excluding boundaries
	 */
	RANGEEX,

	/**
	 * Operator exclude range
	 */
	EXCLUDE

}
