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

/**
 * Host specification
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class Xhost {

	private boolean _any;
	private boolean _not;
	private boolean _noroute;
	private String _route;
	private boolean _urpffailed;
	private String _firstAddress;
	private String _lastAddress;
	private String _table;
	private String _dynaddr;
	private String _dynaddrMask;
	private String _ifName;

	public String getDynaddr() {
		return _dynaddr;
	}

	public boolean setDynaddr(String dynaddr) {
		_dynaddr = dynaddr;
		return true;
	}

	public String getDynaddrMask() {
		return _dynaddrMask;
	}

	public boolean setDynaddrMask(String dynaddrMask) {
		_dynaddrMask = dynaddrMask;
		return true;
	}

	public String getIfName() {
		return _ifName;
	}

	public boolean setIfName(String ifName) {
		_ifName = ifName;
		return true;
	}

	public String getFirstAddress() {
		return _firstAddress;
	}

	public boolean setFirstAddress(String firstAddress) {
		_firstAddress = firstAddress;
		return true;
	}

	public String getLastAddress() {
		return _lastAddress;
	}

	public boolean setLastAddress(String lastAddress) {
		_lastAddress = lastAddress;
		return true;
	}

	public boolean isNoroute() {
		return _noroute;
	}

	public boolean setNoroute(boolean noroute) {
		_noroute = noroute;
		return true;
	}

	public boolean isNot() {
		return _not;
	}

	public boolean setNot(boolean not) {
		_not = not;
		return true;
	}

	public String getRoute() {
		return _route;
	}

	public boolean setRoute(String route) {
		_route = route;
		return true;
	}

	public boolean isUrpffailed() {
		return _urpffailed;
	}

	public boolean setUrpffailed(boolean urpffailed) {
		_urpffailed = urpffailed;
		return true;
	}

	public boolean isAny() {
		return _any;
	}

	public boolean setAny(boolean any) {
		_any = any;
		return true;
	}

	public String getTable() {
		return _table;
	}

	public boolean setTable(String table) {
		_table = table;
		return true;
	}

}
