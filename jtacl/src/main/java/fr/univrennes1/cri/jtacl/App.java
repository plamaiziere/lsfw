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

import fr.univrennes1.cri.jtacl.core.exceptions.JtaclConfigurationException;
import fr.univrennes1.cri.jtacl.core.monitor.Log;
import fr.univrennes1.cri.jtacl.core.monitor.Monitor;
import fr.univrennes1.cri.jtacl.shell.Shell;
import java.io.IOException;
import static java.util.Arrays.*;
import java.util.List;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

/**
 * The application (lsfw's main class)
 *
 */
public class App
{

	static public final int EXIT_SUCCESS = 0;
	static public final int EXIT_FAILURE = 1;
	static public final int EXIT_ERROR = 255;

	protected static void installLogger() {
		class ShortFormatter extends Formatter {
			// inefficient implementation
			public String format(LogRecord record) {
				return "[" + record.getLevel() + "]" +
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
	}

	protected static OptionParser initOptions() {
		OptionParser optParser = new OptionParser();
		optParser.acceptsAll(asList("c", "command"),
			"Execute the command in argument and quit.")
			.withRequiredArg().describedAs("command to execute");

		optParser.acceptsAll(asList("f", "file"),
			"Use the configuration file in argument.")
			.withRequiredArg().describedAs("configuration file");

		optParser.acceptsAll(asList("i", "input"),
			"Read and execute commands from the input file and quit.")
			.withRequiredArg().describedAs("input file");

		optParser.acceptsAll(asList("h", "help"),
			"This help.");

		optParser.acceptsAll(asList("n", "no-interactive"),
			"Non interactive mode.");

		optParser.acceptsAll(asList("t", "test"),
			"Test mode.");

		optParser.acceptsAll(asList("v", "verbose"),
			"Use verbose reports.");

		optParser.acceptsAll(asList("o", "option"), "Set option").
			withRequiredArg().describedAs("option to set (option=value)");

		return optParser;

	}

	protected static boolean setOption(String option) {

		String[] opts = option.split("=");
		if (opts.length != 2)
			return false;
		String optName = opts[0].trim();
		String optValue = opts[1].trim();
		try {
			Monitor.getInstance().getOptions().setOption(optName, optValue);
		} catch (JtaclConfigurationException e) {
			return false;
		}
		return true;
	}

	protected static void quitError(OptionParser optParser, String message) {
		System.err.println(message);
		try {
			optParser.printHelpOn(System.out);
		} catch (IOException ex) {
			// do nothing
		}
		System.exit(EXIT_ERROR);
	}

	protected static void quit(int error) {
		System.exit(error);
	}

	public static void main( String[] args ) throws IOException {

		try {
			installLogger();

			/*
			 * --option
			 */
			OptionParser optParser = initOptions();
			OptionSet optionSet = optParser.parse(args);
			if (optionSet.has("option")) {
				List<?> options = optionSet.valuesOf("o");
				for (Object o: options) {
					String opt = (String)o;
					boolean ok = setOption(opt);
					if (!ok)
						quitError(optParser, "Invalid option");
				}
			}

			/*
			 * --file
			 */
			String configFile = null;
			if (optionSet.has("file")) {
				configFile = (String) optionSet.valueOf("file");
			} else {
					quitError(optParser, "option --file is missing");
			}

			/*
			 * init the monitor
			 */
			Monitor.getInstance().configure(configFile);
			Monitor.getInstance().init();

			boolean verbose = optionSet.has("verbose");

			Shell shell;
			int ret;
			/*
			 * --command
			 */
			if (optionSet.has("command")) {
				String line = (String) optionSet.valueOf("command");
				shell = new Shell(false, verbose);
				ret = shell.runCommand(line);
				quit(ret);
			}

			/*
			 * --input
			 */
			if (optionSet.has("input")) {
				String fileName = (String) optionSet.valueOf("input");
				shell = new Shell(false, verbose);
				ret = shell.runFromFile(fileName);
				quit(ret);
			}

			/*
			 * normal shell
			 */
			boolean interactive = !optionSet.has("no-interactive");
			shell = new Shell(interactive, verbose);
			ret = shell.runFromFile(null);
			quit(ret);
		} catch (Exception ex) {
			System.err.println("Error: " + ex.getMessage());
			if (!(ex instanceof JtaclConfigurationException))
				ex.printStackTrace();
			System.exit(EXIT_ERROR);
		}
	}

}
