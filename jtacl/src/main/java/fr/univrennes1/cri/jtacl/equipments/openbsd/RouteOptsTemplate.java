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

package fr.univrennes1.cri.jtacl.equipments.openbsd;

import java.util.ArrayList;
import java.util.List;

/**
 * Template to build route options. This class is used at parsing time
 * as an intermediate storage.
 * @see PfRouteOpts
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class RouteOptsTemplate {

	private int _rt;
	private List<Xhost> _hosts = new ArrayList<Xhost>();
	private PoolOptsTemplate _poolOpts;

	public List<Xhost> getHosts() {
		return _hosts;
	}

	public PoolOptsTemplate getPoolOpts() {
		return _poolOpts;
	}

	public boolean setPoolOpts(PoolOptsTemplate poolOpts) {
		_poolOpts = poolOpts;
		return true;
	}

	public int getRt() {
		return _rt;
	}

	public boolean setRt(int rt) {
		_rt = rt;
		return true;
	}

}
