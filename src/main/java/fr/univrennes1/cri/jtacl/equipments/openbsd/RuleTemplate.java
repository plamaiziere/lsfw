/*
 * Copyright (c) 2010, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.equipments.openbsd;

import fr.univrennes1.cri.jtacl.lib.misc.StringsList;

import java.util.ArrayList;
import java.util.List;

/**
 * Template to build PF rule. This class is used at parsing time
 * as an intermediate storage.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 * @see PfRule
 */
public class RuleTemplate {

    private String _action;
    private String _dir;
    private boolean _quick;
    private StringsList _ifList = new StringsList();
    private StringsList _protoList = new StringsList();
    private StringsList _osList = new StringsList();
    private String _af;
    private boolean _all;
    private List<Xhost> _sourceHostList = new ArrayList<>();
    private List<PortItemTemplate> _sourcePortList = new ArrayList<>();
    private List<Xhost> _destHostList = new ArrayList<>();
    private List<PortItemTemplate> _destPortList = new ArrayList<>();
    private FilterOptsTemplate _filterOpts = new FilterOptsTemplate();


    public String getAction() {
        return _action;
    }

    public boolean setAction(String action) {
        _action = action;
        return true;
    }

    public String getDir() {
        return _dir;
    }

    public boolean setDir(String dir) {
        _dir = dir;
        return true;
    }

    public boolean isQuick() {
        return _quick;
    }

    public boolean setQuick(boolean quick) {
        _quick = quick;
        return true;
    }

    public StringsList getIfList() {
        return _ifList;
    }

    public String getAf() {
        return _af;
    }

    public boolean setAf(String af) {
        _af = af;
        return true;
    }

    public StringsList getProtoList() {
        return _protoList;
    }

    public StringsList getOsList() {
        return _osList;
    }

    public List<Xhost> getSourceHostList() {
        return _sourceHostList;
    }

    public List<PortItemTemplate> getSourcePortList() {
        return _sourcePortList;
    }

    public List<Xhost> getDestHostList() {
        return _destHostList;
    }

    public List<PortItemTemplate> getDestPortList() {
        return _destPortList;
    }

    public boolean isAll() {
        return _all;
    }

    public boolean setAll(boolean all) {
        _all = all;
        return true;
    }

    public FilterOptsTemplate getFilterOpts() {
        return _filterOpts;
    }

    public boolean addSourceHost(List<Xhost> hosts) {
        _sourceHostList.addAll(hosts);
        return true;
    }

    public boolean addDestinationHost(List<Xhost> hosts) {
        _destHostList.addAll(hosts);
        return true;
    }

    public boolean addSourcePort(List<PortItemTemplate> ports) {
        _sourcePortList.addAll(ports);
        return true;
    }

    public boolean addDestinationPort(List<PortItemTemplate> ports) {
        _destPortList.addAll(ports);
        return true;
    }

}
