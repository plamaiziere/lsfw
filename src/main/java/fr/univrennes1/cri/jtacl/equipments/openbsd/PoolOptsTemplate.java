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
 * Template to build pool options. This class is used at parsing time
 * as an intermediate storage.
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PoolOptsTemplate {

	private int _type;
	private int _opts;
	private boolean _staticPort;
	private String _key;

	public String getKey() {
		return _key;
	}

	public boolean setKey(String key) {
		_key = key;
		return true;
	}

	public int getOpts() {
		return _opts;
	}

	public boolean setOpts(int opts) {
		_opts = opts;
		return true;
	}

	public boolean isStaticPort() {
		return _staticPort;
	}

	public boolean setStaticPort(boolean staticPort) {
		_staticPort = staticPort;
		return true;
	}

	public int getType() {
		return _type;
	}

	public boolean setType(int type) {
		_type = type;
		return true;
	}


}
