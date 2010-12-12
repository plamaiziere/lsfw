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

	public void setIcmpCode(String icmpCode) {
		_icmpCode = icmpCode;
	}

	public String getIcmpType() {
		return _icmpType;
	}

	public void setIcmpType(String icmpType) {
		_icmpType = icmpType;
	}

}
