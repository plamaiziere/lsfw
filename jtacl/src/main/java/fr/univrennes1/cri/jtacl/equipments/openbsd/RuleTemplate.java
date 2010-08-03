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

package fr.univrennes1.cri.jtacl.equipments.openbsd;

import fr.univrennes1.cri.jtacl.lib.misc.StringsList;
import java.util.ArrayList;
import java.util.List;

/**
 * Template to build PF rule. This class is used at parsing time
 * as an intermediate storage.
 * @see PfRule
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class RuleTemplate {

	private String _action;
	private String _dir;
	private boolean _quick;
	private StringsList _ifList = new StringsList();
	private StringsList _protoList = new StringsList();
	private StringsList _osList = new StringsList();
	private String _af;
	private boolean _all;
	private List<Xhost> _sourceHostList = new ArrayList<Xhost>();
	private List<PortItemTemplate> _sourcePortList = new ArrayList<PortItemTemplate>();
	private List<Xhost> _destHostList = new ArrayList<Xhost>();
	private List<PortItemTemplate> _destPortList = new ArrayList<PortItemTemplate>();
	private FilterOptsTemplate _filterOpts = new FilterOptsTemplate();


	public String getAction() {
		return _action;
	}

	public void setAction(String action) {
		_action = action;
	}

	public String getDir() {
		return _dir;
	}

	public void setDir(String dir) {
		_dir = dir;
	}

	public boolean isQuick() {
		return _quick;
	}

	public void setQuick(boolean quick) {
		_quick = quick;
	}

	public StringsList getIfList() {
		return _ifList;
	}

	public String getAf() {
		return _af;
	}

	public void setAf(String af) {
		_af = af;
	}

	public StringsList getProtoList() {
		return _protoList;
	}

	public StringsList getOsList() {
		return _osList;
	}

	public List<Xhost> getSourceHostList() {
		return _sourceHostList;
	}

	public List<PortItemTemplate> getSourcePortList() {
		return _sourcePortList;
	}

	public List<Xhost> getDestHostList() {
		return _destHostList;
	}

	public List<PortItemTemplate> getDestPortList() {
		return _destPortList;
	}

	public boolean isAll() {
		return _all;
	}

	public void setAll(boolean all) {
		_all = all;
	}

	public FilterOptsTemplate getFilterOpts() {
		return _filterOpts;
	}

}
