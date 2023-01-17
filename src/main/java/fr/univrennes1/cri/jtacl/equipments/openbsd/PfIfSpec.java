/*
 * Copyright (c) 2011, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.equipments.openbsd;

/**
 * Interface specification in rule
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PfIfSpec {
	protected String _ifName;
	protected boolean _ifNot = false;
	
	public PfIfSpec(boolean ifNot, String ifName) {
		_ifNot = ifNot;
		_ifName = ifName;
	}
	
	public String getIfName() {
		return _ifName;
	}
	
	public boolean isIfNot() {
		return _ifNot;
	}

	@Override
	public String toString() {
		if (_ifNot)
			return "!" + _ifName;
		else
			return _ifName;
	}

}
