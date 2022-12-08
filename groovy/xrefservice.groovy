/*
 * Copyright (c) 2013, Universite de Rennes 1
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
 * This script does a "xref service" on all equipments.
 *
 * Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */

import fr.univrennes1.cri.jtacl.shell.*;

def void usage() {
    println("Usage xrefservice xrefargs");
    println();
    println('This script runs the command "xref service [xrefargs]" on all the equipments.');
    println('Example:');
    println('    xrefservice tcp 22');
}

def String[] argss = lsfw.getArgs();
def String cargs = lsfw.getcArgs();
if (argss.size() != 0 && argss[0] == '-help') {
    usage();
    return;
}

def shell = new Shell();
def equipments = lsfw.getMonitor().getEquipments();
equipments.each() {
    entry ->
       String name = entry.getKey();
       String className = entry.getValue().getClass().getName();
       if (className != 'fr.univrennes1.cri.jtacl.equipments.SimpleRouter')
           shell.runCommand("eq $name xref service $cargs");
}