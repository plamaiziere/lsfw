/*
 * Copyright (c) 2013 - 2018, Universite de Rennes 1
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

package fr.univrennes1.cri.jtacl.equipments.fortigate;

import fr.univrennes1.cri.jtacl.core.network.NetworkEquipment;
import fr.univrennes1.cri.jtacl.equipments.generic.GenericEquipmentShell;
import org.parboiled.Parboiled;
import org.parboiled.buffers.InputBuffer;
import org.parboiled.errors.ParseError;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;

/**
 * Fortigate fw sub shell command
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class FgFwShell extends GenericEquipmentShell {

	protected FgFw _fgfw;
	protected FgFwShellParser _shellParser;
	protected ReportingParseRunner _parseRunner;
	protected PrintStream _outStream;

	@Override
	public void shellHelp(PrintStream output) {
		printHelp(output, "/help/fortigate");
	}

	public FgFwShell(FgFw cpfw) {
		_fgfw = cpfw;
		_shellParser = Parboiled.createParser(FgFwShellParser.class);
		_parseRunner = new ReportingParseRunner(_shellParser.CommandLine());
	}

	public void commandShowService(PrintStream output, FgFwShellParser parser) {

		String service = parser.getService();
		Map<String, FgService> services = _fgfw.getFgServices();

		if (!service.isEmpty()) {
			FgService servobj = services.get(service);
			if (servobj == null) {
				output.println("No such service: " + service);
			} else {
				output.println(servobj.toString());
			}
		} else {
			List<String> snames = _fgfw.getServicesName();
			for (String name: snames) {
				FgService servobj = services.get(name);
				output.println(servobj.toString());
			}
		}
	}

	public void commandShowNetwork(PrintStream output, FgFwShellParser parser) {

		String network = parser.getNetwork();
		Map<String, FgNetworkObject> networks = _fgfw.getFgNetworks();

		if (!network.isEmpty()) {
			FgNetworkObject netobj = networks.get(network);
			if (netobj == null) {
				output.println("No such network: " + network);
			} else {
				output.println(netobj.toString());
			}
		} else {
			List<String> snames = _fgfw.getNetworksName();
			for (String name: snames) {
				FgNetworkObject netobj = networks.get(name);
				output.println(netobj.toString());
			}
		}
	}

	public void commandShowRules(PrintStream output, FgFwShellParser parser)
    {
        //showLayer(output, _cpfw.getRootLayer());
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
			printXrefIp(_outStream, _fgfw.getNetCrossRef(), _shellParser);
		if (shellCmd.equals("xref-service"))
			printXrefService(_outStream, _fgfw.getServiceCrossRef(), _shellParser);
		if (shellCmd.equals("show-service"))
			commandShowService(_outStream, _shellParser);
		if (shellCmd.equals("show-network"))
			commandShowNetwork(_outStream, _shellParser);
		if (shellCmd.equals("show-rules"))
			commandShowRules(_outStream, _shellParser);

	}

	@Override
	public NetworkEquipment getEquipment() {
		return _fgfw;
	}
}
