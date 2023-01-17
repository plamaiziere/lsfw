/*
 * Copyright (c) 2010, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
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
 * Icmp type definitions and lookup.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class IPIcmp extends ArrayList<IPIcmpEnt> {

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
	 * Returns the entry associated to the icmp-type name in argument.
	 * @param name the name of the icmp type.
	 * @return An {@link IPIcmpEnt} entry describing the icmp type.
	 * Returns null if there is no icmp type matching this name.
	 */
	public IPIcmpEnt getIcmpByName(String name) {
		for (IPIcmpEnt ent: this) {
			if (ent._name.equalsIgnoreCase(name))
				return ent;
		}
		return null;
 	}

	/**
	 * Returns the entry associated to the icmp-type and code number in argument.
	 * @param number the number of the icmp type.
	 * @param code the code of the icmp type (-1: no code)
	 * @return An {@link IPIcmpEnt} entry describing the icmp type.
	 * Returns null if there is no icmp type matching this icmp-type and code.
	 */
	public IPIcmpEnt getIcmpByNumber(int number, int code) {
		for (IPIcmpEnt ent: this) {
			if (ent._icmp == number && ent._code == code)
				return ent;
		}
		return null;
 	}

	/**
	 * Returns the entry associated to the icmp type number in argument.
	 * @param icmp number of the icmp-type
	 * @return An {@link IPIcmpEnt} entry describing the icmp-type.
	 * Returns null if there is no icmp-type matching this icmp-type number.
	 */
	public IPIcmpEnt getIcmpByNumber(int icmp) {
		return getIcmpByNumber(icmp, -1);
 	}

	/**
	 * Reads icmp-type definition from a stream. <br/><br/>
	 * For each icmp-type a single line should be present with the following
	 * information:<br>
	 * <ul>
	 * <li>icmp-type name</li>
     * <li>icmp-type number</li>
	 * <li>icmp code number</li>
	 * </ul>
	 *
	 * Items are separated by any number of blanks and/or tab characters.  A
     *``#'' indicates the beginning of a comment; characters up to the end of
     * the line are not interpreted by routines which search the file.
     *
     * icmp-type names may contain any printable character other than a field
     * delimiter, newline, or comment character.
	 * @param input the stream to read.
	 */
	public void readIcmp(InputStream input) throws IOException {
		BufferedReader reader =  new BufferedReader(new InputStreamReader(input));
		clear();
		for (;;) {
			String line = reader.readLine();
			if (line == null)
				break;
			line = IPIcmp.stripComment(line);
			line = line.trim();
			String[] splited = line.split("\\s+");
			if (splited.length < 3)
				continue;
			// icmp-type name
			String name = splited[0];

			// icmp-type number
			int icmp = Integer.valueOf(splited[1]);
			// code number
			int code = Integer.valueOf(splited[2]);

			IPIcmpEnt ent = new IPIcmpEnt(name, icmp, code);
			add(ent);
		}
	}

	/**
	 * Converts the icmp type or message in argument to a icmp type number and code
	 * If icmp type is an integer, return this integer. Otherwise, search
	 * the icmp type using the icmp type database.
	 * @param icmp icmp type name or message to lookup.
	 * @return an IPIcmpEnt describing the icmp-type or message.
	 * Or null if the icmp type is not in the icmp type database.
	 */
	public IPIcmpEnt icmpLookup(String icmp) {
		int icmpNumber;
		try {
			icmpNumber = Integer.valueOf(icmp);
			return new IPIcmpEnt("", icmpNumber, -1);
		} catch (NumberFormatException ex) {
			// not a number;
		}
		IPIcmpEnt ent = getIcmpByName(icmp);
		return ent;
	}

}
