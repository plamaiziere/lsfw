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
 * The result of an ACL (accept, deny)
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public enum AclResult {
	/**
	 * the acl permits the access.
	 */
	ACCEPT,
	/**
	 * the acl denies the access.
	 */
	DENY,
	/**
	 May accept or deny the access (we don't know)
	*/
	MAY;
}
