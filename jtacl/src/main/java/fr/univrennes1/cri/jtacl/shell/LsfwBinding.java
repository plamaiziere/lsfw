/*
 * Copyright (c) 2012, Universite de Rennes 1
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
package fr.univrennes1.cri.jtacl.shell;

import fr.univrennes1.cri.jtacl.core.monitor.Monitor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Binding between lsfw and groovy scripts
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class LsfwBinding {
	protected Monitor _monitor = Monitor.getInstance();

	protected String _cArgs;
	protected ArrayList<String> _args;

	public LsfwBinding(String cArgs) {
		_cArgs = cArgs;
		_args = new ArrayList<String>();
		if (cArgs.length() != 0) {
			String[] args = cArgs.split("\\s+");
			_args.addAll(Arrays.asList(args));
		} else
			_args = new ArrayList<String>();
	}

	public ArrayList<String> getArgs() {
		return _args;
	}

	public String getcArgs() {
		return _cArgs;
	}

	public Monitor getMonitor() {
		return _monitor;
	}
}
