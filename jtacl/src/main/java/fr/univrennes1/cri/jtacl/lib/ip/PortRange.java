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

	/**
	 * Construct a new range with only one port [port..port].
	 * @param port port of the range.
	 */
	public PortRange(int port) {
		if (port < 0 || port > MAX)
			throw new IllegalArgumentException("Invalid port: " + port);
		_firstPort = port;
		_lastPort = port;
	}

	/**
	 * Construct a new range between two ports. lastPort may be &lt firstPort.
	 * @param firstPort first port of the range.
	 * @param lastPort last port of the range.
	 */
	public PortRange(int firstPort, int lastPort) {
		if (firstPort < 0 || firstPort > MAX || lastPort < 0 || lastPort > MAX)
			throw new IllegalArgumentException("Invalid port range first: " +
					firstPort + " last: " + lastPort);

		/*
		 * check order
		 */
		if (firstPort <= lastPort) {
			_firstPort = firstPort;
			_lastPort = lastPort;
		} else {
			_lastPort = firstPort;
			_firstPort = lastPort;
		}
	}

	/**
	 * Returns the first port of the range.
	 * Ensures that firstPort() <= lastPort().
	 * @return the first port of the range..
	 */
	public int getFirstPort() {
		return _firstPort;
	}

	/**
	 * Returns the last port of the range.
	 * Ensures that lastPort() >= firstPort().
	 * @return the last port of the range..
	 */
	public int getLastPort() {
		return _lastPort;
	}

	@Override
	public String toString() {
		return "[" + _firstPort + ", " +  _lastPort + "]";
	}


}
