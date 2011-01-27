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

import fr.univrennes1.cri.jtacl.equipments.Generic.GenericEquipmentShell;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
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
			System.out.println(name.getName() + " = " + name.getIpValue() +
					" [" + name.getRefCount() + "]");
		}
	}

	public void shellHelp() {
		try {
			InputStream stream = null;
			stream = this.getClass().getResourceAsStream("/help/pix");
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
			System.out.println(group.getGroupId() + " [" + group.getRefCount() + "]");
		}
	}

	public PixShell(Pix pix) {
		_pix = pix;
		_shellParser = Parboiled.createParser(PixShellParser.class);
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

		return false;
	}
}
