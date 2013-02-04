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

import fr.univrennes1.cri.jtacl.core.network.NetworkEquipment;
import fr.univrennes1.cri.jtacl.equipments.generic.GenericEquipmentShell;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.TreeMap;
import org.parboiled.Parboiled;
import org.parboiled.buffers.InputBuffer;
import org.parboiled.errors.ParseError;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

/**
 * PIX Jtacl sub shell command
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PixShell extends GenericEquipmentShell {

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

	@Override
	public void shellHelp(PrintStream output) {
		printHelp(output, "/help/pix");
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

	public PixShell(Pix pix) {
		_pix = pix;
		_shellParser = Parboiled.createParser(PixShellParser.class);
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
			shellHelp(output);

		if (shellCmd.equals("show-name"))
			commandShowNames(_shellParser);

		String using = null;
		if (!_shellParser.getParam().isEmpty())
			using = _shellParser.getParam().get(0);

		if (shellCmd.equals("show-enhanced-group")) {
			HashMap<String, ObjectGroup> groups = _pix.getEnhancedGroups();
			outputGroups(groups, using);
		}

		if (shellCmd.equals("show-icmp-group")) {
			HashMap<String, ObjectGroup> groups = _pix.getIcmpGroups();
			outputGroups(groups, using);
		}

		if (shellCmd.equals("show-network-group")) {
			HashMap<String, ObjectGroup> groups = _pix.getNetworkGroups();
			outputGroups(groups, using);
		}

		if (shellCmd.equals("show-protocol-group")) {
			HashMap<String, ObjectGroup> groups = _pix.getProtocolGroups();
			outputGroups(groups, using);
		}

		if (shellCmd.equals("show-service-group")) {
			HashMap<String, ObjectGroup> groups = _pix.getServiceGroups();
			outputGroups(groups, using);
		}

		if (shellCmd.equals("xref")) {
			printXrefIp(_outStream, _pix.getNetCrossRef(), _shellParser);
		}
	}

	@Override
	public NetworkEquipment getEquipment() {
		return _pix;
	}

}
