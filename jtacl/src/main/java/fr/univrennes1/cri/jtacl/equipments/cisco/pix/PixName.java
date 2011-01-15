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

package fr.univrennes1.cri.jtacl.equipments.cisco.pix;

/**
 * PIX name
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PixName {

	protected  String _name;
	protected String _ipValue;
	protected int _refCount;

	public PixName(String name, String ipValue) {
		_name = name;
		_ipValue = ipValue;
	}

	public String getName() {
		return _name;
	}

	public String getIpValue() {
		return _ipValue;
	}

	public int getRefCount() {
		return _refCount;
	}

	public void incRefCount() {
		_refCount++;
	}

	public boolean isUsed() {
		return _refCount > 0;
	}

}
