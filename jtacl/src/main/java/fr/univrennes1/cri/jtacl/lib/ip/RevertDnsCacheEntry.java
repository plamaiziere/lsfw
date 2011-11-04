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

package fr.univrennes1.cri.jtacl.lib.ip;

/**
 * Hostname revert entry in the dns cache.
 * @author patrick.lamaiziere@univ-rennes1.fr
 */

public class RevertDnsCacheEntry {
	protected String _hostname;
	protected long _date;

	public RevertDnsCacheEntry(String hostname, long date) {
		_hostname = hostname;
		_date = date;
	}

	public long getDate() {
		return _date;
	}

	public String getHostname() {
		return _hostname;
	}
}
