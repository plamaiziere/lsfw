/*
 * Copyright (c) 2013 - 2018, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.equipments.checkpointR80;

/**
 * Checkpoint network object type
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public enum CpNetworkType {
    UNHANDLED,
    IP,
    IPS,
    RANGE,
    GROUP,
    ANY
}
