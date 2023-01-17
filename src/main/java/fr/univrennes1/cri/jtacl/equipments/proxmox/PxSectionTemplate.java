/*
 * Copyright (c) 2021, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */
package fr.univrennes1.cri.jtacl.equipments.proxmox;

/**
 * Template to build section. This class is used at parsing time
 * as an intermediate storage.
 * @see
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PxSectionTemplate {
	protected String _sectionName;
	protected String _name;


	public String getSectionName() {
		return _sectionName;
	}

	public boolean setSectionName(String sectionName) {
		this._sectionName = sectionName;
		return true;
	}

	public String getName() {
		return _name;
	}

	public boolean setName(String name) {
		this._name = name;
		return true;
	}
}
