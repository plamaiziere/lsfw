/*
 * Copyright (c) 2013, Universite de Rennes 1
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

package fr.univrennes1.cri.jtacl.equipments.checkpoint;

import fr.univrennes1.cri.jtacl.core.exceptions.JtaclInternalException;

/**
 * Checkpoint service object
 * Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class CpService {
	protected String _name;
	protected String _serviceTable;
	protected String _comment;
	protected int _serviceType = 0;
	protected CpPortItem _portItem;
	protected Integer _protocol;

	public final static int TCP_SERVICE = 1;
	public final static int UDP_SERVICE = 2;
	public final static int SERVICE_GROUP = 3;

	public CpService(String name, String serviceTable, String comment,
			int serviceType) {

		_name = name;
		_serviceTable = serviceTable;
		_comment = comment;
		_serviceType = serviceType;
	}

	public static CpService newTcpUdpService(String name, String serviceTable,
		String comment, int serviceType, CpPortItem portItem) {

		if (serviceType != TCP_SERVICE && serviceType != UDP_SERVICE)
			throw new JtaclInternalException("invalid serviceType");

		CpService service = new CpService(name, serviceTable, comment,
			serviceType);

		service._portItem = portItem;
		return service;
	}

	public boolean isTcpService() {
		return _serviceType == TCP_SERVICE;
	}

	public boolean isUdpService() {
		return _serviceType == UDP_SERVICE;
	}

	public boolean isServiceGroup() {
		return _serviceType == SERVICE_GROUP;
	}

	@Override
	public String toString() {

		String st = null;
		switch (_serviceType) {
			case TCP_SERVICE:	st = "TCP_SERVICE";
								break;
			case UDP_SERVICE:	st = "UDP_SERVICE";
								break;
			case SERVICE_GROUP: st = "SERVICE_GROUP";
								break;
		}

		String s = st + ", " + _serviceTable + ", " + _name + ", " + _comment
				+ ", ";

		switch (_serviceType) {
			case TCP_SERVICE:	s+= _portItem.toString();
								break;
			case UDP_SERVICE:	s+= _portItem.toString();
								break;
			case SERVICE_GROUP: s+= "SERVICE_GROUP, ";
								break;
		}
		return s;
	}

}
