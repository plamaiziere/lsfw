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

package fr.univrennes1.cri.jtacl.core.network;

import fr.univrennes1.cri.jtacl.core.exceptions.JtaclRoutingException;
import fr.univrennes1.cri.jtacl.core.monitor.Monitor;
import fr.univrennes1.cri.jtacl.core.monitor.Options;
import fr.univrennes1.cri.jtacl.core.probing.Probe;
import fr.univrennes1.cri.jtacl.lib.ip.IPNet;
import fr.univrennes1.cri.jtacl.lib.ip.IPRangeable;
import java.io.PrintStream;

import static fr.univrennes1.cri.jtacl.core.network.Iface.newIface;

/**
 *	Describes a network equipment.<br/><br/>
 *	A network equipment has a name and is connected to the network by at least
 *	one interface {@link Iface}
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class NetworkEquipment {

	/**
	 * name of this equipment.
	 */
	protected String _name;

	/**
	 * a free comment for this equipment.
	 */
	protected String _comment;

	/**
	 * the path of the file containing the configuration of this equipment.<br/>
	 * the format and the reading of this file depend of the implementation.
	 */
	protected String _configurationFileName;

	/**
	 * the iface owned by the equipment.
	 */
	protected IfacesByName _ifaces;

	/**
	 * a reference to the monitor.
	 */
	protected Monitor _monitor;

	/**
	 * a reference to the options
	 */
	protected Options _monitorOptions;

	/**
	 * Create a new {@link NetworkEquipment} with this name and this comment.<br/>
	 * @param monitor the {@link Monitor} monitor associated with this equipment.
	 * @param name the name of the equipment.
	 * @param comment a free comment for this equipment.
	 * @param configurationFileName name of the configuration file to use (may be null).
	 */
	public NetworkEquipment(Monitor monitor, String name, String comment, String configurationFileName) {
		_monitor = monitor;
		_monitorOptions = _monitor.getOptions();
		_name = name;
		_comment = comment;
		_configurationFileName = configurationFileName;
		_ifaces = new IfacesByName();
	}

	/**
	 * Create a new {@link Iface} named name and attach it to this equipment.
	 * <br/><br/>
	 * @param name the name of the Interface. The name of an {@link Iface} must
	 * be unique in one {@link NetworkEquipment}.
	 * @param comment a free comment for this interface.
	 * @return the {@link Iface} created
	 */
	public Iface addIface(String name, String comment) {
		Iface iface = Iface.newIface(_monitor, name, comment, this);
		_ifaces.put(iface);
		return iface;
	}

	/**
	 * Create a new loopback {@link Iface} named name and attach it to this equipment.
	 * <br/><br/>
	 * @param name the name of the Interface. The name of an {@link Iface} must
	 * be unique in one {@link NetworkEquipment}.
	 * @param comment a free comment for this interface.
	 * @return the {@link Iface} created
	 */
	public Iface addLoopbackIface(String name, String comment) {
		Iface iface = Iface.newLoopback(_monitor, name, comment, this);
		_ifaces.put(iface);
		return iface;
	}

	/**
	 * Returns the name of this {@link NetworkEquipment} equipment.
	 * @return name of this {@link NetworkEquipment} equipment.
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Returns the free comment of this {@link NetworkEquipment} equipment.
	 * @return the free comment of this {@link NetworkEquipment} equipment.
	 */
	public String getComment() {
		return _comment;
	}

	/**
	 * Returns the name of the configuration file of this {@link NetworkEquipment}.
	 * @return the name of the configuration file (may be null)
	 */
	public String getConfigurationFileName() {
		return _configurationFileName;
	}

	/**
	 * Returns the {@link Iface} interfaces of this {@link NetworkEquipment}.
	 * @return a {@link IfacesByName} map containing the {@link Iface} interfaces.
	 * The returned map could be empty but not null.
	 */
	public IfacesByName getIfaces() {
		return _ifaces;
	}

	/**
	 * Returns the {@link Iface} named 'name' in this {@link NetworkEquipment}.
	 * @param name the name of the {@link Iface} interface.
	 * @return the {@link Iface} with this name. Null if this {@link NetworkEquipment}
	 * does not contain any interface with this name.
	 */
	public Iface getIface(String name) {
		return _ifaces.get(name);
	}

	/**
	 * Returns all the {@link IfaceLink} links associated to this
	 * {@link NetworkEquipment}.
	 * @return an {@link IfaceLinks} list containing the links. The list could be
	 * empty but not null.
	 */
	public IfaceLinks getIfaceLinks() {
		IfaceLinks links = new IfaceLinks();
		/*
		 * each iface
		 */
		for (Iface iface: _ifaces.values()) {
			IfaceLinksByIp ifacelinks = iface.getLinks();
			/*
			 * each link in the iface
			 */
			links.addAll(ifacelinks.values());
		}
		return links;
	}

	/**
	 * Returns the {@link IfaceLink} link with this {@link IPNet} IP address in this
	 * {@link NetworkEquipment}.
	 * @param ip the IP address of the {@link IfaceLink} link.
	 * @return the {@link IfaceLink} with this IP address. Null if this {@link NetworkEquipment}
	 * does not contain any link with this IP address.
	 */
	public IfaceLink getIfaceLink(IPNet ip) {
		for (Iface iface: _ifaces.values()) {
			IfaceLink link = iface.getLink(ip);
			if (link != null)
				return link;
		}
		return null;
	}

	/**
	 * Returns the {@link Iface} directely connected to this {@link IPNet}
	 * IP address in this {@link NetworkEquipment}.
	 * @param ip the IP address directely connected to the {@link Iface} interface.
	 * @return the {@link Iface} directely connected to this IP address.
	 * Null if this {@link NetworkEquipment} does not contain any interface connected
	 * to this IP address.
	 */
	public Iface getIfaceConnectedTo(IPNet ip) {
		for (Iface iface: _ifaces.values()) {
			if (iface.getLinkConnectedTo(ip) != null)
				return iface;
		}
		return null;
	}

	/**
	 * Configures this {@link NetworkEquipment} equipment.<br/>
	 * This method is called by the {@link Monitor} monitor when all the
	 * {@link NetworkEquipment} equipments have been created. This method should
	 * be overrided, by example to read the configuration file of the equipment.
	 */
	public void configure() {}

    /**
	 * This method is called when a {@link Probe} probe is received on this
	 * {@link NetworkEquipment} equipment.<br/> This method should be overrided.
	 * @param link the {@link IfaceLink} link from which the probe was received.
	 * @param probe the {@link Probe} received
	 */
	public void incoming(IfaceLink link, Probe probe) {}

    /**
	 *  Sends a {@link Probe} probe on this {@link NetworkEquipment} equipment.
	 * @param link the {@link IfaceLink} link to use.
	 * @param probe the {@link Probe} to send.
	 * @param nexthop the {@link IPNet} IP address of the next hop.
	 */
	public void outgoing(IfaceLink link, Probe probe, IPNet nexthop) {
		link.getIface().outgoing(link, probe, nexthop);
	}

    /**
	 * Returns a {@link ShowableRoutes} interface. This method must be overrided.
	 * @return a {@link ShowableRoutes} interface.
	 */
	public ShowableRoutes getShowableRoutes() {
		throw new UnsupportedOperationException("Equipments must override this method");
	}

	/**
	 * Interprets and runs a shell command. This method is called by the shell
	 * and could be overrided to implement commands specific to the equipment.
	 * @param command the command to run.
	 * @param output Stream to output.
	 */
	public void runShell(String command, PrintStream output) {
		/*
		 * do nothing by default
		 */
		output.println("Unknown equipment command");
	}

	/**
	 * Returns true if the configuration of this equipment has changed.
	 * This is used by the monitor to auto reload the equipment on change.
	 * <br/> This method could be overrided to signal a change in the
	 * configuration to the monitor.
	 * @return true if the configuration of this equipment has changed.
	 */
	public boolean hasChanged() {
		return false;
	}

	/**
	 * Returns the routes on this equipment that match the nearest network
	 * including this destination range.
	 * @param destination the {@link IPRangeable} IP range of the destination.
	 * @return a {@link Routes} list containing the routes. The list could be
	 * empty but not null.
	 * @throws JtaclRoutingException if problem occurs.
	 */
	public Routes getRoutes(IPRangeable destination) throws JtaclRoutingException {
		return null;
	}
}
