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
 * Test on a port
 * Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */

import fr.univrennes1.cri.jtacl.lib.ip.*

class TestPort {

	IPNet ip
	String hostname
	boolean dns
	boolean ping
	String sourceIP
	String sourcePort
	String destIP
	String protocol
	String destPort

	TestPort() {
	}

	TestPort(IPNet ip, String hostname, boolean dns, boolean ping, String sourceIP,
		String sourcePort, String destIP, String protocol, String destPort) {

		this.ip = ip
		this.hostname = hostname
		this.dns = dns
		this.ping = ping
		this.sourceIP = sourceIP
		this.sourcePort = sourcePort
		this.destIP = destIP
		this.protocol = protocol
		this.destPort = destPort
	}

	void fromString(String line) {
		def split = line.split(';')
		String sip = split[0].trim()
		ip = new IPNet(sip)
		hostname = split[1].trim()
		String sdns = split[2].trim()
		String sping = split[3].trim()
		if (sdns == '++dns++')
			dns = true
		else
			dns = false

		if (sping == '++ping++')
			ping = true
		else
			ping = false
		sourceIP = split[4].trim()
		sourcePort = split[5].trim()
		destIP = split[6].trim()
		protocol = split[7].trim()
		destPort = split[8].trim()
	}

	String toString() {
		String s = ip.toString('i') + "; " + hostname + "; "
		if (dns)
			s += "++dns++; "
		else
			s += "--dns--; "
		if (ping)
			s += "++ping++"
		else
			s += "--ping--"

		s += "; " + sourceIP + "; " + sourcePort + "; " + destIP + "; " + protocol + "; " + destPort
		return s
	}
}

