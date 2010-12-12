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

	public void setDynaddr(String dynaddr) {
		_dynaddr = dynaddr;
	}

	public String getDynaddrMask() {
		return _dynaddrMask;
	}

	public void setDynaddrMask(String dynaddrMask) {
		_dynaddrMask = dynaddrMask;
	}

	public String getIfName() {
		return _ifName;
	}

	public void setIfName(String ifName) {
		_ifName = ifName;
	}

	public String getFirstAddress() {
		return _firstAddress;
	}

	public void setFirstAddress(String firstAddress) {
		_firstAddress = firstAddress;
	}

	public String getLastAddress() {
		return _lastAddress;
	}

	public void setLastAddress(String lastAddress) {
		_lastAddress = lastAddress;
	}

	public boolean isNoroute() {
		return _noroute;
	}

	public void setNoroute(boolean noroute) {
		_noroute = noroute;
	}

	public boolean isNot() {
		return _not;
	}

	public void setNot(boolean not) {
		_not = not;
	}

	public String getRoute() {
		return _route;
	}

	public void setRoute(String route) {
		_route = route;
	}

	public boolean isUrpffailed() {
		return _urpffailed;
	}

	public void setUrpffailed(boolean urpffailed) {
		_urpffailed = urpffailed;
	}

	public boolean isAny() {
		return _any;
	}

	public void setAny(boolean any) {
		_any = any;
	}

	public String getTable() {
		return _table;
	}

	public void setTable(String table) {
		_table = table;
	}

}
