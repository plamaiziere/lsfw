/*
 * Copyright (c) 2010, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.equipments.cisco.router;

import fr.univrennes1.cri.jtacl.lib.misc.StringsList;

/**
 * Template to build ACE (access-list element). This class is used at parsing time
 * as an intermediate storage.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 * @see AccessListElement
 */
public class AceTemplate {
    private String _action;
    private String _protocol;
    private String _srcIp;
    private String _srcIpMask;
    private String _srcPortOperator;
    private String _srcFirstPort;
    private String _srcLastPort;
    private String _dstIp;
    private String _dstIpMask;
    private String _dstPortOperator;
    private String _dstFirstPort;
    private String _dstLastPort;
    private String _subType;
    private Integer _code;
    private boolean _inactive;
    private StringsList _tcpFlags = new StringsList();
    private String _tcpKeyword;

    public String getAction() {
        return _action;
    }

    public boolean setAction(String action) {
        _action = action;
        return true;
    }

    public String getDstFirstPort() {
        return _dstFirstPort;
    }

    public boolean setDstFirstPort(String dstFirstPort) {
        _dstFirstPort = dstFirstPort;
        return true;
    }

    public String getDstIp() {
        return _dstIp;
    }

    public boolean setDstIp(String dstIp) {
        _dstIp = dstIp;
        return true;
    }

    public String getDstIpMask() {
        return _dstIpMask;
    }

    public boolean setDstIpMask(String dstIpMask) {
        _dstIpMask = dstIpMask;
        return true;
    }

    public String getDstLastPort() {
        return _dstLastPort;
    }

    public boolean setDstLastPort(String dstLastPort) {
        _dstLastPort = dstLastPort;
        return true;
    }

    public String getDstPortOperator() {
        return _dstPortOperator;
    }

    public boolean setDstPortOperator(String dstPortOperator) {
        _dstPortOperator = dstPortOperator;
        return true;
    }

    public String getSubType() {
        return _subType;
    }

    public boolean setSubType(String subType) {
        _subType = subType;
        return true;
    }

    public Integer getCode() {
        return _code;
    }

    public boolean setCode(Integer code) {
        _code = code;
        return true;
    }

    public boolean getInactive() {
        return _inactive;
    }

    public boolean setInactive(boolean inactive) {
        _inactive = inactive;
        return true;
    }

    public String getProtocol() {
        return _protocol;
    }

    public boolean setProtocol(String protocol) {
        _protocol = protocol;
        return true;
    }

    public String getSrcFirstPort() {
        return _srcFirstPort;
    }

    public boolean setSrcFirstPort(String srcFirstPort) {
        _srcFirstPort = srcFirstPort;
        return true;
    }

    public String getSrcIp() {
        return _srcIp;
    }

    public boolean setSrcIp(String srcIp) {
        _srcIp = srcIp;
        return true;
    }

    public String getSrcIpMask() {
        return _srcIpMask;
    }

    public boolean setSrcIpMask(String srcIpMask) {
        _srcIpMask = srcIpMask;
        return true;
    }

    public String getSrcLastPort() {
        return _srcLastPort;
    }

    public boolean setSrcLastPort(String srcLastPort) {
        _srcLastPort = srcLastPort;
        return true;
    }

    public String getSrcPortOperator() {
        return _srcPortOperator;
    }

    public boolean setSrcPortOperator(String srcPortOperator) {
        _srcPortOperator = srcPortOperator;
        return true;
    }

    public StringsList getTcpFlags() {
        return _tcpFlags;
    }

    public String getTcpKeyword() {
        return _tcpKeyword;
    }

    public boolean setTcpKeyword(String tcpKeyword) {
        _tcpKeyword = tcpKeyword;
        return true;
    }

}
