/*
 * Copyright (c) 2010, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.core.monitor;

import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
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

	protected static final Logger _debug = Logger.getLogger("jtacl.debug");
	protected static final Logger _notifier = Logger.getLogger("jtacl.notifier");
	protected static final Logger _config = Logger.getLogger("jtacl.config");
	protected static final Handler _consoleHandler =
			_debug.getParent().getHandlers()[0];

	public static Logger debug() {
		return _debug;
	}

	public static Logger notifier() {
		return _notifier;
	}

	public static Logger config() {
		return _config;
	}

	public static Handler consoleHandler() {
		return _consoleHandler;
	}

	public static void install() {
		class ShortFormatter extends Formatter {
			// inefficient implementation
			public String format(LogRecord record) {
				return "[" + record.getLevel() + "]" +
				 record.getMessage() + "\n";
			}
		}

		ShortFormatter fmt = new ShortFormatter();

		_consoleHandler.setFormatter(fmt);
		_debug.getParent().setLevel(Level.ALL);
		_notifier.setLevel(Level.WARNING);
		_debug.setLevel(Level.OFF);
		_config.setLevel(Level.SEVERE);
	}


}
