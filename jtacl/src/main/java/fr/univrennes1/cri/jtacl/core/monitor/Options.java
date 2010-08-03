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

package fr.univrennes1.cri.jtacl.core.monitor;

import fr.univrennes1.cri.jtacl.core.exceptions.JtaclConfigurationException;
import java.util.logging.Level;

/**
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class Options {

	/**
	 * The max number of hop while probing. The monitor stops probing if this
	 * number is reached.
	 */
	private int _maxHop = -1;

	public int getMaxHop() {
		return _maxHop;
	}

	public void setMaxHop(int maxHop) {
		_maxHop = maxHop;
	}

	public Level getDebugLevel() {
		return Log.debug().getLevel();
	}

	public void setDebugLevel(Level level) {
		Log.debug().setLevel(level);
	}

	public Level getNotifyLevel() {
		return Log.notifier().getLevel();
	}

	public void setNotifyLevel(Level level) {
		Log.notifier().setLevel(level);
	}

	public Level getConfigLevel() {
		return Log.config().getLevel();
	}

	public void setConfigLevel(Level level) {
		Log.config().setLevel(level);
	}


	public void setOption(String optionName, String value) {

		try {
			if (optionName.equalsIgnoreCase("maxhop")) {
				setMaxHop(Integer.valueOf(value));
				return;
			}

			if (optionName.equalsIgnoreCase("debug.level")) {
				setDebugLevel(Level.parse(value));
				return;
			}

			if (optionName.equalsIgnoreCase("notify.level")) {
				setNotifyLevel(Level.parse(value));
				return;
			}

			if (optionName.equalsIgnoreCase("config.level")) {
				setConfigLevel(Level.parse(value));
				return;
			}

		} catch (Exception e) {
			throw new JtaclConfigurationException(e.getMessage());
		}
		throw new JtaclConfigurationException("Option unknown");
	}

	public String getOptionsList() {
		return "debug.level=" + getDebugLevel() + "\n" +
				"notify.level=" + getNotifyLevel() + "\n" +
				"config.level=" + getConfigLevel() + "\n" +
				"maxhop=" + getMaxHop();

	}

}