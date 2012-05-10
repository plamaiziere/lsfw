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

package fr.univrennes1.cri.jtacl.equipments.openbsd;

import fr.univrennes1.cri.jtacl.core.network.NetworkEquipment;
import fr.univrennes1.cri.jtacl.equipments.generic.GenericEquipmentShell;
import java.io.PrintStream;
import org.parboiled.Parboiled;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

/**
 * PacketFilter Jtacl sub shell command
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PacketFilterShell extends GenericEquipmentShell {

	protected PacketFilter _pf;
	protected PacketFilterShellParser _shellParser;
	protected ReportingParseRunner _parseRunner;
	protected PrintStream _outStream;

	@Override
	public void shellHelp(PrintStream output) {
		printHelp(output, "/help/pf");
	}

	public PacketFilterShell(PacketFilter pf) {
		_pf = pf;
		_shellParser = Parboiled.createParser(PacketFilterShellParser.class);
		_parseRunner = new ReportingParseRunner(_shellParser.CommandLine());
	}

	@Override
	public boolean shellCommand(String command, PrintStream output) {
		_outStream = output;
		_parseRunner.getParseErrors().clear();
		ParsingResult<?> result = _parseRunner.run(command);

		if (!result.matched) {
			return false;
		}

		String shellCmd = _shellParser.getCommand();

		if (shellCmd.equals("help")) {
			shellHelp(_outStream);
			return true;
		}

		if (shellCmd.equals("xref")) {
			printXrefIp(_outStream, _pf.getNetCrossRef(), _shellParser);
			return true;
		}

		return false;
	}

	@Override
	public NetworkEquipment getEquipment() {
		return _pf;
	}

}
