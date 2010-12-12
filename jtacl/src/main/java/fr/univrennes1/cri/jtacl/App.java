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

package fr.univrennes1.cri.jtacl;

import fr.univrennes1.cri.jtacl.core.monitor.Log;
import fr.univrennes1.cri.jtacl.shell.Shell;
import java.io.IOException;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * The application
 *
 */
public class App 
{
    public static void main( String[] args ) throws IOException {

		class ShortFormatter extends Formatter {
			// inefficient implementation
			public String format(LogRecord record) {
			    return "[" + record.getLevel() + "]" +
//				 record.getSourceClassName() + ":" +
//				 record.getSourceMethodName() + "] " +
				 record.getMessage() + "\n";
			}
		}

		ShortFormatter fmt = new ShortFormatter();

		Logger l = Logger.getLogger("");
		Handler [] hdler = l.getHandlers();
		hdler[0].setFormatter(fmt);
		l.setLevel(Level.ALL);
		Log.notifier().setLevel(Level.ALL);
		Log.debug().setLevel(Level.OFF);
		Log.config().setLevel(Level.SEVERE);
		new Shell().run(args);
	}

}
