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

package fr.univrennes1.cri.jtacl.lib.ip;

/**
 * Icmp type definition:<br/>
 * <ul>
 * <li>name: the official name of the icmp-type.</li>
 * <li>icmp: the icmp-type number.</li>
 * <li>code: the icmp code number.</li>
 * </ul>
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class IPIcmpEnt {

	protected String _name;
	protected int _icmp;
	protected int _code;

	/**
	 * Constructs a new icmp type definition.
	 * @param name the official name of the icmp type.
	 * @param icmp the icmp type number.
	 * @param code the icmp code number.
	 */
	public IPIcmpEnt(String name, int icmp, int code) {
		_name = name;
		_icmp = icmp;
		_code = code;
	}

	/**
	 * Returns the name of this icmp-type.
	 * @return the name of this icmp-type.
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Returns the icmp-type number of this icmp-type.
	 * @return the icmp-type number of this icmp-type.
	 */
	public int getIcmp() {
		return _icmp;
	}

	/**
	 * Returns the icmp-type code of this icmp-type.
	 * @return the icmp-type code of this icmp-type. (-1 if there is no code).
	 */
	public int getCode() {
		return _code;
	}

	@Override
	public String toString() {
		return _name + ":" + _icmp + "," + _code;
	}
	
}
