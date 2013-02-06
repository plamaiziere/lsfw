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

import fr.univrennes1.cri.jtacl.analysis.CrossRefContext;
import fr.univrennes1.cri.jtacl.analysis.IPNetCrossRef;
import fr.univrennes1.cri.jtacl.core.exceptions.JtaclConfigurationException;
import fr.univrennes1.cri.jtacl.core.monitor.Log;
import fr.univrennes1.cri.jtacl.core.monitor.Monitor;
import fr.univrennes1.cri.jtacl.core.network.Iface;
import fr.univrennes1.cri.jtacl.core.network.IfaceLink;
import fr.univrennes1.cri.jtacl.core.network.Route;
import fr.univrennes1.cri.jtacl.core.network.Routes;
import fr.univrennes1.cri.jtacl.core.probing.AclResult;
import fr.univrennes1.cri.jtacl.core.probing.MatchResult;
import fr.univrennes1.cri.jtacl.core.probing.Probe;
import fr.univrennes1.cri.jtacl.core.probing.ProbeRequest;
import fr.univrennes1.cri.jtacl.core.probing.ProbeResults;
import fr.univrennes1.cri.jtacl.equipments.generic.GenericEquipment;
import fr.univrennes1.cri.jtacl.lib.ip.AddressFamily;
import fr.univrennes1.cri.jtacl.lib.ip.IPNet;
import fr.univrennes1.cri.jtacl.lib.ip.IPRange;
import fr.univrennes1.cri.jtacl.lib.misc.Direction;
import fr.univrennes1.cri.jtacl.lib.misc.ParseContext;
import fr.univrennes1.cri.jtacl.lib.xml.XMLUtils;
import java.io.PrintStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.parboiled.Parboiled;
import org.parboiled.parserunners.BasicParseRunner;
import org.parboiled.support.ParsingResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Checkpoint Firewall
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

	/**
	 * Parser
	 */
	protected CpParser _parser = Parboiled.createParser(CpParser.class);

	/**
	 * interfaces
	 */
	protected HashMap<String, CPfwIface> _cpfwIfaces
		= new HashMap<String, CPfwIface>();

	/**
	 * IPNet cross references
	 */
	protected Map<IPNet, IPNetCrossRef> _netCrossRef
			= new HashMap<IPNet, IPNetCrossRef>();

	/**
	 * parse context
	 */
	 protected ParseContext _parseContext = new ParseContext();

	/*
	 * services keyed by name
	 */
	protected HashMap<String, CpService> _services
			= new HashMap<String, CpService>();

	/*
	 * network objects keyed by name
	 */
	protected HashMap<String, CpNetworkObject> _networkObjects
			= new HashMap<String, CpNetworkObject>();

	/*
	 * firewall rules
	 */
	protected LinkedList <CpFwRule> _fwRules = new LinkedList<CpFwRule>();

	/**
	 * IPNet cross references
	 */
	Map<IPNet, IPNetCrossRef> getNetCrossRef() {
		return _netCrossRef;
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

	protected CpPortItem parsePort(String sPorts) {

		ParsingResult<?> result = new BasicParseRunner(
			_parser.CpPortItem()).run(sPorts);
		if (!result.matched)
			throwCfgException("invalid port specification: " + sPorts, true);

		PortItemTemplate port = _parser.getPortItem();

		String sfirst = port.getFirstPort();
		String slast = port.getLastPort();
		int first = -1;
		int last = -1;
		try {
			first = Integer.parseInt(sfirst);
		} catch (NumberFormatException ex) {
			throwCfgException("invalid port number: " + sfirst, true);
		}
		if (slast != null ) {
			try {
				last = Integer.parseInt(slast);
			} catch (NumberFormatException ex) {
				throwCfgException("invalid port number: " + slast, true);
			}
		}

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

	protected CpService parseTcpUdpService(Element e) {

		String sName = XMLUtils.getTagValue(e, "Name");
		String sComment = XMLUtils.getTagValue(e, "comments");
		String sType = XMLUtils.getTagValue(e, "type");
		String sPorts = XMLUtils.getTagValue(e, "port");
		String sSourcePorts = XMLUtils.getTagValue(e, "src_port");
		String sInAny = XMLUtils.getTagValue(e, "include_in_any");

		/*
		 * ports
		 */
		CpPortItem portItem = parsePort(sPorts);
		CpPortItem srcPortItem =
			sSourcePorts == null ? null : parsePort(sSourcePorts);

		/*
		 * in any
		 */
		boolean inAny = Boolean.parseBoolean(sInAny);
		/*
		 * service type
		 */
		CpService service = null;
		if (sType.equalsIgnoreCase("tcp"))
			service = new CpTcpService(sName, sComment, portItem, srcPortItem,
				inAny);
		if (sType.equalsIgnoreCase("udp"))
			service = new CpUdpService(sName, sComment, portItem, srcPortItem,
				inAny);

		if (inAny) {
			CpGroupService any = (CpGroupService) _services.get("Any");
			any.addReference(sName, service);
		}

		return service;
	}

	protected CpService parseIcmpService(Element e) {

		String sName = XMLUtils.getTagValue(e, "Name");
		String className = XMLUtils.getTagValue(e, "Class_Name");
		String sComment = XMLUtils.getTagValue(e, "comments");
		String sIcmpType = XMLUtils.getTagValue(e, "icmp_type");
		String sIcmpCode = XMLUtils.getTagValue(e, "icmp_code");

		/*
		 * icmp code and type
		 */
		int icmpCode = -1;
		if (sIcmpCode != null) {
			icmpCode = Integer.parseInt(sIcmpCode);
		}
		int icmpType = Integer.parseInt(sIcmpType);

		/*
		 * address family
		 */
		AddressFamily af = AddressFamily.INET;
		if (className.equalsIgnoreCase("icmpv6_service"))
			af = AddressFamily.INET6;

		CpService service = new CpIcmpService(sName, sComment, af,
			icmpType, icmpCode);

		return service;
	}

	protected CpService parseOtherService(Element e) {

		String sName = XMLUtils.getTagValue(e, "Name");
		String sComment = XMLUtils.getTagValue(e, "comments");
		String sProtocol = XMLUtils.getTagValue(e, "protocol");
		String sExp = XMLUtils.getTagValue(e, "exp");
		String sInAny = XMLUtils.getTagValue(e, "include_in_any");

		/*
		 * protocol
		 */
		Integer proto = sProtocol == null ? null : Integer.parseInt(sProtocol);

		/*
		 * in any
		 */
		boolean inAny = Boolean.parseBoolean(sInAny);

		CpService service =
			new CpOtherService(sName, sComment, proto, sExp, inAny);

		if (inAny) {
			CpGroupService any = (CpGroupService) _services.get("Any");
			any.addReference(sName, service);
		}

		return service;
	}

	protected CpService parseServiceGroup(Element e) {
		String sName = XMLUtils.getTagValue(e, "Name");
		String sComment = XMLUtils.getTagValue(e, "comments");

		CpGroupService service = new CpGroupService(sName, sComment);

		/*
		 * members of this group (should be one member)
		 */
		List<Element> members = XMLUtils.getDirectChildren(e, "members");

		/*
		 * each reference
		 */
		List<Element> references =
			XMLUtils.getDirectChildren(members.get(0), "reference");
		for (Element reference: references) {
			String refName = XMLUtils.getTagValue(reference, "Name");
			service.addReference(refName, null);
		}

		return service;
	}

	protected CpService parseUnhandledService(Element e) {
		String sName = XMLUtils.getTagValue(e, "Name");
		String sComment = XMLUtils.getTagValue(e, "comments");
		String sClassName = XMLUtils.getTagValue(e, "Class_Name");

		return new CpUnhandledService(sName, sClassName, sComment);
	}

	protected CpNetworkObject parseNetworkHost(Element e) {
		String sName = XMLUtils.getTagValue(e, "Name");
		String sComment = XMLUtils.getTagValue(e, "comments");
		String sClassName = XMLUtils.getTagValue(e, "Class_Name");

		String sBroadcast = XMLUtils.getTagValue(e, "broadcast");
		boolean broadcast = sBroadcast == null ||
			sBroadcast.equalsIgnoreCase("allow");

		String sIp = XMLUtils.getTagValue(e, "ipaddr");
		String sNetmask = XMLUtils.getTagValue(e, "netmask");

		if (sNetmask != null)
			sIp += "/" + sNetmask;

		IPNet ip = null;
		try {
			ip = new IPNet(sIp);
		} catch (UnknownHostException ex) {
			throwCfgException("invalid IP address: " + sIp, true);
		}

		CpNetworkIP networkobj = new CpNetworkIP(sName, sClassName, sComment,
			ip, broadcast);

		return networkobj;
	}

	protected CpNetworkObject parseNetworkHostV6(Element e) {
		String sName = XMLUtils.getTagValue(e, "Name");
		String sComment = XMLUtils.getTagValue(e, "comments");
		String sClassName = XMLUtils.getTagValue(e, "Class_Name");

		String sIp = XMLUtils.getTagValue(e, "ipv6_address");
		String sNetmask = XMLUtils.getTagValue(e, "ipv6_prefix");

		if (sNetmask != null)
			sIp += "/" + sNetmask;

		IPNet ip = null;
		try {
			ip = new IPNet(sIp);
		} catch (UnknownHostException ex) {
			throwCfgException("invalid IP address: " + sIp, true);
		}

		CpNetworkIP networkobj = new CpNetworkIP(sName, sClassName, sComment,
			ip, true);

		return networkobj;
	}

	protected CpNetworkObject parseNetworkGroup(Element e) {
		String sName = XMLUtils.getTagValue(e, "Name");
		String sComment = XMLUtils.getTagValue(e, "comments");
		String sClassName = XMLUtils.getTagValue(e, "Class_Name");

		CpNetworkGroup ngroup = new CpNetworkGroup(sName, sClassName, sComment);

		if (sClassName.equalsIgnoreCase("network_object_group")) {
			/*
			 * members of this group (should be one member)
			 */
			List<Element> members = XMLUtils.getDirectChildren(e, "members");

			/*
			 * each reference
			 */
			List<Element> references =
				XMLUtils.getDirectChildren(members.get(0), "reference");
			for (Element reference: references) {
				String refName = XMLUtils.getTagValue(reference, "Name");
				ngroup.addBaseReference(refName, null);
			}
		}

		if (sClassName.equalsIgnoreCase("group_with_exception")) {
			/* base of this group */
			List<Element> base = XMLUtils.getDirectChildren(e, "base");
			String refname = XMLUtils.getTagValue(base.get(0), "Name");
			ngroup.addBaseReference(refname, null);
			List<Element> except = XMLUtils.getDirectChildren(e, "exception");
			String exceptName = XMLUtils.getTagValue(except.get(0), "Name");
			ngroup.addExcludedReference(exceptName, null);
		}

		return ngroup;
	}

	protected CpNetworkObject parseNetworkRange(Element e) {
		String sName = XMLUtils.getTagValue(e, "Name");
		String sComment = XMLUtils.getTagValue(e, "comments");
		String sClassName = XMLUtils.getTagValue(e, "Class_Name");

		String sIpFirst = XMLUtils.getTagValue(e, "ipaddr_first");
		String sIpLast = XMLUtils.getTagValue(e, "ipaddr_last");

		IPNet ipFirst = null;
		try {
			ipFirst = new IPNet(sIpFirst);
		} catch (UnknownHostException ex) {
			throwCfgException("invalid IP address: " + sIpFirst, true);
		}

		IPNet ipLast = null;
		try {
			ipLast = new IPNet(sIpLast);
		} catch (UnknownHostException ex) {
			throwCfgException("invalid IP address: " + sIpLast, true);
		}

		IPRange ipRange = new IPRange(ipFirst, ipLast);
		CpNetworkRange networkobj = new CpNetworkRange(sName, sClassName,
				sComment, ipRange);

		return networkobj;
	}

	protected CpNetworkObject parseUnhandledNetwork(Element e) {
		String sName = XMLUtils.getTagValue(e, "Name");
		String sComment = XMLUtils.getTagValue(e, "comments");
		String sClassName = XMLUtils.getTagValue(e, "Class_Name");

		return new CpUnhandledNetwork(sName, sClassName, sComment);
	}

	protected void loadServices(String filename) {

		Document doc = XMLUtils.getXMLDocument(filename);
		doc.getDocumentElement().normalize();

		_parseContext = new ParseContext();
		CpService service;

		Element root = doc.getDocumentElement();
		List<Element> list = XMLUtils.getDirectChildren(root, "service");
		int i = 0;
		for (Element e: list) {
			_parseContext.set(filename, i, XMLUtils.elementToText(e));
			String className = XMLUtils.getTagValue(e, "Class_Name");
			service = null;
			if (className.equalsIgnoreCase("tcp_service"))
				service = parseTcpUdpService(e);
			if (className.equalsIgnoreCase("udp_service"))
				service = parseTcpUdpService(e);
			if (className.equalsIgnoreCase("icmp_service"))
				service = parseIcmpService(e);
			if (className.equalsIgnoreCase("icmpv6_service"))
				service = parseIcmpService(e);
			if (className.equalsIgnoreCase("other_service"))
				service = parseOtherService(e);
			if (className.equalsIgnoreCase("service_group"))
				service = parseServiceGroup(e);

			/*
			 * unhandled by lsfw
			 */
			if (service == null) {
				service = parseUnhandledService(e);
				warnConfig("service is unhandled: " + service, false);
			}
			i++;

			if (service != null)
				_services.put(service.getName(), service);

			if (service != null && Log.debug().isLoggable(Level.INFO)) {
			Log.debug().info("CpService: " + service.toString());
			}
		}
	}

	protected void linkServices() {
		/*
		 * each service
		 */
		for (String serviceName: _services.keySet()) {
			CpService service = _services.get(serviceName);
			if (service.getType() != CpServiceType.GROUP)
				continue;
			/*
			 * resolves the references of the group
			 */
			CpGroupService group = (CpGroupService) service;
			for (String refName: group.getReferencesName()) {
				CpService ref = _services.get(refName);
				if (ref == null) {
					warnConfig("cannot link service group: "
							+ serviceName + " to member: " + refName, false);
				} else {
					group.addReference(refName, ref);
				}
			}
		}
	}

	protected void loadNetworkObject(String filename) {

		Document doc = XMLUtils.getXMLDocument(filename);
		doc.getDocumentElement().normalize();

		_parseContext = new ParseContext();
		CpNetworkObject networkObj;

		Element root = doc.getDocumentElement();
		List<Element> list = XMLUtils.getDirectChildren(root, "network_object");
		int i = 0;
		for (Element e: list) {
			_parseContext.set(filename, i, XMLUtils.elementToText(e));
			String className = XMLUtils.getTagValue(e, "Class_Name");
			networkObj = null;
			if (className.equalsIgnoreCase("host_plain"))
				networkObj = parseNetworkHost(e);
			if (className.equalsIgnoreCase("network"))
				networkObj = parseNetworkHost(e);
			if (className.equalsIgnoreCase("ipv6_object"))
				networkObj = parseNetworkHostV6(e);
			if (className.equalsIgnoreCase("network_object_group")
					|| className.equalsIgnoreCase("group_with_exception"))
				networkObj = parseNetworkGroup(e);
			if (className.equalsIgnoreCase("address_range"))
				networkObj = parseNetworkRange(e);

			/*
			 * unhandled by lsfw
			 */
			if (networkObj == null) {
				networkObj = parseUnhandledNetwork(e);
				warnConfig("network object is unhandled: " + networkObj, false);
			}

			i++;

			if (networkObj != null)
				_networkObjects.put(networkObj.getName(), networkObj);

			if (networkObj != null && Log.debug().isLoggable(Level.INFO)) {
				Log.debug().info("CpNetworkObject: " + networkObj.toString());
			}
		}
	}

	protected void linkNetworkObjects() {
		/*
		 * each network object
		 */
		for (String objectName: _networkObjects.keySet()) {
			CpNetworkObject nobj = _networkObjects.get(objectName);
			if (nobj.getType() != CpNetworkType.GROUP)
				continue;
			/*
			 * resolves the base references of the group
			 */
			CpNetworkGroup group = (CpNetworkGroup) nobj;
			for (String refName: group.getBaseReferencesName()) {
				CpNetworkObject ref = _networkObjects.get(refName);
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
			for (String refName: group.getExcludedReferencesName()) {
				CpNetworkObject ref = _networkObjects.get(refName);
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

	protected CpFwServicesSpec parseFwServicesSpec(Element e) {

		CpFwServicesSpec servicesSpec = new CpFwServicesSpec();
		/*
		 * members/reference
		 */
		List<Element> members = XMLUtils.getDirectChildren(e, "members");
		List<Element> references
				= XMLUtils.getDirectChildren(members.get(0), "reference");

		for (Element ref: references) {
			String refName = XMLUtils.getTagValue(ref, "Name");
			CpService service = _services.get(refName);
			if (service == null) {
				warnConfig("unknown service object: " + refName, true);
				continue;
			}
			servicesSpec.addReference(refName, service);
		}
		return servicesSpec;
	}

	protected CpFwIpSpec parseFwIpSpec(Element e) {

		CpFwIpSpec ipSpec = new CpFwIpSpec();
		/*
		 * members/reference
		 */
		List<Element> members = XMLUtils.getDirectChildren(e, "members");
		List<Element> references
				= XMLUtils.getDirectChildren(members.get(0), "reference");

		for (Element ref: references) {
			String refName = XMLUtils.getTagValue(ref, "Name");
			CpNetworkObject nobj = _networkObjects.get(refName);
			if (nobj == null) {
				warnConfig("unknown network object: " + refName, true);
				continue;
			}
			ipSpec.addReference(refName, nobj);
		}
		return ipSpec;
	}

	protected String parseFwAction(Element e) {

		/*
		 * action/Class_Name
		 */
		List<Element> action
				= XMLUtils.getDirectChildren(e, "action");

		String sAction = XMLUtils.getTagValue(action.get(0), "Class_Name");
		return sAction;
	}

	protected CpFwRule parseFwRule(Element e) {
		String sName = XMLUtils.getTagValue(e, "name");
		String sComment = XMLUtils.getTagValue(e, "comments");
		String sClassName = XMLUtils.getTagValue(e, "Class_Name");
		String sRuleNumber = XMLUtils.getTagValue(e, "Rule_Number");
		String sDisabled = XMLUtils.getTagValue(e, "disabled");

		List<Element> sources = XMLUtils.getDirectChildren(e, "src");
		List<Element> dsts = XMLUtils.getDirectChildren(e, "dst");
		List<Element> services = XMLUtils.getDirectChildren(e, "services");
		List<Element> actions = XMLUtils.getDirectChildren(e, "action");
		/*
		 * sanity checks
		 */
		if (sRuleNumber == null || sDisabled == null ||sources == null
			|| dsts == null || sources.isEmpty() || dsts.isEmpty()
			|| services == null || services.isEmpty() || actions == null
				|| actions.isEmpty()) {
			warnConfig("cannot parse rule", true);
			return null;
		}

		Element source = sources.get(0);
		CpFwIpSpec srcIpSpec = parseFwIpSpec(source);

		Element dst = dsts.get(0);
		CpFwIpSpec dstIpSpec = parseFwIpSpec(dst);

		Element service = services.get(0);
		CpFwServicesSpec servicesSpec = parseFwServicesSpec(service);

		String sAction = parseFwAction(actions.get(0));

		Integer rNumber = Integer.parseInt(sRuleNumber);
		Boolean disabled = Boolean.parseBoolean(sDisabled);

		CpFwRule fwrule = new CpFwRule(sName, sClassName, sComment, rNumber,
				disabled, srcIpSpec, dstIpSpec, servicesSpec, sAction);

		srcIpSpec.linkTo(fwrule);
		dstIpSpec.linkTo(fwrule);

		return fwrule;
	}

	protected void loadFwRules(String filename) {
		Document doc = XMLUtils.getXMLDocument(filename);
		doc.getDocumentElement().normalize();

		_parseContext = new ParseContext();

		Element root = doc.getDocumentElement();
		/*
		 * fw_policies/fw_policie/rule/rule
		 */
		List<Element> policie = XMLUtils.getDirectChildren(root, "fw_policie");
		List<Element> rule = XMLUtils.getDirectChildren(policie.get(0), "rule");
		List<Element> rules = XMLUtils.getDirectChildren(rule.get(0), "rule");

		int i = 0;
		for (Element e: rules) {
			_parseContext.set(filename, i, XMLUtils.elementToText(e));
			String className = XMLUtils.getTagValue(e, "Class_Name");
			CpFwRule fwRule = null;
			if (className.equalsIgnoreCase("security_rule"))
				fwRule = parseFwRule(e);

			if (fwRule != null ) {
				_fwRules.add(fwRule);
			}
			if (fwRule != null && Log.debug().isLoggable(Level.INFO)) {
				Log.debug().info("CpFwRule: " + fwRule);
			}
		}
	}

	protected void loadConfiguration(Document doc) {

		/* services */
		/*
		 *  ANY service object.
		 */
		CpService servAny = new CpGroupService("Any", "Any service");
		_services.put("Any", servAny);

		NodeList list = doc.getElementsByTagName("services");
		if (list.getLength() < 1) {
			throw new JtaclConfigurationException(
					"At least one services file must be specified");
		}
		for (int i = 0; i < list.getLength(); i++) {
			Element e = (Element) list.item(i);
			String filename = e.getAttribute("filename");
			if (filename.isEmpty())
				throw new JtaclConfigurationException("Missing services file name");
			loadServices(filename);
			famAdd(filename);
		}

		/* network_objects */
		list = doc.getElementsByTagName("network_objects");
		if (list.getLength() < 1) {
			throw new JtaclConfigurationException(
					"At least one network_object file must be specified");
		}
		/*
		 *  ANY network object.
		 */
		CpNetworkObject any = new CpNetworkAny("Any", "ANY_object", "any network object");
		_networkObjects.put("Any", any);

		for (int i = 0; i < list.getLength(); i++) {
			Element e = (Element) list.item(i);
			String filename = e.getAttribute("filename");
			if (filename.isEmpty())
				throw new JtaclConfigurationException("Missing network_objects file name");
			loadNetworkObject(filename);
			famAdd(filename);
		}

		/* fwpolicies */
		list = doc.getElementsByTagName("fwpolicies");
		if (list.getLength() != 1) {
			throw new JtaclConfigurationException(
				"one fwpolicies file must be specified");
		}
		Element e = (Element) list.item(0);
		String filename = e.getAttribute("filename");
		if (filename.isEmpty())
			throw new JtaclConfigurationException("Missing fwpolicies file name");
		loadFwRules(filename);
		famAdd(filename);

		/* implicit drop rule */
		CpFwRule fwdrop = new CpFwRule("implicit_drop", "");
		_fwRules.add(fwdrop);

		if (Log.debug().isLoggable(Level.INFO)) {
			Log.debug().info("'Any' service: " + servAny.toString());
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
			CPfwIface cpfwIface;
			Iface iface = getIface(name);
			if (iface == null) {
				if (comment.isEmpty())
					throw new JtaclConfigurationException("Missing interface comment: " + s);
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

	protected IPNet parseIp(String ip) {

		IPNet ipnet = null;
		try {
			ipnet = new IPNet(ip);
		} catch (UnknownHostException ex) {
			throwCfgException("invalid IP address: " + ex.getMessage(), true);
		}
		return ipnet;
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
		linkNetworkObjects();

		/*
		 * routing
		 */
		routeDirectlyConnectedNetworks();
		loadRoutesFromXML(doc);
		/*
		 * compute cross reference
		 */
		ipNetCrossReference();
	}

	protected IPNetCrossRef getIPNetCrossRef(IPNet ipnet) {
		IPNetCrossRef ref = _netCrossRef.get(ipnet);
		if (ref == null) {
			ref = new IPNetCrossRef(ipnet);
			_netCrossRef.put(ipnet, ref);
		}
		return ref;
	}

	protected void crossRefNetworkLink(IPNetCrossRef ipNetRef,
			Object obj) {

		if (obj instanceof CpNetworkObject) {
			CpNetworkObject nobj = (CpNetworkObject) obj;
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
			CrossRefContext refContext =
				new CrossRefContext(rule.toText(), "rule", "", null, 0);
			ipNetRef.addContext(refContext);

		}
	}

	/**
	 * Compute IPNet cross references
	 */
	protected void ipNetCrossReference() {
		/*
		 * network object
		 */
		for (CpNetworkObject nobj: _networkObjects.values()) {
			if (nobj.getType() != CpNetworkType.IP &&
					nobj.getType() != CpNetworkType.RANGE)
				continue;
			IPNet ipnet = null;
			switch (nobj.getType()) {
				case IP	:	CpNetworkIP nip = (CpNetworkIP) nobj;
							ipnet = nip.getIpAddress();
							break;
				case RANGE: CpNetworkRange nrange = (CpNetworkRange) nobj;
							ipnet = nrange.getIpRange().getIpFirst();
							break;
			}
			IPNetCrossRef ipNetRef = getIPNetCrossRef(ipnet);
			crossRefNetworkLink(ipNetRef, nobj);
		}
	}

	@Override
	public void incoming(IfaceLink link, Probe probe) {

		if (Log.debug().isLoggable(Level.INFO))
			Log.debug().info("probe" + probe.uidToString() + " incoming on " + _name);

		probe.decTimeToLive();
		if (!probe.isAlive()) {
			probe.killError("TimeToLive expiration");
			return;
		}

		/*
		 * Filter in the probe
		 */
		packetFilter(link, Direction.IN, probe);

		/*
		 * Check if the destination of the probe is on this equipment.
		 */
		IfaceLink ilink = getIfaceLink(probe.getDestinationAddress());
		if (ilink != null) {
			/*
			 * Set the probe's final position and notify the monitor
			 */
			probe.setOutgoingLink(ilink, probe.getDestinationAddress());
			probe.destinationReached("destination reached");
			return;
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
		ArrayList<Probe> probes = new ArrayList<Probe>();
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

	protected MatchResult ruleFilter(Probe probe, CpFwRule rule) {

		ProbeRequest request = probe.getRequest();

		/*
		 * implicit drop rule
		 */
		if (rule.isImplicitDrop())
			return MatchResult.ALL;

		/*
		 * disabled
		 */
		if (rule.isDisabled())
			return MatchResult.NOT;

		/*
		 * check source IP
		 */
		CpFwIpSpec ipspec = rule.getSourceIp();
		MatchResult mIpSource =
			ipspec.getNetworks().matches(probe.getSourceAddress());
		if (mIpSource == MatchResult.NOT)
			return MatchResult.NOT;

		/*
		 * check destination IP
		 */
		ipspec = rule.getDestIp();
		MatchResult mIpDest =
			ipspec.getNetworks().matches(probe.getDestinationAddress());
		if (mIpDest == MatchResult.NOT)
			return MatchResult.NOT;

		/*
		 * check services
		 */
		MatchResult mService;
		if (request.getProtocols() != null) {
			CpFwServicesSpec services = rule.getServices();
			mService = services.getServices().matches(request);
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
		boolean first = true;

		/*
		 * check each rule
		 */
		MatchResult match;
		for (CpFwRule rule: _fwRules) {
			match = ruleFilter(probe, rule);
			if (match != MatchResult.NOT) {
				/*
				 * store the result in the probe
				 */
				AclResult aclResult = new AclResult();
				aclResult.setResult(rule.getAction().equals("accept_action") ?
					AclResult.ACCEPT : AclResult.DENY);
				if (match != MatchResult.ALL)
					aclResult.addResult(AclResult.MAY);

				results.addMatchingAcl(direction, rule.toText(),
					aclResult);

				results.setInterface(direction,
					ifaceName + " (" + ifaceComment + ")");


				/*
				 * the active ace is the ace accepting or denying the packet.
				 * this is the first ace that match the packet.
				 */
				if (first) {
						results.addActiveAcl(direction,
								rule.toText(),
								aclResult);
					results.setAclResult(direction,
							aclResult);
					first = false;
				}
			}
		}
	}

	@Override
	public void runShell(String command, PrintStream output) {
		_shell.shellCommand(command, output);
	}

	public HashMap<String, CpService> getServices() {
		return _services;
	}

	public HashMap<String, CpNetworkObject> getNetworkObjects() {
		return _networkObjects;
	}

	public LinkedList<CpFwRule> getFwRules() {
		return _fwRules;
	}

	/**
	 * Sorted list of services name
	 * @return a sorted list of services name.
	 */
	public List<String> getServicesName() {

		List<String> list = new LinkedList<String>();
		list.addAll(_services.keySet());
		Collections.sort(list);
		return list;
	}

	/**
	 * Sorted list of networks name
	 * @return a sorted list of networks name.
	 */
	public List<String> getNetworksName() {

		List<String> list = new LinkedList<String>();
		list.addAll(_networkObjects.keySet());
		Collections.sort(list);
		return list;
	}


}
