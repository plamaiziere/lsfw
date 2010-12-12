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

package fr.univrennes1.cri.jtacl.lib.misc;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * An  {@link ArrayList} list of String items.
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class StringsList extends ArrayList<String> {

	public void readFromFile(String fileName)
			throws FileNotFoundException, IOException {

		clear();
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		try {
			for (;;) {
				String s = reader.readLine();
				if (s != null)
					add(s);
				else
					break;
			}
		} finally {
			reader.close();
		}
	}
}
