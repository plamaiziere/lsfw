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

package fr.univrennes1.cri.jtacl.equipments.cisco.pix;

/**
 * Describes a port object.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PortObject {

	/**
	 * Operator equal
	 */
	public final String EQ = "eq";

	/**
	 * Operator less than
	 */
	public final String LT = "lt";

	/**
	 * Operator greater than
	 */
	public final String GT = "gt";

	/**
	 * Operator not equal
	 */
	public final String NEQ = "neq";

	/**
	 * Operator range
	 */
	public final String RANGE = "range";

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
	 * Constructs a new {@link PortObject} instance using an operator
	 * with one operand.
	 * @param operator operator to apply.
	 * @param port port operand.
	 */
	public PortObject(String operator, int port) {
		_operator = operator;
		_firstPort = port;
		_lastPort = -1;
	}

	/**
	 * Constructs a new {@link PortObject} instance using an operator
	 * with two operands (operator range).
	 * @param operator operator to apply.
	 * @param firstPort first port operand.
	 * @param lastPort last port operand.
	 */
	public PortObject(String operator, int firstPort, int lastPort) {
		_operator = operator;
		_firstPort = firstPort;
		_lastPort = lastPort;
	}

	/**
	 * Checks if this {@link PortObject} matches the port in argument.
	 * @param port port to check.
	 * @return true if this {@link PortObject} matches the port in argument.
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
		if (_operator.equals(GT)) {
			return port > _firstPort;
		}
		if (_operator.equals(RANGE)) {
			return port >= _firstPort && port <= _lastPort;
		}
		return false;
	}

	/**
	 * Returns the first port of this port object.
	 * @return the first port of this port object.
	 */
	public int getFirstPort() {
		return _firstPort;
	}

	/**
	 * Returns the last port of this port object.
	 * Valid if operator == RANGE
	 * @return the last port of this port object.
	 */
	public int getLastPort() {
		return _lastPort;
	}

	/**
	 * Returns the operator of this port object.
	 * @return the operator of this port object.
	 */
	public String getOperator() {
		return _operator;
	}

}
