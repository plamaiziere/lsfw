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
 * IP references check :
 *     - DNS entry
 *     - ping
 * Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */

import fr.univrennes1.cri.jtacl.shell.*;
import fr.univrennes1.cri.jtacl.App;
import fr.univrennes1.cri.jtacl.lib.ip.*;

def static void main(String inputfile, String outputfile, boolean debug) {
	XrefsIP xrefs = new XrefsIP()
	xrefs.fromFile(inputfile)

	Map<IPNet, Boolean> mping = new HashMap<IPNet, Boolean>();
	Map<IPNet, Boolean> mdns = new HashMap<IPNet, Boolean>();

	/* check dns and ping */
	xrefs.each() {
		xref ->

		/* dns */
		Boolean fdns = mdns.get(xref.ip)
		if (fdns == null) {
		    fdns = false
		    if (xref.hostname != "nohost") {
		        try {
		            IPNet ipdns = new IPNet(xref.hostname)
		        } catch (UnknownHostException ex) {
		            fdns = true
		        }
		    }
			mdns.put(xref.ip, fdns)
		}
		xref.dns = fdns

		/* ping */
		Boolean fping = mping.get(xref.ip)
		if (fping == null) {
		    fping = false
		    try {
		        fping = Config.ping(xref.ip.toString('i'), '1')
		    } catch (UnknownHostException ex) {
		        fping = false
		    }
		    mping.put(xref.ip, fping)
		}

		xref.ping = fping
	}
	xrefs.toFile(outputfile)
}

def String[] argss = lsfw.getArgs()

if (argss.size() != 2) {
    println("Usage checkxrefs filename-in filename-out")
    return;
}

def filename = argss[0]
def out = argss[1]
main(filename, out, false)

