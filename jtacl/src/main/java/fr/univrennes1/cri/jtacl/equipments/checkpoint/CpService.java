/*
 * Copyright (c) 2013, Universite de Rennes 1
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

package fr.univrennes1.cri.jtacl.equipments.checkpoint;

import fr.univrennes1.cri.jtacl.core.probing.MatchResult;
import fr.univrennes1.cri.jtacl.lib.ip.PortSpec;

/**
 * Checkpoint service object
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public abstract class CpService {
	protected String _name;
	protected String _className;
	protected String _comment;
	protected CpServiceType _type;

	public CpService(String name, String className, String comment,
			CpServiceType type) {

		_name = name;
		_className = className;
		_comment = comment;
		_type = type;
	}

	public String getName() {
		return _name;
	}

	public String getClassName() {
		return _className;
	}

	public String getComment() {
		return _comment;
	}

	public CpServiceType getType() {
		return _type;
	}

	public boolean isTcpService() {
		return _type == CpServiceType.TCP;
	}

	public boolean isUdpService() {
		return _type == CpServiceType.UDP;
	}

	public boolean isServiceGroup() {
		return _type == CpServiceType.GROUP;
	}

	public boolean isIcmpService() {
		return _type == CpServiceType.ICMP;
	}

	/**
	 * Checks if the portSpec in argument matches this service object.
	 * @param port portSpec to check
	 * @return the MatchResult of the given portSpec
	 */
	public abstract MatchResult matches(PortSpec port);
}
