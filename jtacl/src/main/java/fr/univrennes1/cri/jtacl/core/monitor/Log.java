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

package fr.univrennes1.cri.jtacl.core.monitor;

import java.util.logging.Logger;

/**
 * Logger used by Jtacl.<br/><br/>
 * debug: debugging messages.<br/>
 * notifier: runtime messages.<br/>
 * config: configuration messages.<br/>
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class Log {

	protected static Logger _debug = Logger.getLogger("jtacl.debug");
	protected static Logger _notifier = Logger.getLogger("jtacl.notifier");
	protected static Logger _config = Logger.getLogger("jtacl.config");

	public static Logger debug() {
		return _debug;
	}

	public static Logger notifier() {
		return _notifier;
	}

	public static Logger config() {
		return _config;
	}
	
}
