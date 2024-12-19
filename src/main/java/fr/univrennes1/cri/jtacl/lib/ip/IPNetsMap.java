/*
 * Copyright (c) 2010, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.lib.ip;

import java.util.HashMap;

/**
 * A HashMap of {@link IPNet} items. Keyed by the {@link Integer} hashCode of
 * the {@link IPNet} item.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class IPNetsMap extends HashMap<Integer, IPNet> {

    /**
     * Return the {@link Integer} key of an {@link IPNet} object.
     *
     * @param ipnet the {@link IPNet} object to use as a key.
     * @return the {@link Integer} key.
     */
    public static Integer key(IPNet ipnet) {
        return ipnet.hashCode();
    }

    /**
     * Associates the specified {@link IPNet} object in this map, using the
     * {@link Integer} hashCode of the object as a key.
     *
     * @param ipnet value to be associated.
     */
    public void put(IPNet ipnet) {
        put(key(ipnet), ipnet);
    }

    /**
     * Returns the {@link IPNet} object associated with the {@link IPNet}
     * address.
     *
     * @param ipnet {@link IPNet} IP address of the object to return.
     * @return the {@link IPNet} object associated. Null if no object was found.
     */
    public IPNet get(IPNet ipnet) {
        return get(ipnet.hashCode());
    }

}
