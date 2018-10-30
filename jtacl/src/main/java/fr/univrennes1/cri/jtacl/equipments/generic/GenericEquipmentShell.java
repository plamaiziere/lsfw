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
import fr.univrennes1.cri.jtacl.analysis.IPCrossRef;
import fr.univrennes1.cri.jtacl.analysis.IPCrossRefMap;
import fr.univrennes1.cri.jtacl.analysis.ServiceCrossRef;
import fr.univrennes1.cri.jtacl.analysis.ServiceCrossRefContext;
import fr.univrennes1.cri.jtacl.analysis.ServiceCrossRefMap;
import fr.univrennes1.cri.jtacl.analysis.ServiceCrossRefType;
import fr.univrennes1.cri.jtacl.core.exceptions.JtaclParameterException;
import fr.univrennes1.cri.jtacl.core.network.NetworkEquipment;
import fr.univrennes1.cri.jtacl.lib.ip.IPNet;
import fr.univrennes1.cri.jtacl.lib.ip.IPRangeable;
import fr.univrennes1.cri.jtacl.lib.ip.IPRangeableComparator;
import fr.univrennes1.cri.jtacl.lib.ip.PortRange;
import fr.univrennes1.cri.jtacl.lib.ip.PortRangeComparator;
import fr.univrennes1.cri.jtacl.lib.ip.PortSpec;
import fr.univrennes1.cri.jtacl.lib.ip.Protocols;
import fr.univrennes1.cri.jtacl.shell.ShellUtils;
import java.io.*;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
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

	protected void printIpContext(PrintStream output, IPRangeable ip,
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
					if (ctx.getFilename() != null) {
						File f = new File(ctx.getFilename());
						output.print(f.getName());
					} else {
						output.printf("none");
					}
					continue;
				}
				// filename with path
				if (fmt.equals("%F")) {
					if (ctx.getFilename() != null) {
						output.print(ctx.getFilename());
					} else {
						output.printf("none");
					}
					continue;
				}
				if (ip.isHost()) {
					// host using ptr
					if (fmt.equals("%h")) {
						try {
							String hostname = ip.toIPNet().getHostname();
							output.print(hostname);
						} catch (UnknownHostException ex) {
							output.print("nohost");
						}
						continue;
					}
					// host using java resolver
					if (fmt.equals("%H")) {
						try {
							String hostname = ip.toIPNet().getCannonicalHostname();
							output.print(hostname);
						} catch (UnknownHostException ex) {
							output.print("nohost");
						}
						continue;
					}
				}
				// ip short
				if (fmt.equals("%i")) {
					output.print(ip.toNetString("::i"));
					continue;
				}
				// ip long
				if (fmt.equals("%I")) {
					output.print(ip.toNetString("i"));
					continue;
				}
				// line short
				if (fmt.equals("%l")) {
					String line = ctx.getContextString().trim();
					Scanner sc = new Scanner(line);
					if (sc.hasNextLine())
						output.print(sc.nextLine());
					else
						output.print(line);
					continue;
				}
				// line long
				if (fmt.equals("%L")) {
					String line = ctx.getContextString().trim();
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

	protected void printServiceContext(PrintStream output, PortRange portrange,
		ServiceCrossRefContext ctx, String format) {

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
				// line short
				if (fmt.equals("%l")) {
					String line = ctx.getContextString().trim();
					Scanner sc = new Scanner(line);
					if (sc.hasNextLine())
						output.print(sc.nextLine());
					else
						output.print(line);
					continue;
				}
				// line long
				if (fmt.equals("%L")) {
					String line = ctx.getContextString().trim();
					output.print(line);
					continue;
				}
				// line number
				if (fmt.equals("%N")) {
					output.print(ctx.getLinenumber());
					continue;
				}
				if (fmt.equals("%r")) {
					output.print(portrange.toText());
					continue;
				}
				if (fmt.equals("%t")) {
					output.print(ctx.getType());
					continue;
				}
				if (fmt.equals("%p")) {
					boolean first = true;
					for (Integer proto: ctx.getProtoSpec()) {
						if (!first)
							output.print("/");
						first = false;
						if (proto == Protocols.UDP)
							output.print("UDP");
						if (proto == Protocols.TCP)
							output.print("TCP");
					}
					continue;
				}
			}
		}
		output.println();
	}

	/**
	 * Print IP cross references
	 * @param output output stream.
	 * @param netCrossRef Map of IPCrossRef.
	 * @param parser Generic equipment parser.
	 */
	public void printXrefIp(PrintStream output, IPCrossRefMap netCrossRef,
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
				fmt += ";/##\\ %l";
			if (format.contains("l"))
				fmt += ";/##\\ %L";
		}
		List<IPRangeable> list = new LinkedList<>(netCrossRef.keySet());
		list.sort(new IPRangeableComparator());
		for (IPRangeable ip: list) {
			IPCrossRef crossref = netCrossRef.get(ip);
			if (ipreq != null && !ipreq.overlaps(ip))
				continue;
			if (parser.getXrefHost() != null && !ip.isHost())
				continue;
			for (CrossRefContext ctx: crossref.getContexts()) {
				printIpContext(output, ip, ctx, fmt);
			}
		}
	}

	/**
	 * Print services cross references
	 * @param output output stream.
	 * @param serviceCrossRef Map of ServiceCrossRef.
	 * @param parser Generic equipment parser.
	 */
	public void printXrefService(PrintStream output,
					ServiceCrossRefMap serviceCrossRef,
					GenericEquipmentShellParser parser) {

		/*
		 * protocols
		 */
		Integer protoreq = null;
		String sprotoreq = parser.getXrefProto();
		if (sprotoreq != null) {
			if (sprotoreq.equals("tcp"))
				protoreq = Protocols.TCP;
			if (sprotoreq.equals("udp"))
				protoreq = Protocols.UDP;
		}

		/*
		 * type
		 */
		String sdirection = parser.getXrefType();

		/*
		 * port range
		 */
		String sportreq = parser.getXrefService();
		PortRange portreq = null;
		if (sportreq != null) {
			try {
				PortSpec portSpecReq = ShellUtils.parsePortSpec(sportreq, sprotoreq);
				portreq = portSpecReq.getRanges().get(0);
			} catch (JtaclParameterException ex) {
				output.println(ex.getMessage());
				return;
			}
		}
		String fmt = parser.getXrefFmt();
		if (fmt == null) {
			fmt = "%r; %p; %t; %c; %C; %F #%N;/##\\ %l;";
		}
		List<PortRange> list = new LinkedList<>(serviceCrossRef.keySet());
		list.sort(new PortRangeComparator());

		for (PortRange portrange: list) {
			ServiceCrossRef crossref = serviceCrossRef.get(portrange);
			if (portreq != null && !portreq.overlaps(portrange))
				continue;
			for (ServiceCrossRefContext ctx: crossref.getContexts()) {
				if (protoreq != null && !ctx.getProtoSpec().contains(protoreq))
					continue;
				if (sdirection != null) {
					if (sdirection.equals("from")
							&& ctx.getType() != ServiceCrossRefType.FROM)
						continue;
					if (sdirection.equals("to")
							&& ctx.getType() != ServiceCrossRefType.TO)
						continue;
					if (sdirection.equals("other")
							&& ctx.getType() != ServiceCrossRefType.OTHER)
						continue;
				}
				printServiceContext(output, portrange, ctx, fmt);
			}
		}
	}
}
