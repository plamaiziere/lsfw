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

import fr.univrennes1.cri.jtacl.analysis.CrossRefContext;
import fr.univrennes1.cri.jtacl.analysis.IPNetCrossRef;
import fr.univrennes1.cri.jtacl.equipments.Generic.GenericEquipmentShell;
import fr.univrennes1.cri.jtacl.lib.ip.IPNet;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.parboiled.Parboiled;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

/**
 * PacketFilter Jtacl sub shell command
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PacketFilterShell implements GenericEquipmentShell {

	protected PacketFilter _pf;
	protected PacketFilterShellParser _shellParser;

	public void shellHelp() {
		try {
			InputStream stream = null;
			stream = this.getClass().getResourceAsStream("/help/pf");
			if (stream == null) {
				System.out.println("cannot print help");
				return;
			}
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			for (;;) {
				String line = reader.readLine();
				if (line == null) {
					break;
				}
				System.out.println(line);
			}
			reader.close();
		} catch (IOException ex) {
			System.out.println("cannot print help");
		}

	}

	protected void commandXrefIp(PacketFilterShellParser parser) {

		for (IPNet ip: _pf.getNetCrossRef().keySet()) {
			IPNetCrossRef crossref = _pf.getNetCrossRef().get(ip);
			System.out.println(ip);
			for (CrossRefContext ctx: crossref.getContexts()) {
				System.out.println(ctx.getContextName());
				System.out.println(ctx.getParseContext().getLine());
			}
		}
	}

	public PacketFilterShell(PacketFilter pf) {
		_pf = pf;
		_shellParser = Parboiled.createParser(PacketFilterShellParser.class);
	}

	public boolean shellCommand(String command) {

		ParsingResult<?> result = ReportingParseRunner.run(_shellParser.CommandLine(),
			command);

		if (!result.matched) {
			return false;
		}

		String shellCmd = _shellParser.getCommand();

		if (shellCmd.equals("help")) {
			shellHelp();
			return true;
		}

		if (shellCmd.equals("xref")) {
			commandXrefIp(_shellParser);
			return true;
		}

		return false;
	}
}
