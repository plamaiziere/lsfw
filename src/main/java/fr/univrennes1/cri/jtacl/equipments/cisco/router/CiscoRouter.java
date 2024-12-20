/*
 * Copyright (c) 2010, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.equipments.cisco.router;

import fr.univrennes1.cri.jtacl.analysis.CrossRefContext;
import fr.univrennes1.cri.jtacl.analysis.IPCrossRef;
import fr.univrennes1.cri.jtacl.analysis.IPCrossRefMap;
import fr.univrennes1.cri.jtacl.analysis.ServiceCrossRef;
import fr.univrennes1.cri.jtacl.analysis.ServiceCrossRefContext;
import fr.univrennes1.cri.jtacl.analysis.ServiceCrossRefMap;
import fr.univrennes1.cri.jtacl.analysis.ServiceCrossRefType;
import fr.univrennes1.cri.jtacl.core.exceptions.JtaclConfigurationException;
import fr.univrennes1.cri.jtacl.core.exceptions.JtaclInternalException;
import fr.univrennes1.cri.jtacl.core.monitor.Log;
import fr.univrennes1.cri.jtacl.core.monitor.Monitor;
import fr.univrennes1.cri.jtacl.core.network.Iface;
import fr.univrennes1.cri.jtacl.core.network.IfaceLink;
import fr.univrennes1.cri.jtacl.core.network.NetworkEquipmentsByName;
import fr.univrennes1.cri.jtacl.core.network.Route;
import fr.univrennes1.cri.jtacl.core.network.Routes;
import fr.univrennes1.cri.jtacl.core.probing.FwResult;
import fr.univrennes1.cri.jtacl.core.probing.MatchResult;
import fr.univrennes1.cri.jtacl.core.probing.Probe;
import fr.univrennes1.cri.jtacl.core.probing.ProbeRequest;
import fr.univrennes1.cri.jtacl.core.probing.ProbeResults;
import fr.univrennes1.cri.jtacl.core.probing.ProbeTcpFlags;
import fr.univrennes1.cri.jtacl.equipments.generic.GenericEquipment;
import fr.univrennes1.cri.jtacl.lib.ip.IPIcmpEnt;
import fr.univrennes1.cri.jtacl.lib.ip.IPNet;
import fr.univrennes1.cri.jtacl.lib.ip.IPRangeable;
import fr.univrennes1.cri.jtacl.lib.ip.IPversion;
import fr.univrennes1.cri.jtacl.lib.ip.PortRange;
import fr.univrennes1.cri.jtacl.lib.ip.PortSpec;
import fr.univrennes1.cri.jtacl.lib.ip.Protocols;
import fr.univrennes1.cri.jtacl.lib.ip.ProtocolsSpec;
import fr.univrennes1.cri.jtacl.lib.misc.Direction;
import fr.univrennes1.cri.jtacl.lib.misc.ParseContext;
import fr.univrennes1.cri.jtacl.lib.misc.StringTools;
import fr.univrennes1.cri.jtacl.lib.misc.StringsList;
import fr.univrennes1.cri.jtacl.lib.xml.XMLUtils;
import org.parboiled.Parboiled;
import org.parboiled.parserunners.BasicParseRunner;
import org.parboiled.support.ParsingResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

/**
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class CiscoRouter extends GenericEquipment {

    protected class CiscoIface {
        protected Iface _iface;
        protected String _name;
        protected String _description;
        protected boolean _shutdown;
        protected ArrayList<AccessGroup> _accessGroup4In
                = new ArrayList<>();
        protected ArrayList<AccessGroup> _accessGroup4Out
                = new ArrayList<>();
        protected ArrayList<AccessGroup> _accessGroup6In
                = new ArrayList<>();
        protected ArrayList<AccessGroup> _accessGroup6Out
                = new ArrayList<>();

        protected ArrayList<IPNet> _ipAddresses = new ArrayList<>();

        public ArrayList<AccessGroup> getAccessGroup4In() {
            return _accessGroup4In;
        }

        public ArrayList<AccessGroup> getAccessGroup4Out() {
            return _accessGroup4Out;
        }

        public ArrayList<AccessGroup> getAccessGroup6In() {
            return _accessGroup6In;
        }

        public ArrayList<AccessGroup> getAccessGroup6Out() {
            return _accessGroup6Out;
        }

        public Iface getIface() {
            return _iface;
        }

        public void setIface(Iface iface) {
            _iface = iface;
        }

        public String getDescription() {
            return _description;
        }

        public void setDescription(String description) {
            _description = description;
        }

        public String getName() {
            return _name;
        }

        public void setName(String name) {
            _name = name;
        }

        public boolean isShutdown() {
            return _shutdown;
        }

        public void setShutdown(boolean shutdown) {
            _shutdown = shutdown;
        }

        public ArrayList<IPNet> getIpAddresses() {
            return _ipAddresses;
        }
    }

    /**
     * Configuration file, mapped into strings.
     */
    protected class ConfigurationFile extends StringsList {

        protected String _fileName;

        public String getFileName() {
            return _fileName;
        }

        @Override
        public void readFromFile(String fileName)
                throws FileNotFoundException, IOException {
            super.readFromFile(fileName);
            _fileName = fileName;
        }
    }

    /**
     * Parser
     */
    protected IOSParser _parser = Parboiled.createParser(IOSParser.class);

    /**
     * ParseRunner for Parse()
     */
    protected BasicParseRunner _parseRunParse =
            new BasicParseRunner(_parser.Parse());

    /**
     * ParseRunner for InAclContext()
     */
    protected BasicParseRunner _parseRunInAclContext =
            new BasicParseRunner(_parser.InAclContext());

    /**
     * ParseRunner for ExitInterface()
     */
    protected BasicParseRunner _parseRunExitInterface =
            new BasicParseRunner(_parser.ExitInterface());

    /**
     * ParseRunner for InInterface()
     */
    protected BasicParseRunner _parseRunInInterface =
            new BasicParseRunner(_parser.InInterface());

    /**
     * ParseRunner for Interface()
     */
    protected BasicParseRunner _parseRunInterface =
            new BasicParseRunner(_parser.Interface());

    /**
     * the list of configuration files, mapped into strings.
     */
    protected ArrayList<ConfigurationFile> _configurations;

    /**
     * interfaces
     */
    protected HashMap<String, CiscoIface> _ciscoIfaces = new HashMap<>();

    /**
     * IP cross references map
     */
    protected IPCrossRefMap _netCrossRef = new IPCrossRefMap();

    /**
     * Services cross references map
     */
    protected ServiceCrossRefMap _serviceCrossRef = new ServiceCrossRefMap();

    /**
     * parse context
     */
    protected ParseContext _parseContext = new ParseContext();

    /**
     * Access lists
     */
    protected ArrayList<AccessList> _accessLists = new ArrayList<>();

    /**
     * IP cross references map
     */
    IPCrossRefMap getNetCrossRef() {
        return _netCrossRef;
    }

    /**
     * Services cross references map
     */
    ServiceCrossRefMap getServiceCrossRef() {
        return _serviceCrossRef;
    }

    protected IOSShell _shell = new IOSShell(this);

    /**
     * Create a new {@link CiscoRouter} with this name and this comment.<br/>
     *
     * @param monitor               the {@link Monitor} monitor associated with this equipment.
     * @param name                  the name of the equipment.
     * @param comment               a free comment for this equipment.
     * @param configurationFileName name of the configuration file to use (may be null).
     */
    public CiscoRouter(Monitor monitor, String name, String comment, String configurationFileName) {
        super(monitor, name, comment, configurationFileName);
    }

    protected void loadConfiguration(Document doc) {
        _configurations = new ArrayList<>();

        NodeList list = doc.getElementsByTagName("file");
        for (int i = 0; i < list.getLength(); i++) {
            Element e = (Element) list.item(i);
            String filename = e.getAttribute("filename");
            if (filename.isEmpty())
                throw new JtaclConfigurationException("Missing file name");
            ConfigurationFile cfg = new ConfigurationFile();
            try {
                cfg.readFromFile(filename);
            } catch (FileNotFoundException ex) {
                throw new JtaclConfigurationException("File not found:" + filename);
            } catch (IOException ex) {
                throw new JtaclConfigurationException("Cannot read file :" + filename);
            }
            famAdd(filename);
            _configurations.add(cfg);
        }
    }

    protected void throwCfgException(String msg) {
        throw new JtaclConfigurationException(_parseContext.toString() + msg);
    }

    protected IPNet parseIp(String ip) {

        IPNet ipnet = null;
        try {
            ipnet = new IPNet(ip);
        } catch (UnknownHostException ex) {
            throwCfgException("invalid IP address: " + ex.getMessage());
        }
        return ipnet;
    }

    protected List<IPNet> parseRouterIp(String sIP, String sNetmask) {
        List<IPNet> iplist = new ArrayList<>();

        if (sIP.equals("any"))
            return iplist;

        if (sNetmask != null) {
            // negate the mask
            IPNet netmask = parseIp(sNetmask);
            long mask = netmask.getIP().longValue();
            mask = ~mask;
            mask = mask & 0xFFFFFFFFL;
            try {
                netmask = new IPNet(BigInteger.valueOf(mask), IPversion.IPV4);
                sNetmask = netmask.toString("i");
            } catch (UnknownHostException ex) {
                throwCfgException("invalid netmask: " + sNetmask +
                        " " + ex.getMessage());
            }
            /*
             * warn if "ip/netmask" is not a network.
             */
            IPNet ip;
            String sIpNetmask = sIP + "/" + sNetmask;
            boolean notANetwork = false;
            try {
                ip = new IPNet(sIpNetmask);
            } catch (UnknownHostException ex) {
                notANetwork = true;
                Log.config().warning(_parseContext.toString() +
                        "netmask is not a network prefix: " + sIpNetmask +
                        " " + ex.getMessage());
            }
            if (notANetwork) {
                ip = parseIp(sIP);
                iplist.add(ip);
                iplist.add(netmask);
            } else {
                ip = parseIp(sIpNetmask);
                iplist.add(ip);
            }
            return iplist;
        }

        iplist.add(parseIp(sIP));
        return iplist;
    }


    protected int parseService(String service, String protocol) {

        int port = _ipServices.serviceLookup(service, protocol);
        if (port == -1)
            throwCfgException("unknown service");
        return port;
    }

    protected int parseProtocol(String protocol) {
        int proto = _ipProtocols.protocolLookup(protocol);
        if (proto == -1)
            throwCfgException("unknown protocol");
        return proto;
    }

    protected IPIcmpEnt parseIcmp4(String icmpName) {
        IPIcmpEnt icmp = _ipIcmp4Types.icmpLookup(icmpName);
        if (icmp == null)
            throwCfgException("unknown icmp-type or message");
        return icmp;
    }

    protected IPIcmpEnt parseIcmp6(String icmpName) {
        IPIcmpEnt icmp = _ipIcmp6Types.icmpLookup(icmpName);
        if (icmp == null)
            throwCfgException("unknown icmp-type or message");
        return icmp;
    }

    protected void parseInterfaces(ConfigurationFile cfg) {
        boolean inInterface = false;
        ParsingResult<?> result;

        CiscoIface curCsIface = null;

        for (int i = 0; i < cfg.size(); i++) {
            String line = cfg.get(i);
            line = StringTools.stripWhiteSpaces(line);
            _parseContext.set(cfg.getFileName(), i + 1, line);
            String lineCfg = _parser.stripComment(line).trim();
            lineCfg = filter(lineCfg);

            if (Log.debug().isLoggable(Level.INFO))
                Log.debug().info("#" + _parseContext.getLineNumber() +
                        ": " + line);

            if (inInterface) {
                result = _parseRunExitInterface.run(lineCfg);
                if (result.matched) {
                    inInterface = false;
                    curCsIface = null;
                } else {
                    result = _parseRunInInterface.run(lineCfg);
                    if (result.matched) {
                        dumpConfiguration(line);
                        if (hasOptParseOnly())
                            continue;
                        /*
                         * Interface description
                         */
                        String rule = _parser.getRuleName();
                        if (rule.equals("description")) {
                            String description = _parser.getDescription();
                            curCsIface.setDescription(description);
                        }
                        /*
                         * ip address
                         */
                        if (rule.equals("ip address")) {
                            String ip = _parser.getIpAddress();
                            String ipNetmask = _parser.getIpNetmask();
                            IPNet ipnet = parseIp(ip + "/" + ipNetmask);
                            curCsIface.getIpAddresses().add(ipnet);
                        }
                        /*
                         * ipv6 address
                         */
                        if (rule.equals("ipv6 address")) {
                            String ipv6Address = _parser.getIpAddress();
                            IPNet ipnet = parseIp(ipv6Address);
                            curCsIface.getIpAddresses().add(ipnet);
                        }

                        /*
                         * ip access-group
                         */
                        if (rule.equals("ip access-group")) {
                            String name = _parser.getName();
                            String sdirection = _parser.getDirection();
                            Direction direction;
                            if (sdirection.equals("in"))
                                direction = Direction.IN;
                            else
                                direction = Direction.OUT;
                            AccessGroup group = new AccessGroup(name, direction);
                            switch (direction) {
                                case IN:
                                    curCsIface.getAccessGroup4In().add(group);
                                    break;
                                case OUT:
                                    curCsIface.getAccessGroup4Out().add(group);
                                    break;
                            }
                        }

                        /*
                         * ipv6 traffic-filter
                         */
                        if (rule.equals("ipv6 traffic-filter")) {
                            String name = _parser.getName();
                            String sdirection = _parser.getDirection();
                            Direction direction;
                            if (sdirection.equals("in"))
                                direction = Direction.IN;
                            else
                                direction = Direction.OUT;
                            AccessGroup group = new AccessGroup(name, direction);
                            switch (direction) {
                                case IN:
                                    curCsIface.getAccessGroup6In().add(group);
                                    break;
                                case OUT:
                                    curCsIface.getAccessGroup6Out().add(group);
                                    break;
                            }
                        }

                        /*
                         * shutdown
                         */
                        if (rule.equals("shutdown"))
                            curCsIface.setShutdown(true);

                    } else {
                        /*
                         * check if we should match a rule
                         */
                        if (_parser.shouldMatchInInterface(lineCfg)) {
                            Log.config().warning(_parseContext.toString() +
                                    "does not match any rule (but should).");
                        }
                    }
                }
            }
            if (lineCfg.startsWith("interface")) {
                result = _parseRunInterface.run(lineCfg);
                if (result.matched) {
                    inInterface = true;
                    dumpConfiguration(line);
                    if (hasOptParseOnly())
                        continue;
                    String interfaceName = _parser.getName();
                    // strip any space in the name.
                    interfaceName = interfaceName.replace(" ", "");
                    curCsIface = new CiscoIface();
                    curCsIface.setName(interfaceName);
                    _ciscoIfaces.put(curCsIface.getName(), curCsIface);

                }
            }
        }
    }

    protected void addInterface(CiscoIface csIface) {
        String description = "";
        if (csIface.getDescription() != null)
            description = csIface.getDescription();

        Iface iface = addIface(csIface.getName(), description);

        for (IPNet ip : csIface.getIpAddresses()) {
            IPNet hostIP = ip.hostAddress();
            IPNet networkIP = ip.networkAddress();
            iface.addLink(hostIP, networkIP);
        }
    }

    /*
     * ip route and ipv6 route
     */
    private void ruleRoute(String rule, IOSParser parser) {

        String routeIPAddress = parser.getIpAddress();
        String routeIPNetmask = parser.getIpNetmask();
        String routeNexthop = parser.getNexthop();

        try {
            IPNet prefix = null;
            // IPv4
            if (rule.equals("ip route"))
                prefix = new IPNet(routeIPAddress + "/" + routeIPNetmask);
            // IPv6
            if (rule.equals("ipv6 route"))
                prefix = new IPNet(routeIPAddress);

            Route<IfaceLink> route;

            /*
             * null route
             */
            if (routeNexthop == null ||
                    routeNexthop.equalsIgnoreCase("null0")) {
                route = new Route<>(prefix);
            } else {
                /*
                 * Retrieve the link associated to the nexthop (may be null)
                 */
                IPNet nexthop = new IPNet(routeNexthop);
                Iface iface = getIfaceConnectedTo(nexthop);
                IfaceLink link = null;
                if (iface != null)
                    link = iface.getLinkConnectedTo(nexthop);
                route = new Route<>(prefix, nexthop, 1, link);
            }
            /*
             * Add the route.
             */
            Log.debug().info(_name + " add route: " + route.toString());
            _routingEngine.addRoute(route);

        } catch (UnknownHostException ex) {
            throwCfgException("Invalid route: " + ex.getMessage());
        }

    }

    /*
     * last acl, used while parsing
     */
    AccessList _lastAcl;

    protected AccessList ruleIpAccessList(AclTemplate tpl) {

        AccessList acl = new AccessList();
        acl.setAclType(tpl.getAclType());
        acl.setIpVersion(tpl.getIpVersion());
        acl.setName(tpl.getName());
        acl.setNumber(tpl.getNumber());
        if (tpl.getNumber() != null)
            acl.setName(tpl.getNumber().toString());

        _accessLists.add(acl);
        _lastAcl = acl;
        return acl;
    }

    protected void ruleAce(AccessList acl, AceTemplate tpl) {

        AccessListElement ace = new AccessListElement();

        if (tpl.getInactive())
            return;

        /*
         * access-list element
         */
        String fileName = _parseContext.getFileName();
        int i = fileName.lastIndexOf(File.separator);
        fileName = (i > -1) ? fileName.substring(i + 1) : fileName;

        ace.setConfigurationLine(fileName + " #" +
                _parseContext.getLineNumber() + ": [" +
                acl.getName() + "] " + _parseContext.getLine().trim());
        ace.setParseContext(_parseContext);

        /*
         * action (permit, deny)
         */
        ace.setAction(tpl.getAction());

        /*
         * protocol
         */
        String sprotocol = tpl.getProtocol();
        if (sprotocol != null) {
            int protocol = parseProtocol(sprotocol);
            ace.setProtocol(protocol);
        }

        /*
         * source ip / netmask
         */
        String sIP = tpl.getSrcIp();
        String sNetmask = tpl.getSrcIpMask();
        if (sIP != null) {
            /*
             * if 'any', ip address and network group are null.
             */
            if (!sIP.equals("any")) {
                List<IPNet> iplist = parseRouterIp(sIP, sNetmask);
                if (iplist.size() == 2) {
                    // not a network
                    ace.setSourceNetmask(iplist.get(1));
                }
                ace.setSourceIp(iplist.get(0));
            }
        }

        /*
         * source service
         */
        String sOperator = tpl.getSrcPortOperator();
        if (sOperator != null) {
            int firstPort = parseService(tpl.getSrcFirstPort(), null);
            int lastPort;
            PortObject pobj;
            if (sOperator.equals("range")) {
                lastPort = parseService(tpl.getSrcLastPort(), null);
                pobj = new PortObject("range", firstPort, lastPort);
            } else
                pobj = new PortObject(sOperator, firstPort);
            ace.setSourcePortObject(pobj);
        }

        /*
         * destination ip / netmask
         */
        sIP = tpl.getDstIp();
        sNetmask = tpl.getDstIpMask();
        if (sIP != null) {
            /*
             * if 'any', ip address and network group are null.
             */
            if (!sIP.equals("any")) {
                List<IPNet> iplist = parseRouterIp(sIP, sNetmask);
                if (iplist.size() == 2) {
                    // not a network
                    ace.setDestNetmask(iplist.get(1));
                }
                ace.setDestIp(iplist.get(0));
            }
        }

        /*
         * destination service
         */
        sOperator = tpl.getDstPortOperator();
        if (sOperator != null) {
            int firstPort = parseService(tpl.getDstFirstPort(), null);
            int lastPort;
            PortObject pobj;
            if (sOperator.equals("range")) {
                lastPort = parseService(tpl.getDstLastPort(), null);
                pobj = new PortObject("range", firstPort, lastPort);
            } else
                pobj = new PortObject(sOperator, firstPort);
            ace.setDestPortObject(pobj);
        }


        /*
         * Icmp-type.
         */
        String sIcmp = tpl.getSubType();
        Integer icmpCode = tpl.getCode();
        if (sprotocol != null && sprotocol.equals("icmp") && sIcmp != null) {
            IPIcmpEnt icmpEnt = parseIcmp4(sIcmp);
            ace.setSubType(icmpEnt.getIcmp());
            ace.setCode(icmpEnt.getCode());
        }

        /*
         * tcp flags.
         */
        if (tpl.getTcpKeyword() != null) {
            ace.setTcpKeyword(tpl.getTcpKeyword());
            ace.getTcpFlags().addAll(tpl.getTcpFlags());
        }

        /*
         * Sanity checks.
         */

        if (acl.getAclType() == AclType.IPEXT &&
                ace.getProtocol() == null)
            throwCfgException("undefined protocol");

        /*
         * add the ACE to the access list
         */
        acl.add(ace);

    }

    protected void ruleAccessList(AclTemplate aclTpl, AceTemplate aceTpl) {

        /*
         * Acl already defined?
         */
        AccessList acl = null;
        for (AccessList iacl : _accessLists) {
            String name;
            Integer number = aclTpl.getNumber();
            if (number != null)
                name = number.toString();
            else
                name = aclTpl.getName();
            if (iacl.getName().equals(name)) {
                acl = iacl;
                break;
            }
        }
        /*
         * Create a new acl if needed
         */
        if (acl == null)
            acl = ruleIpAccessList(aclTpl);

        /*
         * Create the new ace
         */
        ruleAce(acl, aceTpl);
    }


    protected void parse(ConfigurationFile cfg) {
        ParsingResult<?> result;

        for (int i = 0; i < cfg.size(); i++) {

            String line = cfg.get(i);
            line = StringTools.stripWhiteSpaces(line);
            String lineCfg = _parser.stripComment(line).trim();
            lineCfg = filter(lineCfg);

            _parseContext = new ParseContext();
            _parseContext.set(cfg.getFileName(), i + 1, line);

            if (Log.debug().isLoggable(Level.INFO))
                Log.debug().info("#" + _parseContext.getLineNumber() +
                        ": " + lineCfg);

            /*
             * check if we are still in an access-list context
             */
            if (_lastAcl != null && !lineCfg.isEmpty()) {
                result = _parseRunInAclContext.run(lineCfg);
                if (!result.matched)
                    _lastAcl = null;
            }

            /*
             * parse the line
             */
            _parser.setAclContext(_lastAcl);

            result = _parseRunParse.run(lineCfg);
            if (result.matched) {
                String rule = _parser.getRuleName();
                dumpConfiguration(line);

                if (hasOptParseOnly())
                    continue;

                /*
                 * route command
                 */
                if (rule.equals("ip route") || rule.equals("ipv6 route"))
                    ruleRoute(rule, _parser);

                /*
                 * ip access list named
                 */
                if (rule.equals("ip access-list named") ||
                        rule.equals("ipv6 access-list named"))
                    ruleIpAccessList(_parser.getAclTemplate());

                /*
                 * access-list (number)
                 */
                if (rule.equals("access-list"))
                    ruleAccessList(_parser.getAclTemplate(),
                            _parser.getAceTemplate());

                /*
                 * ACE standard / extended
                 */
                if (rule.equals("ace standard") ||
                        rule.equals("ace extended")) {
                    if (_lastAcl != null)
                        ruleAce(_lastAcl, _parser.getAceTemplate());
                }

            } else {
                /*
                 * check if we should match a rule
                 */
                if (_parser.shouldMatchInMain(lineCfg)) {
                    Log.config().warning(_parseContext.toString() +
                            "does not match any rule (but should).");
                }
            }
        }
    }

    @Override
    public NetworkEquipmentsByName configure() {
        if (_configurationFileName.isEmpty())
            return null;

        /*
         * Read the XML configuration file
         */
        famAdd(_configurationFileName);
        Document doc = XMLUtils.getXMLDocument(_configurationFileName);
        loadOptionsFromXML(doc);
        loadFiltersFromXML(doc);
        loadConfiguration(doc);

        /*
         * parse and add interfaces
         */
        for (ConfigurationFile cfg : _configurations) {
            parseInterfaces(cfg);
        }

        for (String n : _ciscoIfaces.keySet()) {
            CiscoIface csIface = _ciscoIfaces.get(n);
            if (!csIface.isShutdown())
                addInterface(csIface);
        }

        // loopback interface
        Iface iface = addLoopbackIface("loopback", "loopback");
        CiscoIface csIface = new CiscoIface();
        csIface.setIface(iface);
        csIface.setName(iface.getName());
        csIface.setDescription(iface.getComment());
        csIface.setShutdown(false);
        for (IfaceLink ilink : iface.getLinks().values()) {
            csIface.getIpAddresses().add(ilink.getIp());
        }
        _ciscoIfaces.put(csIface.getName(), csIface);

        /*
         * parse ACL
         */
        for (ConfigurationFile cfg : _configurations) {
            parse(cfg);
        }

        /*
         * add an implicit deny ACE in each ACL.
         */
        for (AccessList acl : _accessLists) {
            AccessListElement ace = new AccessListElement();
            ace.setAction("deny");
            ace.setConfigurationLine("[" + acl.getName() + "]" +
                    " *** implicit deny ***");
            ace.setImplicit(true);
            acl.add(ace);
        }

        /*
         * routing
         */
        routeDirectlyConnectedNetworks();
        loadRoutesFromXML(doc);
        /*
         * compute cross reference
         */
        if (_monitorOptions.getXref())
            CrossReferences();

        return null;
    }

    protected IPCrossRef getIPNetCrossRef(IPNet ipnet) {
        if (!_monitorOptions.getXref())
            throw new JtaclInternalException(
                    "Cross reference computing without crossreference option set");
        IPCrossRef ref = _netCrossRef.get(ipnet);
        if (ref == null) {
            ref = new IPCrossRef(ipnet);
            _netCrossRef.put(ref);
        }
        return ref;
    }

    protected ServiceCrossRef getServiceCrossRef(PortRange portrange) {
        if (!_monitorOptions.getXref())
            throw new JtaclInternalException(
                    "Cross reference computing without crossreference option set");
        ServiceCrossRef ref = _serviceCrossRef.get(portrange);
        if (ref == null) {
            ref = new ServiceCrossRef(portrange);
            _serviceCrossRef.put(ref);
        }
        return ref;
    }

    protected void crossRefPortObject(PortObject portObject,
                                      ServiceCrossRefContext refContext) {

        for (PortRange range : portObject.getPortSpec().getRanges()) {
            ServiceCrossRef refService = getServiceCrossRef(range);
            refService.addContext(refContext);
        }
    }

    /*
     * Cross reference for an access list element
     */
    protected void crossRefAccessList(AccessListElement ace) {
        ParseContext context = ace.getParseContext();
        CrossRefContext refContext = new CrossRefContext(context.getLine(),
                "ace",
                ace.getAction(),
                context.getFileName(),
                context.getLineNumber());

        IPNet ip = ace.getSourceIp();
        if (ip != null) {
            IPCrossRef ipNetRef = getIPNetCrossRef(ip);
            ipNetRef.addContext(refContext);
        }

        ip = ace.getDestIp();
        if (ip != null) {
            IPCrossRef ipNetRef = getIPNetCrossRef(ip);
            ipNetRef.addContext(refContext);
        }


        /*
         * services cross references
         */
        ProtocolsSpec protoSpec = new ProtocolsSpec();
        if (ace.getProtocol() != null) {
            protoSpec.add(ace.getProtocol());
        }

        if (ace.getSourcePortObject() != null) {
            ServiceCrossRefContext serviceContext =
                    new ServiceCrossRefContext(protoSpec,
                            ServiceCrossRefType.FROM,
                            context.getLine(), "ace", ace.getAction(),
                            context.getFileName(), context.getLineNumber());
            crossRefPortObject(ace.getSourcePortObject(), serviceContext);
        }

        if (ace.getDestPortObject() != null) {
            ServiceCrossRefContext serviceContext =
                    new ServiceCrossRefContext(protoSpec,
                            ServiceCrossRefType.TO,
                            context.getLine(), "ace", ace.getAction(),
                            context.getFileName(), context.getLineNumber());
            crossRefPortObject(ace.getDestPortObject(), serviceContext);
        }
    }

    /**
     * Compute cross references
     */
    protected void CrossReferences() {
        /*
         * access list
         */
        for (AccessList acl : _accessLists) {
            /*
             * access list element
             */
            for (AccessListElement ace : acl) {
                if (!ace.isImplicit())
                    crossRefAccessList(ace);
            }
        }
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
         * Check if the destination of the probe is on this equipment.
         */
        IPNet ipdest = probe.getDestinationAddress().toIPNet();
        if (ipdest != null) {
            IfaceLink ilink = getIfaceLink(ipdest);
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
            @SuppressWarnings("unchecked")
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

    protected MatchResult compareIpIpNetmask(IPRangeable ip, IPNet aceIp,
                                             IPNet aceNetmask) {

        /*
         * if aceNetmask is null, aceIp is a valid network.
         * so compare the networks.
         */
        if (aceNetmask == null) {
            if (!aceIp.contains(ip)) {
                if (!aceIp.overlaps(ip))
                    return MatchResult.NOT;
                else
                    return MatchResult.MATCH;
            }
            return MatchResult.ALL;
        }

        /*
         * if the ace address is not a valid network.
         *
         * compare IP
         */
        if (ip.isHost()) {
            BigInteger res = ip.toIPNet().getIP().and(aceNetmask.getIP());
            int comp = res.compareTo(aceIp.getIP());
            if (comp == 0)
                return MatchResult.ALL;
            return MatchResult.NOT;
        }
        /*
         * else we are not sure about the result
         */
        return MatchResult.UNKNOWN;
    }

    protected String iosTcpFlagToFlag(String flag) {
        if (flag.equals("ack"))
            return "a";
        if (flag.equals("fin"))
            return "f";
        if (flag.equals("psh"))
            return "p";
        if (flag.equals("rst"))
            return "r";
        if (flag.equals("syn"))
            return "s";
        if (flag.equals("urg"))
            return "u";
        return null;
    }

    protected boolean tcpFlagsFilter(ProbeTcpFlags reqFlags,
                                     AccessListElement ace) {

        String flags = "";
        for (String f : ace.getTcpFlags()) {
            String flag = iosTcpFlagToFlag(f.substring(1));
            if (f.startsWith("+"))
                flag = flag.toUpperCase();
            flags += flag;
        }
        if (ace.getTcpKeyword().equals("match-any"))
            return reqFlags.matchAny(flags);
        if (ace.getTcpKeyword().equals("match-all"))
            return reqFlags.matchAll(flags);
        return false;
    }

    /**
     *
     */
    protected MatchResult probeFilter(Probe probe, AccessListElement ace,
                                      Direction direction) {

        ProbeRequest request = probe.getRequest();

        /*
         * check ip source
         */
        IPNet aceIpSource = ace.getSourceIp();
        IPNet aceSourceNetmask = ace.getSourceNetmask();
        IPRangeable probeIpSource = probe.getSourceAddress();

        MatchResult mIpSource = MatchResult.ALL;
        if (aceIpSource != null) {
            mIpSource = compareIpIpNetmask(probeIpSource, aceIpSource,
                    aceSourceNetmask);
            if (mIpSource == MatchResult.NOT)
                return MatchResult.NOT;

        }

        /*
         * check ip destination
         */
        IPNet aceIpDest = ace.getDestIp();
        IPNet aceDestNetmask = ace.getDestNetmask();
        IPRangeable probeIpDest = probe.getDestinationAddress();
        MatchResult mIpDest = MatchResult.ALL;

        if (aceIpDest != null) {
            mIpDest = compareIpIpNetmask(probeIpDest, aceIpDest, aceDestNetmask);
            if (mIpDest == MatchResult.NOT)
                return MatchResult.NOT;
        }

        /*
         * check protocol
         */
        List<Integer> reqProto = request.getProtocols();
        Integer aceProto = ace.getProtocol();

        if (reqProto != null) {
            /*
             * protocol
             */
            if (aceProto != null && !ProtocolComparator.matches(reqProto, aceProto))
                return MatchResult.NOT;
        }

        /*
         * check source service
         */
        PortSpec reqSourcePort = request.getSourcePort();
        PortObject aceSrcPort = ace.getSourcePortObject();
        MatchResult mSourcePort = MatchResult.ALL;

        if (reqSourcePort != null) {
            /*
             * port object
             */
            if (aceSrcPort != null) {
                mSourcePort = aceSrcPort.matches(reqSourcePort);
                if (mSourcePort == MatchResult.NOT)
                    return MatchResult.NOT;
            }
        }

        /*
         * check destination service
         */
        PortSpec reqDestPort = request.getDestinationPort();
        PortObject aceDestPort = ace.getDestPortObject();
        MatchResult mDestPort = MatchResult.ALL;

        if (reqDestPort != null) {
            /*
             * port object
             */
            if (aceDestPort != null) {
                mDestPort = aceDestPort.matches(reqDestPort);
                if (mDestPort == MatchResult.NOT)
                    return MatchResult.NOT;
            }
        }

        /*
         * icmp type
         */
        Integer reqSubType = request.getSubType();
        Integer reqCode = request.getCode();

        Integer aceSubType = ace.getSubType();
        Integer aceCode = ace.getCode();

        if (reqProto != null &&
                reqProto.contains(Protocols.ICMP)) {
            if (reqSubType != null) {

                /*
                 * icmp type
                 */
                if (aceSubType != null && aceSubType.intValue() != reqSubType)
                    return MatchResult.NOT;
                /*
                 * icmp code
                 */
                if (reqCode != -1) {
                    if (aceCode != null && aceCode.intValue() != reqCode)
                        return MatchResult.NOT;
                }
            }

        }

        /*
         * Check TCP flags
         */
        ProbeTcpFlags reqTcpFlags = request.getTcpFlags();
        StringsList aceFlags = ace.getTcpFlags();
        if (reqTcpFlags != null && !aceFlags.isEmpty()) {
            if (!tcpFlagsFilter(reqTcpFlags, ace))
                return MatchResult.NOT;
        }

        if (mIpSource == MatchResult.ALL && mIpDest == MatchResult.ALL &&
                mSourcePort == MatchResult.ALL && mDestPort == MatchResult.ALL)
            return MatchResult.ALL;

        return MatchResult.MATCH;
    }

    /**
     * Packet filter
     */
    protected void packetFilter(IfaceLink link, Direction direction, Probe probe) {

        String ifaceName = link.getIfaceName();
        String ifaceComment = link.getIface().getComment();
        ProbeResults results = probe.getResults();

        /*
         * retrieve access groups associated in direction with the interface.
         */
        CiscoIface csIface = _ciscoIfaces.get(ifaceName);
        List<AccessGroup> agroups = null;
        if (probe.isIPv4()) {
            switch (direction) {
                case IN:
                    agroups = csIface.getAccessGroup4In();
                    break;
                case OUT:
                    agroups = csIface.getAccessGroup4Out();
                    break;
            }
        } else {
            switch (direction) {
                case IN:
                    agroups = csIface.getAccessGroup6In();
                    break;
                case OUT:
                    agroups = csIface.getAccessGroup6Out();
                    break;
            }
        }

        /*
         * for each access list matching access groups associated.
         */
        boolean first = true;
        for (AccessList acl : _accessLists) {
            /*
             * acl matches any access groups?
             */
            boolean mgroup = false;
            for (AccessGroup group : agroups) {
                if (group.getName().equals(acl.getName())) {
                    mgroup = true;
                    break;
                }
            }
            // access group doesn't match
            if (!mgroup)
                continue;

            /*
             * check each access list element
             */
            MatchResult match;
            for (AccessListElement ace : acl) {
                match = probeFilter(probe, ace, direction);
                if (match != MatchResult.NOT) {
                    /*
                     * store the result in the probe
                     */
                    FwResult aclResult = new FwResult();
                    aclResult.setResult(ace.getAction().equals("permit") ?
                            FwResult.ACCEPT : FwResult.DENY);
                    if (match != MatchResult.ALL)
                        aclResult.addResult(FwResult.MAY);

                    results.addMatchingAcl(direction,
                            ace.getConfigurationLine(),
                            aclResult);

                    results.setInterface(direction,
                            ifaceName + " (" + ifaceComment + ")");


                    /*
                     * the active ace is the ace accepting or denying the packet.
                     * this is the first ace that match the packet.
                     */
                    if (first) {
                        results.addActiveAcl(direction,
                                ace.getConfigurationLine(),
                                aclResult);
                        first = false;
                    }
                }
            }
        }
        results.setAclResult(direction, results.reduceMatchingFwResults(direction));
    }

    @Override
    public void runShell(String command, PrintStream output) {
        _shell.shellCommand(command, output);
    }

}
