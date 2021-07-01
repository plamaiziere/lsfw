/*
 * Copyright (c) 2013 - 2021, Universite de Rennes 1
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
package fr.univrennes1.cri.jtacl.equipments.proxmox;

/**
 * Template to build options. This class is used at parsing time
 * as an intermediate storage.
 * @see PxVeParser
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PxOptionTemplate {

	protected String _name;
	protected String _value;

	public String getName() {
		return _name;
	}

	public boolean setName(String name) {
		_name = name;
		return true;
	}

	public String getValue() {
		return _value;
	}

	public boolean setValue(String value) {
		_value = value;
		return true;
	}
}
