/*
 * Copyright (c) 2012, Universite de Rennes 1
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

package fr.univrennes1.cri.jtacl.equipments.generic;

import java.util.List;
import junit.framework.TestCase;

/**
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class GenericEquipmentShellParserTest extends TestCase {
	
	public GenericEquipmentShellParserTest(String testName) {
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
	 * Test of expandFormat method, of class GenericEquipmentShellParser.
	 */
	public void testExpandFormat() {
		System.out.println("expandFormat");
		String format = ";%i %o";
		GenericEquipmentShellParser instance = new GenericEquipmentShellParser();
		
		List<String> result = instance.expandFormat(format);
		assertEquals(";", result.get(0));
		assertEquals("%i", result.get(1));
		assertEquals(" ", result.get(2));
		assertEquals("%o", result.get(3));
	}

}
