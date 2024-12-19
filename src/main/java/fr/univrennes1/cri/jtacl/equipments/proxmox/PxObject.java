/*
 * Copyright (c) 2013 - 2021, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */
package fr.univrennes1.cri.jtacl.equipments.proxmox;

import fr.univrennes1.cri.jtacl.lib.misc.ParseContext;

/**
 * Proxmox object
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PxObject {
    protected ParseContext _context;

    public PxObject(ParseContext context) {
        _context = context;
    }

    public ParseContext getContext() {
        return _context;
    }

}
