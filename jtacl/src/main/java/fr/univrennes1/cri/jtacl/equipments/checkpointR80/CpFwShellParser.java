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

package fr.univrennes1.cri.jtacl.equipments.checkpointR80;

import fr.univrennes1.cri.jtacl.equipments.generic.GenericEquipmentShellParser;
import org.parboiled.Rule;

/**
 * CpFw sub shell parser
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class CpFwShellParser extends GenericEquipmentShellParser {

	protected String _service;
	protected String _network;


	@Override
	protected boolean clear() {
		_service = null;
		_network = null;
		return super.clear();
	}

	public String getService() {
		return _service;
	}

	public boolean setService(String service) {
		_service = service;
		return true;
	}

	public String getNetwork() {
		return _network;
	}

	public boolean setNetwork(String network) {
		_network = network;
		return true;
	}

	public Rule CommandLine() {
		return
			Sequence(
				clear(),
				FirstOf(
					CommandHelp(),
					CommandXrefIp(),
					CommandXrefService(),
					CommandShowService(),
					CommandShowNetwork(),
					CommandShowRules()
				)
			);
	}

	public Rule CommandShowService() {
		return
			Sequence(
				IgnoreCase("show"),
				WhiteSpaces(),
				IgnoreCase("service"),
				FirstOf(
					WhiteSpaces(),
					EOI
				),
				UntilEOI(),
				setService(match().trim()),
				setCommand("show-service")
		);
	}

	public Rule CommandShowNetwork() {
		return
			Sequence(
				IgnoreCase("show"),
				WhiteSpaces(),
				IgnoreCase("network"),
				FirstOf(
					WhiteSpaces(),
					EOI
				),
				UntilEOI(),
				setNetwork(match().trim()),
				setCommand("show-network")
		);
	}

	public Rule CommandShowRules() {
		return
			Sequence(
				IgnoreCase("show"),
				WhiteSpaces(),
				IgnoreCase("rules"),
				SkipSpaces(),
				EOI,
				setCommand("show-rules")
		);
	}

}
