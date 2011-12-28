/*
 * Copyright (c) 2011, Universite de Rennes 1
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

package fr.univrennes1.cri.jtacl.lib.misc;

/**
 * Convenient methods for dealing with strings
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class StringTools {

	/**
	 * Returns the index of the first occurence of any character specified in
	 * characters in the string string.
	 * @param string String to search in
	 * @param characters Characters to search.
	 * @return the index of the first occurence, -1 otherwise.
	 */
	public static int IndexOfChars(String string, String characters) {

		int i = 0;
		for (i = 0; i < string.length(); i++) {
			if (characters.indexOf(string.charAt(i)) >= 0)
				return i;
		}
		return -1;
	}


}
