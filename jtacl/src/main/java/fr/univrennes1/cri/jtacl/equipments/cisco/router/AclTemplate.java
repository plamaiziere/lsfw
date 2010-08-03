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

package fr.univrennes1.cri.jtacl.equipments.cisco.router;

import fr.univrennes1.cri.jtacl.lib.ip.IPversion;

/**
 * Template to build Access List. This class is used at parsing time as an
 * intermediate storage.
 * @see AccessList
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class AclTemplate {
	private Integer _number;
	private String _name;
	private AclType _type;
	private IPversion _ipVersion;

	public Integer getNumber() {
		return _number;
	}

	public void setNumber(Integer number) {
		this._number = number;
	}

	public String getName() {
		return _name;
	}

	public void setName(String _name) {
		this._name = _name;
	}

	public void setAclType(AclType type) {
		_type = type;
	}

	public AclType getAclType() {
		return _type;
	}

	public IPversion getIpVersion() {
		return _ipVersion;
	}

	public void setIpVersion(IPversion _ipVersion) {
		this._ipVersion = _ipVersion;
	}

}
