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

import fr.univrennes1.cri.jtacl.App;
import fr.univrennes1.cri.jtacl.core.exceptions.JtaclConfigurationException;
import fr.univrennes1.cri.jtacl.core.exceptions.JtaclParameterException;
import fr.univrennes1.cri.jtacl.core.exceptions.JtaclRuntimeException;
import fr.univrennes1.cri.jtacl.core.monitor.Log;
import fr.univrennes1.cri.jtacl.core.monitor.Monitor;
import fr.univrennes1.cri.jtacl.core.network.NetworkEquipment;
import fr.univrennes1.cri.jtacl.core.network.NetworkEquipmentsByName;
import fr.univrennes1.cri.jtacl.core.probing.AclResult;
import fr.univrennes1.cri.jtacl.core.probing.ProbesTracker;
import fr.univrennes1.cri.jtacl.core.probing.Probing;
import fr.univrennes1.cri.jtacl.core.probing.RoutingResult;
import fr.univrennes1.cri.jtacl.core.topology.NetworkLink;
import fr.univrennes1.cri.jtacl.core.topology.NetworkLinks;
import fr.univrennes1.cri.jtacl.lib.ip.IPNet;
import groovy.lang.Binding;
import groovy.ui.Console;
import groovy.util.GroovyScriptEngine;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.UnknownHostException;
import java.util.*;
import java.util.logging.Level;
import org.parboiled.Parboiled;
import org.parboiled.buffers.InputBuffer;
import org.parboiled.errors.ParseError;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

/**
 * lsfw shell
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class Shell {

	protected String _prompt = "lsfw> ";
	protected ShellParser _parser = Parboiled.createParser(ShellParser.class);
	protected ReportingParseRunner _parseRunner =
		new ReportingParseRunner(_parser.CommandLine());
	protected Monitor _monitor = Monitor.getInstance();;
	protected boolean _interactive;
	protected Probing _lastProbing;
	protected boolean _testResult;
	protected PrintStream _outStream = System.out;

	static protected final List<String> _specialPorts = Arrays.asList(
		"none", "any", "known", "reg", "dyn");

	static protected final List<String> _expectStrings = Arrays.asList(
		"ROUTED", "NONE-ROUTED", "UNKNOWN", "ACCEPT", "DENY", "MAY",
		"UNACCEPTED");

	public Shell() {
		_interactive = false;
	}

	public Shell(boolean interactive) {
		_interactive = interactive;
	}

	protected String substitute(String line) {

		String r = line;
		for (String s: _monitor.getDefines().keySet()) {
			r = r.replace("$" + s, _monitor.getDefines().get(s));
		}
		return r;
	}

	protected void autoReload() {

		for (String eqName: _monitor.getEquipments().keySet()) {

			NetworkEquipment equipment =
					_monitor.getEquipments().get(eqName);
			if (equipment.hasChanged()) {
				Log.debug().info("reloading " + eqName);
				try {
					_monitor.reloadEquipment(equipment);
				} catch (JtaclRuntimeException ex) {
					System.err.println("Error: " + ex.getMessage());
				}
			}
		}
	}

	protected LsfwBinding newBinding(String args) {
		return new LsfwBinding(args);
	}

	public void helpCommand(ShellParser command) {
		try {
			String topic = command.getHelpTopic();
			if (topic == null) {
				topic = "help";
			}
			InputStream stream = null;
			do {
				stream = this.getClass().getResourceAsStream("/help/" + topic);
				if (stream == null) {
					topic = "help";
				}
			} while (stream == null);
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			for (;;) {
				String line = reader.readLine();
				if (line == null) {
					break;
				}
				_outStream.println(line);
			}
			reader.close();
			/*
			 * system informations
			 */
			if (topic.equals("help")) {
				_outStream.println();
				_outStream.println("--- System informations ---");
				_outStream.println("Java runtime: " +
					System.getProperty("java.runtime.name") + " (" +
					System.getProperty("java.runtime.version") + ")");
				_outStream.println("Java VM: " +
					System.getProperty("java.vm.name") + " (" +
					System.getProperty("java.vm.version") + ")");

				_outStream.println("OS: " +
					System.getProperty("os.name") + " " +
					System.getProperty("os.version") + "/" +
					System.getProperty("os.arch"));
			}
		} catch (IOException ex) {
			_outStream.println("cannot print help");
		}

	}

	public void optionCommand(ShellParser command) {
	String name = command.getSetValueName();
	String value = command.getSetValueValue();

		if (name != null && value != null)
			try {
				_monitor.getOptions().setOption(name, value);
			} catch (JtaclConfigurationException e) {
				_outStream.println("Error: " + e.getMessage());
			}
		else
			_outStream.println(_monitor.getOptions().getOptionsList());
	}

	public void defineCommand(ShellParser command) {
		String name = command.getSetValueName();
		String value = command.getSetValueValue();

		if (name != null) {
			String define = _monitor.getDefines().get(name);
			if (define != null)
				_monitor.getDefines().remove(name);
			if (value != null) {
				_monitor.getDefines().put(name, value);
			}
		} else {
			for (String d: _monitor.getDefines().keySet()) {
				_outStream.println(d + "=" + _monitor.getDefines().get(d));
			}
		}

	}

	public void topologyCommand(ShellParser command) {

		String equipmentName = command.getEquipments();
		String option = command.getTopologyOption();

		/*
		 * Try to parse the value as an IP. If ok, display network links
		 * containing this IP. Overwise this is an equipment.
		 */
		IPNet ip = null;
		NetworkEquipment equipment = null;

		if (equipmentName != null) {
			try {
				ip = new IPNet(equipmentName);
			} catch (UnknownHostException ex) {
				// do nothing
			}
			equipment = null;
			if (ip == null) {
				equipment = _monitor.getEquipments().get(equipmentName);
				if (equipment == null) {
					_outStream.println("No such equipment: " + equipmentName);
					return;
				}
			}
		}
		/*
		 * filter out each link by IP or equipment.
		 */
		NetworkLinks links = _monitor.getTopology().getNetworkLinks();
		Collections.sort(links);

		for (NetworkLink link: links) {
			NetworkLink candidate = null;
			/*
			 * IP
			 */
			if (ip != null) {
				try {
					if (link.getNetwork().overlaps(ip)) {
						candidate = link;
					}
				} catch (UnknownHostException ex) {
					_outStream.println("Error " + ex.getMessage());
					return;
				}
			} else {
				/*
				 * Equipment
				 */
				if (equipmentName != null) {
					if (link.isConnectedTo(equipment))
						candidate = link;
				}
				else {
					candidate = link;
				}
			}
			/*
			 * filter out by connected or !connected.
			 * a NetworkLink is 'connected' if it is associated to a least two
			 * IfaceLink.
			 */
			if (candidate != null && option != null) {
				if (option.equalsIgnoreCase("connected")) {
					if (!candidate.isInterconnection())
						candidate = null;
				}
				if (option.equalsIgnoreCase("!connected")) {
					if (candidate.isInterconnection())
						candidate = null;
				}
			}
			/*
			 * output
			 */
			if (candidate != null)
				_outStream.println(candidate.toString());

		}
	}

	public void routeCommand(ShellParser command) {

		String equipmentName = command.getEquipments();
		if (equipmentName != null) {
			if (!_monitor.getEquipments().containsKey(equipmentName)) {
				_outStream.println("No such equipment: " + equipmentName);
				return;
			}
		}
		NetworkEquipmentsByName equipments = _monitor.getEquipments();
		for (NetworkEquipment equipment: equipments.values()) {
			if (equipmentName== null || equipment.getName().equals(equipmentName)) {
				_outStream.println("Routes on " + equipment.getName());
				_outStream.println("-------------------");
				_outStream.println(equipment.getShowableRoutes().showRoutes());
				_outStream.println("-------------------");
				_outStream.println();
			}
		}
	}

	public void equipmentCommand(ShellParser command) {

		String equipmentName = command.getEquipments();
		NetworkEquipment equipment = _monitor.getEquipments().get(equipmentName);
		if (equipment == null) {
			_outStream.println("No such equipment: " + equipmentName);
			return;
		}

		equipment.runShell(command.getSubCommand(), _outStream);
	}

	public void reloadCommand(ShellParser command) {

		String equipmentName = command.getEquipments();

		if (equipmentName == null) {
			_monitor.reload();
			return;
		}

		NetworkEquipment equipment = _monitor.getEquipments().get(equipmentName);
		if (equipment == null) {
			_outStream.println("No such equipment: " + equipmentName);
			return;
		}

		try {
			_monitor.reloadEquipment(equipment);
		} catch (JtaclRuntimeException ex) {
			System.err.println("Error: " + ex.getMessage());
		}
	}

	public boolean probeCommand(String commandLine, ShellParser command) {

		ProbeCommandTemplate probeCmd = command.getProbeCmdTemplate();
		boolean testMode = probeCmd.getProbeExpect() != null;
		boolean learnMode = probeCmd.getProbeOptLearn();
		boolean silent = testMode || learnMode;

		ProbeCommand cmd = new ProbeCommand();
		try {
			cmd.buildRequest(probeCmd);
		} catch (JtaclParameterException ex) {
			_outStream.println(ex.getMessage());
			return false;
		}

		String expect = probeCmd.getProbeExpect();
		if (expect == null)
			expect = "";

		/*
		 * probe
		 */
		cmd.runCommand();
		_lastProbing = cmd.getProbing();

		/*
		 * results
		 */

		boolean verbose = probeCmd.getProbeOptVerbose();
		boolean active = probeCmd.getProbeOptActive();
		boolean matching = probeCmd.getProbeOptMatching();

		if (verbose) {
			active = true;
			matching = true;
		}

		if (!active)
			matching = true;
		/*
		 * each tracker
		 */
		for (ProbesTracker tracker: _lastProbing) {
			if (!silent) {
				ShellReport report = new ShellReport(tracker, verbose, active,
					matching);
				_outStream.print(report.showResults());
			}
		}

		/*
		 * Global ACL result
		 */
		AclResult aclResult = _lastProbing.getAclResult();

		/*
		 * Global routing result
		 */
		RoutingResult routingResult = _lastProbing.getRoutingResult();

		if (!silent) {
			_outStream.println("Global ACL result is: " + aclResult);
			_outStream.println("Global routing result is: " + routingResult);
			_outStream.println();
		}

		/*
		 * XXX we need a better logic here.
		 */
		boolean testExpect = false;
		boolean notExpect = expect.startsWith("!");
		if (notExpect && expect.length() > 1)
			expect = expect.substring(1);

		expect = expect.toUpperCase();

		if (!expect.isEmpty() && !_expectStrings.contains(expect)) {
			_outStream.println("invalid expect: " + expect);
			return false;
		}

		if (expect.equals("ROUTED") &&
				routingResult == RoutingResult.ROUTED)
			testExpect = true;
		if (expect.equals("NONE-ROUTED") &&
				routingResult == RoutingResult.NOTROUTED)
			testExpect = true;
		if (expect.equals("UNACCEPTED") &&
				(routingResult == RoutingResult.NOTROUTED ||
				(aclResult.hasDeny() && !aclResult.hasMay())))
			testExpect = true;
		if (expect.equals("UNKNOWN") &&
				routingResult == RoutingResult.UNKNOWN)
			testExpect = true;

		if (expect.equals("ACCEPT") &&
				aclResult.hasAccept() && !aclResult.hasMay())
			testExpect = true;
		if (expect.equals("DENY") &&
				aclResult.hasDeny() && !aclResult.hasMay())
			testExpect = true;

		if (expect.equals("MAY") &&
				aclResult.hasMay())
				testExpect = true;

		if (notExpect)
			testExpect = !testExpect;

		if (testMode) {
			if (!testExpect) {
				_outStream.println(commandLine + " [FAILED]");
			} else {
				_outStream.println(commandLine + " [OK]");
			}
		}

		if (learnMode) {
			_outStream.print(commandLine);
			_outStream.print(" [ACL: " + aclResult);
			_outStream.print("; Routing: " + routingResult);
			_outStream.println("]");
		}

		return testExpect;
	}

	public void groovyCommand(ShellParser parser) {

		Binding binding = new Binding();
		LsfwBinding lsfw = newBinding(parser.getGroovyArgs());
		binding.setVariable("lsfw", lsfw);

		GroovyScriptEngine scriptEngine;
		try {
			scriptEngine = new GroovyScriptEngine(parser.getGroovyDirectory());
		} catch (IOException ex) {
			_outStream.println(ex.getMessage());
			return;
		}
		try {
			scriptEngine.run(parser.getGroovyScript(), binding);
		} catch (Exception ex) {
			_outStream.println();
			_outStream.println("Error: " + ex.getMessage());
		}
	}

	public void groovyConsoleCommand(ShellParser parser) {
		Binding binding = new Binding();
		LsfwBinding lsfw = newBinding(parser.getGroovyArgs());
		binding.setVariable("lsfw", lsfw);
		Console console = new Console(binding);
		console.run();
	}

	public void parseShellCommand(String commandLine) {

		if (Log.debug().isLoggable(Level.INFO))
			Log.debug().info(commandLine);

		// comment
		if (commandLine.startsWith("#"))
			return;
		// macro substitution
		String subs = substitute(commandLine);

		// tee redirection
		boolean teeAppend = false;
		String teeFile = null;
		int p = subs.lastIndexOf("|>>");
		if (p != -1) {
			teeAppend = true;
			teeFile = subs.substring(p + 3);
			teeFile = teeFile.trim();
			subs = subs.substring(0, p ).trim();

		} else {
			p = subs.lastIndexOf("|>");
			if (p != -1) {
				teeAppend = false;
				teeFile = subs.substring(p + 2);
				teeFile = teeFile.trim();
				subs = subs.substring(0, p ).trim();
			}
		}

		// empty command
		if (subs.isEmpty())
			return;

		_parseRunner.getParseErrors().clear();
		ParsingResult<?> result = _parseRunner.run(subs);

		if (!result.matched) {
			if (result.hasErrors()) {
				ParseError error = result.parseErrors.get(0);
				InputBuffer buf = error.getInputBuffer();
				_outStream.println("Syntax error: " +
					buf.extract(0, error.getStartIndex()));
			}
		}

		/*
		 * tee stdout
		 */
		if (teeFile != null) {
			try {
				ShellConsole.out().tee(teeFile, teeAppend);
			} catch (FileNotFoundException ex) {
				_outStream.println("Cannot open file: " + teeFile);
				return;
			}
		}

		if (_parser.getCommand().equals("quit")) {
			if (_interactive)
				_outStream.println("Goodbye!");
			System.exit(0);
		}

		if (_interactive && _monitor.getOptions().getAutoReload())
			autoReload();

		if (_parser.getCommand().equals("probe") ||
			  _parser.getCommand().equals("probe6")) {
			boolean test = probeCommand(commandLine, _parser);
			if (_parser.getProbeCmdTemplate().getProbeExpect() != null) {
				if (!test)
					_testResult = false;
			}
		}
		if (_parser.getCommand().equals("option"))
			optionCommand(_parser);
		if (_parser.getCommand().equals("topology"))
			topologyCommand(_parser);
		if (_parser.getCommand().equals("route"))
			routeCommand(_parser);
		if (_parser.getCommand().equals("help"))
			helpCommand(_parser);
		if (_parser.getCommand().equals("define"))
			defineCommand(_parser);
		if (_parser.getCommand().equals("equipment"))
			equipmentCommand(_parser);
		if (_parser.getCommand().equals("reload"))
			reloadCommand(_parser);
		if (_parser.getCommand().equals("groovy"))
			groovyCommand(_parser);
		if (_parser.getCommand().equals("groovyconsole"))
			groovyConsoleCommand(_parser);

		/*
		 * 'untee' stdout
		 */
		if (teeFile != null) {
			try {
				ShellConsole.out().unTee();
			} catch (IOException ex) {
				_outStream.println("Cannot unTee file: " + teeFile);
			}
		}
	}

	public int runCommand(String commandLine) {
		_testResult = true;
		parseShellCommand(commandLine.trim());
		if (!_testResult)
			return App.EXIT_FAILURE;
		return App.EXIT_SUCCESS;
	}

	public int runFromFile(String fileName) {
		_testResult = true;
		BufferedReader dataIn = null;
		if (fileName == null) {
			 dataIn = new BufferedReader(new InputStreamReader(System.in));
		} else {
			try {
				dataIn = new BufferedReader(new FileReader(fileName));
			} catch (FileNotFoundException ex) {
				throw new JtaclRuntimeException(ex.getMessage());
			}
		}

		for (;;) {
			if (_interactive)
				_outStream.print(_prompt);
			String commandLine;
			try {
				commandLine = dataIn.readLine();
			} catch (IOException ex) {
				throw new JtaclRuntimeException(ex.getMessage());
			}
			if (commandLine == null) {
				break;
			}
			commandLine = commandLine.trim();
			parseShellCommand(commandLine);
		}
		try {
			dataIn.close();
		} catch (IOException ex) {
				throw new JtaclRuntimeException(ex.getMessage());
		}
		if (!_testResult)
			return App.EXIT_FAILURE;
		return App.EXIT_SUCCESS;
	}

	public PrintStream getOutputStream() {
		return _outStream;
	}

	public void setOutputStream(PrintStream outStream) {
		_outStream = outStream;
	}

}