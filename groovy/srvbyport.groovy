/*
 * Copyright (c) 2012, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

/*
 * Returns service entry by port number
 * Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */

import fr.univrennes1.cri.jtacl.lib.ip.*;

def services = IPServices.getInstance();
def argss = lsfw.getArgs();

if (argss.size() == 0) {
    println("Usage srvbyport portnumber [portnumber]");
    return;
}

for (sport in argss) {
    def port = sport.toInteger();
    def ent = services.getServByPort(port, null);

    if (!ent) {
        println "port: " + port + " not found!";
    } else {
        print("port: " + port + ", name: " + ent.getName());
        print(", aliases:");
        ent.getAliases().each() { print " " + it; }
        println();
    }
}
