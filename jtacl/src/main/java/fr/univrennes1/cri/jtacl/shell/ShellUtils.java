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
import fr.univrennes1.cri.jtacl.core.network.*;
import fr.univrennes1.cri.jtacl.core.probing.ExpectedProbing;
import fr.univrennes1.cri.jtacl.core.topology.NetworkLink;
import fr.univrennes1.cri.jtacl.lib.ip.*;

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

		int port = ipServices.serviceLookup(service, protocol);
		if (port == -1) {
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
			PortSpec spec;
			try {
				spec = new PortSpec(PortOperator.RANGE, portFirst, portLast);
			} catch (IllegalArgumentException ex) {
				throw new JtaclParameterException(ex.getMessage());
			}
			return spec;
		}

		/*
		 * service
		 */
		Integer port = parseService(sportSpec, sprotocol);
		if (port == null)
			return null;

		PortSpec spec;
		try {
			spec = new PortSpec(PortOperator.EQ, port);
		} catch (IllegalArgumentException ex) {
			throw new JtaclParameterException(ex.getMessage());
		}
		return spec;
	}

	/**
	 * Returns all the {@link IfaceLink} links matching an 'equipment specification'
	 * string.
	 *
	 * The format of the 'equipment specification' string is:
	 * equipment-name'|'[iface-name|IPaddress]
	 *
	 * @param equipmentSpecification Equipment specification used to filter.
	 * @return a {@link IfaceLinks} list containing the links.
	 */
	public static IfaceLinks getIfaceLinksByEquipmentSpec(IPNet sourceIP,
			String equipmentSpecification) {

		IfaceLinks resLinks = new IfaceLinks();

		String [] specSplit = equipmentSpecification.split("\\|");
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
				if (link.getIfaceName().equals(ifaceName) && link.getIp().sameIPVersion(sourceIP))  {
					/*
					 * pick up the first link that matches the ip version
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
				if (link.getIp().equals(sourceIP) ||
						link.getNetwork().contains(sourceIP))
					resLinks.add(link);
				continue;
			}
		}
		return resLinks;
	}

	/**
	 * Returns {@link IfaceLink} list of links that matches the specified IP
	 * @param ip ip to search and interface link for
	 * @return a {@link IfaceLink} list of links is found, null otherwise.
	 */
	public static IfaceLinks getLoopBackIfaceLinksByIP(IPNet ip) {

	    IfaceLinks ilinks = new IfaceLinks();
		Monitor monitor = Monitor.getInstance();
		NetworkEquipmentsByName equipments = monitor.getEquipments();
		for(NetworkEquipment equipment: equipments.values()) {
		    IfaceLink link = getIfaceLinkLoopback(equipment, ip.getIpVersion());
		    if (link != null) ilinks.add(link);
        }
		return ilinks.isEmpty() ? null : ilinks;
	}


	/**
	 * Returns {@link IfaceLink} link of the loopback iface of an equipment
	 * @param ipVersion ip version of the loopback link
	 * @param equipmentName equipment used to filter.
	 * @return a {@link IfaceLink} if a link is found, null otherwise.
	 */
	public static IfaceLink getIfaceLinksByLoopbackEquipment(IPversion ipVersion,
			String equipmentName) {

		Monitor monitor = Monitor.getInstance();
		NetworkEquipment equipment = monitor.getEquipments().get(equipmentName);
		if (equipment == null) {
			throw new JtaclParameterException(
					"No such equipment: " + equipmentName);
		}

		return getIfaceLinkLoopback(equipment, ipVersion);
	}

	/**
	 * Returns {@link IfaceLink} link of the loopback iface of an equipment
	 * @param ipVersion ip version of the loopback link
	 * @param equipment equipment used to filter.
	 * @return a {@link IfaceLink} if a link is found, null otherwise.
	 */
	public static IfaceLink getIfaceLinkLoopback(NetworkEquipment equipment, IPversion ipVersion) {

		/*
		 * filter the iface links
		 */
		IfaceLinks links = equipment.getIfaceLinks();
		for (IfaceLink link: links) {
			/*
			 * search interface loopback
			 */
			if (link.isLoopback() && (link.getIp().getIpVersion() == ipVersion))  {
                /*
                 * pick up the first link that matches the ip version
                 */
                return link;
            }
		}
		return null;
	}

	public static ExpectedProbing parseExpectedProbing(String expect) {

		boolean notExpect = expect.startsWith("!");
		if (notExpect && expect.length() > 1)
				expect = expect.substring(1);

		ExpectedProbing ep;
		try {
			ep = new ExpectedProbing(notExpect, expect);
		} catch (JtaclInternalException ex) {
			throw new JtaclParameterException("invalid expect: " + expect);
		}

		return ep;
	}

	public static IfaceLink findOnRouteIfaceLink(NetworkLink link,
		IPRangeable destination) {

		IfaceLinks res = new IfaceLinks();
		IfaceLinks ilinks = link.getIfaceLinks();
		/*
		 * for each equipment, try to route the destination ip address, if the
		 * networklink returned (via the ifacelink) is not the same as 'link',
		 * it means that the equipment routes to another network. So we can
		 * use the ifacelink on this equipment to reach the destination.
		 */
		for (IfaceLink l: ilinks) {
			NetworkEquipment eq = l.getEquipment();
			Routes routes = eq.getRoutes(destination);
			//noinspection unchecked
			for (Route<IfaceLink> r: routes) {
				IfaceLink rilink = r.getLink();
				if (rilink != null && rilink.getNetworkLink() != link)
					res.add(l);
			}
		}
		if (res.size() != 1)
			return null;
		else
			return res.get(0);
	}

}
