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

import java.net.InetAddress;

/**
 * InetAddress entry in the dns cache.
 * @author patrick.lamaiziere@univ-rennes1.fr
 */

public class DnsCacheEntry {
	protected String _hostname;
	protected InetAddress _ips [];
	protected long _date;

	public DnsCacheEntry(String hostname, InetAddress ips [], long date) {
		_hostname = hostname;
		_ips = ips;
		_date = date;
	}

	public long getDate() {
		return _date;
	}

	public InetAddress[] getIps() {
		return _ips;
	}

	public String getHostname() {
		return _hostname;
	}

}
