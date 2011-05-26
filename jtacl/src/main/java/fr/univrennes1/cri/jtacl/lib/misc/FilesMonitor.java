/*
 * Copyright (c) 2011, Universite de Rennes 1
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

package fr.univrennes1.cri.jtacl.lib.misc;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A basic and simple File Alteration Monitor.
 * @author patrick.lamaiziere@univ-rennes1.fr
 */
public class FilesMonitor {

	protected Map<String, Long> _filesMap = new HashMap<String, Long>();

	public void addFile(String filename) {
		File file = new File(filename);
		Long date = file.lastModified();
		Long df = _filesMap. get(filename);
		if (df != null)
			_filesMap.remove(filename);
		_filesMap.put(filename, date);
	}

	public List<String> checkFiles() {
		ArrayList<String> files = new ArrayList<String>();

		for (String filename: _filesMap.keySet()) {
			Long date = _filesMap.get(filename);
			File file = new File(filename);
			long df = file.lastModified();
			if (date.longValue() != df)
				files.add(filename);
		}

		return files;
	}

}
