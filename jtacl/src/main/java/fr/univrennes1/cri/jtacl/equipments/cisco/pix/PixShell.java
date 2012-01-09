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

package fr.univrennes1.cri.jtacl.equipments.cisco.pix;

import fr.univrennes1.cri.jtacl.analysis.CrossRefContext;
import fr.univrennes1.cri.jtacl.analysis.IPNetCrossRef;
import fr.univrennes1.cri.jtacl.equipments.generic.GenericEquipmentShell;
import fr.univrennes1.cri.jtacl.lib.ip.IPNet;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.TreeMap;
import org.parboiled.Parboiled;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

/**
 * PIX Jtacl sub shell command
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PixShell implements GenericEquipmentShell {

	protected Pix _pix;
	protected PixShellParser _shellParser;
	protected ReportingParseRunner _parseRunner;
	protected PrintStream _outStream;

	protected void commandShowNames(PixShellParser parser) {

		TreeMap<String, PixName> names =
				new TreeMap<String, PixName>(_pix.getNames());

		for (PixName name: names.values()) {
			if (!parser.getParam().isEmpty()) {
				if (parser.getParam().get(0).equals("unused") &&
						name.isUsed())
					continue;
				if (parser.getParam().get(0).equals("used") &&
						!name.isUsed())
					continue;
			}
			_outStream.println(name.getName() + " = " + name.getIpValue() +
					" [" + name.getRefCount() + "]");
		}
	}

	public void shellHelp(PrintStream output) {
		_outStream = output;
		try {
			InputStream stream = null;
			stream = this.getClass().getResourceAsStream("/help/pix");
			if (stream == null) {
				_outStream.println("cannot print help");
				return;
			}
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			for (;;) {
				String line = reader.readLine();
				if (line == null) {
					break;
				}
				_outStream.println(line);
			}
			reader.close();
		} catch (IOException ex) {
			_outStream.println("cannot print help");
		}

	}

	protected void outputGroups(HashMap<String, ObjectGroup> groups, String using) {

		TreeMap<String, ObjectGroup> tgroups =
				new TreeMap<String, ObjectGroup>(groups);

		for (ObjectGroup group: tgroups.values()) {
			if (using != null) {
				if (using.equals("unused") &&
						group.isUsed())
					continue;
				if (using.equals("used") &&
						!group.isUsed())
					continue;
			}
			_outStream.println(group.getGroupId() + " [" + group.getRefCount() + "]");
		}
	}

	protected void commandXrefIp(PixShellParser parser) {

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

		for (IPNet ip: _pix.getNetCrossRef().keySet()) {
			IPNetCrossRef crossref = _pix.getNetCrossRef().get(ip);
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
							ip.getPtrHostname();
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

	public PixShell(Pix pix) {
		_pix = pix;
		_shellParser = Parboiled.createParser(PixShellParser.class);
		_parseRunner = new ReportingParseRunner(_shellParser.CommandLine());
	}

	public boolean shellCommand(String command, PrintStream output) {
		_outStream = output;
		_parseRunner.getParseErrors().clear();
		ParsingResult<?> result = _parseRunner.run(command);

		if (!result.matched) {
			return false;
		}

		String shellCmd = _shellParser.getCommand();

		if (shellCmd.equals("help")) {
			shellHelp(output);
			return true;
		}

		if (shellCmd.equals("show-name")) {
			commandShowNames(_shellParser);
			return true;
		}

		String using = null;
		if (!_shellParser.getParam().isEmpty())
			using = _shellParser.getParam().get(0);

		if (shellCmd.equals("show-enhanced-group")) {
			HashMap<String, ObjectGroup> groups = _pix.getEnhancedGroups();
			outputGroups(groups, using);
			return true;
		}

		if (shellCmd.equals("show-icmp-group")) {
			HashMap<String, ObjectGroup> groups = _pix.getIcmpGroups();
			outputGroups(groups, using);
			return true;
		}

		if (shellCmd.equals("show-network-group")) {
			HashMap<String, ObjectGroup> groups = _pix.getNetworkGroups();
			outputGroups(groups, using);
			return true;
		}

		if (shellCmd.equals("show-protocol-group")) {
			HashMap<String, ObjectGroup> groups = _pix.getProtocolGroups();
			outputGroups(groups, using);
			return true;
		}

		if (shellCmd.equals("show-service-group")) {
			HashMap<String, ObjectGroup> groups = _pix.getServiceGroups();
			outputGroups(groups, using);
			return true;
		}

		if (shellCmd.equals("xref")) {
			commandXrefIp(_shellParser);
			return true;
		}

		return false;
	}
}
