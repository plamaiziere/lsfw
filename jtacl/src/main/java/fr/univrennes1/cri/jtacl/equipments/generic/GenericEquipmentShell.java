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

import java.io.*;

/**
 * Equipment sub shell generic.
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public abstract class GenericEquipmentShell {

	/**
	 * Displays the help of this shell
	 * @param output Stream to output.
	 */
	abstract public void shellHelp(PrintStream output);

	/**
	 * Runs the specified shell command in argument.
	 * @param command command to run.
	 * @param output Stream to output.
	 * @return true if the command is part of this shell, false otherwise.
	 */
	abstract public boolean shellCommand(String command, PrintStream output);
	
	/**
	 * Print the help ressource
	 * @param output output stream
	 * @param ressource ressource to print 
	 */
	public void printHelp(PrintStream output, String ressource) {
		try {
			InputStream stream;
			stream = this.getClass().getResourceAsStream(ressource);
			if (stream == null) {
				output.println("cannot print help");
				return;
			}
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			for (;;) {
				String line = reader.readLine();
				if (line == null) {
					break;
				}
				output.println(line);
			}
			reader.close();
		} catch (IOException ex) {
			output.println("cannot print help");
		}
	}

}
