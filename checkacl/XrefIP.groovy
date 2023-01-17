/*
 * Copyright (c) 2012, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

/*
 * IP reference
 * Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */

import fr.univrennes1.cri.jtacl.lib.ip.*

class XrefIP {

	IPNet ip
	String hostname
	String equipment
	String fileandline
	boolean dns
	boolean ping
	String ctx
	String ctxtype
	String rule

	void fromString(String line) {
		def split = line.split(';')
		String sip = split[0].trim()
		ip = new IPNet(sip)
		hostname = split[1].trim()
		equipment = split[2].trim()
		fileandline = split[3].trim()
		ctxtype = split[4].trim()
		ctx = split[5].trim()
		String sdns = split[6].trim()
		String sping = split[7].trim()
		if (sdns == '++dns++')
			dns = true
		else
			dns = false

		if (sping == '++ping++')
			ping = true
		else
			ping = false
		rule = split[8].trim()
	}

	String toString() {
		String s = ip.toString('i') + "; " + hostname + "; " + equipment + "; " + fileandline + "; " + ctxtype + "; " + ctx + "; "
		if (dns)
			s += "++dns++; "
		else
			s += "--dns--; "
		if (ping)
			s += "++ping++; "
		else
			s += "--ping--; "

		s += rule
		return s
	}
}

