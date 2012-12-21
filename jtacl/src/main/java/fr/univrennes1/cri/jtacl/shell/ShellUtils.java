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

package fr.univrennes1.cri.jtacl.shell;

import fr.univrennes1.cri.jtacl.core.exceptions.JtaclInternalException;
import fr.univrennes1.cri.jtacl.core.exceptions.JtaclParameterException;
import fr.univrennes1.cri.jtacl.core.monitor.Monitor;
import fr.univrennes1.cri.jtacl.core.network.Iface;
import fr.univrennes1.cri.jtacl.core.network.IfaceLink;
import fr.univrennes1.cri.jtacl.core.network.IfaceLinks;
import fr.univrennes1.cri.jtacl.core.network.NetworkEquipment;
import fr.univrennes1.cri.jtacl.core.probing.ExpectedProbing;
import fr.univrennes1.cri.jtacl.lib.ip.IPNet;
import fr.univrennes1.cri.jtacl.lib.ip.IPServices;
import fr.univrennes1.cri.jtacl.lib.ip.PortOperator;
import fr.univrennes1.cri.jtacl.lib.ip.PortSpec;
import fr.univrennes1.cri.jtacl.lib.ip.TcpFlags;
import java.net.UnknownHostException;

/**
 * Shell utilites functions (mostly parse)
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class ShellUtils {
	
	/**
	 * Returns true if all the flags specified in 'flags' are valid TCP flags.
	 * @param flags flags to check.
	 * @return true if all the flags specified in 'flags' are valid TCP flags.
	 */
	public static boolean checkTcpFlags(String flags) {

		for (int i = 0; i < flags.length(); i++) {
			if (!TcpFlags.isFlag(flags.charAt(i)))
				return false;
		}
		return true;
	}
	
	/**
	 * Returns the port number of the service / protocol in arguments.
	 * @param service service to get
	 * @param protocol protocol to use
	 * @return the port number of the service.
	 * @throws JtaclParameterException if the service is unknown.
	 */
	public static Integer parseService(String service, String protocol) {
		IPServices ipServices = IPServices.getInstance();

		Integer	port = ipServices.serviceLookup(service, protocol);
		if (port.intValue() == -1) {
			throw new JtaclParameterException(
				"unknown service: " + service);
		}
		return port;
	}
	

	/**
	 * Parses and returns the ports specification in arguments.
	 * The following keywords can be used instead of a service name:
	 * <ul>
     * <li>none : a "none" port (empty specification).</li>
     * <li>any  : a "any" port (range 0-65535)</li>
     * <li>known: a "well known" port between 0..1023.</li>
     * <li>reg  : a "registered" port between 1024..49151.</li>
     * <li>dyn  : a "dynamic" port between 49152..65535.</li>
	 * <li>A range of ports can be specified using the first port and the last port
	 * of the range between parenthesis: (first,last).
	 * Spaces are not allowed in this notation.</li>
	 * </ul>
	 * @param sportSpec ports specification to parse.
	 * @param sprotocol protocol to use.
	 * @return returns a PortSpec specification.
	 * @throws JtaclParameterException if the ports specification is invalid.
	 */
	public static PortSpec parsePortSpec(String sportSpec, String sprotocol) {

		/*
		 * predefined intervals
		 */
		sportSpec = sportSpec.toLowerCase();
		if (sportSpec.equals("none"))
			return PortSpec.NONE;

		if (sportSpec.equals("any"))
			return PortSpec.ANY;

		if (sportSpec.equals("reg"))
			return PortSpec.REGISTERED;

		if (sportSpec.equals("dyn"))
			return PortSpec.DYNAMIC;

		if (sportSpec.equals("known"))
			return PortSpec.WELLKNOWN;

		/*
		 * interval
		 */
		if (sportSpec.startsWith("(") && sportSpec.endsWith(")")) {
			String sports = sportSpec.substring(1, sportSpec.length() - 1);
			String [] ports = sports.split(",");
			if (ports.length != 2) {
				throw new JtaclParameterException(
					"invalid services range: " + sportSpec);
			}
			Integer portFirst = parseService(ports[0], sprotocol);
			if (portFirst == null)
				return null;
			Integer portLast = parseService(ports[1], sprotocol);
			if (portLast == null)
				return null;
			PortSpec spec = new PortSpec(PortOperator.RANGE, portFirst, portLast);
			return spec;
		}

		/*
		 * service
		 */
		Integer port = parseService(sportSpec, sprotocol);
		if (port == null)
			return null;

		PortSpec spec = new PortSpec(PortOperator.EQ, port);
		return spec;
	}
	
	/**
	 * Returns all the {@link IfaceLink} links matching an 'equipment specification'
	 * string.
	 *
	 * The format of the 'equipment specification' string is:
	 * equipment-name'|'[iface-name|IPaddress]
	 *
	 * @param EquipmentSpecification Equipment specification used to filter.
	 * @return a {@link IfaceLinks} list containing the links.
	 */
	public static IfaceLinks getIfaceLinksByEquipmentSpec(IPNet sourceIP,
			String EquipmentSpecification) {

		IfaceLinks resLinks = new IfaceLinks();

		String [] specSplit = EquipmentSpecification.split("\\|");
		String equipmentName = specSplit[0];
		Monitor monitor = Monitor.getInstance();
		NetworkEquipment equipment = monitor.getEquipments().get(equipmentName);
		if (equipment == null) {
			throw new JtaclParameterException(
					"No such equipment: " + equipmentName);
		}
		IPNet ipaddress = null;
		Iface iface = null;
		String ifaceName = null;

		if (specSplit.length ==2) {
			try {
				/*
				 * we can use either an IP address or the name of an interface
				 * try with IP address first.
				 */
				ipaddress = new IPNet(specSplit[1]);
			} catch (UnknownHostException ex) {
				//do nothing (not an IP address)
			}
			if (ipaddress == null) {
				/*
				 * try with an interface
				 */
				ifaceName = specSplit[1];
				iface = equipment.getIface(ifaceName);
				if (iface == null) {
					throw new JtaclParameterException(
						"No such interface: " + ifaceName);
				}
			}
		}
		/*
		 * filter the iface links
		 */
		IfaceLinks links = equipment.getIfaceLinks();
		for (IfaceLink link: links) {
			/*
			 * by interface name
			 */
			if (iface != null) {
				if (link.getIfaceName().equals(ifaceName)) {
					/*
					 * pick up the first link
					 */
					resLinks.add(link);
					break;
				}
				continue;
			}
			/*
			 * by IP address
			 */
			if (ipaddress  != null) {
				if (link.getIp().equals(ipaddress)) {
					resLinks.add(link);
				}
				continue;
			}
			if (sourceIP != null) {
				/*
				 * by source IP address
				 */
				try {
					if (link.getIp().equals(sourceIP) ||
							link.getNetwork().networkContains(sourceIP))
						resLinks.add(link);

					} catch (UnknownHostException ex) {
						// should not happen, just in case.
						throw new JtaclInternalException("Invalid network in " +
							link.getNetwork().toString());
					}
				continue;
			}
		}
		return resLinks;
	}

	public static ExpectedProbing parseExpectedProbing(String expect) {

		boolean notExpect = expect.startsWith("!");
		if (notExpect && expect.length() > 1)
				expect = expect.substring(1);

		ExpectedProbing ep = null;
		try {
			ep = new ExpectedProbing(notExpect, expect);
		} catch (JtaclInternalException ex) {
			throw new JtaclParameterException("invalid expect: " + expect);
		}
		
		return ep;
	}
	
}