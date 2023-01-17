/*
 * Copyright (c) 2012, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

/*
 * Host description
 * Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */

import fr.univrennes1.cri.jtacl.lib.ip.*

class Xhost {

	IPNet ip
	String equipment
	String hostname
	boolean dns
	boolean ping

	Xhost() {
	}

	Xhost(IPNet ip,String hostname,  String equipment, boolean dns, boolean ping) {
		this.ip = ip
		this.hostname = hostname
		this.equipment = equipment
		this.dns = dns
		this.ping = ping
	}

	void fromString(String line) {
		def split = line.split(';')
		String sip = split[0].trim()
		ip = new IPNet(sip)
		hostname = split[1].trim()
		equipment = split[2].trim()
		String sdns = split[3].trim()
		String sping = split[4].trim()
		if (sdns == '++dns++')
			dns = true
		else
			dns = false

		if (sping == '++ping++')
			ping = true
		else
			ping = false
	}

	String toString() {
		String s = ip.toString('i') + "; " + hostname + "; " + equipment + "; "
		if (dns)
			s += "++dns++; "
		else
			s += "--dns--; "
		if (ping)
			s += "++ping++"
		else
			s += "--ping--"

		return s
	}
}

