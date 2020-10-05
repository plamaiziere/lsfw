/*
 * Copyright (c) 2013 - 2020, Universite de Rennes 1
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

package fr.univrennes1.cri.jtacl.equipments.fortigate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.univrennes1.cri.jtacl.analysis.*;
import fr.univrennes1.cri.jtacl.core.exceptions.JtaclConfigurationException;
import fr.univrennes1.cri.jtacl.core.exceptions.JtaclInternalException;
import fr.univrennes1.cri.jtacl.core.monitor.Log;
import fr.univrennes1.cri.jtacl.core.monitor.Monitor;
import fr.univrennes1.cri.jtacl.core.network.Iface;
import fr.univrennes1.cri.jtacl.core.network.IfaceLink;
import fr.univrennes1.cri.jtacl.core.network.Route;
import fr.univrennes1.cri.jtacl.core.network.Routes;
import fr.univrennes1.cri.jtacl.core.probing.FwResult;
import fr.univrennes1.cri.jtacl.core.probing.MatchResult;
import fr.univrennes1.cri.jtacl.core.probing.Probe;
import fr.univrennes1.cri.jtacl.core.probing.ProbeResults;
import fr.univrennes1.cri.jtacl.equipments.generic.GenericEquipment;
import fr.univrennes1.cri.jtacl.lib.ip.*;
import fr.univrennes1.cri.jtacl.lib.misc.Direction;
import fr.univrennes1.cri.jtacl.lib.misc.ParseContext;
import fr.univrennes1.cri.jtacl.lib.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.UnknownHostException;
import java.util.*;
import java.util.logging.Level;

public class FgFw extends GenericEquipment {

	protected class FgIface {
		protected Iface _iface;
		protected String _name;
		protected String _description;

		public FgIface(Iface iface) {
			_iface = iface;
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
	}

	/**
	 * interfaces
	 */
	protected HashMap<String, FgIface> _fgFwIfaces
		= new HashMap<>();

	/**
	 * parse context
	 */
	 protected ParseContext _parseContext = new ParseContext();

	/**
	 * IP cross references map
	 */
	protected IPCrossRefMap _netCrossRef = new IPCrossRefMap();

	/**
	 * Services cross references map
	 */
	protected ServiceCrossRefMap _serviceCrossRef = new ServiceCrossRefMap();

	/**
	 * IP cross references map
	 */
	IPCrossRefMap getNetCrossRef() {
		return _netCrossRef;
	}

	/**
	 * service cross references map
	 */
	ServiceCrossRefMap getServiceCrossRef() {
		return _serviceCrossRef;
	}

    /*
	 * services keyed by origin key
	 */
	protected HashMap<String, FgService> _fgServices
			= new HashMap<>();

	/*
	 * network objects keyed by origin key
	 */
	protected HashMap<String, FgNetworkObject> _fgNetworks
			= new HashMap<>();

    public HashMap<String, FgService> getFgServices() {
        return _fgServices;
    }

    public HashMap<String, FgNetworkObject> getFgNetworks() {
        return _fgNetworks;
    }

	/*
	 * firewall rules
	 */
	protected List <FgFwRule> _fgRules = new LinkedList<>();

	public List<FgFwRule> getFgRules() { return _fgRules; }

	/**
	 * Sorted list of services by orgin key
	 * @return a sorted list of services by origin key.
	 */
	public List<String> getServicesName() {

		List<String> list = new LinkedList<>();
		list.addAll(_fgServices.keySet());
		Collections.sort(list);
		return list;
	}

	/**
	 * Sorted list of networks by origin key
	 * @return a sorted list of networks by origin key
	 */
	public List<String> getNetworksName() {

		List<String> list = new LinkedList<>();
		list.addAll(_fgNetworks.keySet());
		Collections.sort(list);
		return list;
	}

    FgFwShell _shell = new FgFwShell(this);

	/**
	 * Create a new {@link FgFw} with this name and this comment.<br/>
	 * @param monitor the {@link Monitor} monitor associated with this equipment.
	 * @param name the name of the equipment.
	 * @param comment a free comment for this equipment.
	 * @param configurationFileName name of the configuration file to use (may be null).
	 */
	public FgFw(Monitor monitor, String name, String comment, String configurationFileName) {
		super(monitor, name, comment, configurationFileName);
	}

	@Override
	public void runShell(String command, PrintStream output) {
		_shell.shellCommand(command, output);
	}

	protected void loadIfaces(Document doc) {

		NodeList list = doc.getElementsByTagName("iface");
		for (int i = 0; i < list.getLength(); i++) {
			Element e = (Element) list.item(i);
			String name = e.getAttribute("name");
			String comment = e.getAttribute("comment");
			String ifIp = e.getAttribute("ip");
			String ifNetwork = e.getAttribute("network");

			String s = "name: " + name + " comment: " + comment +
					" IP: " + ifIp + " network: " + ifNetwork;

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
			FgIface fnfwIface;
			Iface iface = getIface(name);
			if (iface == null) {
				if (comment.isEmpty())
					throw new JtaclConfigurationException("Missing interface comment: " + s);
				iface = addIface(name, comment);
				fnfwIface = new FgIface(iface);
				_fgFwIfaces.put(name, fnfwIface);
			}
			iface.addLink(ip, network);
		}
	}

	protected void throwCfgException(String msg, boolean context) {
		String s = "Equipment: " + _name + " ";
		if (context)
			s += _parseContext + msg;
		else
			s += msg;

		throw new JtaclConfigurationException(s);
	}

	protected void warnConfig(String msg, boolean context) {
		String s = "Equipment: " + _name + " ";
		if (context)
			s += _parseContext + msg;
		else
			s += msg;

		Log.config().warning(s);
	}

	@Override
	public void configure() {
		if (_configurationFileName.isEmpty())
			return;

		/*
		 * Read the XML configuration file
		 */
		famAdd(_configurationFileName);
		Document doc = XMLUtils.getXMLDocument(_configurationFileName);
		loadOptionsFromXML(doc);
		loadFiltersFromXML(doc);

		loadIfaces(doc);
		// loopback interface
        Iface iface = addLoopbackIface("loopback", "loopback");
        _fgFwIfaces.put("loopback", new FgIface(iface));

		loadConfiguration(doc);
		linkServices();
		linkNetworkObjects();

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
	}

	protected void loadConfiguration(Document doc) {

         /* fwpolicy */
		NodeList list = doc.getElementsByTagName("fwpolicy");
		if (list.getLength() < 1) {
			throwCfgException("At least one fwpolicy must be specified", false);
		}

		List<String> filenames = new ArrayList<>();
		for (int i = 0; i < list.getLength(); i++) {
            Element e = (Element) list.item(i);
            String filename = e.getAttribute("filename");
            if (!filename.isEmpty()) {
                filenames.add(filename);
            }
        }

        if (filenames.isEmpty()) {
            throwCfgException("Missing policy file name", false);
        }

        for (String f: filenames) {
            famAdd(f);
            FileReader jf;
            JsonNode rootNode = null;
            try {
                jf = new FileReader(f);
                ObjectMapper objectMapper = new ObjectMapper();
                rootNode = objectMapper.readTree(jf);
            } catch (IOException ex) {
                throwCfgException("Cannot read file " + ex.getMessage(), false);
            }
            JsonNode dictNode = rootNode.path("services");
            loadJsonServices(dictNode);

            dictNode = rootNode.path("serv_groups");
            loadJsonServicesGroup(dictNode);

            dictNode = rootNode.path("addresses");
            loadJsonAddresses(dictNode);

            dictNode = rootNode.path("addr_groups");
            loadJsonNetworkGroup(dictNode);

            dictNode = rootNode.path("rules");
            loadJsonFwRules(dictNode);
        }

        // implicit drop rule at the end of the rules
        FgFwRule fwdrop = FgFwRule.newImplicitDropRule();
        _fgRules.add(fwdrop);
    }

    protected void linkServices() {
		/*
		 * each service
		 */
		for (String serviceKey: _fgServices.keySet()) {
            FgService service = _fgServices.get(serviceKey);
            if (service.getType() == FgServiceType.GROUP) {
                /*
                 * resolves the references of the group
                 */
                FgServicesGroup group = (FgServicesGroup) service;
                for (String refKey : group.getReferencesName()) {
                    FgService ref = _fgServices.get(refKey);
                    if (ref == null) {
                        warnConfig("cannot link service group: "
                                + serviceKey + " to member: " + refKey, false);
                    } else {
                        group.addReference(refKey, ref);
                        ref.linkWith(group);
                    }
                }
            }
        }
    }

   /*
     * Resolve network groups references
     */
	protected void linkNetworkObjects() {
		/*
		 * each network object
		 */
		for (String objectName: _fgNetworks.keySet()) {
            FgNetworkObject nobj = _fgNetworks.get(objectName);
            if (nobj.getType() == FgNetworkType.GROUP) {
                /*
                 * resolves the base references of the group
                 */
                FgNetworkGroup group = (FgNetworkGroup) nobj;
                for (String refName : group.getBaseReferencesName()) {
                    FgNetworkObject ref = _fgNetworks.get(refName);
                    if (ref == null) {
                        warnConfig("cannot link network group: "
                                + objectName + " to member: " + refName, false);
                    } else {
                        group.addBaseReference(refName, ref);
                        ref.linkWith(group);
                    }
                }
                /*
                 * excluded ref
                 */
                for (String refName : group.getExcludedReferencesName()) {
                    FgNetworkObject ref = _fgNetworks.get(refName);
                    if (ref == null) {
                        warnConfig("cannot link network group: "
                                + objectName
                                + " to excluded member: " + refName, false);
                    } else {
                        group.addExcludedReference(refName, ref);
                        ref.linkWith(group);
                    }
                }
            }
        }
	}

    protected int parseNumber(String number) {
	    int i = 0;
        try {
            i = Integer.parseInt(number);
        } catch (NumberFormatException ex) {
            throwCfgException("invalid number: " + number, true);
        }
        return i;
    }

    protected List<FgPortsSpec> parseListPortsSpecs(String listPortsSpecs) {
        //XXX: form : rangedest1[-rangedest1][:rangesource1-rangesource1] rangedest2[-rangedest2] ...
	    List<FgPortsSpec> lps = new LinkedList<>();
	    String[] lp = listPortsSpecs.split(" ");
	    for(String p: lp) {
	        lps.add(parsePortsSpec(p));
        }
	    return lps;
    }

    protected FgPortsSpec parsePortsSpec(String portsSpec) {
        //XXX: form : rangedest1[-rangedest1][:rangesource1-rangesource1]
        PortSpec source = null;
        PortSpec dest = null;
	    String[] lpr = portsSpec.split(":");
	    if (lpr.length > 2) throwCfgException("invalid source/destination ports ranges: " + portsSpec, true);
        dest = parsePortRange(lpr[0]);
	    if (lpr.length == 2) {
	        source = parsePortRange(lpr[1]);
        }
        return new FgPortsSpec(source, dest);
	}

    protected PortSpec parsePortRange(String portrange) {
       //XXX: form : range1[-range1]
	    PortSpec portSpec = new PortSpec();

        String[] sr = portrange.split("-");
        if (sr.length > 2) throwCfgException("invalid port range: " + portrange, true);

        int port1 = parseNumber(sr[0]);
        int port2 = port1;
        if (sr.length == 2) {
            port2 = parseNumber(sr[1]);
        }
        portSpec.add(new PortRange(port1, port2));
        return portSpec;
	}

	List<IPRangeable> parseFqdn(String fqdn) {
	    List<IPRangeable> ips = new LinkedList<>();
        try {
            List<IPNet> lip = IPNet.getAllByName(fqdn, IPversion.IPV4);
            for (IPNet ip: lip) {
                ips.add(new IPRange(ip));
            }
        } catch (UnknownHostException e) {
            throwCfgException("cannot resolve FQDN: " + fqdn, true);
        }
        return ips;
	}

	List<IPRangeable> parseServiceIpRange(String iprange) {
	    List<IPRangeable> ips = new LinkedList<>();

	    IPNet ip;
	    try {
	        ip = new IPNet(iprange);
	        ips.add(new IPRange(ip, ip));
        } catch (UnknownHostException e) {
            throwCfgException("invalid IP " + iprange, true);
        }
        return ips;
	}

	IPRangeable parseAddressSubnet(String subnet) {
        String[] sip = subnet.split(" ");
        if (sip.length != 2) throwCfgException("Invalid subnet " + subnet, true);
        IPNet ip = null;
        try {
            ip = new IPNet(sip[0] + "/" + sip[1]);
        } catch (UnknownHostException e) {
            throwCfgException("Invalid subnet " + subnet, true);
        }
        return new IPRange(ip);
    }

    IPRangeable parseAddressIPRange(String start, String end) {
	    IPRange r = null;
	    try {
	        IPNet ip1 = new IPNet(start);
	        IPNet ip2 = new IPNet(end);
	        r = new IPRange(ip1, ip2);
        } catch (UnknownHostException e) {
            throwCfgException("Invalid ip range " + start + ", " + end, true);
        }
	    return r;
    }

    protected void checkFwRuleIface(FgIfacesSpec ifaces) {
	    for (String intf: ifaces) {
	        if (!intf.equals("any") && !_fgFwIfaces.containsKey(intf))
                throwCfgException("Unknown interface " + intf, true);
        }
	}

    protected List<String> parseJsonOriginKeyList(JsonNode node) {
	    List<String> list = new LinkedList<>();
	    Iterator<JsonNode> it = node.elements();
	    while(it.hasNext()) {
	        JsonNode n = it.next();
            list.add(n.path("q_origin_key").textValue());
        }
	    return list;
	}

	protected FgFwIpSpec parseJsonFwRulesAddresses(JsonNode n) {
        FgFwIpSpec ipSpec = new FgFwIpSpec();
        List<String> saddresses = parseJsonOriginKeyList(n);
        for (String saddress: saddresses) {
            FgNetworkObject address = _fgNetworks.get(saddress);
            if (address == null) {
                warnConfig("Unknown address object " + saddress, true);
            } else {
                ipSpec.addReference(saddress, address);
            }
        }
        return ipSpec;
	}

	protected FgFwServicesSpec parseJsonFwRulesServices(JsonNode n) {
        FgFwServicesSpec servSpec = new FgFwServicesSpec();
        List<String> sservices = parseJsonOriginKeyList(n);
        for (String sservice: sservices) {
            FgService service = _fgServices.get(sservice);
            if (service == null) {
                warnConfig("Unknown service object " + sservice, true);
            } else {
                servSpec.addReference(sservice, service);
            }
        }
        return servSpec;
	}

    /*
     * parse and load fw rules from JSON
     */
    protected void loadJsonFwRules(JsonNode jrules) {
        int number = 0;
        Iterator<JsonNode> it = jrules.elements();
        while (it.hasNext()) {
            JsonNode n = it.next();
            _parseContext = new ParseContext();
            _parseContext.setLine(n.toString());
            String suid = n.path("policyid").asText();
            String sname = n.path("name").textValue();
            String soriginKey = n.path("q_origin_key").textValue();
            String scomment = n.path("comment").textValue();

            /* Interfaces */
            FgIfacesSpec srcIfaces = new FgIfacesSpec();
            srcIfaces.addAll(parseJsonOriginKeyList(n.path("srcintf")));
            checkFwRuleIface(srcIfaces);

            FgIfacesSpec dstIfaces = new FgIfacesSpec();
            dstIfaces.addAll(parseJsonOriginKeyList(n.path("dstintf")));
            checkFwRuleIface(dstIfaces);

            /* addresses */
            FgFwIpSpec srcAddresses = parseJsonFwRulesAddresses(n.path("srcaddr"));
            srcAddresses.setNotIn(!n.path("srcaddr-negate").textValue().equals("disable"));

            FgFwIpSpec dstAddresses = parseJsonFwRulesAddresses(n.path("dstaddr"));
            dstAddresses.setNotIn(!n.path("dstaddr-negate").textValue().equals("disable"));

            /* services */
            FgFwServicesSpec servSpec = parseJsonFwRulesServices(n.path("service"));
            servSpec.setNotIn(!n.path("service-negate").textValue().equals("disable"));

            /* misc */
            boolean disable = !n.path("status").textValue().equals("enable");
            String saction = n.path("action").textValue();
            FgFwRuleAction action = null;
            if (saction.equals("accept")) action =FgFwRuleAction.ACCEPT;
            if (saction.equals("deny")) action = FgFwRuleAction.DROP;
            if (action == null) throwCfgException("Unknown action " + saction, true);
            String slabel = n.path("global-label").textValue();

            FgFwRule rule = FgFwRule.newSecurityRule(sname, soriginKey, scomment, slabel, suid, number
                , disable, srcIfaces, dstIfaces, srcAddresses, dstAddresses, servSpec, action);

            srcAddresses.linkTo(rule);
            dstAddresses.linkTo(rule);
            servSpec.linkTo(rule);
            _fgRules.add(rule);
            number++;
        }
    }

	/*
	 * parse and load addresses from JSON
	 */
	protected void loadJsonAddresses(JsonNode jAddresses) {
        Iterator<JsonNode> it = jAddresses.elements();
	    while (it.hasNext()) {
	        JsonNode n = it.next();
            _parseContext = new ParseContext();
            _parseContext.setLine(n.toString());
            String sname = n.path("name").textValue();
            String soriginKey = n.path("q_origin_key").textValue();
	        String scomment = n.path("comment").textValue();
	        String stype = n.path("type").textValue();
	        String suid = n.path("uuid").textValue();

	        FgNetworkObject address = null;

	        if (stype.equals("ipmask")) {
	            IPRangeable r = parseAddressSubnet(n.path("subnet").textValue());
	            address = new FgNetworkIP(sname, soriginKey, scomment, suid, r, null);
            }

	        if (stype.equals("iprange")) {
	            String start = n.path("start-ip").textValue();
	            String end = n.path("end-ip").textValue();
	            IPRangeable r = parseAddressIPRange(start, end);
	            address = new FgNetworkIP(sname, soriginKey, scomment, suid, r, null);
            }

	        if (address == null) {
                address = new FgUnhandledNetwork(sname, soriginKey, scomment, suid);
                warnConfig("address is unhandled: " + address, false);

            }
	        _fgNetworks.put(soriginKey, address);
        }
    }

	/*
	 * parse and load network groups from JSON
	 */
	protected void loadJsonNetworkGroup(JsonNode jServGroup) {

	    Iterator<JsonNode> it = jServGroup.elements();
	    while (it.hasNext()) {
	        JsonNode n = it.next();
            _parseContext = new ParseContext();
            _parseContext.setLine(n.toString());
            String sname = n.path("name").textValue();
            String soriginKey = n.path("q_origin_key").textValue();
	        String scomment = n.path("comment").textValue();
	        String suid = n.path("uuid").textValue();
	        FgNetworkGroup networkGroup = new FgNetworkGroup(sname, soriginKey, scomment, suid);

	        Iterator<JsonNode> itMembers = n.path("member").elements();
	        while (itMembers.hasNext()) {
	            JsonNode m = itMembers.next();
	            String ok = m.path("q_origin_key").textValue();
                /* XXXX: references are resolved later */
	            networkGroup.addBaseReference(ok, null);
            }

	        Iterator<JsonNode> itExcludeMembers = n.path("exclude-member").elements();
	        while (itExcludeMembers.hasNext()) {
	            JsonNode m = itExcludeMembers.next();
	            String ok = m.path("q_origin_key").textValue();
                /* XXXX: references are resolved later */
	            networkGroup.addExcludedReference(ok, null);
            }
	        _fgNetworks.put(soriginKey, networkGroup);
        }
    }

	/*
	 * parse and load services groups from JSON
	 */
	protected void loadJsonServicesGroup(JsonNode jServGroup) {

	    Iterator<JsonNode> it = jServGroup.elements();
	    while (it.hasNext()) {
	        JsonNode n = it.next();
            _parseContext = new ParseContext();
            _parseContext.setLine(n.toString());
            String sname = n.path("name").textValue();
            String soriginKey = n.path("q_origin_key").textValue();
	        String scomment = n.path("comment").textValue();
	        FgServicesGroup servicesGroup = new FgServicesGroup(sname, soriginKey, scomment);

	        Iterator<JsonNode> itMembers = n.path("member").elements();
	        while (itMembers.hasNext()) {
	            JsonNode m = itMembers.next();
	            String ok = m.path("q_origin_key").textValue();
                /* XXXX: references are resolved later */
	            servicesGroup.addReference(ok, null);
            }
	        _fgServices.put(soriginKey, servicesGroup);
        }
    }

	/*
	 * parse and load services objects from JSON
	 */
	protected void loadJsonServices(JsonNode jServices) {

	    Iterator<JsonNode> it = jServices.elements();
	    while (it.hasNext()) {
	        JsonNode n = it.next();
            _parseContext = new ParseContext();
            _parseContext.setLine(n.toString());
            String sname = n.path("name").textValue();
            String soriginKey = n.path("q_origin_key").textValue();
            String sprotocol = n.path("protocol").textValue();
            String siprange = n.path("iprange").textValue();
            String sfqdn = n.path("fqdn").textValue();
	        String scomment = n.path("comment").textValue();

	        List<IPRangeable> ips = null;
	        if (!sfqdn.isEmpty()) {
	            ips = parseFqdn(sfqdn);
            } else sfqdn = null;

	        if (!siprange.equals("0.0.0.0")) {
	            ips.addAll(parseServiceIpRange(siprange));
            }

	        FgAddressService service = null;
	        /* TCP / UDP ... */
	        if (sprotocol.equals("TCP/UDP/SCTP") || sprotocol.equals("ALL")) {
	            JsonNode ntcpports = n.path("tcp-portrange");
	            List<FgPortsSpec> tcpPortsSpec = null;
	            if (!ntcpports.isMissingNode() && !ntcpports.textValue().isEmpty())
	                tcpPortsSpec = parseListPortsSpecs(ntcpports.textValue());

	            JsonNode nudpports = n.path("udp-portrange");
	            List<FgPortsSpec> udpPortsSpec = null;
	            if (!nudpports.isMissingNode() && !nudpports.textValue().isEmpty())
	                udpPortsSpec = parseListPortsSpecs(nudpports.textValue());

	            JsonNode nsctpports = n.path("sctp-portrange");
	            List<FgPortsSpec> sctpPortsSpec = null;
	            if (!nsctpports.isMissingNode() && !nsctpports.textValue().isEmpty())
	                sctpPortsSpec = parseListPortsSpecs(nsctpports.textValue());

	            if (sprotocol.equals("ALL")) {
                    service = new FgProtoAllService(
                            sname
                            , soriginKey
                            , scomment
                            , ips
                            , sfqdn
                            , udpPortsSpec
                            , tcpPortsSpec
                            , sctpPortsSpec
                    );
                } else {
                    service = new FgTcpUdpSctpService(
                            sname
                            , soriginKey
                            , scomment
                            , ips
                            , sfqdn
                            , udpPortsSpec
                            , tcpPortsSpec
                            , sctpPortsSpec
                    );
                }
            }

	        /* ICMP / ICMP6 */
            if (sprotocol.equals("ICMP") || sprotocol.equals("ICMP6")) {
                String sicmpType = n.path("icmptype").asText();
                String scode = n.path("icmpcode").asText();
                Integer icmpType = sicmpType.isEmpty() ? null : parseNumber(sicmpType) ;
                AddressFamily af = sprotocol.equals("ICMP") ? AddressFamily.INET : AddressFamily.INET6;
                service = new FgIcmpService(sname, soriginKey, scomment, ips, sfqdn, af, icmpType
                        , scode.isEmpty() ? -1 : parseNumber(scode));
            }

            /* IP */
            if (sprotocol.equals("IP")) {
                String sprotoNumber = n.path("protocol-number").asText();
                service = new FgProtocolService(sname, soriginKey, scomment, ips, sfqdn, parseNumber(sprotoNumber));
            }

	        if (service == null) {
	             /*
                 * unhandled by lsfw
                 */
                service = new FgUnhandledService(sname, soriginKey, scomment, ips, sfqdn);
                warnConfig("service is unhandled: " + service, false);
            };
            _fgServices.put(soriginKey, service);
        }
    }


	protected IPCrossRef getIPNetCrossRef(IPRangeable iprange) {
		if (!_monitorOptions.getXref())
			throw new JtaclInternalException(
					"Cross reference computing without crossreference option set");
		IPCrossRef ref = _netCrossRef.get(iprange);
		if (ref == null) {
			ref = new IPCrossRef(iprange);
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

	protected void crossRefOwnerService(PortRange range,
			ServiceCrossRefContext refctx, Object obj) {

		ProtocolsSpec protoSpec = refctx.getProtoSpec();
		ServiceCrossRefType xrefType = refctx.getType();

		if (obj instanceof FgServicesGroup) {
			FgServicesGroup group = (FgServicesGroup) obj;
			if (Log.debug().isLoggable(Level.INFO)) {
				Log.debug().info("xref owner service/group: " + group.toString());
			}

			ServiceCrossRefContext nrefctx =
				new ServiceCrossRefContext(protoSpec,
						xrefType, group.toString(),
						group.getType().toString(),
						group.getName(),
						null,
						0);
			ServiceCrossRef serv = getServiceCrossRef(range);
			serv.addContext(nrefctx);
			for (Object owner: group.getLinkedTo())
				crossRefOwnerService(range, refctx, owner);
		}

		if (obj instanceof FgFwRule) {
			FgFwRule rule = (FgFwRule) obj;
			if (Log.debug().isLoggable(Level.INFO)) {
				Log.debug().info("xref owner service/rule: " + rule.toString());
			}
			ServiceCrossRefContext nrefctx =
				new ServiceCrossRefContext(protoSpec,
						xrefType, rule.toText(), "RULE", "", null, 0);
			ServiceCrossRef serv = getServiceCrossRef(range);
			serv.addContext(nrefctx);
		}
	}

	protected void crossRefService(FgTcpUdpSctpService service) {

		if (Log.debug().isLoggable(Level.INFO)) {
			Log.debug().info("xref tcp/udp service: " + service.toString());
		}

		List<Object> owners = service.getLinkedTo();
		if (service.isUdp()) {
		    for (FgPortsSpec portsSpec: service.getUdpPortsSpec()) {
		        if (portsSpec.hasSourcePortSpec())
		            addServiceCrossRef(service, portsSpec.getSourcePorts(), ServiceCrossRefType.FROM, Protocols.UDP, owners);
		        if (portsSpec.hasDestPortSpec())
		            addServiceCrossRef(service, portsSpec.getDestPorts(), ServiceCrossRefType.TO, Protocols.UDP, owners);
            }
        }

		if (service.isTcp()) {
		    for (FgPortsSpec portsSpec: service.getTcpPortsSpec()) {
		        if (portsSpec.hasSourcePortSpec())
		            addServiceCrossRef(service, portsSpec.getSourcePorts(), ServiceCrossRefType.FROM, Protocols.TCP, owners);
		        if (portsSpec.hasDestPortSpec())
		            addServiceCrossRef(service, portsSpec.getDestPorts(), ServiceCrossRefType.TO, Protocols.TCP, owners);
            }
        }
	}

    protected void addServiceCrossRef(FgTcpUdpSctpService service, PortSpec portSpec
            , ServiceCrossRefType serviceCrossRefType, Integer proto, List<Object> owners) {

        ProtocolsSpec protoSpec = new ProtocolsSpec();
        protoSpec.add(proto);
        ServiceCrossRefContext refctx =
            new ServiceCrossRefContext(protoSpec,
                    serviceCrossRefType, service.toString(),
                    service.getType().toString(),
                    service.getName(),
                    null,
                    0);
        for (PortRange range: portSpec.getRanges()) {
            ServiceCrossRef xref = getServiceCrossRef(range);
            xref.addContext(refctx);
            for (Object owner: owners) {
                crossRefOwnerService(range, refctx, owner);
            }
        }
    }

    protected void crossRefNetworkLink(IPCrossRef ipNetRef,
			Object obj) {

		if (obj instanceof FgNetworkObject) {
			FgNetworkObject nobj = (FgNetworkObject) obj;
			if (Log.debug().isLoggable(Level.INFO)) {
				Log.debug().info("xref NetworkLink/Network: " + nobj.toString());
			}
			CrossRefContext refContext =
				new CrossRefContext(nobj.toString(), nobj.getType().toString(),
					nobj.getName(), null, 0);
			ipNetRef.addContext(refContext);
			for (Object linkobj: nobj.getLinkedTo()) {
				crossRefNetworkLink(ipNetRef, linkobj);
			}
		}
		if (obj instanceof FgAddressService) {
		    FgAddressService nobj = (FgAddressService) obj;
            if (Log.debug().isLoggable(Level.INFO)) {
                Log.debug().info("xref NetworkLink/Service: " + nobj.toString());
            }
            CrossRefContext refContext = new CrossRefContext(nobj.toString(), nobj.getType().toString(),
                nobj.getName(), null, 0);
			ipNetRef.addContext(refContext);
			for (Object linkobj: nobj.getLinkedTo()) {
				crossRefNetworkLink(ipNetRef, linkobj);
			}
        }

		if (obj instanceof FgFwRule) {
			FgFwRule rule = (FgFwRule) obj;
			if (Log.debug().isLoggable(Level.INFO)) {
				Log.debug().info("xref NetworkLink/rule: " + rule.toText());
			}
			CrossRefContext refContext =
				new CrossRefContext(rule.toText(), "RULE", "", null, 0);
			ipNetRef.addContext(refContext);
		}
	}


	/**
	 * Compute cross references
	 */
	protected void CrossReferences() {
		/*
		 * network object
		 */
		for (FgNetworkObject nobj: _fgNetworks.values()) {

			IPRangeable ip;
			IPRangeable ip6;
			IPCrossRef ipNetRef;
            IPCrossRef ipNetRef6;

			switch (nobj.getType()) {
                case IPRANGE: FgNetworkIP nip = (FgNetworkIP) nobj;
							ip = nip.getIpRange();
							if (ip != null) {
                                ipNetRef = getIPNetCrossRef(ip);
                                crossRefNetworkLink(ipNetRef, nobj);
                            }
                            ip6 = nip.getIpRange6();
                            if (ip6 != null) {
                                ipNetRef6 = getIPNetCrossRef(ip6);
                                crossRefNetworkLink(ipNetRef6, nobj);
                            }
                            break;
		    }
        }

		/*
		 * services object
		 */
		for (FgService sobj: _fgServices.values()) {
		    if (sobj instanceof FgTcpUdpSctpService) {
    			crossRefService((FgTcpUdpSctpService) sobj);
            }
		    if (sobj instanceof FgAddressService) {
		        FgAddressService nserv = (FgAddressService) sobj;
		        if (nserv.hasRanges()) {
                    List<IPRangeable> ranges = nserv.getipRanges();
                    for (IPRangeable range: ranges) {
                        IPCrossRef ipNetRef = getIPNetCrossRef(range);
                        crossRefNetworkLink(ipNetRef, sobj);
                    }
                }
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
		Routes routes;
		routes = _routingEngine.getRoutes(probe);
		if (routes.isEmpty()) {
			probe.killNoRoute("No route to " + probe.getDestinationAddress());
			return;
		}
		probe.routed(probe.getDestinationAddress().toString("i::"));

		if (Log.debug().isLoggable(Level.INFO)) {
			for (Route r: routes) {
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
		for (int i = 1; i < routes.size(); i ++) {
			probes.add(probe.newInstance());
		}

		/*
		 * Set the position of the probes.
		 */
		for (int i = 0; i < routes.size(); i ++) {
			//noinspection unchecked
			Route<IfaceLink> route = routes.get(i);
			probes.get(i).setOutgoingLink(route.getLink(), route.getNextHop());
		}

		/*
		 * Filter out the probes
		 */
		for (Probe p: probes) {
			packetFilter(p.getOutgoingLink(), Direction.OUT, p);
		}

		/*
		 * Send the probes over the network.
		 */
		for (Probe p: probes) {
			/*
			 * Do not send the probe if the outgoing link is the same as the input.
			 */
			if (!p.getOutgoingLink().equals(link))
				outgoing(p.getOutgoingLink(), p, p.getNextHop());
			else
				probe.killLoop("same incoming and outgoing link");
		}
	}

	protected MatchResult interfaceFilter(IfaceLink link, FgIfacesSpec ifacesSpec) {
	    return ifacesSpec.isAny() ? MatchResult.ALL
                                  : (ifacesSpec.contains(link.getIfaceName()) ? MatchResult.ALL : MatchResult.NOT);
    }

	protected MatchResult ipSpecFilter(FgFwIpSpec ipSpec, IPRangeable range) {

		MatchResult mres = ipSpec.getNetworks().matches(range);
		if (ipSpec.isNotIn())
			mres = mres.not();
		return mres;
	}

	protected MatchResult servicesSpecFilter(FgFwServicesSpec servicesSpec,
                                             Probe probe) {

		FgServicesMatch smatch = servicesSpec.getServices().matches(probe);
		MatchResult mres = smatch.getMatchResult();
		if (servicesSpec.isNotIn())
			mres = mres.not();
		return mres;
	}

	protected MatchResult ruleFilter(IfaceLink link, Direction direction, Probe probe, FgFwRule rule) {
	    /*
	     * disabled
	     */
	    if (rule.isDisabled()) return MatchResult.NOT;

	    /* implicit drop */
        if (rule.isImplicitDrop()) return MatchResult.ALL;

	    /*
	     * interfaces
	     */
        FgIfacesSpec ifacesSpec = direction == Direction.IN ? rule._sourceIfaces : rule._destIfaces;
        if (interfaceFilter(link, ifacesSpec) == MatchResult.NOT) return MatchResult.NOT;

        /*
		 * check source IP
		 */
		FgFwIpSpec ipspec = rule.getSourceIp();
		MatchResult mIpSource = ipSpecFilter(ipspec, probe.getSourceAddress());
		if (mIpSource == MatchResult.NOT)
			return MatchResult.NOT;

		/*
		 * check destination IP
		 */
		ipspec = rule.getDestIp();
		MatchResult mIpDest =ipSpecFilter(ipspec, probe.getDestinationAddress());
		if (mIpDest == MatchResult.NOT)
			return MatchResult.NOT;

		/*
		 * check services
		 */
		MatchResult mService;
		if (probe.getRequest().getProtocols() != null) {
			FgFwServicesSpec services = rule.getServices();
			mService = servicesSpecFilter(services, probe);
			if (mService == MatchResult.NOT)
				return MatchResult.NOT;
		} else {
			mService = MatchResult.ALL;
		}

		if (mIpSource == MatchResult.ALL && mIpDest == MatchResult.ALL &&
				mService == MatchResult.ALL)
			return MatchResult.ALL;

		return MatchResult.MATCH;
    }

	/**
	 * Packet filter
	 */
	protected void packetFilter (IfaceLink link, Direction direction, Probe probe) {
        String ifaceName = link.getIfaceName();
        String ifaceComment = link.getIface().getComment();
        ProbeResults results = probe.getResults();

        MatchResult match;
        for (FgFwRule rule : _fgRules) {
            String ruleText = rule.toText();
            match = ruleFilter(link, direction, probe, rule);
            /* if the rule matches */
            if (match != MatchResult.NOT) {
                /*
                 * store the result in the probe
                 */
                FwResult aclResult = new FwResult();
                switch (rule.getRuleAction()) {
                    case ACCEPT:
                        aclResult.addResult(FwResult.ACCEPT);
                        break;
                    case DROP:
                        aclResult.addResult(FwResult.DENY);
                        break;
                }
                results.addMatchingAcl(direction, ruleText,
                        aclResult);

                results.setInterface(direction,
                        ifaceName + " (" + ifaceComment + ")");

                /*
                 * the active ace is the ace accepting or denying the packet.
                 * this is the first ace that match the packet.
                 */
                if (results.getActiveAcl(direction).isEmpty() && (aclResult.hasAccept() || aclResult.hasDeny())) {
                    results.addActiveAcl(direction,
                            ruleText,
                            aclResult);
                    results.setAclResult(direction,
                            aclResult);
                }
            }
        }
    }
}
