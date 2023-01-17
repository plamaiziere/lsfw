/*
 * Copyright (c) 2010, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.lib.ip;

import fr.univrennes1.cri.jtacl.core.exceptions.JtaclInternalException;
import fr.univrennes1.cri.jtacl.core.probing.MatchResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Port specification in a ProbeRequest.
 * Immutable classe.
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
	protected List<PortRange> _ranges = new ArrayList<>();

	/**
	 * PortSpec NONE
	 */
	public static final PortSpec NONE = new PortSpec(PortOperator.NONE);

	/**
	 * PortSpec ANY
	 */
	public static final PortSpec ANY = new PortSpec(PortOperator.ANY);

	/**
	 * PortSpec WELLKNOWN: port < 1024
	 */
	public static final PortSpec WELLKNOWN =
			new PortSpec(PortOperator.LT, 1024);

	/**
	 * PortSpec REGISTERED: 1024 <= port <= 49151
	 */
	public static final PortSpec REGISTERED =
			new PortSpec(PortOperator.RANGE, 1024, 49151);

	/**
	 * PortSpec DYNAMIC: port > 49151
	 */
	public static final PortSpec DYNAMIC =
			new	PortSpec(PortOperator.RANGE, 49152, PortRange.MAX);

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
	 * Constructs a new empty instance
	 */
	public PortSpec() {
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
					_ranges.add(new PortRange(port + 1, PortRange.MAX));
				break;

			case LT:
				if (port > 0)
					_ranges.add(new PortRange(0, port - 1));

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
	 * (operator range). lastPort may be &lt firstPort.
	 * @param operator operator to apply.
	 * @param firstPort first port operand.
	 * @param lastPort last port operand.
	 */
	public PortSpec(PortOperator operator, int firstPort, int lastPort) {
		int first;
		int last;

		if (firstPort <= lastPort) {
			first = firstPort;
			last = lastPort;
		} else {
			first = lastPort;
			last = firstPort;
		}
		_operator = operator;
		switch (operator) {
			case RANGE:
				_ranges.add(new PortRange(first, last));
				break;

			case RANGEEX:
				_ranges.add(new PortRange(first + 1, last - 1));
				break;

			case EXCLUDE:
				_ranges.add(new PortRange(0, first - 1));
				_ranges.add(new PortRange(last + 1, PortRange.MAX));
				break;

			default:
				throw new JtaclInternalException("Invalid port operator");

		}

	}

    /**
     * Add a {@link PortRange} to this instance
     * @param range to add
     */
	public void add(PortRange range) {
	    _ranges.add(range);
    }

	/**
	 * Returns an immutable list of {@link PortRange} of this instance.
	 * @return an immutable list of {@link PortRange} of this instance.
	 * The list could be empty but not null.
	 */
	public List<PortRange> getRanges() {
		return Collections.unmodifiableList(_ranges);
	}

	/**
	 * Checks if this instance matches the port spec in argument.
	 * <ul>
	 * <li>Returns MatchResult.ALL if at least one range of this PortSpec includes
	 * one range of the PortSpec in argument.</li>
	 * <li>Returns MatchResult.MATCH if at least one range of this PortSpec overlaps
	 * one range of the PortSpec in argument.</li>
	 * <li>Returns MachResult.NOT otherwise.</li>
	 * </ul>
	 * @param portSpec port spec to check.
	 * @return The match result between this instance and the PortSpec in argument.
	 */
	public MatchResult matches(PortSpec portSpec) {

		if (_ranges.isEmpty())
			return MatchResult.NOT;

		int all = 0;
		int match = 0;
		for (PortRange rangeSelf: _ranges) {
				int firstSelf = rangeSelf.getFirstPort();
				int lastSelf = rangeSelf.getLastPort();
				int lastAll = 0;
				int lastMatch = 0;

			for (PortRange range: portSpec.getRanges()) {
				int first = range.getFirstPort();
				int last = range.getLastPort();

				/*
				 * all: this PortSpec include the other PortSpec:
				 */
				if (firstSelf <= first && lastSelf >= last) {
					lastAll++;
				} else {
					/*
					 * else matches if there is an overlap.
					 */
					if ((firstSelf >= first && firstSelf <= last) ||
							(lastSelf >= first && lastSelf <= last) ||
							(first >= firstSelf && first <= lastSelf) ||
							(last >= firstSelf && last <= lastSelf)) {
						lastMatch++;
					}
				}
			}
			if (lastAll > 0)
				all++;
			match += lastMatch;
		}

		if (all == _ranges.size())
			return MatchResult.ALL;

		if (match > 0)
			return MatchResult.MATCH;

		return MatchResult.NOT;

	}

	@Override
	public String toString() {
		String s = "";
		if (_operator != null) s =_operator.toString();
		for (PortRange range: _ranges) {
			s = s + " " + range;
		}

		return s;
	}

}
