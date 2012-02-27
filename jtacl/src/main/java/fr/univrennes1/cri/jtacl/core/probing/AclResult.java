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

/**
 * The result of an ACL (accept, deny, match) : a set of flags.
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class AclResult {

	/**
	 * the result
	 */
	protected int _result;

	/**
	 * Constructs a new and empty instance.
	 */
	public AclResult() {
		super();
	}

	/**
	 * Constructs a new instance using OR-ing the flags in argument.
	 * @param flags array of flags to set.
	 */
	public AclResult(int ... flags) {
		super();
		addResult(flags);
	}

	/**
	 * the acl permits the access.
	 */
	public static final int ACCEPT = 1;
	/**
	 * the acl denies the access.
	 */
	public static final int DENY = 2;
	/**
	 * the acl matches but does not accepts nor denies.
	 */
	public static final int MATCH = 4;
	/**
	 *  may accept, deny or match the access (we don't know)
	 */
	public static final int MAY = 128;

	/**
	 * Returns true if the acl result has the ACCEPT flag set.
	 * @return true if the acl result has the ACCEPT flag set.
	 */
	public boolean hasAccept() {
		return (_result & ACCEPT) != 0;
	}

	/**
	 * Returns true if the acl result has the DENY flag set.
	 * @return true if the acl result has the DENY flag set.
	 */
	public boolean hasDeny() {
		return (_result & DENY) != 0;
	}

	/**
	 * Returns true if the acl result has the MATCH flag set.
	 * @return true if the acl result has the MATCH flag set.
	 */
	public boolean hasMatch() {
		return (_result & MATCH) != 0;
	}

	/**
	 * Returns true if the acl result has the MAY flag set.
	 * @return true if the acl result has the MAY flag set.
	 */
	public boolean hasMay() {
		return (_result & MAY) != 0;
	}

	/**
	 * Returns true if the acl result contains all the flags in argument.
	 * @param flags array of flags to test.
	 * @return true if the acl result contains all the flags in argument.
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
	public AclResult newInstance() {
		return new AclResult(_result);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final AclResult other = (AclResult) obj;
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 5;
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
