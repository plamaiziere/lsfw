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
 * Template to build port specification.  This class is used at parsing
 * time as an intermediate storage.
 * @see PfPortItem
 * 
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PortItemTemplate {

	private String _operator;
	private String _firstPort;
	private String _lastPort;

	public String getFirstPort() {
		return _firstPort;
	}

	public void setFirstPort(String firstPort) {
		_firstPort = firstPort;
	}

	public String getLastPort() {
		return _lastPort;
	}

	public void setLastPort(String lastPort) {
		_lastPort = lastPort;
	}

	public String getOperator() {
		return _operator;
	}

	public void setOperator(String operator) {
		_operator = operator;
	}

}
