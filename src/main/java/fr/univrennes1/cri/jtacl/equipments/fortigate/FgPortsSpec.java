/*
 * Copyright (c) 2013 - 2020, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */
package fr.univrennes1.cri.jtacl.equipments.fortigate;

import fr.univrennes1.cri.jtacl.core.probing.MatchResult;
import fr.univrennes1.cri.jtacl.core.probing.ProbeRequest;
import fr.univrennes1.cri.jtacl.lib.ip.PortSpec;

public class FgPortsSpec {
    protected PortSpec _sourcePorts;
    protected PortSpec _destPorts;

    public FgPortsSpec(PortSpec sourcePorts, PortSpec destPorts) {
        _sourcePorts = sourcePorts;
        _destPorts = destPorts;
    }

    public PortSpec getSourcePorts() { return _sourcePorts; }
    public PortSpec getDestPorts() { return _destPorts; }
    public boolean hasSourcePortSpec() {return _sourcePorts != null;}
    public boolean hasDestPortSpec() {return _destPorts != null;}

    @Override
    public String toString() {
        String s = "";
        if (hasSourcePortSpec()) s+= "src.ports=" + _sourcePorts + (hasDestPortSpec() ? ", " : "");
        if (hasDestPortSpec()) s+= "dst.ports=" + _destPorts;
        return s;
    }

	public MatchResult matches(ProbeRequest request) {

		/*
		 * source port
		 */
		PortSpec port = request.getSourcePort();
		int sourceMay = 0;
		MatchResult mres = MatchResult.ALL;
		if (port != null && _sourcePorts != null) {
			mres = _sourcePorts.matches(port);
			/*
			 * does not match at all
			 */
			if (mres == MatchResult.NOT) {
				return MatchResult.NOT;
			}
		}
		if (mres != MatchResult.ALL)
			sourceMay++;

		/*
		 * destination port
		 */
		port = request.getDestinationPort();
		int destMay = 0;
		mres = MatchResult.ALL;
		if (port != null && _destPorts != null) {
			mres = _destPorts.matches(port);
			/*
			 * does not match at all
			 */
			if (mres == MatchResult.NOT) {
				return MatchResult.NOT;
			}
		}
		if (mres != MatchResult.ALL)
			destMay++;
		if (sourceMay == 0 && destMay == 0) {
			return MatchResult.ALL;
		}

		return MatchResult.MATCH;
    }
}
