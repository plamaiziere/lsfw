package fr.univrennes1.cri.jtacl.equipments.fortigate;

import fr.univrennes1.cri.jtacl.core.probing.MatchResult;
import fr.univrennes1.cri.jtacl.core.probing.ProbeRequest;
import fr.univrennes1.cri.jtacl.lib.ip.*;
import junit.framework.TestCase;

import java.net.UnknownHostException;

public class FgTcpUdpSctpServiceTest extends TestCase {

    public void testMatches() throws UnknownHostException {

        PortSpec sSourceUdp = new PortSpec(PortOperator.RANGE, 1000, 2000);
        PortSpec sudp = new PortSpec(PortOperator.RANGE, 30000, 40000);

        ProtocolsSpec proto = new ProtocolsSpec();
        proto.add(Protocols.UDP);

        FgTcpUdpSctpService fs = new FgTcpUdpSctpService(
                "test", "test", "test", new IPRange("123.0.0.0"), null, sSourceUdp, sudp, null, null, null, null);
        ProbeRequest request = new ProbeRequest();
        request.setProtocols(proto);
        request.setDestinationPort(new PortSpec(PortOperator.EQ, 35000));
        assertTrue("Match ALL", fs.matches(request).getMatchResult() == MatchResult.ALL);

        request.setDestinationPort(new PortSpec(PortOperator.EQ, 1000));
        assertTrue("Match not", fs.matches(request).getMatchResult() == MatchResult.NOT);

        request.setDestinationPort(new PortSpec(PortOperator.RANGE, 29000, 31100));
        assertTrue("Match may", fs.matches(request).getMatchResult() == MatchResult.MATCH);

        proto = new ProtocolsSpec();
        proto.add(Protocols.TCP);
        request.setProtocols(proto);
        assertTrue("Match not", fs.matches(request).getMatchResult() == MatchResult.NOT);

        proto.add(Protocols.UDP);
        request.setProtocols(proto);
        assertTrue("Match not", fs.matches(request).getMatchResult() == MatchResult.MATCH);
    }
}
