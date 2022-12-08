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
 * Service definition:<br/>
 * <ul>
 * <li>name: the official name of the service.</li>
 * <li>aliases: alternate names for the service.</li>
 * <li>port: the port number at which the service resides.</li>
 * <li>proto: the name of the protocol to use when contacting the service.</li>
 * </ul>
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class IPServEnt {

	protected String _name;
	protected List<String> _aliases;
	protected int _port;
	protected String _proto;

	/**
	 * Constructs a new service definition.
	 * @param name the official name of the service.
	 * @param aliases alternate names for the service.
	 * @param port the port number at which the service resides.
	 * @param proto the name of the protocol to use when contacting the service.
	 */
	public IPServEnt(String name, List<String> aliases, int port, String proto) {
		_name = name;
		_aliases = new ArrayList<>();
		_aliases.addAll(aliases);
		_port = port;
		_proto = proto;
	}

	/**
	 * Returns the list of the aliases of this service.
	 * @return a List<String> containing the aliases. The list could be empty but not nul.
	 */
	public List<String> getAliases() {
		return _aliases;
	}

	/**
	 * Returns the name of this service.
	 * @return the name of this service.
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Returns the port number of this service.
	 * @return the port number of this service.
	 */
	public int getPort() {
		return _port;
	}

	/**
	 * Returns the protocol name to use with this service.
	 * @return the protocol name to use with this service.
	 */
	public String getProto() {
		return _proto;
	}

}
