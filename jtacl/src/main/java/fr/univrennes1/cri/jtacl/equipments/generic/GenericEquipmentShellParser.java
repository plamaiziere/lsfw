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

package fr.univrennes1.cri.jtacl.equipments.generic;

import fr.univrennes1.cri.jtacl.lib.misc.CommonRules;
import java.util.ArrayList;
import java.util.List;
import org.parboiled.Action;
import org.parboiled.Context;
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

	public void clear() {
		_command = "";
		_param = new ArrayList<String>();
		_xrefObject = null;
		_xrefFormat = null;
		_xrefIp = null;
	}

	public String getCommand() {
		return _command;
	}

	public void setCommand(String command) {
		_command = command;
	}

	public List<String> getParam() {
		return _param;
	}

	public String getXrefObject() {
		return _xrefObject;
	}
	public void setXrefObject(String xrefObject) {
		_xrefObject = xrefObject;
	}

	public String getXrefFormat() {
		return _xrefFormat;
	}

	public void setXrefFormat(String xrefFormat) {
		_xrefFormat = xrefFormat;
	}

	public String getXrefIp() {
		return _xrefIp;
	}

	public void setXrefIp(String xrefIp) {
		_xrefIp = xrefIp;
	}

	public Rule CommandHelp() {
		return Sequence(
			IgnoreCase("help"),
			new Action() {
				public boolean run(Context context) {
					setCommand("help");
					return true;
				}
			},
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
					new Action() {
						public boolean run(Context context) {
							setXrefObject("ip");
							return true;
						}
					},
					Optional(
						Sequence(
							WhiteSpaces(),
							FirstOf(
								IgnoreCase("long"),
								IgnoreCase("short")
							),
							new Action() {
								public boolean run(Context context) {
								setXrefFormat(context.getMatch().toLowerCase());
								return true;
								}
							}
						)
					),
					Optional(
						Sequence(
							WhiteSpaces(),
							StringAtom(),
							new Action() {
								public boolean run(Context context) {
									setXrefIp(context.getMatch());
									return true;
								}
							}
						)
					)
				)
			),
			new Action() {
				public boolean run(Context context) {
					setCommand("xref");
					return true;
				}
			},
			EOI
		);
	}

}
