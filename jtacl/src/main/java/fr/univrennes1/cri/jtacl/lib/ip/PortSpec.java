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

package fr.univrennes1.cri.jtacl.lib.ip;

import fr.univrennes1.cri.jtacl.core.exceptions.JtaclInternalException;
import fr.univrennes1.cri.jtacl.core.monitor.MatchResult;
import java.util.ArrayList;
import java.util.List;

/**
 * Port specification in a ProbeRequest.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PortSpec {

	/**
	 * Current operator
	 */
	protected PortOperator _operator;

	/**
	 * Range(s) of ports.
	 */
	protected List<PortRange> _ranges = new ArrayList<PortRange>();

	/**
	 * Constructs a new instance using an operator with none operand.
	 * (ANY or NONE)
	 * @param operator operator to apply.
	 */
	public PortSpec(PortOperator operator) {
		_operator = operator;
		switch (operator) {
			case NONE:
				break;

			case ANY:
				_ranges.add(new PortRange(0, PortRange.MAX));
				break;

			default:
				throw new JtaclInternalException("Invalid port operator");
			}
	}

	/**
	 * Constructs a new instance using an operator with one operand.
	 * @param operator operator to apply.
	 * @param port port operand.
	 */
	public PortSpec(PortOperator operator, int port) {
		_operator = operator;
		switch (operator) {
			case EQ:
				_ranges.add(new PortRange(port, port));
				break;

			case NEQ:
				if (port > 0) 
					_ranges.add(new PortRange(0, port - 1));

				if (port < PortRange.MAX)
					_ranges.add(new PortRange(port - 1, PortRange.MAX));
				break;

			case LT:
				if (port > 0)
					_ranges.add(new PortRange(0, port -1));

				break;

			case LTE:
				_ranges.add(new PortRange(0, port));
				break;

			case GT:
				if (port < PortRange.MAX)
					_ranges.add(new PortRange(port + 1, PortRange.MAX));
				break;

			case GTE:
				_ranges.add(new PortRange(port, PortRange.MAX));
				break;

			default:
				throw new JtaclInternalException("Invalid port operator");
		}
	}

	/**
	 * Constructs a new instance using an operator with two operands
	 * (operator range).
	 * @param operator operator to apply.
	 * @param firstPort first port operand.
	 * @param lastPort last port operand.
	 */
	public PortSpec(PortOperator operator, int firstPort, int lastPort) {
		_operator = operator;
		switch (operator) {
			case RANGE:
				_ranges.add(new PortRange(firstPort, lastPort));
				break;

			case RANGEEX:
				_ranges.add(new PortRange(firstPort + 1, lastPort - 1));
				break;

			case EXCLUDE:
				_ranges.add(new PortRange(0, firstPort - 1));
				_ranges.add(new PortRange(lastPort + 1, PortRange.MAX));
				break;

			default:
				throw new JtaclInternalException("Invalid port operator");

		}

	}

	/**
	 * Returns the list of {@link PortRange} of this instance.
	 * @return the list of {@link PortRange} of this instance. The list could
	 * be empty but not null.
	 */
	public List<PortRange> getRanges() {
		return _ranges;
	}


	/**
	 * Checks if this instance matches the port spec in argument.
	 * @param port port spec to check.
	 * @return true if this instance matches the port in argument.
	 */
	public MatchResult matches(PortSpec portSpec) {

		if (_operator == PortOperator.NONE)
			return MatchResult.NOT;

		if (_operator == PortOperator.ANY)
			return MatchResult.ALL;

		int match = 0;
		int overlaps = 0;
		for (PortRange rangeSelf: _ranges) {
				int firstSelf = rangeSelf.getFirstPort();
				int lastSelf = rangeSelf.getLastPort();
			for (PortRange range: portSpec.getRanges()) {
				int first = range.getFirstPort();
				int last = range.getLastPort();

				if (firstSelf >= first && firstSelf <= last &&
						lastSelf >= first && lastSelf <= last) {
					match++;
				} else {
					if ((firstSelf >= first && firstSelf <= last) ||
							(lastSelf >= first && lastSelf <= last)) {
						overlaps++;
					}
				}
			}
		}

		if (match == _ranges.size())
			return MatchResult.ALL;

		if (overlaps > 0)
			return MatchResult.MATCH;

		return MatchResult.NOT;

	}

	@Override
	public String toString() {
		String s = _operator.toString();
		for (PortRange range: _ranges) {
			s = s + " " + range;
		}

		return s;
	}

}
