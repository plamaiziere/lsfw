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

package fr.univrennes1.cri.jtacl.equipments.openbsd;

import fr.univrennes1.cri.jtacl.core.exceptions.JtaclInternalException;
import fr.univrennes1.cri.jtacl.core.probing.MatchResult;
import fr.univrennes1.cri.jtacl.lib.ip.PortOperator;
import fr.univrennes1.cri.jtacl.lib.ip.PortSpec;

/**
 * Describes a Port Item.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PfPortItem {

	/**
	 * Operator equal
	 */
	public static final String EQ = "=";

	/**
	 * Operator not equal
	 */
	public static final String NEQ = "!=";

	/**
	 * Operator less than
	 */
	public static final String LT = "<";

	/**
	 * Operator less than or equal
	 */
	public static final String LTE = "<=";

	/**
	 * Operator greater than
	 */
	public static final String GT = ">";

	/**
	 * Operator greater than or equal
	 */
	public static final String GTE = ">=";

	/**
	 * Operator range including boundaries
	 */
	public static final String RANGE = ":";

	/**
	 * Operator range, excluding boundaries
	 */
	public static final String RANGEEX = "><";

	/**
	 * Operator except range
	 */
	public static final String EXCEPT = "<>";

	/**
	 * Current operator
	 */
	protected String _operator;

	/**
	 * port spec associated to this item
	 */
	protected PortSpec _portSpec;

	/**
	 * Constructs a new {@link PfPortItem} instance using an operator
	 * with one operand.
	 * @param operator operator to apply.
	 * @param port port operand.
	 */
	public PfPortItem(String operator, int port) {
		_operator = operator;

		if (_operator.equals(EQ))
			_portSpec = new PortSpec(PortOperator.EQ, port);

		if (_operator.equals(NEQ))
			_portSpec = new PortSpec(PortOperator.NEQ, port);

		if (_operator.equals(LT))
			_portSpec = new PortSpec(PortOperator.LT, port);

		if (_operator.equals(LTE))
			_portSpec = new PortSpec(PortOperator.LTE, port);

		if (_operator.equals(GT))
			_portSpec = new PortSpec(PortOperator.GT, port);

		if (_operator.equals(GTE))
			_portSpec = new PortSpec(PortOperator.GTE, port);

		if (_portSpec == null)
			throw new JtaclInternalException("Invalid port operator" +
					_operator);
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

		if (_operator.equals(RANGE))
			_portSpec = new PortSpec(PortOperator.RANGE, firstPort, lastPort);

		if (_operator.equals(RANGEEX))
			_portSpec = new PortSpec(PortOperator.RANGEEX, firstPort, lastPort);

		if (_operator.equals(EXCEPT))
			_portSpec = new PortSpec(PortOperator.EXCLUDE, firstPort, lastPort);

		if (_portSpec == null)
			throw new JtaclInternalException("Invalid port operator" +
					_operator);
	}

	/**
	 * Checks if this {@link PfPortItem} matches the port in argument.
	 * @param portRequest port to check.
	 * @return a {@link MatchResult} between the port spec in argument and this
	 * item.
	 */
	public MatchResult matches(PortSpec portRequest) {
		return _portSpec.matches(portRequest);
	}

	@Override
	public String toString() {
		return _portSpec.toString();
	}


}
