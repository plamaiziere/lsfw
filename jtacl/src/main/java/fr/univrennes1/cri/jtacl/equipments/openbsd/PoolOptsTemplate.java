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

	public void setKey(String key) {
		_key = key;
	}

	public int getOpts() {
		return _opts;
	}

	public void setOpts(int opts) {
		_opts = opts;
	}

	public boolean isStaticPort() {
		return _staticPort;
	}

	public void setStaticPort(boolean staticPort) {
		_staticPort = staticPort;
	}

	public int getType() {
		return _type;
	}

	public void setType(int type) {
		_type = type;
	}


}
