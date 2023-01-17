/*
 * Copyright (c) 2010, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.equipments.openbsd;

/**
 * icmp item specification
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class IcmpItem {

	private String _icmpType;
	private String _icmpCode;

	public String getIcmpCode() {
		return _icmpCode;
	}

	public boolean setIcmpCode(String icmpCode) {
		_icmpCode = icmpCode;
		return true;
	}

	public String getIcmpType() {
		return _icmpType;
	}

	public boolean setIcmpType(String icmpType) {
		_icmpType = icmpType;
		return true;
	}

}
