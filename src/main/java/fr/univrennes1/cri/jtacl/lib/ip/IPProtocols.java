/*
 * Copyright (c) 2010, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.lib.ip;

/**
 * Protocols definitions and lookup (singleton).
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class IPProtocols extends Protocols {

	protected static IPProtocols _instance = new IPProtocols();

	public static IPProtocols getInstance() {
		return _instance;
	}

}
