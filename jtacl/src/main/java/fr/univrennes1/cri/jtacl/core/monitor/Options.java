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

package fr.univrennes1.cri.jtacl.core.monitor;

import fr.univrennes1.cri.jtacl.core.exceptions.JtaclConfigurationException;
import fr.univrennes1.cri.jtacl.lib.ip.IPNet;
import java.util.logging.Level;

/**
 * Jtacl's options.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class Options {

	/**
	 * The max number of hop while probing. The monitor stops probing if this
	 * number is reached.
	 */
	private int _maxHop = -1;

	/**
	 * Auto-reload option
	 */
	private boolean _autoReload = false;

	/**
	 *  xref option
	 */
	private boolean _xref = true;

	/**
	 * secure level
	 */
	private int _secureLevel = 0;

	/**
	 * Returns the max number of hop while probing.
	 * @return the max number of hop while probing.
	 */
	public int getMaxHop() {
		return _maxHop;
	}

	/**
	 * Sets the max number of hop while probing.
	 * @param maxHop number to set.
	 */
	public void setMaxHop(int maxHop) {
		_maxHop = maxHop;
	}

	/**
	 * Returns the level of the debug logger.
	 * @return the level of the debug logger.
	 */
	public Level getDebugLevel() {
		return Log.debug().getLevel();
	}

	/**
	 * Sets the level of the debug logger.
	 * @param level the level to set.
	 */
	public void setDebugLevel(Level level) {
		Log.debug().setLevel(level);
	}

	/**
	 * Returns the level of the notify logger.
	 * @return the level of the notify logger.
	 */
	public Level getNotifyLevel() {
		return Log.notifier().getLevel();
	}

	/**
	 * Sets the level of the notify logger.
	 * @param level the level to set.
	 */
	public void setNotifyLevel(Level level) {
		Log.notifier().setLevel(level);
	}

	/**
	 * Returns the level of the config logger.
	 * @return the level of the config logger.
	 */
	public Level getConfigLevel() {
		return Log.config().getLevel();
	}

	/**
	 * Sets the level of the config logger.
	 * @param level the level to set.
	 */
	public void setConfigLevel(Level level) {
		Log.config().setLevel(level);
	}

	/**
	 * Gets the autoreload option
	 * @return the state of autoreload.
	 */
	public boolean getAutoReload() {
		return _autoReload;
	}

	/**
	 * Sets the autoreload option
	 * @param flag state of autoreload.
	 */
	public void setAutoReload(boolean flag) {
		_autoReload = flag;
	}

	/**
	 * Gets the DNS cache TTL value.
	 * @return the DNS cache TTL value.
	 */
	public long getDnsCacheTTL() {
		return IPNet.getDnsCacheTtl();
	}

	/**
	 * Sets the DNS cache TTL value.
	 * @param value value to set.
	 */
	public void setDnsCacheTTL(long value) {
		IPNet.setDnsCacheTt(value);
	}

	/** Gets the xref option value.
	 * @return the xref option value.
	 */
	public boolean getXref() {
		return _xref;
	}

	/** Sets the xref option value.
	 * @param xref value to set.
	 */
	public void setXref(boolean xref) {
		_xref = xref;
	}

	/**
	 * Returns the securelevel.
	 * @return the securelevel.
	 */
	public int getSecureLevel() {
		return _secureLevel;
	}

	/**
	 * Sets the securelevel.
	 * @param securelevel to set.
	 */
	public void setSecureLevel(int secureLevel) {
		if (secureLevel < _secureLevel)
			throw new JtaclConfigurationException
						("secure level can only be raised");
			_secureLevel = secureLevel;
	}


	/**
	 * Sets the option with the specified value.
	 * @param optionName the name of the option to set.
	 * @param value value to set.
	 * @throws JtaclConfigurationException if this option is unknown.
	 */
	public void setOption(String optionName, String value) {

		try {
			if (optionName.equalsIgnoreCase("maxhop")) {
				setMaxHop(Integer.valueOf(value));
				return;
			}

			if (optionName.equalsIgnoreCase("autoreload")) {
				setAutoReload(Boolean.valueOf(value));
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

			if (optionName.equalsIgnoreCase("dns.cache.ttl")) {
				setDnsCacheTTL(Long.valueOf(value));
				return;
			}

			if (optionName.equalsIgnoreCase("crossreference")) {
				setXref(Boolean.valueOf(value));
				return;
			}

			if (optionName.equalsIgnoreCase("securelevel")) {
				setSecureLevel(Integer.valueOf(value));
				return;
			}

		} catch (Exception e) {
			throw new JtaclConfigurationException(e.getMessage());
		}
		throw new JtaclConfigurationException("Option unknown");
	}

	/**
	 * Returns a textual representation of the options.
	 * @return a textual representation of the options.
	 */
	public String getOptionsList() {
		return
				"autoreload=" + getAutoReload() + "\n" +
				"config.level=" + getConfigLevel() + "\n" +
				"crossreference=" + getXref() + "\n" +
				"debug.level=" + getDebugLevel() + "\n" +
				"dns.cache.ttl=" + getDnsCacheTTL() + "\n" +
				"maxhop=" + getMaxHop() + "\n" +
				"notify.level=" + getNotifyLevel() + "\n" +
				"secureLevel=" + getSecureLevel();
	}

}