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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Services definitions and lookup.
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class Services extends ArrayList<IPServEnt> {

	/**
	 * Strip comment ("#") from the string in argument.
	 * @param str String to strip.
	 * @return the string stripped.
	 */
	protected static String stripComment(String str) {
		int p = str.indexOf('#');
		if (p < 0)
			return str;
		if (p == 0)
			return "";
		return str.substring(0, p);
	}

	/**
	 * Returns the entry associated to the service name and protocol in argument.
	 * @param name the name or alias of the service.
	 * @param proto the name of the protocol (udp/tcp). If null, the first entry
	 * matching the service name is returned.
	 * @return An {@link IPServEnt} entry describing the service.
	 * Returns null if there is no service matching this name and this protocol.
	 */
	public IPServEnt getServByName(String name, String proto) {
		for (IPServEnt ent: this) {
			if (proto != null && !ent._proto.equalsIgnoreCase(proto))
				continue;
			if (ent._name.equalsIgnoreCase(name))
				return ent;
			for (String alias: ent._aliases) {
				if (alias.equalsIgnoreCase(name))
					return ent;
			}
		}
		return null;
 	}

	/**
	 * Returns the entry associated to the service port and protocol in argument.
	 * @param port the port number of the service.
	 * @param proto the name of the protocol (udp/tcp). If null, the first entry
	 * matching the port is returned.
	 * @return An {@link IPServEnt} entry describing the service.
	 * Returns null if there is no service matching this port number and this protocol.
	 */
	public IPServEnt getServByPort(int port, String proto) {
		for (IPServEnt ent: this) {
			if (proto != null && !ent._proto.equalsIgnoreCase(proto))
				continue;
			if (ent._port == port)
				return ent;
		}
		return null;
 	}

	/**
	 * Reads services definition from a stream. The format is the one used by
	 * /etc/services in FreeBSD. From services(5):<br/>
     * For each service a single line should be present with the following
	 * information:</br>
	 * <ul>
	 * <li>official service name</li>
     * <li>port number</li>
     *  <li>protocol name</li>
     *  <li>aliases</li>
	 * </ul>
     * Items are separated by any number of blanks and/or tab characters.  The
     * port number and protocol name are considered a single item; a ``/'' is
     * used to separate the port and protocol (e.g. ``512/tcp'').  A ``#'' indi-
     * cates the beginning of a comment; subsequent characters up to the end of
     * the line are not interpreted by the routines which search the file.
	 * <br/>
     * Service names may contain any printable character other than a field
     * delimiter, newline, or comment character.
	 * @param input the stream to read.
	 */
	public void readServices(InputStream input) throws IOException {
		BufferedReader reader =  new BufferedReader(new InputStreamReader(input));
		clear();
		for (;;) {
			String line = reader.readLine();
			if (line == null)
				break;
			line = Services.stripComment(line);
			line = line.trim();
			String[] splited = line.split("\\s+");
			if (splited.length < 2)
				continue;
			// protocol name
			String name = splited[0];

			// port number/protocol
			String[] sproto = splited[1].split("\\/");
			if (sproto.length != 2)
				continue;
			int port = Integer.valueOf(sproto[0]);
			String proto = sproto[1];
			// aliases
			ArrayList<String> aliases = new ArrayList<String>();
			for (int i = 2; i < splited.length; i++)
				aliases.add(splited[i]);
			IPServEnt ent = new IPServEnt(name, aliases, port, proto);
			add(ent);
		}
	}

	/**
	 * Converts the service in argument to a port number.
	 * If service is an integer, return this integer. Otherwise, search
	 * the service using the services database.
	 * @param service service name to lookup.
	 * @param protocol protocol used by the service  (udp or tcp).
	 * @return the port number associated to the service or -1 if the service is
	 * not in the services database.
	 */
	public int serviceLookup(String service, String protocol) {
		int port;
		try {
			port = Integer.valueOf(service);
			return port;
		} catch (NumberFormatException ex) {
			// not a number;
		}
		IPServEnt ent = getServByName(service, protocol);
		if (ent == null)
			return -1;
		return ent.getPort();
	}

}
