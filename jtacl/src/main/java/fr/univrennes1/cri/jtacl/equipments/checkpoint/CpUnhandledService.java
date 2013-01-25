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

/**
 * Checkpoint service left unhandled by lsfw
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class CpUnhandledService extends CpService {

	/**
	 * Construct a new unhandled service
	 * @param name service name
	 * @param className class name
	 * @param comment comment
	 */
	public CpUnhandledService(String name, String className, String comment) {

		super(name, className, comment, CpServiceType.UNHANDLED);
	}

	@Override
	public String toString() {
		return _name + ", " + _className + ", " + _comment + ", " +  _type;
	}

}