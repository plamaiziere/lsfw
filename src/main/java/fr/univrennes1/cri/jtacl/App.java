/*
 * Copyright (c) 2010, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl;

import fr.univrennes1.cri.jtacl.core.exceptions.JtaclConfigurationException;
import fr.univrennes1.cri.jtacl.core.monitor.Log;
import fr.univrennes1.cri.jtacl.core.monitor.Monitor;
import fr.univrennes1.cri.jtacl.shell.Shell;
import fr.univrennes1.cri.jtacl.shell.ShellConsole;

import java.io.IOException;

import static java.util.Arrays.*;

import java.util.List;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

/**
 * The application (lsfw's main class)
 */
public class App {

    static public final int EXIT_SUCCESS = 0;
    static public final int EXIT_FAILURE = 1;
    static public final int EXIT_ERROR = 255;

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

        optParser.acceptsAll(asList("o", "option"), "Set option").
                withRequiredArg().describedAs("option to set (option=value)");

        optParser.acceptsAll(asList("b", "bind"), "bind address").
                withRequiredArg().describedAs("bind address");

        optParser.acceptsAll(asList("p", "port"), "bind port number").
                withRequiredArg().describedAs("port number");

        return optParser;

    }

    protected static void setOption(String option) {

        String[] opts = option.split("=");
        if (opts.length != 2)
            throw new JtaclConfigurationException("invalid option " + option);
        String optName = opts[0].trim();
        String optValue = opts[1].trim();
        Monitor.getInstance().getOptions().setOption(optName, optValue);
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

    public static void main(String[] args) throws IOException {

        try {
            Log.install();

            /*
             * --option
             */
            OptionParser optParser = initOptions();
            OptionSet optionSet = optParser.parse(args);
            if (optionSet.has("option")) {
                List<?> options = optionSet.valuesOf("o");
                for (Object o : options) {
                    String opt = (String) o;
                    try {
                        setOption(opt);
                    } catch (Exception ex) {
                        quitError(optParser, ex.getMessage());
                    }
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

            ShellConsole.install();
            Shell shell;
            int ret;
            /*
             * --command
             */
            if (optionSet.has("command")) {
                String line = (String) optionSet.valueOf("command");
                shell = new Shell(false);
                ret = shell.runCommand(line);
                quit(ret);
            }

            /*
             * --input
             */
            if (optionSet.has("input")) {
                String fileName = (String) optionSet.valueOf("input");
                shell = new Shell(false);
                ret = shell.runFromFile(fileName);
                quit(ret);
            }

            if (optionSet.has("bind") || optionSet.has("port")) {
                String bind = (String) optionSet.valueOf("bind");
                Integer port = Integer.valueOf((String) optionSet.valueOf("port"));
                shell = new Shell(false);
                shell.runFromSocket(bind, port);
                quit(0);
            }

            /*
             * normal shell
             */
            boolean interactive = !optionSet.has("no-interactive");
            shell = new Shell(interactive);
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
