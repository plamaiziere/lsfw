/*
 * Copyright (c) 2010, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.equipments.openbsd;

/**
 * Template to build port specification.  This class is used at parsing
 * time as an intermediate storage.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 * @see PfPortItem
 */
public class PortItemTemplate {

    private String _operator;
    private String _firstPort;
    private String _lastPort;

    public String getFirstPort() {
        return _firstPort;
    }

    public boolean setFirstPort(String firstPort) {
        _firstPort = firstPort;
        return true;
    }

    public String getLastPort() {
        return _lastPort;
    }

    public boolean setLastPort(String lastPort) {
        _lastPort = lastPort;
        return true;
    }

    public String getOperator() {
        return _operator;
    }

    public boolean setOperator(String operator) {
        _operator = operator;
        return true;
    }

}
