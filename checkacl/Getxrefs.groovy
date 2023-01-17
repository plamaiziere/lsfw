/*
 * Copyright (c) 2012, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

/*
 * Scritp to retrieve ip references on all equipments
 * Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */

import fr.univrennes1.cri.jtacl.shell.*;
import fr.univrennes1.cri.jtacl.App;
import fr.univrennes1.cri.jtacl.core.monitor.*;

def static void main(Monitor monitor, String outputfile, boolean debug) {

	def shell = new Shell();
	/*
	 *  shell redirection
	 */
	ByteArrayOutputStream outb = new ByteArrayOutputStream();
	PrintStream output = new PrintStream(outb);
	shell.setOutputStream(output);

	/*
     * augment the DNS cache time to live value
	 */
	shell.runCommand('option dns.cache.ttl=6000000');

	/*
	 * issue a xref command on equipment (except of type SimpleRouter)
	 */
	def equipments = monitor.getInstance().getEquipments();
	equipments.each() {
		entry ->
		   String name = entry.getKey();
		   String className = entry.getValue().getClass().getName();
		   if (className != 'fr.univrennes1.cri.jtacl.equipments.SimpleRouter') {
		       if (debug)
					println("xref on $name ...");
		       shell.runCommand("eq $name" + ' xref ip fmt "%i; %h; %e; %f #%N; %c; %C; --ping--; --dns--; %l" host');
		   }
	}

	/*
	 * retrieve command output
	 */
	def inp = new InputStreamReader(new ByteArrayInputStream(outb.toByteArray()));
	def lines = inp.readLines();

	XrefsIP xrefs = new XrefsIP()
	xrefs.fromList(lines)

	/*
	 * sort by IP
	 */
	xrefs.sort() {
		a, b ->
		return a.ip <=> b.ip
	}

	/*
	 * save
	 */
	xrefs.toFile(outputfile)

}

def String[] argss = lsfw.getArgs();

if (argss.size() != 1) {
    println("Usage getxrefs filename");
    return;
}

def filename = argss[0];
main(lsfw.getMonitor(), filename, false);

