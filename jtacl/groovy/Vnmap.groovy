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

	boolean _debug = false;
	int MIN_CHUNK = 256;

	Shell _shell;
	String _source;
	String _sourcePort;
	String _dest;
	int _first;
	int _last;
	boolean _tcp;
	boolean _udp;
	List<List<Integer>> _udpRangesToTest = new ArrayList<List<Integer>>();
	List<List<Integer>> _tcpRangesToTest = new ArrayList<List<Integer>>();
	int _tcpRangeTest = 0;
	int _udpRangeTest = 0;

	VirtualNmap(Shell shell, String source, String sourcePort, String dest,
		boolean udp, boolean tcp, int first, int last) {

		_shell = shell;
		_source = source;
		_sourcePort = sourcePort;
		_dest = dest;
		_udp = udp;
		_tcp = tcp;
		_first = first;
		_last = last;
	}

	boolean isDeniedRange(boolean udp, int first, int last) {

		if (udp) {
			_udpRangeTest++;
			int ret = _shell.runCommand("probe quick-deny expect UNACCEPTED $_source $_dest udp $_sourcePort:($first,$last)");
			return ret == App.EXIT_SUCCESS;
		}

		_tcpRangeTest++;
		int ret = _shell.runCommand("probe quick-deny expect UNACCEPTED $_source $_dest tcp $_sourcePort:($first,$last) flags Sa");
		return ret == App.EXIT_SUCCESS;
	}

	/*
	 * We do a dichotomic search on the range of ports to test. If a range is denied, there is
	 * no need to probe for accept. If the range is not denied, we split it in two parts and
	 * repeat the processus until the range is smallest than MIN_CHUNK.
	 * This greatly improves the speed.
	 */
	void getRangesToTest(boolean udp, int first, int last, List<List<Integer>> rangesToTest) {

		boolean denied = isDeniedRange(udp, first, last);
		if (denied)
			return;
		int range = last - first;
		if (range <= MIN_CHUNK) {
			List<Integer> ports = new ArrayList<Integer>();
			ports.add(first);
			ports.add(last);
			rangesToTest.add(ports);
			return;
		}

		int pf1 = first;
		int pl1 = first + (range / 2);
		int pf2 = pl1 + 1;
		int pl2 = last;

		if (pf1 <= pl1) {
			getRangesToTest(udp, pf1, pl1, rangesToTest);
		}

		if (pf2 <= pl2) {
			getRangesToTest(udp, pf2, pl2, rangesToTest);
		}
	}

	void nmap() {

		_udpRangesToTest.clear();
		_tcpRangesToTest.clear();

		if (_udp)
			getRangesToTest(true, _first, _last, _udpRangesToTest);

		if (_tcp)
			getRangesToTest(false, _first, _last, _tcpRangesToTest);

		if (_debug)
			println("Range test udp=$_udpRangeTest tcp=$_tcpRangeTest");

		_udpRangesToTest.each() {
			ports ->
			int f = ports[0];
			int l = ports[1];

			if (_debug)
				println("range udp : $f, $l");

			f.upto(l) {
				int res = _shell.runCommand("probe quick-deny expect ACCEPT $_source $_dest udp $_sourcePort:$it")
				if (res == App.EXIT_SUCCESS)
				    println("udp; $_source; $_sourcePort; $_dest; $it");
			}
		}

		_tcpRangesToTest.each() {
			ports ->
			int f = ports[0];
			int l = ports[1];

			if (_debug)
				println("range tcp : $f, $l");

			f.upto(l) {
				int res = _shell.runCommand("probe quick-deny expect ACCEPT $_source $_dest tcp $_sourcePort:$it flags Sa")
				if (res == App.EXIT_SUCCESS)
				    println("tcp; $_source; $_sourcePort; $_dest; $it");
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

def vn = new VirtualNmap(shell, source, sourceport, dest, udp, tcp, first, last);
vn.nmap();

