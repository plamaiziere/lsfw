/*
 * Copyright (c) 2010, Universite de Rennes 1
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

package fr.univrennes1.cri.jtacl.equipments.openbsd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Describes a PF anchor container.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PfAnchor {

	/**
	 * Comparator class to sort anchors in alphabetical order.
	 */
	static class AnchorComparator implements Comparator<PfAnchor> {

		@Override
		public int compare(PfAnchor anchor1, PfAnchor anchor2) {
			return anchor1.getName().compareTo(anchor2.getName());
		}
	}

	/**
	 * comparator
	 */
	private static AnchorComparator _anchorCompare = new AnchorComparator();

	/**
	 * the root anchor
	 */
	protected PfAnchor _root;

	/**
	 * name of the anchor
	 */
	protected String _name;

	/**
	 * parent anchor
	 */
	protected PfAnchor _parent;

	/**
	 * children anchors.
	 */
	protected List<PfAnchor> _children = new ArrayList<>();

	/**
	 * List of rules
	 */
	protected List<PfGenericRule> _rules = new ArrayList<>();

	/**
	 * Map of tables
	 */
	protected Map<String, PfTable> _tables = new HashMap<>();

	/**
	 * Creates a new root anchor.
	 * @return the root anchor.
	 */
	public static PfAnchor newRootAnchor() {
		PfAnchor anchor = new PfAnchor();
		anchor._root = anchor;
		anchor._name = "/";
		return anchor;
	}

	/**
	 * Creates a new anchor attached to this anchor.
	 * @return a new anchor attached to this anchor.
	 */
	public PfAnchor newAnchor(String name) {
		PfAnchor anchor = new PfAnchor();
		anchor._root = _root;
		_children.add(anchor);
		anchor._parent = this;
		anchor._name = name;
		return anchor;
	}

	/**
	 * Returns the root anchor.
	 * @return the root anchor.
	 */
	public PfAnchor getRoot() {
		return _root;
	}

	/**
	 * Checks if this anchor is the root anchor.
	 * @return true if this anchor is the root anchor.
	 */
	public boolean isRoot() {
		return _root == this;
	}

	/**
	 * Returns the name of this anchor.
	 * @return the name of this anchor.
	 */
	public String getName() {
		return _name;
	}

	/*
	 * Returns the path of this anchor.
	 * @return the path of this anchor.
	 */
	public String getPath() {
		String s;
		if (_parent != null)
			s = _parent.getPath() + "/" + _name;
		else
			s = "";
		return s;
	}

	/**
	 * Returns the parent anchor of this anchor.
	 * (null if this anchor is the root anchor)
	 */
	public PfAnchor getParent() {
		return _parent;
	}

	/**
	 * Returns the children anchors list of this anchor.
	 * @return the children anchors list of this anchor.
	 */
	public List<PfAnchor> getChildren() {
		return _children;
	}

	/**
	 * Returns the rules contained in this anchor.
	 * @return the rules contained in this anchor.
	 */
	public List<PfGenericRule> getRules() {
		return _rules;
	}

	/**
	 * Returns the tables contained in this anchor.
	 * @return the tables contained in this anchor.
	 */
	public Map<String, PfTable> getTables() {
		return _tables;
	}

	/**
	 * Add a rule in this anchor
	 * @param rule rule to add
	 */
	public void addRule(PfGenericRule rule) {
		_rules.add(rule);
	}

	/**
	 * Add a table in this anchor
	 * @param table table to add
	 */
	public void addTable(PfTable table) {
		if (_tables.containsKey(table.getName()))
			_tables.remove(table.getName());
		_tables.put(table.getName(), table);
	}

	/**
	 * Retrieves a table by its name in this anchor
	 * @param name name of the table to retrieve.
	 * @return the table named 'name'. Null if the table does not no exist.
	 */
	public PfTable getTable(String name) {
		return _tables.get(name);
	}
	
	/**
	 * Searches the table in argument in this anchor and if not found in the root
	 * anchor.
	 * @param name name of the table to retrieve.
	 * @return the table named 'name'. Null if the table does not no exist.
	 */
	public PfTable findTable(String name) {
		PfTable table = getTable(name);
		if (table == null && !isRoot())
			table = _root.getTable(name);
		return table;
	}

	/**
	 * Retrieves the anchor named name in the children list of this anchor.
	 * @param name name of the anchor to retreive.
	 * @return the child anchor named name, or null if not found.
	 */
	public PfAnchor getAnchor(String name) {
		for (PfAnchor child: _children) {
			String childName = child.getName();
			if (childName != null && childName.equals(name))
				return child;
		}
		return null;
	}

	/**
	 * Find the anchors with the path 'anchorPath'. If the path is ended by '*',
	 * retrieve the anchors sorted by alphabetical order.
	 * @param anchorPath path of the anchors to retrieve.
	 * @return the anchors with the path 'anchorPath'. The list is empty is
	 * none anchor was found. Return null if the path is invalid.
	 */
	public List<PfAnchor> findAnchors(String anchorPath) {
		String apath = anchorPath;

		int p = apath.indexOf('/');
		String path = "";
		String spath = "";
		if (p < 0)
			path = apath;
		if (p == 0) {
			path = "/";
			spath = apath.substring(1);
		}
		if (p > 0) {
			path = apath.substring(0, p);
			spath  = apath.substring(p + 1);
		}
		if (p < 0) {
			path = apath;
			spath = "";
		}

		while (!spath.isEmpty() && spath.charAt(0) == '/') {
			spath = spath.substring(1);
		}

		if (path.equals("/"))
			return _root.findAnchors(spath);
		if (path.equals("..")) {
			if (isRoot())
				return null;
			return _parent.findAnchors(spath);
		}

		if (path.equals("*")) {
			if (!spath.isEmpty())
				return null;
			_children.sort(_anchorCompare);
			return _children;
		}

		List<PfAnchor> anchorList = new ArrayList<>();
		if (path.isEmpty()) {
			anchorList.add(this);
			return anchorList;
		}

		PfAnchor anchor = getAnchor(path);
		if (anchor == null)
			return anchorList;
		return anchor.findAnchors(spath);
	}

	/**
	 * Find the anchor with the path 'anchorPath'. If the anchor does not exist
	 * it is created.
	 * @param anchorPath path of the anchor to retrieve.
	 * @return the anchor with the path 'anchorPath'.
	 * Null if the path is invalid.
	 */
	public PfAnchor findOrCreateAnchor(String anchorPath) {
		String apath = anchorPath;

		/*
		 * remove anchor spec
		 */
		if (apath.endsWith("*"))
			apath = apath.substring(0, apath.length() - 1);

		int p = apath.indexOf('/');
		String path = "";
		String spath = "";
		if (p < 0)
			path = apath;
		if (p == 0) {
			path = "/";
			spath = apath.substring(1);
		}
		if (p > 0) {
			path = apath.substring(0, p);
			spath  = apath.substring(p + 1);
		}
		if (p < 0) {
			path = apath;
			spath = "";
		}

		while (!spath.isEmpty() && spath.charAt(0) == '/') {
			spath = spath.substring(1);
		}

		if (path.equals("/"))
			return _root.findOrCreateAnchor(spath);
		if (path.equals("..")) {
			if (isRoot())
				return null;
			return _parent.findOrCreateAnchor(spath);
		}
		if (path.isEmpty())
			return this;
		PfAnchor anchor = getAnchor(path);
		if (anchor == null)
			anchor = newAnchor(path);
		return anchor.findOrCreateAnchor(spath);
	}

}
