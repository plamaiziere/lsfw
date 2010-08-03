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

import fr.univrennes1.cri.jtacl.core.network.NetworkEquipment;
import fr.univrennes1.cri.jtacl.core.network.NetworkEquipmentsByName;
import fr.univrennes1.cri.jtacl.lib.xml.XMLUtils;
import java.net.URL;
import junit.framework.TestCase;
import org.w3c.dom.Document;

/**
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class XMLConfigLoaderTest extends TestCase {
    
    public XMLConfigLoaderTest(String testName) {
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
	 * Test of parseEquipments method, of class XMLConfigLoader.
	 */
	public void testParseConfiguration() {
		System.out.println("parseConfiguration");
		URL url = this.getClass().getResource("/TestParseConfiguration.xml");
		String fileName  = url.getFile();
		Document doc = XMLUtils.getXMLDocument(fileName);
		Monitor monitor = new Monitor();
		XMLConfigLoader instance = new XMLConfigLoader(monitor);
		instance.loadEquipments(doc);
		
		// check if the equipement has been added.
		NetworkEquipmentsByName equipments = monitor.getEquipments();
		assertEquals(1, equipments.size());
		
		NetworkEquipment equipment = equipments.get("router1");
		assertNotNull(equipment);
		assertEquals("router1", equipment.getName());
		assertEquals("router #1", equipment.getComment());
	}

}
