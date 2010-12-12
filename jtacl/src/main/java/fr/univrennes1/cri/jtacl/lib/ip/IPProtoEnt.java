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

package fr.univrennes1.cri.jtacl.lib.ip;

import java.util.ArrayList;
import java.util.List;

/**
 * Protocol definition:<br/>
 * <ul>
 * <li>name: the official name of the protocol.</li>
 * <li>aliases: alternate names for the protocol.</li>
 * <li>proto: the protocol number.</li>
 * </ul>
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class IPProtoEnt {

	protected String _name;
	protected List<String> _aliases;
	protected int _proto;

	/**
	 * Constructs a new protocol definition.
	 * @param name the official name of the protocol.
	 * @param aliases alternate names for the protocol.
	 * @param proto the protocol number.
	 */
	public IPProtoEnt(String name, List<String> aliases, int proto) {
		_name = name;
		_aliases = new ArrayList<String>();
		_aliases.addAll(aliases);
		_proto = proto;
	}

	/**
	 * Returns the list of the aliases of this protocol.
	 * @return a List<String> containing the aliases. The list could be empty but not nul.
	 */
	public List<String> getAliases() {
		return _aliases;
	}

	/**
	 * Returns the name of this protocol.
	 * @return the name of this protocol.
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Returns the protocol number of this protocol.
	 * @return the protocol number of this protocol.
	 */
	public int getProto() {
		return _proto;
	}

}
