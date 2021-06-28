/*
 * Copyright (c) 2021, Universite de Rennes 1
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

package fr.univrennes1.cri.jtacl.equipments.proxmox;

import fr.univrennes1.cri.jtacl.core.network.NetworkEquipment;
import fr.univrennes1.cri.jtacl.equipments.generic.GenericEquipmentShell;
import org.parboiled.Parboiled;
import org.parboiled.buffers.InputBuffer;
import org.parboiled.errors.ParseError;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

import java.io.PrintStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * proxmox fw sub shell command
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PxFwShell extends GenericEquipmentShell {

	protected PxEquipment _pxfw;
	protected PxShellParser _shellParser;
	protected ReportingParseRunner _parseRunner;
	protected PrintStream _outStream;

	@Override
	public void shellHelp(PrintStream output) {
		printHelp(output, "/help/proxmox");
	}

	public PxFwShell(PxEquipment pxfw) {
		_pxfw = pxfw;
		_shellParser = Parboiled.createParser(PxShellParser.class);
		_parseRunner = new ReportingParseRunner(_shellParser.CommandLine());
	}

	public void commandShowAlias(PrintStream output, PxShellParser parser) {

		String ident = parser.getIdent();
		Map<String, PxNetworkAlias> aliases = _pxfw.getAllAliases();;

		if (!ident.isEmpty()) {
			PxNetworkAlias pxAlias = aliases.get(ident);
			if (pxAlias == null) {
				output.println("No such alias: " + ident);
			} else {
				output.println(pxAlias.toString());
			}
		} else {
			List<String> snames = new LinkedList(aliases.keySet());
			Collections.sort(snames);
			for (String name: snames) {
				PxNetworkAlias pxAlias = aliases.get(name);
				output.println(pxAlias.toString());
			}
		}
	}

	public void commandShowIpSet(PrintStream output, PxShellParser parser) {

		String ident = parser.getIdent();
		Map<String, PxNetworkIpSet> ipsets = _pxfw.getAllIpsets();

		if (!ident.isEmpty()) {
			PxNetworkIpSet ipset = ipsets.get(ident);
			if (ipset == null) {
				output.println("No such ipset: " + ident);
			} else {
				output.println(ipsets.toString());
			}
		} else {
			List<String> snames = new LinkedList(ipsets.keySet());
			Collections.sort(snames);
			for (String name: snames) {
				PxNetworkIpSet ipset = ipsets.get(name);
				output.println(ipset.toString());
			}
		}
	}


	public void commandShowRules(PrintStream output, PxShellParser parser)
    {
		String ident = parser.getIdent();
		if (!ident.isEmpty()) {
			PxGroupRules grules = _pxfw.findGroupRules(ident);
			if (grules == null) {
				output.println("No such rule group: " + ident);
			} else {
				for (PxRule rule: grules) {
					output.println(rule.toText());
				}
			}
		} else {
			PxGroupRules grules = _pxfw._rules;
			for (PxRule rule: grules) {
				output.println(rule.toText());
			}
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
			printXrefIp(_outStream, _pxfw.getNetCrossRef(), _shellParser);
		if (shellCmd.equals("xref-service"))
			printXrefService(_outStream, _pxfw.getServiceCrossRef(), _shellParser);
		if (shellCmd.equals("show-ipset"))
			commandShowIpSet(_outStream, _shellParser);
		if (shellCmd.equals("show-alias"))
			commandShowAlias(_outStream, _shellParser);
		if (shellCmd.equals("show-rules"))
			commandShowRules(_outStream, _shellParser);

	}

	@Override
	public NetworkEquipment getEquipment() {
		return _pxfw;
	}
}
