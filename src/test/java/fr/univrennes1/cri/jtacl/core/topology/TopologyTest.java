/*
 * Copyright (c) 2010, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.core.topology;

import fr.univrennes1.cri.jtacl.core.monitor.Monitor;
import fr.univrennes1.cri.jtacl.core.network.Iface;
import fr.univrennes1.cri.jtacl.core.network.IfaceLink;
import fr.univrennes1.cri.jtacl.core.network.NetworkEquipment;
import fr.univrennes1.cri.jtacl.lib.ip.IPNet;

import java.net.UnknownHostException;

import junit.framework.TestCase;

/**
 * Test Class for {@link Topology}.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class TopologyTest extends TestCase {

    public TopologyTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of registerNetworkequipment method, of class Topology.
     */
    public void testRegisterNetworkequipment() {
        System.out.println("registerNetworkequipment");
        Topology topology = new Topology();
        Monitor monitor = Monitor.getInstance();
        NetworkEquipment ne = new NetworkEquipment(monitor, "n1", "equipmnent 1", null);
        topology.registerNetworkequipment(ne);
        assertTrue(topology.isRegistered(ne));
    }

    /**
     * Test of isRegistered method, of class Topology.
     */
    public void testIsRegistered() {
        System.out.println("isRegistered");
        Topology topology = new Topology();
        Monitor monitor = Monitor.getInstance();
        NetworkEquipment ne = new NetworkEquipment(monitor, "n1", "equipment 1", null);

        assertFalse(topology.isRegistered(ne));
        topology.registerNetworkequipment(ne);
        assertTrue(topology.isRegistered(ne));
    }

    /**
     * Test of getEquipment method, of class Topology.
     */
    public void testFindequipment() {
        System.out.println("findequipment");
        Topology topology = new Topology();
        Monitor monitor = Monitor.getInstance();
        NetworkEquipment ne = new NetworkEquipment(monitor, "n1", "equipment 1", null);

        assertTrue(topology.getEquipment("foo") == null);
        topology.registerNetworkequipment(ne);
        assertTrue(topology.getEquipment("n1") == ne);
    }

    /**
     * Test of makeTopology, of class Topology.
     */
    public void testMakeTopology() throws UnknownHostException {
        System.out.println("makeTopology");

        /* test case with 2 router
         * 192.168.0.0/24 - R1 - 10.0.0.0/24 - R2 -192.0.0.0/24
         */

        Topology topology = new Topology();
        // R1
        Monitor monitor = Monitor.getInstance();
        NetworkEquipment r1 = new NetworkEquipment(monitor, "R1", "Router R1", null);
        Iface r1i1 = r1.addIface("eth0", "interface eth0");
        r1i1.addLink(new IPNet("192.168.0.1"), new IPNet("192.168.0.0/24"));

        Iface r1i2 = r1.addIface("eth1", "inteface eth1");
        r1i2.addLink(new IPNet("10.0.0.1"), new IPNet("10.0.0.0/24"));

        // R2
        NetworkEquipment r2 = new NetworkEquipment(monitor, "R2", "Router R2", null);
        Iface r2i1 = r2.addIface("eth0", "interface eth0");
        r2i1.addLink(new IPNet("192.168.1.1"), new IPNet("192.168.1.0/24"));

        Iface r2i2 = r2.addIface("eth1", "interface eth1");
        r2i2.addLink(new IPNet("10.0.0.2"), new IPNet("10.0.0.0/24"));

        topology.registerNetworkequipment(r1);
        topology.registerNetworkequipment(r2);

        topology.makeTopology();
        String s = topology.toString();


        // R1 - 10.0.0.0/24 - R2
        NetworkLinks nlinks = topology.getNetworkLinksByNetwork(new IPNet("10.0.0.0/24"));
        assertEquals(1, nlinks.size());
        IfaceLink ilink = nlinks.get(0).getIfaceLink(new IPNet("10.0.0.1"));
        assertTrue(ilink != null);
        ilink = nlinks.get(0).getIfaceLink(new IPNet("10.0.0.2"));
        assertTrue(ilink != null);

        // R1 - 192.168.0.0
        nlinks = topology.getNetworkLinksByNetwork(new IPNet("192.168.0.0/24"));
        assertEquals(1, nlinks.size());
        ilink = nlinks.get(0).getIfaceLink(new IPNet("192.168.0.1"));
        assertTrue(ilink != null);

        // R2 - 192.168.1.0
        nlinks = topology.getNetworkLinksByNetwork(new IPNet("192.168.1.0/24"));
        assertEquals(1, nlinks.size());
        ilink = nlinks.get(0).getIfaceLink(new IPNet("192.168.1.1"));
        assertTrue(ilink != null);

    }

}
