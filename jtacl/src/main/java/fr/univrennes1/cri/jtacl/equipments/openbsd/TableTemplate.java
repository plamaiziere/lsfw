/*
 * Copyright (c) 2010, Université de Rennes 1
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

import fr.univrennes1.cri.jtacl.lib.misc.StringsList;
import java.util.ArrayList;
import java.util.List;

/**
 * Template to build table definition. This class is used at parsing time
 * as an intermediate storage.
 * @see PfTable
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class TableTemplate {

	private List<Xhost> _hosts = new ArrayList<Xhost>();
	private StringsList _options = new StringsList();
	private String _name;
	private StringsList _fileNames = new StringsList();

	public StringsList getFileNames() {
		return _fileNames;
	}

	public List<Xhost> getHosts() {
		return _hosts;
	}

	public String getName() {
		return _name;
	}

	public void setName(String name) {
		_name = name;
	}

	public StringsList getOptions() {
		return _options;
	}

	public void setOptions(StringsList options) {
		_options = options;
	}

}
