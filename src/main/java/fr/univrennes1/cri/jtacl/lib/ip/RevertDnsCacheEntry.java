/*
 * Copyright (c) 2011, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.lib.ip;

import java.net.InetAddress;

/**
 * Hostname revert entry in the dns cache.
 *
 * @author patrick.lamaiziere@univ-rennes1.fr
 */

public class RevertDnsCacheEntry {
    protected InetAddress _address;
    protected String _hostname;
    protected long _date;

    public RevertDnsCacheEntry(InetAddress address, String hostname, long date) {
        _address = address;
        _hostname = hostname;
        _date = date;
    }

    public long getDate() {
        return _date;
    }

    public String getHostname() {
        return _hostname;
    }

    public InetAddress getAddress() {
        return _address;
    }

}
