/*
 * Copyright (c) 2012, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.equipments.openbsd;

import fr.univrennes1.cri.jtacl.core.probing.ProbeExtension;

/**
 * Packet Filter probe extension.
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PfProbeExtension implements ProbeExtension {

	private String _tag;

	/**
	 * Returns the tag, if any, of the probe.
	 * @return the tag, if any, of the probe.
	 */
	public String getTag() {
		return _tag;
	}

	/**
	 * Sets the tag associated to the probe. no tag : null:
	 * @param tag tag to set.
	 */
	public void setTag(String tag) {
		_tag = tag;
	}

	@Override
	public ProbeExtension newInstance() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

}
