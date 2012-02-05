/*
 * Copyright (c) 2012, Universite de Rennes 1
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

/*
 * This script does a "virtual nmap" between a source to a destination to test
 * opened ports within a range.
 * The source uses a source port and, for tcp, the flag SYN is set and ACK unset.
 *
 * Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */

import fr.univrennes1.cri.jtacl.shell.*;
import fr.univrennes1.cri.jtacl.App;

class VirtualNmap {

	static void nmap(Shell shell, String source, String sourcePort, String dest,
		boolean udp, boolean tcp, int first, int last) {

		first.upto(last) {
			if (udp) {
				if (shell.runCommand("probe expect ACCEPT $source $dest udp $sourcePort:$it")
						== App.EXIT_SUCCESS)
				    println("udp; $source; $sourcePort; $dest; $it");
			}
			if (tcp) {
				if (shell.runCommand("probe expect ACCEPT $source $dest tcp $sourcePort:$it flags Sa")
						== App.EXIT_SUCCESS)
				    println("tcp; $source; $sourcePort; $dest; $it");
			}
		}
	}

	static void usage() {
		println('Usage: vnmap (udp|tcp|udp/tcp) source sourceport destination portfirst portlast');
		println();
		println('This script does a "virtual nmap" between a source to a destination to test opened ports within a range.');
		println('The source uses a source port and, for tcp, the flag SYN is set and ACK unset.');
		println();
		println('Example:');
		println('Test tcp ports from "1.2.3.4" using a "dyn"(amic) port to "192.168.1.12" port 1 to 65535:');
		println('   vnmap tcp 1.2.3.4 dyn 192.168.1.12 1 65535');
	}
}

def args = lsfw.getArgs();
if (args.size() == 1 && args[0] == '-help') {
	VirtualNmap.usage();
	return;
}
if (args.size() != 6) {
	VirtualNmap.usage();
	return;
}

String proto = args[0].toLowerCase();
String source = args[1];
String sourceport = args[2];
String dest = args[3];
int first = args[4].toInteger();
int last = args[5].toInteger();

boolean udp = false;
boolean tcp = false;

if (proto == 'udp' || proto == 'udp/tcp' || proto == 'tcp/udp')
	udp = true;

if (proto == 'tcp' || proto == 'udp/tcp' || proto == 'tcp/udp')
	tcp = true;

if (!tcp && !udp) {
	VirtualNmap.usage();
	return;
}

def shell = new Shell();
shell.setOutputStream(DevNull.out);
VirtualNmap.nmap(shell, source, sourceport, dest, udp, tcp, first, last);
