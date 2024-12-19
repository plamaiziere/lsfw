/*
 * Copyright (c) 2012, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */
package fr.univrennes1.cri.jtacl.core.probing;

/**
 * Probe extension can be used by equipment to store some data into a probe.
 * (by instance tag).
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public interface ProbeExtension {
    ProbeExtension newInstance();
}
