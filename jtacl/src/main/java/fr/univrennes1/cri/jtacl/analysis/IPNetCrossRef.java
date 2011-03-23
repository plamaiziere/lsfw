/*
 * Copyright (c) 2010, Universite de Rennes 1
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

package fr.univrennes1.cri.jtacl.analysis;

import fr.univrennes1.cri.jtacl.lib.ip.IPNet;
import java.util.ArrayList;
import java.util.List;

/**
 * Cross reference of an IPNet.
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class IPNetCrossRef {
	protected IPNet _ip;

	protected List<CrossRefContext> _contexts;

	public IPNetCrossRef(IPNet ip) {
		_ip = ip;
		_contexts = new ArrayList<CrossRefContext>();
	}

	public List<CrossRefContext> getContexts() {
		return _contexts;
	}

}