/*
 * Copyright (c) 2010, Université de Rennes 1
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

package fr.univrennes1.cri.jtacl.equipments.openbsd;

/**
 * Describes a Port Item.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PfPortItem {

	/**
	 * Operator equal
	 */
	public final String EQ = "=";

	/**
	 * Operator not equal
	 */
	public final String NEQ = "!=";

	/**
	 * Operator less than
	 */
	public final String LT = "<";

	/**
	 * Operator less than or equal
	 */
	public final String LTE = "<=";

	/**
	 * Operator greater than
	 */
	public final String GT = ">";

	/**
	 * Operator greater than or equal
	 */
	public final String GTE = ">=";

	/**
	 * Operator range including boundaries
	 */
	public final String RANGE = ":";

	/**
	 * Operator range, excluding boundaries
	 */
	public final String RANGEEX = "><";

	/**
	 * Operator except range
	 */
	public final String EXCEPT = "<>";

	/**
	 * Current operator
	 */
	protected String _operator;

	/**
	 * first port
	 */
	protected int _firstPort;

	/**
	 * last port (operator range)
	 */
	protected int _lastPort;

	/**
	 * Constructs a new {@link PfPortItem} instance using an operator
	 * with one operand.
	 * @param operator operator to apply.
	 * @param port port operand.
	 */
	public PfPortItem(String operator, int port) {
		_operator = operator;
		_firstPort = port;
		_lastPort = -1;
	}

	/**
	 * Constructs a new {@link PfPortItem} instance using an operator
	 * with two operands (operator range).
	 * @param operator operator to apply.
	 * @param firstPort first port operand.
	 * @param lastPort last port operand.
	 */
	public PfPortItem(String operator, int firstPort, int lastPort) {
		_operator = operator;
		_firstPort = firstPort;
		_lastPort = lastPort;
	}

	/**
	 * Checks if this {@link PfPortItem} matches the port in argument.
	 * @param port port to check.
	 * @return true if this {@link PfPortItem} matches the port in argument.
	 */
	public boolean matches(int port) {
		if (_operator.equals(EQ)) {
			return port == _firstPort;
		}
		if (_operator.equals(NEQ)) {
			return port != _firstPort;
		}
		if (_operator.equals(LT)) {
			return port < _firstPort;
		}
		if (_operator.equals(LTE)) {
			return port <= _firstPort;
		}
		if (_operator.equals(GT)) {
			return port > _firstPort;
		}
		if (_operator.equals(GTE)) {
			return port >= _firstPort;
		}
		if (_operator.equals(RANGE)) {
			return port >= _firstPort && port <= _lastPort;
		}
		if (_operator.equals(RANGEEX)) {
			return port > _firstPort && port < _lastPort;
		}
		if (_operator.equals(EXCEPT)) {
			return !(port >= _firstPort && port <= _lastPort);
		}
		return false;
	}

	@Override
	public String toString() {
		return _firstPort + _operator + _lastPort;
	}


}
