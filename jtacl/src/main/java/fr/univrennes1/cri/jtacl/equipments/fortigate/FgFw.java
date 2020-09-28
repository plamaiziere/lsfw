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
import fr.univrennes1.cri.jtacl.equipments.checkpointR80.CpService;
import fr.univrennes1.cri.jtacl.equipments.checkpointR80.CpServiceType;
import fr.univrennes1.cri.jtacl.equipments.generic.GenericEquipment;
import fr.univrennes1.cri.jtacl.lib.ip.*;
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
	protected HashMap<String, FgIface> _fnFwIfaces
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
				_fnFwIfaces.put(name, fnfwIface);
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
		loadConfiguration(doc);
		linkServices();
/*
		linkNetworkObjects();
*/

		/*
		 * routing
		 */
		routeDirectlyConnectedNetworks();
		loadRoutesFromXML(doc);
		/*
		 * compute cross reference
		 */
/*
		if (_monitorOptions.getXref())
			CrossReferences();
*/
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

            //JsonNode layersNode = rootNode.path("layers");
            //loadJsonCpLayers(layersNode);
        }

        // implicit drop rule at the end of the root layer
        //CpFwRule fwdrop = CpFwRule.newImplicitDropRule(_rootLayer);
        //_rootLayer.getRules().add(fwdrop);

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

    protected int parseNumber(String number) {
	    int i = 0;
        try {
            i = Integer.parseInt(number);
        } catch (NumberFormatException ex) {
            throwCfgException("invalid number: " + number, true);
        }
        return i;
    }

    protected List<PortSpec> parsePortsRanges(String portsRanges) {
	    List<PortSpec> portsSpecs = new ArrayList<>();
	    String[] lpr = portsRanges.split(":");
	    if (lpr.length > 2) throwCfgException("invalid source/destination ports ranges: " + portsRanges, true);
	    if (lpr.length == 1) {
	        portsSpecs.add(null);
	        portsSpecs.add(parsePortRange(lpr[0]));
        } else {
   	        portsSpecs.add(parsePortRange(lpr[0]));
	        portsSpecs.add(parsePortRange(lpr[1]));
        }
	        return portsSpecs;
	}

    protected PortSpec parsePortRange(String portrange) {
	    PortSpec portSpec = new PortSpec();

	    //XXX: form : port1 port2 range1-range1 port3 range2-range2 ...
	    String[] slp = portrange.split(" ");
        for (String sp: slp) {
            String[] sr = sp.split("-");
            if (sr.length > 2) throwCfgException("invalid port range: " + portrange, true);

            int port1 = parseNumber(sr[0]);
            int port2 = port1;
            if (sr.length == 2) {
                port2 = parseNumber(sr[1]);
            }
            portSpec.add(new PortRange(port1, port2));
        }
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

            List<IPRangeable> ips = new LinkedList<>();
	        if (!sfqdn.isEmpty()) {
	            ips = parseFqdn(sfqdn);
            } else sfqdn = null;

	        if (!siprange.equals("0.0.0.0")) {
	            ips.addAll(parseServiceIpRange(siprange));
            } else {
	            ips = null;
	        }

	        FgAddressService service = null;
	        /* TCP / UDP ... */
	        if (sprotocol.equals("TCP/UDP/SCTP") || sprotocol.equals("ALL")) {
	            JsonNode ntcpports = n.path("tcp-portrange");
	            List<PortSpec> tcpPortsSpec = null;
	            if (!ntcpports.isMissingNode() && !ntcpports.textValue().isEmpty())
	                tcpPortsSpec = parsePortsRanges(ntcpports.textValue());

	            JsonNode nudpports = n.path("udp-portrange");
	            List<PortSpec> udpPortsSpec = null;
	            if (!nudpports.isMissingNode() && !nudpports.textValue().isEmpty())
	                udpPortsSpec = parsePortsRanges(nudpports.textValue());

	            JsonNode nsctpports = n.path("sctp-portrange");
	            List<PortSpec> sctpPortsSpec = null;
	            if (!nsctpports.isMissingNode() && !nsctpports.textValue().isEmpty())
	                sctpPortsSpec = parsePortsRanges(nsctpports.textValue());

	            if (sprotocol.equals("ALL")) {
                    service = new FgProtoAllService(
                            sname
                            , soriginKey
                            , scomment
                            , ips
                            , sfqdn
                            , udpPortsSpec == null ? null : udpPortsSpec.get(0)
                            , udpPortsSpec == null ? null : udpPortsSpec.get(1)
                            , tcpPortsSpec == null ? null : tcpPortsSpec.get(0)
                            , tcpPortsSpec == null ? null : tcpPortsSpec.get(1)
                            , sctpPortsSpec == null ? null : sctpPortsSpec.get(0)
                            , sctpPortsSpec == null ? null : sctpPortsSpec.get(1)
                    );
                } else {
                    service = new FgTcpUdpSctpService(
                            sname
                            , soriginKey
                            , scomment
                            , ips
                            , sfqdn
                            , udpPortsSpec == null ? null : udpPortsSpec.get(0)
                            , udpPortsSpec == null ? null : udpPortsSpec.get(1)
                            , tcpPortsSpec == null ? null : tcpPortsSpec.get(0)
                            , tcpPortsSpec == null ? null : tcpPortsSpec.get(1)
                            , sctpPortsSpec == null ? null : sctpPortsSpec.get(0)
                            , sctpPortsSpec == null ? null : sctpPortsSpec.get(1)
                    );
                }
            }

	        /* ICMP */
            if (sprotocol.equals("ICMP")) {
                String sicmp = n.path("icmptype").asText();
                String stype = n.path("icmpcode").asText();
                int icmp = parseNumber(sicmp);
                service = new FgIcmpService(sname, soriginKey, scomment, ips, sfqdn, AddressFamily.INET, icmp
                        , stype.isEmpty() ? -1 : parseNumber(stype));
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
		/* TODO
		if (obj instanceof CpFwRule) {
			CpFwRule rule = (CpFwRule) obj;
			if (Log.debug().isLoggable(Level.INFO)) {
				Log.debug().info("xref NetworkLink/rule: " + rule.toText());
			}
			CrossRefContext refContext =
				new CrossRefContext(rule.toText(), "rule", "", null, 0);
			ipNetRef.addContext(refContext);

		}*/
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
		for (CpService sobj: _cpServices.values()) {
			if (sobj.getType() != CpServiceType.TCP &&
					sobj.getType() != CpServiceType.UDP)
				continue;
			crossRefTcpUdpService(sobj);
		}
	}

}
