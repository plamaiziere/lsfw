/*
 * Copyright (c) 2010, Universite de Rennes 1
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

package fr.univrennes1.cri.jtacl.core.monitor;

/**
 * Port operators.
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public enum ProbePortOperator {
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
