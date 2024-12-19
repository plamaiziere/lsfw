/*
 * Copyright (c) 2010, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.lib.ip;

import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

/**
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class IPProtocolsTest extends TestCase {

    public IPProtocolsTest(String testName) {
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
     * Test of getProtoByName method, of class IPProtocols.
     */
    public void testGetProtoByName() throws IOException {
        System.out.println("getProtoByName");
        IPProtocols protocols = IPProtocols.getInstance();
        InputStream stream = protocols.getClass().getResourceAsStream("/ip/protocols");
        protocols.readProtocols(stream);

        IPProtoEnt ent = protocols.getProtoByName("pfsync");
        assertEquals("pfsync", ent.getName());
        assertEquals(1, ent.getAliases().size());
        assertEquals("PFSYNC", ent.getAliases().get(0));
        assertEquals(240, ent.getProto());
    }

    /**
     * Test of getProtoByNumber method, of class IPProtocols.
     */
    public void testGetProtoByNumber() throws IOException {
        System.out.println("getProtoByNumber");
        IPProtocols protocols = IPProtocols.getInstance();
        InputStream stream = protocols.getClass().getResourceAsStream("/ip/protocols");
        protocols.readProtocols(stream);

        IPProtoEnt ent = protocols.getProtoByNumber(240);
        assertEquals("pfsync", ent.getName());
        assertEquals(1, ent.getAliases().size());
        assertEquals("PFSYNC", ent.getAliases().get(0));
        assertEquals(240, ent.getProto());
    }

}
