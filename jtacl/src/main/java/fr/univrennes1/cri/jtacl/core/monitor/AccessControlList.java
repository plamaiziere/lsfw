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

package fr.univrennes1.cri.jtacl.core.monitor;

/**
 * Describe one ACL.<br/>
 * An acl has a {@link AclResult} result and a String representation of the acl.
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
	protected AclResult _result;

	/**
	 * Creates a new {@link AccessControlList} acl.
	 * @param string the {@link String} textual representation of the acl.
	 * @param result the {@link AclResult} result of the acl.
	 */
	public AccessControlList(String string, AclResult result) {
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
	 * Returns the {@link AclResult} result of this acl.
	 * @return the {@link AclResult} result of this acl.
	 */
	public AclResult getResult() {
		return _result;
	}

	@Override
	public String toString() {
		return String.format("%6s %s", _result.toString(), _aclString);
	}
	
}
