/*
 * Copyright (c) 2013 - 2020, Universite de Rennes 1
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

package fr.univrennes1.cri.jtacl.equipments.fortigate;

import fr.univrennes1.cri.jtacl.core.probing.Probe;
import fr.univrennes1.cri.jtacl.lib.ip.IPRangeable;
import fr.univrennes1.cri.jtacl.lib.ip.PortSpec;

import java.util.List;

public class FgProtoAllService extends FgAddressService {
    protected FgTcpUdpSctpService _service;

    public FgProtoAllService(String name, String originKey, String comment
            , List<IPRangeable> ipRanges
            , String fqdn
            , PortSpec udpSourcePortSpec
            , PortSpec udpPortSpec
            , PortSpec tcpSourcePortSpec
            , PortSpec tcpPortSpec
            , PortSpec sctpSourcePortSpec
            , PortSpec sctpPortSpec) {

        super(name, originKey, comment, ipRanges, fqdn, FgServiceType.ALL);
        _service = new FgTcpUdpSctpService(name, originKey, comment, ipRanges, fqdn, udpSourcePortSpec
            , udpPortSpec, tcpSourcePortSpec, tcpPortSpec, sctpSourcePortSpec, sctpPortSpec);
    }

    @Override
    public FgServicesMatch matches(Probe probe) {
        return _service.matches(probe);
    }

    @Override
    public String toString() {
        return _service.toString();
    }
}
