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
import fr.univrennes1.cri.jtacl.equipments.generic.GenericEquipmentShell;
import fr.univrennes1.cri.jtacl.lib.ip.IPNet;
import java.io.PrintStream;
import java.net.UnknownHostException;
import java.util.Scanner;
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

	protected void commandXrefIp(PacketFilterShellParser parser) {

		IPNet ipreq = null;
		if (parser.getXrefIp() != null) {
			try {
				ipreq = new IPNet(parser.getXrefIp());
			} catch (UnknownHostException ex) {
				_outStream.println("Error: " + ex.getMessage());
				return;
			}
		}

		String format = parser.getXrefFormat();
		if (format != null)
			format = format.toLowerCase();
		boolean fshort = format != null && format.contains("s");
		boolean flong = format != null && format.contains("l");
		boolean fhost = format != null && format.contains("h");
		boolean fptr = format != null && format.contains("p");

		for (IPNet ip: _pf.getNetCrossRef().keySet()) {
			IPNetCrossRef crossref = _pf.getNetCrossRef().get(ip);
			try {
				if (ipreq != null && !ipreq.overlaps(ip))
					continue;
				if (parser.getXrefHost() != null && !ip.isHost())
					continue;
			}
			catch (UnknownHostException ex) {
				_outStream.println("Error " + ex.getMessage());
				return;
			}
			for (CrossRefContext ctx: crossref.getContexts()) {
				_outStream.print(ip.toString("::i"));
				if (fhost || fptr) {
					try {
						String hostname = fhost ? ip.getCannonicalHostname() :
							ip.getHostname();
						_outStream.print("; " + hostname);
					} catch (UnknownHostException ex) {
						_outStream.print("; nohost");
					}
				}
				_outStream.print("; " + ctx.getContextName());
				_outStream.print("; " + ctx.getComment());
				String line = ctx.getParseContext().getLine().trim();
				if (fshort) {
					_outStream.print("; ");
					Scanner sc = new Scanner(line);
					if (sc.hasNextLine())
						_outStream.println(sc.nextLine());
					else
						_outStream.println(line);
				} else {
					if (flong) {
						_outStream.print("; ");
						_outStream.println(line);
					} else {
						_outStream.println();
					}
				}
			}
		}
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
			commandXrefIp(_shellParser);
			return true;
		}

		return false;
	}
}
