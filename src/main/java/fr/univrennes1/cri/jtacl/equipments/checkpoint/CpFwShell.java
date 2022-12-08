/*
 * Copyright (c) 2013, Universite de Rennes 1
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

package fr.univrennes1.cri.jtacl.equipments.checkpoint;

import fr.univrennes1.cri.jtacl.core.network.NetworkEquipment;
import fr.univrennes1.cri.jtacl.equipments.generic.GenericEquipmentShell;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import org.parboiled.Parboiled;
import org.parboiled.buffers.InputBuffer;
import org.parboiled.errors.ParseError;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

/**
 * CpFw sub shell command
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class CpFwShell extends GenericEquipmentShell {

	protected CpFw _cpfw;
	protected CpFwShellParser _shellParser;
	protected ReportingParseRunner _parseRunner;
	protected PrintStream _outStream;

	@Override
	public void shellHelp(PrintStream output) {
		printHelp(output, "/help/checkpoint");
	}

	public CpFwShell(CpFw cpfw) {
		_cpfw = cpfw;
		_shellParser = Parboiled.createParser(CpFwShellParser.class);
		_parseRunner = new ReportingParseRunner(_shellParser.CommandLine());
	}

	public void commandShowService(PrintStream output, CpFwShellParser parser) {

		String service = parser.getService();
		Map<String, CpService> services = _cpfw.getServices();

		if (!service.isEmpty()) {
			CpService servobj = services.get(service);
			if (servobj == null) {
				output.println("No such service: " + service);
			} else {
				output.println(servobj.toString());
			}
		} else {
			List<String> snames = _cpfw.getServicesName();
			for (String name: snames) {
				CpService servobj = services.get(name);
				output.println(servobj.toString());
			}
		}
	}

	public void commandShowNetwork(PrintStream output, CpFwShellParser parser) {

		String network = parser.getNetwork();
		Map<String, CpNetworkObject> networks = _cpfw.getNetworkObjects();

		if (!network.isEmpty()) {
			CpNetworkObject netobj = networks.get(network);
			if (netobj == null) {
				output.println("No such network: " + network);
			} else {
				output.println(netobj.toString());
			}
		} else {
			List<String> snames = _cpfw.getNetworksName();
			for (String name: snames) {
				CpNetworkObject netobj = networks.get(name);
				output.println(netobj.toString());
			}
		}
	}

	public void commandShowRules(PrintStream output, CpFwShellParser parser) {

		for (CpFwRule rule: _cpfw.getFwRules()) {
			output.println(rule.toText());
		}
	}


	@Override
	public void shellCommand(String command, PrintStream output) {
		_outStream = output;
		_parseRunner.getParseErrors().clear();
		ParsingResult<?> result = _parseRunner.run(command);

		if (!result.matched) {
			if (result.hasErrors()) {
				ParseError error = result.parseErrors.get(0);
				InputBuffer buf = error.getInputBuffer();
				_outStream.println("Syntax error: " +
					buf.extract(0, error.getStartIndex()));
			}
			return;
		}

		String shellCmd = _shellParser.getCommand();

		if (shellCmd.equals("help"))
			shellHelp(_outStream);
		if (shellCmd.equals("xref-ip"))
			printXrefIp(_outStream, _cpfw.getNetCrossRef(), _shellParser);
		if (shellCmd.equals("xref-service"))
			printXrefService(_outStream, _cpfw.getServiceCrossRef(), _shellParser);
		if (shellCmd.equals("show-service"))
			commandShowService(_outStream, _shellParser);
		if (shellCmd.equals("show-network"))
			commandShowNetwork(_outStream, _shellParser);
		if (shellCmd.equals("show-rules"))
			commandShowRules(_outStream, _shellParser);

	}

	@Override
	public NetworkEquipment getEquipment() {
		return _cpfw;
	}

}
