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

import fr.univrennes1.cri.jtacl.lib.misc.ParseContext;
import fr.univrennes1.cri.jtacl.lib.misc.StringsList;

/**
 * Describes a PF table.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PfTable extends PfGenericRule {

	/**
	 * name of the table
	 */
	protected String _name;

	/**
	 * file names
	 */
	protected StringsList _fileNames = new StringsList();

	/**
	 * parse context
	 */
	protected ParseContext _parseContext;

	/**
	 * IP specification
	 */
	protected PfIpSpec _ipspec = new PfIpSpec();

	/**
	 * Returns the fileNames of this table.
	 * @return the fileNames of this table.
	 */
	public StringsList getFileNames() {
		return _fileNames;
	}

	/**
	 * Returns the Ip specification of this table.
	 * @return the Ip specification of this table.
	 */
	public PfIpSpec getIpspec() {
		return _ipspec;
	}

	/**
	 * Returns the name of this table.
	 * @return the name of this table.
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Sets the name of this table.
	 * @param name name to set.
	 */
	public void setName(String name) {
		_name = name;
	}

	/**
	 * Returns the parse context of this table.
	 * @return the parse context of this table. May be null.
	 */
	public ParseContext getParseContext() {
		return _parseContext;
	}

	/**
	 * Sets the parse context of this table.
	 * @param parseContext parse context to set. May be null.
	 */	
	public void setParseContext(ParseContext parseContext) {
		_parseContext = parseContext;
	}

}
