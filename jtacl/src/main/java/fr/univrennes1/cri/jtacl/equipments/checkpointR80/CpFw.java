/*
 * Copyright (c) 2013 - 2018, Universite de Rennes 1
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

package fr.univrennes1.cri.jtacl.equipments.checkpointR80;

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
import fr.univrennes1.cri.jtacl.core.probing.*;
import fr.univrennes1.cri.jtacl.equipments.generic.GenericEquipment;
import fr.univrennes1.cri.jtacl.lib.ip.*;
import fr.univrennes1.cri.jtacl.lib.misc.Direction;
import fr.univrennes1.cri.jtacl.lib.misc.ParseContext;
import fr.univrennes1.cri.jtacl.lib.xml.XMLUtils;
import org.parboiled.Parboiled;
import org.parboiled.parserunners.BasicParseRunner;
import org.parboiled.support.ParsingResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.UnknownHostException;
import java.util.*;
import java.util.logging.Level;

/**
 * Checkpoint Firewall >= R80
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class CpFw extends GenericEquipment {

	protected class CPfwIface {
		protected Iface _iface;
		protected String _name;
		protected String _description;

		public CPfwIface(Iface iface) {
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

	protected class CpFwFilter {
		protected boolean _serviceInspected;

		public boolean hasServiceInspected() {
			return _serviceInspected;
		}

		public void setServiceInspected(boolean serviceInspected) {
			_serviceInspected = serviceInspected;
		}
	}

	/**
	 * Parser
	 */
	protected CpParser _parser = Parboiled.createParser(CpParser.class);

	/**
	 * interfaces
	 */
	protected HashMap<String, CPfwIface> _cpfwIfaces
		= new HashMap<>();

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
	 * name of the gateway for rule installation
	 */
	protected String _gatewayName;

	/**
	 * name of the fw policy
	 */
	protected String _policyName;

    /*
     * name of layers to skip (by default 'Application')
     */
	protected List<String> _skippedLayers = new ArrayList<>();

    /*
     * Checkpoint objects keyed by uid
     */
	protected HashMap<String, CpObject> _cpObjects
            = new HashMap<>();

    /*
     * layers objects keyed by name
     */
    protected HashMap<String, CpLayer> _cpLayers = new HashMap<>();

    /*
     * main layer
     */
    protected CpLayer _rootLayer;

    public CpLayer getRootLayer() { return _rootLayer; }

    /*
	 * rule base actions keyed by name
	 */
	protected HashMap<String, CpFwRuleBaseAction> _cpActions = new HashMap<>();

	/*
	 * services keyed by name
	 */
	protected HashMap<String, CpService> _cpServices
			= new HashMap<>();

	/*
	 * network objects keyed by name
	 */
	protected HashMap<String, CpNetworkObject> _cpNetworks
			= new HashMap<>();

	/*
	 * firewall rules
	 */
	protected LinkedList <CpFwRule> _fwRules = new LinkedList<>();

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

	CpFwShell _shell = new CpFwShell(this);

	/**
	 * Create a new {@link CpFw} with this name and this comment.<br/>
	 * @param monitor the {@link Monitor} monitor associated with this equipment.
	 * @param name the name of the equipment.
	 * @param comment a free comment for this equipment.
	 * @param configurationFileName name of the configuration file to use (may be null).
	 */
	public CpFw(Monitor monitor, String name, String comment, String configurationFileName) {
		super(monitor, name, comment, configurationFileName);
	}

	protected boolean StringNullOrEmpty(String s) {
	    return s == null || s.isEmpty();
    }

	protected IPNet parseIpAddress(String ipAddress) {
	    IPNet ip = null;
	    if (ipAddress == null)
            throwCfgException("invalid IP address (is null)" , true);
        try {
            ip = new IPNet(ipAddress);
        } catch (UnknownHostException ex) {
            throwCfgException("invalid IP address: " + ipAddress, true);
        }
        return ip;
    }

    protected IPRange parseIpRange(String ipAddressFirst, String ipAddressLast) {

        if (ipAddressFirst == null)
            throwCfgException("invalid IP address (ipAddressFirst is null)" , true);
        if (ipAddressLast == null)
            throwCfgException("invalid IP address (ipAddressLast is null)" , true);

        IPNet ipFirst = parseIpAddress(ipAddressFirst);
        IPNet ipLast = parseIpAddress(ipAddressLast);
        return new IPRange(ipFirst, ipLast);
    }

    protected int parsePortNumber(String port) {
	    int i = 0;
        try {
            i = Integer.parseInt(port);
        } catch (NumberFormatException ex) {
            throwCfgException("invalid port number: " + port, true);
        }
        return i;
    }

	protected CpPortItem parsePort(String sPorts) {

		ParsingResult<?> result = new BasicParseRunner(
			_parser.CpPortItem()).run(sPorts);
		if (!result.matched)
			throwCfgException("invalid port specification: " + sPorts, true);

		PortItemTemplate port = _parser.getPortItem();

		String sfirst = port.getFirstPort();
		String slast = port.getLastPort();
		int first = parsePortNumber(sfirst);
		int last = slast != null ? parsePortNumber(slast) : -1;

		String operator = port.getOperator();
		if (operator == null)
			operator = "=";

		CpPortItem portItem;
		if (last != -1) {
			portItem = new CpPortItem(operator, first, last);
		} else {
			portItem = new CpPortItem(operator, first);
		}
		return portItem;
	}

	protected CpService parseTcpUdpService(String name, String className, String comment, String uid, JsonNode n) {

	    String sPort = n.path("port").textValue();
	    String sSourcePort = n.path("source-port").textValue();
	    String sProtocol = n.path("protocol").textValue();
		boolean inAny = n.path("match-for-any").asBoolean();

		/*
		 * ports
		 */
		CpPortItem portItem = parsePort(sPort);
		CpPortItem srcPortItem =
			sSourcePort == null ? null : parsePort(sSourcePort);

		/*
		 * service type
		 */
		CpService service = null;
		if (className.equalsIgnoreCase("service-tcp"))
			service = new CpTcpService(name, comment, uid, portItem, srcPortItem, sProtocol, inAny);
		if (className.equalsIgnoreCase("service-udp"))
			service = new CpUdpService(name, comment, uid, portItem, srcPortItem, sProtocol, inAny);

		return service;
	}

	protected CpService parseIcmpService(String name, String className, String comment, String uid, JsonNode n) {

        /*
         * icmp code and type
         */
		int icmpType = n.path("icmp-type").asInt();
		int icmpCode = n.path("icmp-code").asInt();

		/*
		 * address family
		 */
		AddressFamily af = AddressFamily.INET;
		if (className.equalsIgnoreCase("service-icmpv6"))
			af = AddressFamily.INET6;

		CpService service = new CpIcmpService(name, comment, uid, af, icmpType, icmpCode);
		return service;
	}

	protected CpService parseOtherService(String name, String className, String comment, String uid, JsonNode n) {

        int protocol = n.path("ip-protocol").asInt();
		String sAction = n.path("action").textValue();
		boolean inAny = n.path("match-for-any").asBoolean();

		CpService service =
			new CpOtherService(name, comment, uid, protocol, sAction,null, inAny);

		return service;
	}

	protected CpService parseServiceGroup(String name, String className, String comment, String uid, JsonNode n) {

		CpGroupService service = new CpGroupService(name, className, comment, uid);

		/*
		 * members of this group (should be one member)
		 */
		JsonNode members = n.path("members");
        Iterator<JsonNode> it = members.elements();
        while (it.hasNext()) {
            JsonNode m = it.next();
            String mname = m.path("name").textValue();
            /* XXXX: references are resolved later */
			service.addReference(mname, null);
		}

		return service;
	}

	protected CpService parseUnhandledService(String name, String className, String comment, String uid, JsonNode n) {

		return new CpUnhandledService(name, className, comment, uid, false);
	}

	protected CpNetworkObject parseSimpleGateway(String name, String className, String comment, String uid, JsonNode n) {
		CpNetworkIPs networkIPs = new CpNetworkIPs(name, className, comment, uid);

		/*
		 * interfaces
		 */
		JsonNode interfaces = n.path("interfaces");
		Iterator<JsonNode> it = interfaces.elements();
		while (it.hasNext()) {
			JsonNode iface = it.next();
			String sIp = iface.path("ipv4-address").textValue();
			if (!StringNullOrEmpty(sIp)) {
				IPNet ip = parseIpAddress(sIp);
				networkIPs.addIp(ip);
			}
			String sIp6 = iface.path("ipv6-address").textValue();
			if (!StringNullOrEmpty(sIp6)) {
				IPNet ip6 = parseIpAddress(sIp6);
				networkIPs.addIp(ip6);
			}
		}
		return networkIPs;
	}

    protected CpNetworkObject parseHost(String name, String className, String comment, String uid, JsonNode n) {

        String sIp = n.path("ipv4-address").textValue();
        String sIp6 = n.path("ipv6-address").textValue();

        IPNet ip = null;
        IPNet ip6 = null;
        if (!StringNullOrEmpty(sIp)) {
            ip = parseIpAddress(sIp);
        }
        if (!StringNullOrEmpty(sIp6)) {
            ip6 = parseIpAddress(sIp6);
        }

        CpNetworkIP networkobj = new CpNetworkIP(name, className, comment, uid, ip, ip6,false);
        return networkobj;
    }

    protected CpNetworkObject parseNetwork(String name, String className, String comment, String uid, JsonNode n) {

        String sBroadcast = n.path("broadcast").textValue();
        boolean broadcast = sBroadcast == null || sBroadcast.equalsIgnoreCase("allow");

        String sIp = n.path("subnet4").textValue();
        Integer sNetmask = n.path("mask-length4").asInt();

        String sIp6 = n.path("subnet6").textValue();
        Integer sNetmask6 = n.path("mask-length6").asInt();

        IPNet ip = null;
        IPNet ip6 = null;
        if (!StringNullOrEmpty(sIp)) {
            sIp += "/" + sNetmask.toString();
            ip = parseIpAddress(sIp);
        }
        if (!StringNullOrEmpty(sIp6)) {
            sIp6 += "/"  + sNetmask6.toString();
            ip6 = parseIpAddress(sIp6);
        }

		CpNetworkIP networkobj = new CpNetworkIP(name, className, comment, uid, ip, ip6, broadcast);
		return networkobj;
	}

	protected CpNetworkObject parseNetworkGroup(String name, String className, String comment, String uid, JsonNode n) {

		CpNetworkGroup ngroup = new CpNetworkGroup(name, className, comment, uid);

		if (className.equalsIgnoreCase("group")) {
            /*
             * members of this group (should be one member)
             */
            JsonNode members = n.path("members");
            Iterator<JsonNode> it = members.elements();
            while (it.hasNext()) {
                JsonNode m = it.next();
                String mname = m.path("name").textValue();
                /* XXXX: references are resolved later */
                ngroup.addBaseReference(mname, null);
            }
		}

		if (className.equalsIgnoreCase("group-with-exclusion")) {
		    /* include */
            JsonNode inc = n.path("include");
			String refname = inc.path("name").textValue();
            /* XXXX: references are resolved later */
			ngroup.addBaseReference(refname, null);
			/* exclude */
			JsonNode exc = n.path("except");
            String exceptName = exc.path("name").textValue();
			ngroup.addExcludedReference(exceptName, null);
		}
		return ngroup;
	}

	protected CpNetworkObject parseNetworkRange(String name, String className, String comment, String uid, JsonNode n) {

		String sIpFirst = n.path("ipv4-address-first").textValue();
		String sIpLast = n.path("ipv4-address-last").textValue();
        String sIpFirst6 = n.path("ipv6-address-first").textValue();
        String sIpLast6 = n.path("ipv6-address-last").textValue();

        IPRange ipRange = null;
        IPRange ipRange6 = null;
        if (!StringNullOrEmpty(sIpFirst)) {
            ipRange = parseIpRange(sIpFirst, sIpLast);
        }
        if (!StringNullOrEmpty(sIpFirst6)) {
            ipRange6 = parseIpRange(sIpFirst6, sIpLast6);
        }

		CpNetworkRange networkobj = new CpNetworkRange(name, className, comment, uid, ipRange, ipRange6);
		return networkobj;
	}

	/*
	 * Resolve Any and service groups references
	 */
	protected void linkServices() {

	    // Any
        CpGroupService any = (CpGroupService) _cpServices.get("Any");

		/*
		 * each service
		 */
		for (String serviceName: _cpServices.keySet()) {
            CpService service = _cpServices.get(serviceName);
            if (service.isInAny()) {
                any.addReference(serviceName, service);
                any.linkWith(service);
            }
            if (service.getType() == CpServiceType.GROUP) {
                /*
                 * resolves the references of the group
                 */
                CpGroupService group = (CpGroupService) service;
                for (String refName : group.getReferencesName()) {
                    CpService ref = _cpServices.get(refName);
                    if (ref == null) {
                        warnConfig("cannot link service group: "
                                + serviceName + " to member: " + refName, false);
                    } else {
                        group.addReference(refName, ref);
                        ref.linkWith(group);
                    }
                }
            }
        }
	}

	/*
	 * parse and load objects from JSON
	 */
	protected void loadJsonCpObjects(JsonNode objectsDictionary) {

	    Iterator<JsonNode> it = objectsDictionary.elements();
	    while (it.hasNext()) {
	        JsonNode n = it.next();
            _parseContext = new ParseContext();
            _parseContext.setLine(n.toString());
	        String uid = n.path("uid").textValue();
            String name = n.path("name").textValue();
	        String className = n.path("type").textValue();
	        String comment = n.path("comments").textValue();

	        CpService service = null;
	        CpNetworkObject network = null;
	        CpFwRuleBaseAction action = null;
	        CpObject obj = null;
	        CpAny any = null;
	        CpLayer layer = null;
	        switch (className) {
                /*
                 * actions objects
                 */
                case "Global":
                case "RulebaseAction":
                   switch (name) {
                        case "Accept":
                            action = new CpFwRuleBaseAction(name, className, comment, uid, CpFwRuleAction.ACCEPT);
                            break;
                        case "Drop":
                            action = new CpFwRuleBaseAction(name, className, comment, uid, CpFwRuleAction.DROP);
                            break;
                        case "Reject": /*TODO */
                            action = new CpFwRuleBaseAction(name, className, comment, uid, CpFwRuleAction.REJECT);
                            break;
                        case "Auth": /*TODO */
                            action = new CpFwRuleBaseAction(name, className, comment, uid, CpFwRuleAction.AUTH);
                            break;
                       case "Inner Layer":
                           action = new CpFwRuleBaseAction(name, className, comment, uid, CpFwRuleAction.LAYER_CALL);
                           break;
                       case "Policy Targets":
                           /* used in install on */
                           obj = new CpObject(name, className, comment, uid);
                           break;
                    }
                    break;
                /*
                 * service objects
                 */
                case "service-tcp":
                case "service-udp":
                    service = parseTcpUdpService(name, className, comment, uid, n);
                    break;
                case "service-icmp":
                case "service-icmp6":
                    service = parseIcmpService(name, className, comment, uid, n);
                    break;
                case "service-other":
                    service = parseOtherService(name, className, comment, uid, n);
                    break;
                case "CpmiAnyObject":
                    any = new CpAny(name, className, comment, uid);
                    break;
                case "service-group":
                    service = parseServiceGroup(name, className, comment, uid, n);
                    break;
                case "service-dce-rpc":
                case "service-rpc":
                case "services-sctp":
                    /*
                     * unhandled by lsfw
                     */
                    service = parseUnhandledService(name, className, comment, uid, n);
                    warnConfig("service is unhandled: " + service, false);
                    break;
                /*
                 * network objects
                 */
                case "host":
                case "CpmiGatewayPlain":
                case "CpmiClusterMember":
                case "CpmiGatewayCluster":
                case "CpmiHostCkp":
                    network = parseHost(name, className, comment, uid, n);
                    break;
                case "network":
                    network = parseNetwork(name, className, comment, uid, n);
                    break;
                case "multicast-address-range":
                case "address-range":
                    network = parseNetworkRange(name, className, comment, uid, n);
                    break;
                case "group":
                case "group-with-exclusion":
                    network = parseNetworkGroup(name, className, comment, uid, n);
                    break;
				case "simple-gateway":
					network = parseSimpleGateway(name, className, comment, uid, n);
					break;
                /*
                 * Layers
                 */
                case "access-layer":
                    layer = new CpLayer(name, className, comment, uid);
                    break;
            }

            // XXXX: any is dual: network or service.
            if (any != null) {
                _cpObjects.put(uid, any);
                _cpServices.put(name, any.getAnyService());
                _cpNetworks.put(name, any.getAnyNetwork());
            }

	        if (action != null) {
	            obj = action;
	            _cpActions.put(name, action);
            }
            if (service != null) {
                obj = service;
                _cpServices.put(name, service);
            }
            if (network != null) {
                obj = network;
                _cpNetworks.put(name, network);
            }
            if (layer != null) {
                obj = layer;
                _cpLayers.put(name, layer);
            }
            if (obj != null) {
                if (_cpObjects.get(uid) != null) {
                    warnConfig("Checkpoint object: " + name + ", " + className
                            + " overrides previous definition", false);

                }
                _cpObjects.put(uid, obj);
                if (Log.debug().isLoggable(Level.INFO)) {
                    Log.debug().info("CpObject: " + obj.toString());
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
		for (String objectName: _cpNetworks.keySet()) {
            CpNetworkObject nobj = _cpNetworks.get(objectName);
            if (nobj.getType() == CpNetworkType.GROUP) {
                /*
                 * resolves the base references of the group
                 */
                CpNetworkGroup group = (CpNetworkGroup) nobj;
                for (String refName : group.getBaseReferencesName()) {
                    CpNetworkObject ref = _cpNetworks.get(refName);
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
                    CpNetworkObject ref = _cpNetworks.get(refName);
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

	/*
	 * parse fw rules
	 */
	protected void parseCpRules(CpLayer layer, JsonNode l) {
	    CpFwRule lastSection = null;

        Iterator<JsonNode> it = l.elements();
        while (it.hasNext()) {
            JsonNode n = it.next();
            _parseContext = new ParseContext();
            _parseContext.setLine(n.toString());
            String uid = n.path("uid").textValue();
            String name = n.path("name").textValue();
            String className = n.path("type").textValue();
            String comment = n.path("comments").textValue();
            CpFwRule rule;
            switch (className) {
                case "access-section":
                    /* XXX Access section contains rules */
                    rule = parseFwAccessSection(name, className, comment, uid, layer, n);
                    /* same section can be repeated */
                    if (lastSection == null || (lastSection != null && ! name.equals(lastSection.getName())
                            && lastSection.getNumber() != rule.getNumber())) {
                        layer.getRules().add(rule);
                    }
                    lastSection = rule;
                    JsonNode rulesNode = n.path("rulebase");
                    parseCpRules(layer, rulesNode);
                    break;
                case "access-rule":
                    rule = parseFwRule(name, className, comment, uid, layer, n);
                    layer.getRules().add(rule);
                    break;
            }
        }
    }

    /*
     * parse and load layers
     */
    protected void loadJsonCpLayers(JsonNode layers) {

        Iterator<JsonNode> it = layers.elements();
        while (it.hasNext()) {
            JsonNode n = it.next();
            _parseContext = new ParseContext();
            _parseContext.setLine(n.toString());
            String uid = n.path("uid").textValue();
            String name = n.path("name").textValue();
            String className = n.path("type").textValue();
            String comment = n.path("comments").textValue();
            if (_skippedLayers.contains(name)) {
                warnConfig("Layer " + name + " skipped.", false);
            } else {
                CpLayer layer = new CpLayer(name, className, comment, uid);
                JsonNode l = n.path("rulebase");
                parseCpRules(layer, l);
                _cpLayers.put(name, layer);
                _cpObjects.put(uid, layer);
            }
        }
    }

    /*
     * parse rule's "install-on"
     */
    protected List<String> parseFWInstallGateway(JsonNode n) {
	    List<String> list = new ArrayList<>();
        Iterator<JsonNode> it = n.elements();
        while (it.hasNext()) {
            JsonNode rn = it.next();
            String refuid = rn.textValue();
            CpObject ref = _cpObjects.get(refuid);
            if (ref != null) {
                list.add(ref.getName());
            } else {
                warnConfig("cannot find object uid: " + refuid, true);
                return null;
            }
        }
        return list;
    }

    /*
     * parse rule's services specification
     */
    protected CpFwServicesSpec parseFwServicesSpec(JsonNode n) {

        CpFwServicesSpec servicesSpec = new CpFwServicesSpec();
        Iterator<JsonNode> it = n.elements();
        while (it.hasNext()) {
            JsonNode rn = it.next();
            String refuid = rn.textValue();
            CpObject ref = _cpObjects.get(refuid);
            if (ref != null) {
                if (ref instanceof CpAny) {
                    CpAny cref = (CpAny) ref;
                    servicesSpec.addReference(cref.getName(), cref.getAnyService());
                }
                if (ref instanceof CpService) {
                    CpService cref = (CpService) ref;
                    servicesSpec.addReference(cref.getName(), cref);
                }
            } else {
                warnConfig("cannot find service object uid: " + refuid, true);
                return null;
            }
        }
        return servicesSpec;
	}

    /*
     * parse rule's IP specification (source or destination)
     */
    protected CpFwIpSpec parseFwIpSpec(JsonNode n) {

        CpFwIpSpec ipSpec = new CpFwIpSpec();
        Iterator<JsonNode> it = n.elements();
        while (it.hasNext()) {
            JsonNode rn = it.next();
            String refuid = rn.textValue();
            CpObject ref = _cpObjects.get(refuid);
            if (ref != null) {
                if (ref instanceof CpAny) {
                    CpAny cref = (CpAny) ref;
                    ipSpec.addReference(cref.getName(), cref.getAnyNetwork());
                }
                if (ref instanceof CpNetworkObject) {
                    CpNetworkObject cref = (CpNetworkObject) ref;
                    ipSpec.addReference(cref.getName(), cref);
                }
            } else {
                warnConfig("cannot find network object uid: " + refuid, true);
                return null;
            }
        }
        return ipSpec;
    }

    /*
     * parse access-section rule
     */
    protected CpFwRule parseFwAccessSection(String name
                                            , String className
                                            , String comment
                                            , String uid
                                            , CpLayer layer
                                            , JsonNode n) {

        Integer ruleNumber = n.path("from").asInt();
        Integer toNumber = n.path("to").asInt();
        CpFwRule fwrule = CpFwRule.newAccessSectionRule(name, className, comment, uid, layer, ruleNumber, toNumber);
        if (Log.debug().isLoggable(Level.INFO)) {
            Log.debug().info("CpFwRule: " + fwrule);
        }
        return fwrule;
    }

    /*
     * parse security fw rule
     */
    protected CpFwRule parseFwRule(String name, String className, String comment, String uid, CpLayer layer, JsonNode n) {

        Integer ruleNumber = n.path("rule-number").asInt();

        // source ip(s)
        JsonNode jsource = n.path("source");
        CpFwIpSpec sourceSpec = parseFwIpSpec(jsource);
        boolean sourceNegate = n.path("source-negate").asBoolean();
        if (sourceNegate) sourceSpec.setNotIn(true);

        // destination ip(s)
        JsonNode jdest = n.path("destination");
        CpFwIpSpec destSpec = parseFwIpSpec(jdest);
        boolean destNegate = n.path("destination-negate").asBoolean();
        if (destNegate) destSpec.setNotIn(true);

        // service(s)
        JsonNode jserv = n.path("service");
        CpFwServicesSpec servSpec = parseFwServicesSpec(jserv);
        boolean servNegate = n.path("service-negate").asBoolean();
        if (servNegate) servSpec.setNotIn(true);

        // rule enabled
        boolean enabled = n.path("enabled").asBoolean();

        // rule action
        String action = n.path("action").textValue();
        CpFwRuleAction ruleAction = null;
        String layerCall = null;
        CpObject o = _cpObjects.get(action);
        if (o != null && o instanceof CpFwRuleBaseAction) {
            ruleAction = ((CpFwRuleBaseAction) o).getAction();
            if (ruleAction == CpFwRuleAction.LAYER_CALL) {
                String layerUIdCall = n.path("inline-layer").textValue();
                CpObject clayer = _cpObjects.get(layerUIdCall);
                if (clayer == null) {
                    warnConfig("layer: " + layer.getName() + " rule: " + name + " (uid:" + uid + ")" + " cannot find layer uid:" + layerUIdCall, false);
                } else {
                    layerCall = clayer.getName();
                }
            }
        } else {
            warnConfig("layer: " + layer.getName() + " rule: " + name + " (uid:" + uid + ")" + " cannot find rule action uid:" + action, false);
        }

        // install on gateway(s)
        JsonNode inst = n.path("install-on");
        List<String> installgw = parseFWInstallGateway(inst);

        CpFwRule fwrule = CpFwRule.newSecurityRule(name, className, comment, uid, layer, ruleNumber
            , !enabled, sourceSpec, destSpec, servSpec, ruleAction, layerCall, installgw);

        /*
         * track references
         */
        sourceSpec.linkTo(fwrule);
        destSpec.linkTo(fwrule);
        servSpec.linkTo(fwrule);
        if (Log.debug().isLoggable(Level.INFO)) {
            Log.debug().info("CpFwRule: " + fwrule);
        }
        return fwrule;
	}

	/*
	 * compute the order of the layers calls
	 */
	protected void computeLayersCalls(CpLayer layer) {
	    for (CpFwRule rule: layer.getRules()) {
	        if (rule.isSecurityRule() && (rule.getRuleAction() == CpFwRuleAction.LAYER_CALL)) {
	            Integer ruleNumber = rule.getNumber();
	            String layerCall = rule.getLayerCall();
	            CpLayer nlayer = _cpLayers.get(layerCall);
	            nlayer.setParentLayer(layer);
	            nlayer.setLayerCallRuleNumber(ruleNumber);
            }
        }
        /* warn if a layer is never called */
        for (CpLayer l: _cpLayers.values()) {
            if (l != _rootLayer) {
                if (! l.hasParentLayer()) {
                    warnConfig("layer: " + l.getName() + " uid: " + l.getUid() + " is never called.", false);
                }
            }
        }
    }

	protected void loadConfiguration(Document doc) {

         /* fwpolicy */
		NodeList list = doc.getElementsByTagName("fwpolicy");
		if (list.getLength() < 1) {
			throwCfgException("At least one fwpolicy must be specified", false);
		}

		List<String> filenames = new ArrayList<>();
		String fwpolicy = null;
		for (int i = 0; i < list.getLength(); i++) {
            Element e = (Element) list.item(i);
            String filename = e.getAttribute("filename");
            if (!filename.isEmpty()) {
                filenames.add(filename);
            }
            String pn = e.getAttribute("name");
            if (!pn.isEmpty()) {
                fwpolicy = pn;
            }
        }

        if (filenames.isEmpty()) {
            throwCfgException("Missing policy file name", false);
        }
        if (fwpolicy == null || fwpolicy.isEmpty()) {
            throwCfgException("Missing policy name", false);
        }

        /* skip these layers */
        _skippedLayers.add("Application");
        _skippedLayers.add(fwpolicy + " Application");


        /* gateway name (for rule installation) */
        list = doc.getElementsByTagName("gatewayName");
        if (list.getLength() == 1) {
            Element e = (Element) list.item(0);
            _gatewayName = e.getAttribute("name");
        }
        if (_gatewayName == null || _gatewayName.isEmpty()) {
            throwCfgException("gateway name must be specified", false);
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
            JsonNode dictNode = rootNode.path("objects-dictionary");
            loadJsonCpObjects(dictNode);
            JsonNode layersNode = rootNode.path("layers");
            loadJsonCpLayers(layersNode);
        }

        /* main layer */
        String rootLayerName = fwpolicy + " Security";
        _rootLayer = _cpLayers.get(rootLayerName);
        _rootLayer.setLayerCallRuleNumber(0);
        if (_rootLayer == null)
            throwCfgException("Cannot find fwpolicy layer: " + rootLayerName, false);

        // implicit drop rule at the end of the root layer
        CpFwRule fwdrop = CpFwRule.newImplicitDropRule(_rootLayer);
        _rootLayer.getRules().add(fwdrop);

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
				throwCfgException("Missing interface name: " + s, false);

			if (ifIp.isEmpty())
				throwCfgException("Missing interface IP: " + s, false);

			IPNet ip = null;
			try {
				ip = new IPNet(ifIp);
				ip = ip.hostAddress();
			} catch (UnknownHostException ex) {
				throwCfgException("Invalid interface IP: " + s, false);
			}

			IPNet network = null;
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
				throwCfgException("Invalid interface network: " + s, false);
			}

			/*
			 * Append the link to an existing Iface or create a new one.
			 */
			CPfwIface cpfwIface;
			Iface iface = getIface(name);
			if (iface == null) {
				if (comment.isEmpty())
					throwCfgException("Missing interface comment: " + s, false);
				iface = addIface(name, comment);
				cpfwIface = new CPfwIface(iface);
				_cpfwIfaces.put(name, cpfwIface);
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
        _cpfwIfaces.put("loopback", new CpFw.CPfwIface(iface));
		loadConfiguration(doc);

		linkServices();
		linkNetworkObjects();

		computeLayersCalls(_rootLayer);

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

		if (obj instanceof CpGroupService) {
			CpGroupService group = (CpGroupService) obj;
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

		if (obj instanceof CpFwRule) {
			CpFwRule rule = (CpFwRule) obj;
			if (Log.debug().isLoggable(Level.INFO)) {
				Log.debug().info("xref owner service/rule: " + rule.toString());
			}
			ServiceCrossRefContext nrefctx =
				new ServiceCrossRefContext(protoSpec,
						xrefType, rule.toText(), "rule", "", null, 0);
			ServiceCrossRef serv = getServiceCrossRef(range);
			serv.addContext(nrefctx);
		}

	}

	protected void crossRefTcpUdpService(CpService service) {

		PortSpec sourcePortSpec = null;
		PortSpec destPortSpec = null;
		ProtocolsSpec protoSpec = null;
		List<Object> owners = null;

		if (Log.debug().isLoggable(Level.INFO)) {
			Log.debug().info("xref tcp/udp service: " + service.toString());
		}

		if (service instanceof CpTcpService) {
			CpTcpService sobj = (CpTcpService) service;
			protoSpec = new ProtocolsSpec();
			protoSpec.add(Protocols.TCP);
			if (sobj.getSourcePort() != null)
				sourcePortSpec = sobj.getSourcePort().getPortSpec();
			if (sobj.getPort() != null)
					destPortSpec = sobj.getPort().getPortSpec();
			owners = sobj.getLinkedTo();
		}

		if (service instanceof CpUdpService) {
			CpUdpService sobj = (CpUdpService) service;
			protoSpec = new ProtocolsSpec();
			protoSpec.add(Protocols.UDP);
			if (sobj.getSourcePort() != null)
				sourcePortSpec = sobj.getSourcePort().getPortSpec();
			if (sobj.getPort() != null)
				destPortSpec = sobj.getPort().getPortSpec();
			owners = sobj.getLinkedTo();
		}

		if (sourcePortSpec != null) {
			ServiceCrossRefContext refctx =
				new ServiceCrossRefContext(protoSpec,
						ServiceCrossRefType.FROM, service.toString(),
						service.getType().toString(),
						service.getName(),
						null,
						0);
			for (PortRange range: sourcePortSpec.getRanges()) {
				ServiceCrossRef xref = getServiceCrossRef(range);
				xref.addContext(refctx);
				for (Object owner: owners) {
					crossRefOwnerService(range, refctx, owner);
				}
			}
		}

		if (destPortSpec != null) {
			ServiceCrossRefContext refctx =
				new ServiceCrossRefContext(protoSpec,
						ServiceCrossRefType.TO, service.toString(),
						service.getType().toString(),
						service.getName(),
						null,
						0);
			for (PortRange range: destPortSpec.getRanges()) {
				ServiceCrossRef xref = getServiceCrossRef(range);
				xref.addContext(refctx);
				for (Object owner: owners) {
					crossRefOwnerService(range, refctx, owner);
				}
			}
		}
	}

	protected void crossRefNetworkLink(IPCrossRef ipNetRef,
			Object obj) {

		if (obj instanceof CpNetworkObject) {
			CpNetworkObject nobj = (CpNetworkObject) obj;
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
		if (obj instanceof CpFwRule) {
			CpFwRule rule = (CpFwRule) obj;
			if (Log.debug().isLoggable(Level.INFO)) {
				Log.debug().info("xref NetworkLink/rule: " + rule.toText());
			}
			CrossRefContext refContext =
				new CrossRefContext(rule.toText(), "rule", "", null, 0);
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
		for (CpNetworkObject nobj: _cpNetworks.values()) {

			IPRangeable ip;
			IPRangeable ip6;
			IPCrossRef ipNetRef;
            IPCrossRef ipNetRef6;

			switch (nobj.getType()) {
				case IP	:   CpNetworkIP nip = (CpNetworkIP) nobj;
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
				case RANGE: CpNetworkRange nrange = (CpNetworkRange) nobj;
							ip = nrange.getIpRange();
							if (ip != null) {
                                ipNetRef = getIPNetCrossRef(ip);
                                crossRefNetworkLink(ipNetRef, nobj);
                            }
                            ip6 = nrange.getIpRange6();
                            if (ip6 != null) {
                                ipNetRef6 = getIPNetCrossRef(ip6);
                                crossRefNetworkLink(ipNetRef6, nobj);
                            }
							break;
				case IPS:   CpNetworkIPs member = (CpNetworkIPs) nobj;
							for (IPRange ipr : member.getIpRanges()) {
								ipNetRef = getIPNetCrossRef(ipr);
								crossRefNetworkLink(ipNetRef, nobj);
							}
							break;
			}
		}

		/*
		 * services object
		 */
		for (CpService sobj: _cpServices.values()) {
			if (sobj.getType() != CpServiceType.TCP &&
					sobj.getType() != CpServiceType.UDP)
				continue;
			crossRefTcpUdpService(sobj);
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

	protected MatchResult ipSpecFilter(CpFwIpSpec ipSpec, IPRangeable range) {

		MatchResult mres = ipSpec.getNetworks().matches(range);
		if (ipSpec.isNotIn())
			mres = mres.not();
		return mres;
	}

	protected MatchResult servicesSpecFilter(CpFwFilter filter,
			CpFwServicesSpec servicesSpec,
			ProbeRequest request) {

		CpServicesMatch smatch = servicesSpec.getServices().matches(request);
		MatchResult mres = smatch.getMatchResult();
		if (servicesSpec.isNotIn())
			mres = mres.not();
		if (smatch.isInspected())
			filter.setServiceInspected(true);
		return mres;
	}

	protected MatchResult ruleFilter(CpFwFilter filter, Probe probe, CpFwRule rule) {

		ProbeRequest request = probe.getRequest();

		/*
		 * applied on this gateway?
		 */
		List<String> gateways = rule.getInstallGateway();
		if (gateways != null && !gateways.contains(_gatewayName) && ! gateways.contains("Policy Targets"))
		    return MatchResult.NOT;

		/*
		 * disabled
		 */
		if (rule.isDisabled())
			return MatchResult.NOT;

        /*
         * implicit drop rule
         */
        if (rule.isImplicitDrop())
            return MatchResult.ALL;

        /*
		 * check source IP
		 */
		CpFwIpSpec ipspec = rule.getSourceIp();
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
		if (request.getProtocols() != null) {
			CpFwServicesSpec services = rule.getServices();
			mService = servicesSpecFilter(filter, services, request);
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
     * per layer filter
     */
    protected void layerFilter(IfaceLink link, Direction direction, Probe probe, CpLayer layer, FwResult callResult) {

        String ifaceName = link.getIfaceName();
        String ifaceComment = link.getIface().getComment();
        ProbeResults results = probe.getResults();

        MatchResult match;
        for (CpFwRule rule : layer.getRules()) {
            /*
             * skip the rule if not a security rule
             */
            if (!rule.isSecurityRule())
                continue;
            CpFwFilter filter = new CpFwFilter();
            String ruleText = rule.toText();
            match = ruleFilter(filter, probe, rule);
            /* if the rule matches */
            if (match != MatchResult.NOT) {
                /*
                 * store the result in the probe
                 */
                FwResult aclResult = new FwResult();
                /*
                 * XXX: if this layer's call is "may" this alters the result
                 */
                if (match != MatchResult.ALL || callResult.hasMay()) {
                    aclResult.addResult(FwResult.MAY);
                }
                switch (rule.getRuleAction()) {
                    case LAYER_CALL:
                        aclResult.addResult(FwResult.MATCH);
                        if (filter.hasServiceInspected()) {
                            ruleText += " {inspect}";
                        }
                        break;
                    case ACCEPT:
                        aclResult.addResult(FwResult.ACCEPT);
                        if (filter.hasServiceInspected()) {
                            ruleText += " {inspect}";
                        }
                        break;
                    case REJECT:
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
                /*
                 * if the rule action is a call to a layer, filter it.
                 */
                if (rule.ruleActionIsLayerCall()) {
                    FwResult r = new FwResult();
                    if (aclResult.hasMay()) {
                        r.addResult(FwResult.MAY);
                    }
                    CpLayer next = _cpLayers.get(rule.getLayerCall());
                    layerFilter(link, direction, probe, next, r);
                }
            }
        }
    }

	/**
	 * Packet filter
	 */
	protected void packetFilter (IfaceLink link, Direction direction, Probe probe) {

	    CpLayer layer = _rootLayer;
	    FwResult r = new FwResult();
	    layerFilter(link, direction, probe, layer, r);
	}


	@Override
	public void runShell(String command, PrintStream output) {
		_shell.shellCommand(command, output);
	}

	public HashMap<String, CpObject> getCpObjets() {
	    return _cpObjects;
    }

    public HashMap<String, CpFwRuleBaseAction> getCpActions() {
        return _cpActions;
    }

    public HashMap<String, CpLayer> getCpLayers() {
	    return _cpLayers;
    }

    public HashMap<String, CpService> getCpServices() {
		return _cpServices;
	}

	public HashMap<String, CpNetworkObject> getCpNetwork() {
		return _cpNetworks;
	}

	public LinkedList<CpFwRule> getFwRules() {
		return _fwRules;
	}

	/**
	 * Sorted list of services name
	 * @return a sorted list of services name.
	 */
	public List<String> getServicesName() {

		List<String> list = new LinkedList<>();
		list.addAll(_cpServices.keySet());
		Collections.sort(list);
		return list;
	}

	/**
	 * Sorted list of networks name
	 * @return a sorted list of networks name.
	 */
	public List<String> getNetworksName() {

		List<String> list = new LinkedList<>();
		list.addAll(_cpNetworks.keySet());
		Collections.sort(list);
		return list;
	}


}
