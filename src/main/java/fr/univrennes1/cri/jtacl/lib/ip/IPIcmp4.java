/*
 * Copyright (c) 2010, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.lib.ip;

/**
 * Icmp type definitions and lookup (IPv4)
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class IPIcmp4 extends IPIcmp {

    protected static IPIcmp _instance = new IPIcmp4();

    public static IPIcmp getInstance() {
        return _instance;
    }

}
