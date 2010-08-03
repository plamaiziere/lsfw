/*
 * Copyright (c) 2010, Université de Rennes 1
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

package fr.univrennes1.cri.jtacl.core.monitor;

import fr.univrennes1.cri.jtacl.core.exceptions.JtaclConfigurationException;
import fr.univrennes1.cri.jtacl.core.network.NetworkEquipment;
import junit.framework.TestCase;

/**
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class MonitorTest extends TestCase {
    
    public MonitorTest(String testName) {
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
	 * Test of createNetworkEquipment method, of class Monitor.
	 */
	public void testCreateNetworkEquipment() {
		System.out.println("createNetworkEquipment");
		String className = "fr.univrennes1.cri.jtacl.equipments.SimpleRouter";
		String equipmentName = "";
		String equipmentComment = "";
		Monitor monitor = Monitor.getInstance();
		NetworkEquipment result = monitor.createNetworkEquipment(className, equipmentName, equipmentComment, null);
		assertEquals(result.getClass().getName(), "fr.univrennes1.cri.jtacl.equipments.SimpleRouter");

		boolean f = false;
		try {
			result = monitor.createNetworkEquipment("foo", equipmentName, equipmentComment, null);
		}
		catch (JtaclConfigurationException e) {
			System.out.println("check: " + e.getMessage());
			f = true;
		}
		assertTrue(f);
	}

}
