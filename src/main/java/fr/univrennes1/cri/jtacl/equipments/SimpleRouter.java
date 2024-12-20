/*
 * Copyright (c) 2010, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.equipments;

import fr.univrennes1.cri.jtacl.core.exceptions.JtaclConfigurationException;
import fr.univrennes1.cri.jtacl.core.monitor.Log;
import fr.univrennes1.cri.jtacl.core.monitor.Monitor;
import fr.univrennes1.cri.jtacl.core.network.Iface;
import fr.univrennes1.cri.jtacl.core.network.IfaceLink;
import fr.univrennes1.cri.jtacl.core.network.NetworkEquipmentsByName;
import fr.univrennes1.cri.jtacl.core.network.Route;
import fr.univrennes1.cri.jtacl.core.network.Routes;
import fr.univrennes1.cri.jtacl.core.probing.AccessControlList;
import fr.univrennes1.cri.jtacl.core.probing.FwResult;
import fr.univrennes1.cri.jtacl.core.probing.Probe;
import fr.univrennes1.cri.jtacl.core.probing.ProbeResults;
import fr.univrennes1.cri.jtacl.equipments.generic.GenericEquipment;
import fr.univrennes1.cri.jtacl.lib.ip.IPNet;
import fr.univrennes1.cri.jtacl.lib.misc.Direction;
import fr.univrennes1.cri.jtacl.lib.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * A test case for a simple equipment doing routing and firewalling.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class SimpleRouter extends GenericEquipment {

    protected ArrayList<String> _acls = new ArrayList<>();

    protected class SimpleRouterLink {
        IfaceLink _link;
        boolean _border;

        protected SimpleRouterLink(IfaceLink link, boolean border) {
            _link = link;
            _border = border;
        }

        protected IfaceLink getLink() {
            return _link;
        }

        protected boolean isBorder() {
            return _border;
        }
    }

    Map<IfaceLink, SimpleRouterLink> _srlinks =
            new HashMap<>();

    /**
     * Create a new {@link SimpleRouter} with this name and this comment.<br/>
     *
     * @param monitor               the {@link Monitor} monitor associated with this equipment.
     * @param name                  the name of the equipment.
     * @param comment               a free comment for this equipment.
     * @param configurationFileName name of the configuration file to use (may be null).
     */
    public SimpleRouter(Monitor monitor, String name, String comment,
                        String configurationFileName) {
        super(monitor, name, comment, configurationFileName);
    }

    protected void loadAcl(Document doc) {
        NodeList list = doc.getElementsByTagName("acl");
        for (int i = 0; i < list.getLength(); i++) {
            Element e = (Element) list.item(i);
            String sstring = e.getAttribute("string");

            if (sstring.isEmpty())
                throw new JtaclConfigurationException("Missing acl string");
            _acls.add(sstring);
        }
    }

    protected void loadIfaces(Document doc) {

        NodeList list = doc.getElementsByTagName("iface");
        for (int i = 0; i < list.getLength(); i++) {
            Element e = (Element) list.item(i);
            String name = e.getAttribute("name");
            String comment = e.getAttribute("comment");
            String ifIp = e.getAttribute("ip");
            String ifNetwork = e.getAttribute("network");
            String ifBorder = e.getAttribute("border");

            String s = "name: " + name + " comment: " + comment +
                    " IP: " + ifIp + " network: " + ifNetwork +
                    " border: " + ifBorder;

            if (ifBorder.isEmpty())
                ifBorder = "false";

            if (name.isEmpty())
                throw new JtaclConfigurationException("Missing interface name: " + s);

            if (ifIp.isEmpty())
                throw new JtaclConfigurationException("Missing interface IP: " + s);

            IPNet ip;
            try {
                ip = new IPNet(ifIp);
                ip = ip.hostAddress();
            } catch (UnknownHostException ex) {
                throw new JtaclConfigurationException("Invalid interface IP: " + s);
            }

            IPNet network;
            try {
                /*
                 * If network attribute is empty, use the network address of the IP
                 * instead.
                 */
                if (ifNetwork.isEmpty())
                    network = new IPNet(ifIp);
                else
                    network = new IPNet(ifNetwork);
                network = network.networkAddress();
            } catch (UnknownHostException ex) {
                throw new JtaclConfigurationException("Invalid interface network: " + s);
            }

            /*
             * Append the link to an existing Iface or create a new one.
             */
            Iface iface = getIface(name);
            if (iface == null) {
                if (comment.isEmpty())
                    throw new JtaclConfigurationException("Missing interface comment: " + s);
                iface = addIface(name, comment);
            }
            IfaceLink link = iface.addLink(ip, network);

            /*
             * a border link accept all the probe in incomming.
             */
            boolean border = Boolean.parseBoolean(ifBorder);
            SimpleRouterLink srlink = new SimpleRouterLink(link, border);
            _srlinks.put(link, srlink);
        }
    }

    @Override
    public NetworkEquipmentsByName configure() {

        if (_configurationFileName.isEmpty())
            return null;

        famAdd(_configurationFileName);
        Document doc = XMLUtils.getXMLDocument(_configurationFileName);

        loadIfaces(doc);
        // loopback interface
        Iface iface = addLoopbackIface("loopback", "loopback");
        for (IfaceLink ilink : iface.getLinks().values()) {
            SimpleRouterLink slink = new SimpleRouterLink(ilink, false);
            _srlinks.put(ilink, slink);
        }

        routeDirectlyConnectedNetworks();
        loadRoutesFromXML(doc);
        loadAcl(doc);

        return null;
    }

    @Override
    public void incoming(IfaceLink link, Probe probe) {

        if (Log.debug().isLoggable(Level.INFO))
            Log.debug().info("probe" + probe.uidToString() + " incoming on " + _name);

        if (!link.isLoopback()) {
            probe.decTimeToLive();
            if (!probe.isAlive()) {
                probe.killError("TimeToLive expiration");
                return;
            }

            /*
             * Filter in the probe
             */
            packetFilter(link, Direction.IN, probe);
        }

        /*
         * Check if the destination of the probe is on this equipment or if
         * this link is a border.
         */
        IPNet ipdest = probe.getDestinationAddress().toIPNet();
        if (ipdest != null) {
            IfaceLink ilink = getIfaceLink(ipdest);
            SimpleRouterLink srlink = _srlinks.get(link);
            if (srlink.isBorder() && ilink == null)
                ilink = srlink.getLink();

            if (ilink != null) {
                /*
                 * Set the probe's final position and notify the monitor
                 */
                probe.setOutgoingLink(ilink, ipdest);
                probe.destinationReached("destination reached");
                return;
            }
        }
        /*
         * Route the probe.
         */
        Routes routes = _routingEngine.getRoutes(probe);
        if (routes.isEmpty()) {
            probe.killNoRoute("No route to " + probe.getDestinationAddress());
            return;
        }
        probe.routed(probe.getDestinationAddress().toString("i::"));

        if (Log.debug().isLoggable(Level.INFO)) {
            for (Route r : routes) {
                Log.debug().info("route: " + r.toString());
            }
        }

        /*
         * if we have several routes for a destination, we have to probe these
         * routes too because our goal is to know if a probe is able to
         * reach a destination, regardless of the route taken.
         */
        ArrayList<Probe> probes = new ArrayList<>();
        probes.add(probe);

        /*
         * Create copies of the incoming probe to describe the other routes.
         */
        for (int i = 1; i < routes.size(); i++) {
            probes.add(probe.newInstance());
        }

        /*
         * Set the position of the probes.
         */
        for (int i = 0; i < routes.size(); i++) {
            //noinspection unchecked
            Route<IfaceLink> route = routes.get(i);
            probes.get(i).setOutgoingLink(route.getLink(), route.getNextHop());
        }

        /*
         * Filter out the probes
         */
        for (Probe p : probes) {
            packetFilter(p.getOutgoingLink(), Direction.OUT, p);
        }

        /*
         * Send the probes over the network.
         */
        for (Probe p : probes) {
            /*
             * Do not send the probe if the outgoing link is the same as the input.
             */
            if (!p.getOutgoingLink().equals(link))
                outgoing(p.getOutgoingLink(), p, p.getNextHop());
            else
                probe.killLoop("same incoming and outgoing link");
        }
    }

    protected void packetFilter(IfaceLink link, Direction direction, Probe probe) {
        /*
         *  Example with a "last rule match" firewall.
         * 	the acls language of this filter is very simple :
         *
         *  <accept|deny> <on|out|in> iface from IP to IP.
         */

        boolean input = direction == Direction.IN;
        ProbeResults result = probe.getResults();
        AccessControlList lastAcl = null;

        for (String _acl : _acls) {
            try {
                String acl = _acl;
                String[] trules = acl.split(" ");
                if (trules.length != 7) {
                    throw new JtaclConfigurationException("invalid acl + " + acl);
                }
                String raction = trules[0];
                String rdirection = trules[1];
                String riface = trules[2];
                String rfromIP = trules[4];
                String rtoIP = trules[6];
                /*
                 * Compare iface * == any
                 */
                if (!riface.equals("*")) {
                    if (!riface.equals(link.getIfaceName())) {
                        continue;
                    }
                }

                /*
                 * Check the direction
                 */
                if (rdirection.equals("in") && !input)
                    continue;
                if (rdirection.equals("out") && input)
                    continue;

                /*
                 * Compare from and to IP addresses
                 */
                IPNet fromIP = new IPNet(rfromIP);
                IPNet toIP = new IPNet(rtoIP);
                if (!fromIP.contains(probe.getSourceAddress())) {
                    continue;
                }
                if (!toIP.contains(probe.getDestinationAddress())) {
                    continue;
                }
                FwResult aclResult = new FwResult(raction.equals("accept") ?
                        FwResult.ACCEPT : FwResult.DENY);

                result.addMatchingAcl(input ? Direction.IN : Direction.OUT, acl, aclResult);
                /*
                 * Keep the last acl that matches the probe because this firewall uses
                 * a "last rule winner" logic.
                 */
                lastAcl = new AccessControlList(acl, aclResult);
            } catch (UnknownHostException ex) {
                throw new JtaclConfigurationException(ex.getMessage());
            }
        }
        if (lastAcl == null) {
            // default policy is accept
            result.setAclResult(input ? Direction.IN : Direction.OUT,
                    new FwResult(FwResult.ACCEPT));
            return;
        }
        /*
         * Put the acl as the active acl in the result
         */
        result.addActiveAcl(input ? Direction.IN : Direction.OUT,
                lastAcl.getAclString(), lastAcl.getResult());
        result.setAclResult(input ? Direction.IN : Direction.OUT, lastAcl.getResult());
    }

}
