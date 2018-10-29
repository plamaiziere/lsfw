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
 * Protocols definitions and lookup.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class Protocols extends ArrayList<IPProtoEnt> {

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
	 * Returns the entry associated to the protocol name in argument.
	 * @param name the name of the protocol.
	 * @return An {@link IPProtoEnt} entry describing the protocol.
	 * Returns null if there is no protocol matching this protocol name.
	 */
	public IPProtoEnt getProtoByName(String name) {
		for (IPProtoEnt ent: this) {
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
	 * Returns the entry associated to the protocol number in argument.
	 * @param proto number of the protocol.
	 * @return An {@link IPProtoEnt} entry describing the protocol.
	 * Returns null if there is no protocol matching this protocol number.
	 */
	public IPProtoEnt getProtoByNumber(int proto) {
		for (IPProtoEnt ent: this) {
			if (ent._proto == proto)
				return ent;
		}
		return null;
 	}

	/**
	 * Reads protocols definition from a stream. The format is the one used by
	 * /etc/protocols in FreeBSD. From protocols(5):<br/>
	 * For each protocol a single line should be present with the following
	 * information:<br>
	 * <ul>
	 * <li>official protocol name</li>
     * <li>protocol number</li>
     * <li>aliases</li>
	 * </ul>
	 *
	 * Items are separated by any number of blanks and/or tab characters.  A
     *``#'' indicates the beginning of a comment; characters up to the end of
     * the line are not interpreted by routines which search the file.
     *
     * Protocol names may contain any printable character other than a field
     * delimiter, newline, or comment character.
	 * @param input the stream to read.
	 */
	public void readProtocols(InputStream input) throws IOException {
		BufferedReader reader =  new BufferedReader(new InputStreamReader(input));
		clear();
		for (;;) {
			String line = reader.readLine();
			if (line == null)
				break;
			line = IPProtocols.stripComment(line);
			line = line.trim();
			String[] splited = line.split("\\s+");
			if (splited.length < 2)
				continue;
			// protocol name
			String name = splited[0];

			// protocol number
			int proto = Integer.valueOf(splited[1]);

			// aliases
			ArrayList<String> aliases = new ArrayList<>();
			for (int i = 2; i < splited.length; i++)
				aliases.add(splited[i]);
			IPProtoEnt ent = new IPProtoEnt(name, aliases, proto);
			add(ent);
		}

	}

	/**
	 * Converts the protocol in argument to a protocol number.
	 * If protocol is an integer, return this integer. Otherwise, search
	 * the protocol using the protocols database.
	 * @param protocol protocol name to lookup.
	 * @return the protocol number associated to the protocol or -1 if the
	 * protocol is not in the protocols database.
	 */
	public int protocolLookup(String protocol) {
		int protocolNumber;
		try {
			protocolNumber = Integer.valueOf(protocol);
			return protocolNumber;
		} catch (NumberFormatException ex) {
			// not a number;
		}
		IPProtoEnt ent = getProtoByName(protocol);
		if (ent == null)
			return -1;
		return ent.getProto();
	}

	/**
	 * value for IP
	 */
	public static int IP = 0;

	/**
	 * value for IPv6
	 */
	public static int IPV6 = 41;

	/**
	 * value for ICMP
	 */
	public static int ICMP = 1;

	/**
	 * value for ICMP6
	 */
	public static int ICMP6 = 58;

	/**
	 * value for TCP
	 */
	public static int TCP = 6;

	/**
	 * value for UDP;
	 */
	public static int UDP = 17;

}
