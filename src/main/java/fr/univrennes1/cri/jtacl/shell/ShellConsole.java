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

package fr.univrennes1.cri.jtacl.shell;

import java.io.PrintStream;

/**
 * A simple shell console to handle redirection of stdout and stderr
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class ShellConsole {

	protected static final PrintStream _systemOut = System.out;
	protected static final ConsoleOutputStream _out =
			new ConsoleOutputStream(_systemOut);

	protected static final PrintStream _pout =
			new PrintStream(_out, true);

	protected static final PrintStream _systemErr = System.err;

	protected static final ConsoleOutputStream _err =
			new ConsoleOutputStream(_systemErr);

	protected static final PrintStream _perr =
			new PrintStream(_err, true);

	/**
	 * Installs the redirection of stderr and stdout.
	 */
	public static void install() {
		System.setOut(_pout);
		System.setErr(_perr);
	}

	/**
	 * Uninstalls the redirection of stderr and stdout.
	 */
	public static void unInstall() {
		System.setOut(_systemOut);
		System.setErr(_systemErr);
	}

	/**
	 * Returns the stream attached to stdout.
	 * @return the stream attached to stdout.
	 */
	public static ConsoleOutputStream out() {
		return _out;
	}

	/**
	 * Returns the stream attached to stderr.
	 * @return the stream attached to stderr.
	 */
	public static ConsoleOutputStream err() {
		return _err;
	}

}
