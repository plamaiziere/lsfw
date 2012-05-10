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

import fr.univrennes1.cri.jtacl.analysis.CrossRefContext;
import fr.univrennes1.cri.jtacl.analysis.IPNetCrossRef;
import fr.univrennes1.cri.jtacl.core.network.NetworkEquipment;
import fr.univrennes1.cri.jtacl.lib.ip.IPNet;
import java.io.*;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Scanner;

/**
 * Equipment sub shell generic.
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public abstract class GenericEquipmentShell {

	/**
	 * Returns the equipment associated to this shell.
	 * Returns the equipment associated to this shell.
	 */
	abstract public NetworkEquipment getEquipment();

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

	/**
	 * Print IP cross references
	 * @param output output stream.
	 * @param netCrossRef Map of IPNetCrossRef.
	 * @param parser Generic equipment parser.
	 */
	public void printXrefIp(PrintStream output,
				Map<IPNet, IPNetCrossRef> netCrossRef,
				GenericEquipmentShellParser parser) {

		IPNet ipreq = null;
		if (parser.getXrefIp() != null) {
			try {
				ipreq = new IPNet(parser.getXrefIp());
			} catch (UnknownHostException ex) {
				output.println("Error: " + ex.getMessage());
				return;
			}
		}

		String format = parser.getXrefFormat();
		if (format != null)
			format = format.toLowerCase();
		boolean fshort = format != null && format.contains("s");
		boolean flong = format != null && format.contains("l");
		boolean fhost = format != null && format.contains("h");
		boolean fptr = format != null && format.contains("p");

		for (IPNet ip: netCrossRef.keySet()) {
			IPNetCrossRef crossref = netCrossRef.get(ip);
			try {
				if (ipreq != null && !ipreq.overlaps(ip))
					continue;
				if (parser.getXrefHost() != null && !ip.isHost())
					continue;
			}
			catch (UnknownHostException ex) {
				output.println("Error " + ex.getMessage());
				return;
			}
			for (CrossRefContext ctx: crossref.getContexts()) {
				output.print(ip.toString("::i"));
				if (fhost || fptr) {
					try {
						String hostname = fhost ? ip.getCannonicalHostname() :
							ip.getHostname();
						output.print("; " + hostname);
					} catch (UnknownHostException ex) {
						output.print("; nohost");
					}
				}
				output.print("; " + ctx.getContextName());
				output.print("; " + ctx.getComment());
				String line = ctx.getParseContext().getLine().trim();
				if (fshort) {
					output.print("; ");
					Scanner sc = new Scanner(line);
					if (sc.hasNextLine())
						output.println(sc.nextLine());
					else
						output.println(line);
				} else {
					if (flong) {
						output.print("; ");
						output.println(line);
					} else {
						output.println();
					}
				}
			}
		}
	}

}
