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

package fr.univrennes1.cri.jtacl.lib.ip;

/**
 * A range of ports [0..65535]
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PortRange {
	private int _firstPort = -1;
	private int _lastPort = -1;

	public static final int MAX = 65535;

	public PortRange(int port) {
		_firstPort = port;
		_lastPort = port;
	}

	public PortRange(int firstPort, int lastPort) {
		_firstPort = firstPort;
		_lastPort = lastPort;
	}

	public int getFirstPort() {
		return _firstPort;
	}

	public int getLastPort() {
		return _lastPort;
	}

	@Override
	public String toString() {
		return "[" + _firstPort + ", " +  _lastPort + "]";
	}


}
