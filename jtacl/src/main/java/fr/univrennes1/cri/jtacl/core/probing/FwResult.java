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

package fr.univrennes1.cri.jtacl.core.probing;

import java.util.List;

/**
 * The result of a firewalling rule (accept, deny, match) : a set of flags.
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class FwResult {

	/**
	 * 'Reduces' a list of results to a single result
	 * @param results list of results to reduce
	 * @return a single result, return ACCEPT  if the result list is empty
	 */
	public static FwResult reduceFwResults(List<FwResult> results) {
		FwResult last = null;
		for (FwResult cur: results) {
			if (cur.hasMatch()) continue;
			if (last == null) {
				if (cur.isCertainlyAccept())
					return new FwResult(FwResult.ACCEPT);
				if (cur.isCertainlyDeny())
					return new FwResult(FwResult.DENY);
				last = cur;
			} else {
				if (last.hasAccept() && cur.isCertainlyAccept())
					return new FwResult(FwResult.ACCEPT);

				if (last.hasDeny() && cur.isCertainlyDeny())
					return new FwResult(FwResult.DENY);

				if (last.hasAccept() && cur.hasAccept()) {
					last = cur;
					continue;
				}
				if (last.hasDeny() && cur.hasDeny()) {
					last = cur;
					continue;
				}
				return last.newInstance();
			}
		}
		return last == null ? new FwResult(FwResult.ACCEPT) : last.newInstance();
	}

	/**
	 * the result
	 */
	protected int _result;

	/**
	 * Constructs a new and empty instance.
	 */
	public FwResult() {
		super();
	}

	/**
	 * Constructs a new instance using OR-ing the flags in argument.
	 * @param flags array of flags to set.
	 */
	public FwResult(int ... flags) {
		super();
		addResult(flags);
	}

	/**
	 * the firewalling rule permits the access.
	 */
	public static final int ACCEPT = 1;
	/**
	 * the firewalling rule denies the access.
	 */
	public static final int DENY = 2;
	/**
	 * the firewalling rule matches but does not accepts nor denies.
	 */
	public static final int MATCH = 4;
	/**
	 *  may accept, deny or match the access (we don't know)
	 */
	public static final int MAY = 128;

	/**
	 * Returns true if the firewalling rule result has the ACCEPT flag set.
	 * @return true if the firewalling rule result has the ACCEPT flag set.
	 */
	public boolean hasAccept() {
		return (_result & ACCEPT) != 0;
	}

	/**
	 * Returns true if the firewalling rule result has the DENY flag set.
	 * @return true if the firewalling rule result has the DENY flag set.
	 */
	public boolean hasDeny() {
		return (_result & DENY) != 0;
	}

	/**
	 * Returns true if the firewalling rule result has the MATCH flag set.
	 * @return true if the firewalling rule result has the MATCH flag set.
	 */
	public boolean hasMatch() {
		return (_result & MATCH) != 0;
	}

	/**
	 * Returns true if the firewalling rule result has the MAY flag set.
	 * @return true if the firewalling rule result has the MAY flag set.
	 */
	public boolean hasMay() {
		return (_result & MAY) != 0;
	}

	/**
	 * Returns true if the firewalling rule result contains all the flags in argument.
	 * @param flags array of flags to test.
	 * @return true if the firewalling rule result contains all the flags in argument.
	 */
	public boolean hasFlags(int ... flags) {
		for (int f: flags) {
			if ((_result & f) != 0)
				return false;
		}
		return true;
	}

	/**
	 * Clears the result.
	 */
	public void clearResult() {
		_result = 0;
	}

	/**
	 * Sets the result OR-ing the flags in arguments.
	 * @param flags : array of flag to set.
	 */
	public void addResult(int ... flags) {
		for (int f: flags) {
			_result |= f;
		}
	}

	/**
	 * Sets the result to the value in argurment.
	 * @param result result value to set.
	 */
	public void setResult(int result) {
		_result = result;
	}

	/**
	 * Returns a new instance of this result.
	 * @return a new instance of this result.
	 */
	public FwResult newInstance() {
		return new FwResult(_result);
	}

	/**
	 * Returns true if this result is certainly DENY. IE flag DENY is set and
	 * flag MAY is unset.
	 * @return true if this result is certainly DENY
	 */
	public boolean isCertainlyDeny() {
		return hasDeny() && !hasMay();
	}

	/**
	 * Returns true if this result is certainly ACCEPT. IE flag ACCEPT is set
	 * and flag MAY is unset.
	 * @return true if this result is certainly ACCEPT
	 */
	public boolean isCertainlyAccept() {
		return hasAccept() && !hasMay();
	}

	/**
	 * Concats an FwResult with this result. The concatenation is defined by
	 * the first following rules (in order):
	 * <li>this.isCertainlyDeny || other.isCertainlyDeny returns DENY</li>
	 * <li>this.isCertainlyAccept && other.isCertainlyAccept returns ACCEPT</li>	 *
	 * <li>this.hasDeny || other.hasDeny returns MAY DENY</li>
	 * <li>this.hasAccept || other.hasAccept returns MAY ACCEPT</li>
	 * @param other FwResult to concat.
	 * @return the result of the concatenation.
	 */
	public FwResult concat(FwResult other) {

		/*
		 * deny
		 */
		if (isCertainlyDeny() || other.isCertainlyDeny()) {
			return new FwResult(FwResult.DENY);
		}

		/*
		 * accept
		 */
		if (isCertainlyAccept() && other.isCertainlyAccept()) {
			return new FwResult(FwResult.ACCEPT);
		}

		/*
		 * may deny
		 */
		if (hasDeny() || other.hasDeny()) {
			return new FwResult(FwResult.MAY | FwResult.DENY);
		}

		/*
		 * may accept
		 */
		if (hasAccept() || other.hasAccept()) {
			return new FwResult(FwResult.MAY | FwResult.ACCEPT);
		}

		return new FwResult();
	}

	/* SumPath of an FwResult with this result. The sumPath is defined by
	 * the first following rules (in order):
	 * <li>this.isCertainlyDeny && other.isCertainlyDeny returns DENY</li>
	 * <li>this.isCertainlyAccept && other.isCertainlyAccept returns ACCEPT</li>
	 * <li>this.hasDeny || other.hasDeny returns MAY DENY</li>
	 * <li>this.hasAccept || other.hasAccept returns MAY ACCEPT</li>
	 * @param other FwResult to sumPath.
	 * @return the result of the sumPath.
	 */
	public FwResult sumPath(FwResult other) {

		/*
		 * deny
		 */
		if (isCertainlyDeny() && other.isCertainlyDeny()) {
			return new FwResult(FwResult.DENY);
		}

		/*
		 * accept
		 */
		if (isCertainlyAccept() && other.isCertainlyAccept()) {
			return new FwResult(FwResult.ACCEPT);
		}

		/*
		 * may deny
		 */
		if (hasDeny() || other.hasDeny()) {
			return new FwResult(FwResult.MAY | FwResult.DENY);
		}

		/*
		 * may accept
		 */
		if (hasAccept() || other.hasAccept()) {
			return new FwResult(FwResult.MAY | FwResult.ACCEPT);
		}

		return new FwResult();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final FwResult other = (FwResult) obj;
		if (this._result != other._result) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 23 * hash + this._result;
		return hash;
	}

	@Override
	public String toString() {
		String s = "";

		if (hasMay())
			s += "MAY ";
		if (hasAccept())
			s += "ACCEPT ";
		if (hasDeny())
			s += "DENY ";
		if (hasMatch())
			s += "MATCH ";
		return s;
	}

}
