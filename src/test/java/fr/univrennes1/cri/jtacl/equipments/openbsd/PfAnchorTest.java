/*
 * Copyright (c) 2010, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.equipments.openbsd;

import java.util.List;
import junit.framework.TestCase;

/**
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PfAnchorTest extends TestCase {
    
    public PfAnchorTest(String testName) {
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
	 * Test of newRootAnchor method, of class PfAnchor.
	 */
	public void testNewRootAnchor() {
		System.out.println("newRootAnchor");
		PfAnchor result = PfAnchor.newRootAnchor();
		assertTrue(result.isRoot());
		assertEquals("/", result.getName());
		assertEquals(result, result.getRoot());
	}

	/**
	 * Test of getPath method, of class PfAnchor.
	 */
	public void testGetPath() {
		System.out.println("getPath");
		PfAnchor root = PfAnchor.newRootAnchor();

		PfAnchor anchor;
		List<PfAnchor> anchorList;

		root.findOrCreateAnchor("/anchor2");
		root.findOrCreateAnchor("/anchor1");
		root.findOrCreateAnchor("/anchor1/anchor11");
		root.findOrCreateAnchor("/anchor1/anchor12");
		root.findOrCreateAnchor("/anchor2");

		anchorList = root.findAnchors("/*");
		assertTrue(anchorList.size() == 2);
		anchor = anchorList.get(0);
		assertEquals("/anchor1", anchor.getPath());
		anchor = anchorList.get(1);
		assertEquals("/anchor2", anchor.getPath());

		anchorList = root.findAnchors("/anchor1/*");
		assertTrue(anchorList.size() == 2);
		anchor = anchorList.get(0);
		assertEquals("/anchor1/anchor11", anchor.getPath());
		anchor = anchorList.get(1);
		assertEquals("/anchor1/anchor12", anchor.getPath());

	}

	/**
	 * Test of findOrCreateAnchor method, of class PfAnchor.
	 */
	public void testFindOrCreateAnchor() {
		System.out.println("findOrCreateAnchor");
		PfAnchor root = PfAnchor.newRootAnchor();

		PfAnchor anchor;

		anchor = root.findOrCreateAnchor("/..");
		assertEquals(null, anchor);

		anchor = root.findOrCreateAnchor("..");
		assertEquals(null, anchor);

		anchor = root.findOrCreateAnchor("///");
		assertTrue(anchor.isRoot());

		anchor = root.findOrCreateAnchor("anchor1//anchor2");
		assertEquals("anchor2", anchor.getName());
		assertEquals("anchor1", anchor.getParent().getName());

		anchor = anchor.findOrCreateAnchor("..////../");
		assertTrue(anchor.isRoot());
	}

	/**
	 * Test of findAnchors method, of class PfAnchor.
	 */
	public void testFindAnchors() {
		System.out.println("findAnchors");
		PfAnchor root = PfAnchor.newRootAnchor();

		PfAnchor anchor;
		List<PfAnchor> anchorList;

		root.findOrCreateAnchor("/anchor2");
		root.findOrCreateAnchor("/anchor1");
		root.findOrCreateAnchor("/anchor1/anchor11");
		root.findOrCreateAnchor("/anchor1/anchor12");
		root.findOrCreateAnchor("/anchor2");

		anchorList = root.findAnchors("/");
		assertTrue(anchorList.size() == 1);
		assertTrue(anchorList.get(0).isRoot());

		anchorList = root.findAnchors("/anchor1");
		assertTrue(anchorList.size() == 1);
		assertEquals("anchor1", anchorList.get(0).getName());

		anchorList = root.findAnchors("/*");
		assertTrue(anchorList.size() == 2);
		assertEquals("anchor1", anchorList.get(0).getName());
		assertEquals("anchor2", anchorList.get(1).getName());

		anchor = anchorList.get(1);
		anchorList = anchor.findAnchors("../anchor1/*");
		assertTrue(anchorList.size() == 2);
		assertEquals("anchor11", anchorList.get(0).getName());
		assertEquals("anchor12", anchorList.get(1).getName());

		anchorList = anchor.findAnchors("/foo");
		assertTrue(anchorList.size() == 0);

		anchorList = anchor.findAnchors("*/foo");
		assertTrue(anchorList == null);

	}

}
