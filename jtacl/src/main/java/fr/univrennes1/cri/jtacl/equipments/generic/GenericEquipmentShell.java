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

import java.io.PrintStream;

/**
 * Equipment sub shell generic interface.
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public interface GenericEquipmentShell {

	/**
	 * Displays the help of this shell
	 * @param output Stream to output.
	 */
	void shellHelp(PrintStream output);

	/**
	 * Runs the specified shell command in argument.
	 * @param command command to run.
	 * @param output Stream to output.
	 * @return true if the command is part of this shell, false otherwise.
	 */
	boolean shellCommand(String command, PrintStream output);

}
