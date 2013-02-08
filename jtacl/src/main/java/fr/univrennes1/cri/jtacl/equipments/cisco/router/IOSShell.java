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

package fr.univrennes1.cri.jtacl.equipments.cisco.router;

import fr.univrennes1.cri.jtacl.core.network.NetworkEquipment;
import fr.univrennes1.cri.jtacl.equipments.generic.GenericEquipmentShell;
import java.io.PrintStream;
import org.parboiled.Parboiled;
import org.parboiled.buffers.InputBuffer;
import org.parboiled.errors.ParseError;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

/**
 * Cisco router sub shell command
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class IOSShell extends GenericEquipmentShell {

	protected CiscoRouter _router;
	protected IOSShellParser _shellParser;
	protected ReportingParseRunner _parseRunner;
	protected PrintStream _outStream;

	@Override
	public void shellHelp(PrintStream output) {
		printHelp(output, "/help/ciscorouter");
	}

	public IOSShell(CiscoRouter router) {
		_router = router;
		_shellParser = Parboiled.createParser(IOSShellParser.class);
		_parseRunner = new ReportingParseRunner(_shellParser.CommandLine());
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
			printXrefIp(_outStream, _router.getNetCrossRef(), _shellParser);
	}

	@Override
	public NetworkEquipment getEquipment() {
		return _router;
	}

}
