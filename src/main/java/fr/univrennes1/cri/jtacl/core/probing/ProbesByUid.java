/*
 * Copyright (c) 2010, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.core.probing;

import java.util.HashMap;

/**
 * A HashMap of {@link Probe} items. Keyed by the Uid of the
 * {@link Probe} item.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class ProbesByUid extends HashMap<Integer, Probe> {

    /**
     * Return the {@link Integer} key of an {@link Probe} probe.
     *
     * @param probe the {@link Probe} probe to use as a key.
     * @return the {@link Integer} key.
     */
    public static Integer key(Probe probe) {
        return probe.getUid();
    }

    /**
     * Associates the specified {@link Probe} probe in this map, using the
     * {@link Integer} Uid of the probe as a key.
     *
     * @param probe value to be associated.
     */
    public void put(Probe probe) {
        put(key(probe), probe);
    }

}
