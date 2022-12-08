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

import java.math.BigInteger;

/**
 * Base of an IP address : value, prefixlen and familly
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */

public final class IPBase {

	private final BigInteger _ip;
	private final int _prefixlen;
	private final IPversion _ipVersion;

	public IPBase(BigInteger ip, int prefixlen, IPversion ipVersion) {
		_ip = ip;
		_prefixlen = prefixlen;
		_ipVersion = ipVersion;
	}

	public IPBase(IPNet ip) {
		_ip = ip.getIP();
		_prefixlen = ip.getPrefixLen();
		_ipVersion = ip.getIpVersion();
	}

	public BigInteger getIP() {
		return _ip;
	}

	public IPversion getIpVersion() {
		return _ipVersion;
	}

	public int getPrefixlen() {
		return _prefixlen;
	}

	public boolean isIPv4() {
		return _ipVersion == IPversion.IPV4;
	}

	public boolean isIPv6() {
		return _ipVersion == IPversion.IPV6;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final IPBase other = (IPBase) obj;
		if (this._ip != other._ip && (this._ip == null ||
				!this._ip.equals(other._ip))) {
			return false;
		}
		if (this._prefixlen != other._prefixlen) {
			return false;
		}
		if (this._ipVersion != other._ipVersion) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 67 * hash + (this._ip != null ? this._ip.hashCode() : 0);
		hash = 67 * hash + this._prefixlen;
		hash = 67 * hash + (this._ipVersion != null ?
				this._ipVersion.hashCode() : 0);
		return hash;
	}

}
