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

package fr.univrennes1.cri.jtacl.parsers;

import java.util.ArrayList;
import java.util.List;
import org.parboiled.Rule;

/**
 * Generic Equipment sub shell rules
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class GenericEquipmentShellParser extends CommonRules<Object> {

	protected String _command = "";
	protected List<String> _param = null;
	protected String _xrefObject = null;
	protected String _xrefFormat = null;
	protected String _xrefIp = null;

	protected boolean clear() {
		_command = "";
		_param = new ArrayList<String>();
		_xrefObject = null;
		_xrefFormat = null;
		_xrefIp = null;
		return true;
	}

	public String getCommand() {
		return _command;
	}

	public boolean setCommand(String command) {
		_command = command;
		return true;
	}

	public List<String> getParam() {
		return _param;
	}

	public String getXrefObject() {
		return _xrefObject;
	}

	public boolean setXrefObject(String xrefObject) {
		_xrefObject = xrefObject;
		return true;
	}

	public String getXrefFormat() {
		return _xrefFormat;
	}

	public boolean setXrefFormat(String xrefFormat) {
		_xrefFormat = xrefFormat;
		return true;
	}

	public String getXrefIp() {
		return _xrefIp;
	}

	public boolean setXrefIp(String xrefIp) {
		_xrefIp = xrefIp;
		return true;
	}

	public Rule CommandHelp() {
		return Sequence(
			IgnoreCase("help"),
			setCommand("help"),
			EOI
		);
	}

	/**
	 * xref [ip [long|short] [IPaddress]]
	 */
	public Rule CommandXref() {
		return Sequence(
			IgnoreCase("xref"),
			WhiteSpaces(),
			Optional(
				Sequence(
					IgnoreCase("ip"),
					setXrefObject("ip"),
					Optional(
						Sequence(
							WhiteSpaces(),
							FirstOf(
								IgnoreCase("long"),
								IgnoreCase("short")
							),
							setXrefFormat(match().toLowerCase())
						)
					),
					Optional(
						Sequence(
							WhiteSpaces(),
							StringAtom(),
							setXrefIp(match())
						)
					)
				)
			),
			setCommand("xref"),
			EOI
		);
	}

}
