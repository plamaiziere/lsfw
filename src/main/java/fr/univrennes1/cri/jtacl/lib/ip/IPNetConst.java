package fr.univrennes1.cri.jtacl.lib.ip;

import java.net.UnknownHostException;

public class IPNetConst {
    static public IPNet ipLoopbackV4;
    static public IPNet netLoopbackV4;
    static public IPNet ipLoopbackV6;
    static public IPNet netLoopbackV6;

    static {
        try {
            ipLoopbackV4 = new IPNet("127.0.0.1");
            netLoopbackV4 = new IPNet("127.0.0.0/8");
            ipLoopbackV6 = new IPNet("::1");
            netLoopbackV6 = new IPNet("::1/128");

        } catch (UnknownHostException e) {
            e.printStackTrace(); // should not happen!
        }
    }


}
