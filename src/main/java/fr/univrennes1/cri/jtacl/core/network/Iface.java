/*
 * Copyright (c) 2010, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.core.network;

import fr.univrennes1.cri.jtacl.core.exceptions.JtaclTopologyException;
import fr.univrennes1.cri.jtacl.core.monitor.Monitor;
import fr.univrennes1.cri.jtacl.core.probing.Probe;
import fr.univrennes1.cri.jtacl.lib.ip.IPNet;
import fr.univrennes1.cri.jtacl.lib.ip.IPNetConst;

/**
 * Describes a network interface.<br/><br/>
 * An interface is part of a {@link NetworkEquipment} network equipment,
 * has some {@link IfaceLink} links to the network and a name.<br/>
 * Probes {@link Probe} are received from the network via the incoming() method
 * and sent via the outgoing() method.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class Iface {

    /**
     * The equipment owner of this iface.
     */
    protected NetworkEquipment _equipment;

    /**
     * Name of this iface.
     */
    protected String _name;

    /**
     * A free comment for this iface.
     */
    protected String _comment;

    /**
     * true if loopback interface
     */
    protected boolean _loopback;

    /**
     * links attached to this iface.
     */
    protected IfaceLinksByIp _links;

    /**
     * a reference to the monitor.
     */
    protected Monitor _monitor;

    private Iface(Monitor monitor, String name, String comment, boolean loopback, NetworkEquipment equipment) {
        _monitor = monitor;
        _name = name;
        _comment = comment;
        _loopback = loopback;
        _equipment = equipment;
        _links = new IfaceLinksByIp();
    }

    /**
     * Create a new {@link Iface} interface with these name and comment, attached
     * to a {@link NetworkEquipment} equipement.
     *
     * @param monitor   the {@link Monitor} monitor associated with this interface.
     * @param name      the name of this interface.
     * @param comment   a free comment for this interface.
     * @param equipment the {@link NetworkEquipment} associated with this interface.
     */
    public static Iface newIface(Monitor monitor, String name, String comment, NetworkEquipment equipment) {
        return new Iface(monitor, name, comment, false, equipment);
    }

    /**
     * Create a new kind of loopback {@link Iface} interface with these name and comment, attached
     * to a {@link NetworkEquipment} equipement.
     *
     * @param monitor   the {@link Monitor} monitor associated with this interface.
     * @param name      the name of this interface.
     * @param comment   a free comment for this interface.
     * @param equipment the {@link NetworkEquipment} associated with this interface.
     */
    public static Iface newLoopback(Monitor monitor, String name, String comment, NetworkEquipment equipment) {
        Iface iface = new Iface(monitor, name, comment, true, equipment);
        // links with loopback addresses
        iface.addLink(IPNetConst.ipLoopbackV4, IPNetConst.netLoopbackV4);
        iface.addLink(IPNetConst.ipLoopbackV6, IPNetConst.netLoopbackV6);
        return iface;
    }

    /**
     * Create a new {@link IfaceLink} link and add it to this {@link Iface} interface.
     *
     * @param ip      The {@link IPNet} IP address of the link.
     * @param network The {@link IPNet} IP address of the link's network.
     * @return the new {@link IfaceLink} link added.
     * @throws JtaclTopologyException if this IP address is already linked to
     *                                this interface.
     */
    public IfaceLink addLink(IPNet ip, IPNet network) {
        IfaceLink link = new IfaceLink(_monitor, this, ip, network);
        return addLink(link);
    }

    /**
     * Add a {@link IfaceLink} link to this {@link Iface} interface.
     *
     * @param link the {@link IfaceLink} link to add.
     * @return the new {@link IfaceLink} link added.
     * @throws JtaclTopologyException if this IP address is already linked to
     *                                this interface.
     */
    public IfaceLink addLink(IfaceLink link) {
        if (hasLink(link.getIp()))
            throw new JtaclTopologyException("IP already presents: " +
                    link.getIp().toString());

        _links.put(link);
        return link;
    }

    /**
     * Get the {@link IfaceLink} link with this {@link IPNet} IP address
     * on this {@link Iface} interface.
     *
     * @param ip the {@link IPNet} IP address of the link
     * @return the {@link IfaceLink} link with this IP address.
     * Null if no link was found.
     */
    public IfaceLink getLink(IPNet ip) {
        return _links.get(ip);
    }

    /**
     * Gets the {@link IfaceLink} link directely connected to this {@link IPNet} IP address
     * on this {@link Iface} interface.
     *
     * @param ip the {@link IPNet} IP address directely connected.
     * @return the {@link IfaceLink} link with the IP address directely connected.
     * Null if no link was found.
     */
    public IfaceLink getLinkConnectedTo(IPNet ip) {
        for (Integer i : _links.keySet()) {
            IfaceLink ilink = _links.get(i);
            if (ilink.getNetwork().contains(ip))
                return ilink;
        }
        return null;
    }

    /**
     * Check if this {@link Iface} has a {@link IfaceLink} with the
     * {@link IPNet} IP address.
     *
     * @param ip the {@link IPNet} IP address to check.
     * @return true if this {@link Iface} has a {@link IfaceLink} with
     * the {@link IPNet} IP address.
     */
    public boolean hasLink(IPNet ip) {
        return _links.get(ip) != null;
    }

    /**
     * Returns the {@link IfaceLink} links of this {@link Iface}.
     *
     * @return a {@link IfaceLinks} list containing the {@link IfaceLink} links.
     * The returned map could be empty but not null.
     */
    public IfaceLinksByIp getLinks() {
        return _links;
    }

    /**
     * Return the {@link NetworkEquipment} associated with this {@link Iface}
     * interface.
     *
     * @return the {@link NetworkEquipment} associated with this {@link Iface}.
     */
    public NetworkEquipment getEquipment() {
        return _equipment;
    }

    /**
     * Returns the name of this {@link Iface} interface.
     *
     * @return the name of this {@link Iface} interface.
     */
    public String getName() {
        return _name;
    }

    /**
     * Returns the free comment of this {@link Iface} interface.
     *
     * @return the free comment of this {@link Iface} interface.
     */
    public String getComment() {
        return _comment;
    }

    /**
     * Returns true if this {@link Iface} interface is a loopback interface.
     *
     * @return true if this {@link Iface} interface is a loopback interface.
     */
    public boolean isLoopback() {
        return _loopback;
    }

    /**
     * This method is called when a {@link Probe} probe is received on this
     * {@link Iface} iface.
     *
     * @param link  the {@link IfaceLink} link from which the probe was received.
     * @param probe the {@link Probe} received
     */
    public void incoming(IfaceLink link, Probe probe) {
        _equipment.incoming(link, probe);
    }

    /**
     * This method is called when a {@link Probe} probe is sent on this
     * {@link Iface} iface.
     *
     * @param link    the {@link IfaceLink} link to use.
     * @param probe   the {@link Probe} to send.
     * @param nextHop the {@link IPNet} IP address of the next hop.
     */
    public void outgoing(IfaceLink link, Probe probe, IPNet nextHop) {
        link.outgoing(probe, nextHop);
    }
}
