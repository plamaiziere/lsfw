/*
 * Copyright (c) 2012, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

/*
 * Returns service entry by service name
 * Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */

import fr.univrennes1.cri.jtacl.lib.ip.*;

def services = IPServices.getInstance();
def argss = lsfw.getArgs();

if (argss.size() == 0) {
    println("Usage portbysrv servicename [servicename]");
    return;
}

for (sservice in argss) {
    def ent = services.getServByName(sservice, null);

    if (!ent) {
        println "service: " + sservice + " not found!";
    } else {
        print("port: " + ent.getPort() + ", name: " + ent.getName());
        print(", aliases:");
        ent.getAliases().each() { print " " + it; }
        println();
    }
}
