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

import fr.univrennes1.cri.jtacl.lib.ip.IPRangeable;
import java.util.ArrayList;
import java.util.List;

/**
 * Cross reference of an IP.
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class IPNetCrossRef {
	protected IPRangeable _ip;

	protected List<CrossRefContext> _contexts;

	public IPNetCrossRef(IPRangeable ip) {
		_ip = ip;
		_contexts = new ArrayList<CrossRefContext>();
	}

	public List<CrossRefContext> getContexts() {
		return _contexts;
	}

	public void addContext(CrossRefContext context) {
		_contexts.add(context);
	}

	public IPRangeable getIP() {
		return _ip;
	}

	@Override
	public String toString() {
		return "IPNetCrossRef [_ip=" + _ip + ", _contexts=" + _contexts + "]";
	}

}
