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
import java.util.List;
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
	 */
	abstract public void shellCommand(String command, PrintStream output);

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

	protected void printContext(PrintStream output, IPNet ip,
		CrossRefContext ctx, String format) {

		List<String> fmts = GenericEquipmentShellParser.expandFormat(format);
		for (String fmt: fmts) {
			if (!fmt.startsWith("%"))
				output.print(fmt);
			else {
				// context name
				if (fmt.equals("%c")) {
					output.print(ctx.getContextName());
					continue;
				}
				// context comnent
				if (fmt.equals("%C")) {
					output.print(ctx.getComment());
					continue;
				}
				// equipment name
				if (fmt.equals("%e")) {
					output.print(getEquipment().getName());
					continue;
				}
				// filename without path
				if (fmt.equals("%f")) {
					File f = new File(ctx.getFilename());
					output.print(f.getName());
					continue;
				}
				// filename with path
				if (fmt.equals("%F")) {
					output.print(ctx.getFilename());
					continue;
				}
				// host using ptr
				if (fmt.equals("%h")) {
					try {
						String hostname = ip.getHostname();
						output.print(hostname);
					} catch (UnknownHostException ex) {
						output.print("nohost");
					}
					continue;
				}
				// host using java resolver
				if (fmt.equals("%H")) {
					try {
						String hostname = ip.getCannonicalHostname();
						output.print(hostname);
					} catch (UnknownHostException ex) {
						output.print("nohost");
					}
					continue;
				}
				// ip short
				if (fmt.equals("%i")) {
					output.print(ip.toString("::i"));
					continue;
				}
				// ip long
				if (fmt.equals("%I")) {
					output.print(ip.toString("i"));
					continue;
				}
				// line short
				if (fmt.equals("%l")) {
					String line = ctx.getParseContext().getLine().trim();
					Scanner sc = new Scanner(line);
					if (sc.hasNextLine())
						output.print(sc.nextLine());
					else
						output.print(line);
					continue;
				}
				// line long
				if (fmt.equals("%L")) {
					String line = ctx.getParseContext().getLine().trim();
					output.print(line);
					continue;
				}
				// line number
				if (fmt.equals("%N")) {
					output.print(ctx.getLinenumber());
					continue;
				}
			}
		}
		output.println();
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
		String fmt = parser.getXrefFmt();
		if (format == null && fmt == null)
			format = "s";
		if (format != null) {
			format = format.toLowerCase();
			fmt = "%i";
			if (format.contains("h"))
				fmt += "; %H";
			if (format.contains("p"))
				fmt += "; %h";
			fmt += "; %c; %C; %F #%N";
			if (format.contains("s"))
				fmt += "; %l";
			if (format.contains("l"))
				fmt += "; %L";
		}
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
				printContext(output, ip, ctx, fmt);
			}
		}
	}

}
