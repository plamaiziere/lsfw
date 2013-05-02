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
 * Describe one ACL.<br/>
 * An acl has a {@link FwResult} result and a String representation of the acl.
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class AccessControlList {


	/**
	 * string representation of the acl.
	 */
	protected String _aclString;

	/**
	 * the result of the acl.
	 */
	protected FwResult _result;

	/**
	 * Creates a new {@link AccessControlList} acl.
	 * @param string the {@link String} textual representation of the acl.
	 * @param result the {@link FwResult} result of the acl.
	 */
	public AccessControlList(String string, FwResult result) {
		_aclString = string;
		_result = result;
	}

	/**
	 * Returns the {@link String} textual representation of this acl.
	 * @return the {@link String} textual representation of this acl.
	 */
	public String getAclString() {
		return _aclString;
	}

	/**
	 * Returns the {@link FwResult} result of this acl.
	 * @return the {@link FwResult} result of this acl.
	 */
	public FwResult getResult() {
		return _result;
	}

	@Override
	public String toString() {
		return String.format("%6s %s", _result.toString(), _aclString);
	}
	
}
