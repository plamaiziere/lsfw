/*
 * Copyright (c) 2013 - 2020, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.equipments.fortigate;

import fr.univrennes1.cri.jtacl.core.probing.Probe;
import fr.univrennes1.cri.jtacl.lib.ip.IPRangeable;

import java.util.List;

public class FgProtoAllService extends FgAddressService {
    protected FgTcpUdpSctpService _service;

    public FgProtoAllService(String name, String originKey, String comment
            , List<IPRangeable> ipRanges
            , String fqdn
            , List<FgPortsSpec> udpPortsSpec
            , List<FgPortsSpec> tcpPortsSpec
            , List<FgPortsSpec> sctpPortsSpec) {

        super(name, originKey, comment, ipRanges, fqdn, FgServiceType.ALL);
        _service = new FgTcpUdpSctpService(name, originKey, comment, ipRanges, fqdn, udpPortsSpec
                , tcpPortsSpec, sctpPortsSpec);
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
